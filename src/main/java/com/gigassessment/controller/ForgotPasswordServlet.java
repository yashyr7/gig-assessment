package com.gigassessment.controller;

import java.io.IOException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import com.gigassessment.auth.AuthConstants;
import com.gigassessment.auth.AuthService;
import com.gigassessment.auth.AuthService.PasswordResetResult;
import com.gigassessment.auth.AuthService.PasswordResetVerificationResult;
import com.gigassessment.auth.PasswordHasher;
import com.gigassessment.security.CsrfTokenService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/forgot-password")
public class ForgotPasswordServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static final String ACTION_RESET = "reset";

	private final AuthService authService = new AuthService();

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		HttpSession session = req.getSession(true);
		consumeFlash(session, req, AuthConstants.FLASH_ERROR, "error");
		consumeFlash(session, req, AuthConstants.FLASH_INFO, "info");
		consumeFlash(session, req, AuthConstants.LAST_USERNAME, "lastUsername");

		String resetUsername = (String) session.getAttribute(AuthConstants.RESET_VERIFIED_USERNAME);
		req.setAttribute("resetVerified", resetUsername != null);
		req.setAttribute("resetUsername", resetUsername);
		req.setAttribute("csrfToken", CsrfTokenService.ensureToken(session));

		req.getRequestDispatcher("/WEB-INF/views/forgot-password.jsp").forward(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		if (!CsrfTokenService.isValid(req)) {
			resp.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		String action = req.getParameter("action");
		if (ACTION_RESET.equals(action)) {
			handleReset(req, resp);
			return;
		}

		handleVerify(req, resp);
	}

	private void handleVerify(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		String username = safeTrim(req.getParameter("username"));
		LocalDate dateOfBirth = parseDate(req);
		PasswordResetVerificationResult result =
				authService.verifyPasswordResetIdentity(username, dateOfBirth);

		switch (result.status()) {
		case SUCCESS -> {
			HttpSession session = req.getSession(true);
			session.setAttribute(AuthConstants.RESET_VERIFIED_USERNAME, result.username());
			session.setAttribute(AuthConstants.FLASH_INFO, "Identity verified. Enter your new password.");
			resp.sendRedirect(req.getContextPath() + "/forgot-password");
		}

		case INVALID -> redirectWithError(
				req,
				resp,
				username,
				"We could not verify that username and date of birth.");

		case SYSTEM_ERROR -> {
			getServletContext().log("Password reset verification failed because of an internal error.");
			redirectWithError(req, resp, username, "Password reset is temporarily unavailable. Please try again later.");
		}

		default -> throw new IllegalArgumentException("Unexpected value: " + result.status());
		}
	}

	private void handleReset(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		HttpSession session = req.getSession(false);
		String username = session == null ? null : (String) session.getAttribute(AuthConstants.RESET_VERIFIED_USERNAME);

		if (username == null || username.isBlank()) {
			redirectWithError(req, resp, "", "Verify your username and date of birth before resetting your password.");
			return;
		}

		char[] password = toPasswordChars(req.getParameter("password"));
		char[] confirmedPassword = toPasswordChars(req.getParameter("confirmedPassword"));

		try {
			PasswordResetResult result = authService.resetPassword(username, password, confirmedPassword);

			switch (result.status()) {
			case SUCCESS -> {
				session.removeAttribute(AuthConstants.RESET_VERIFIED_USERNAME);
				session.setAttribute(AuthConstants.FLASH_INFO, "Your password has been reset. You can log in now.");
				resp.sendRedirect(req.getContextPath() + "/login");
			}

			case WEAK_PASSWORD -> redirectWithError(
					req,
					resp,
					username,
					"Password must be at least 5 characters.");

			case PASSWORD_MISMATCH -> redirectWithError(
					req,
					resp,
					username,
					"Passwords do not match.");

			case INVALID -> {
				session.removeAttribute(AuthConstants.RESET_VERIFIED_USERNAME);
				redirectWithError(req, resp, "", "Password reset expired. Verify your username and date of birth again.");
			}

			case SYSTEM_ERROR -> {
				getServletContext().log("Password reset failed because of an internal error.");
				redirectWithError(req, resp, username, "Password reset is temporarily unavailable. Please try again later.");
			}

			default -> throw new IllegalArgumentException("Unexpected value: " + result.status());
			}
		} finally {
			PasswordHasher.clear(password);
			PasswordHasher.clear(confirmedPassword);
		}
	}

	private void redirectWithError(
			HttpServletRequest request,
			HttpServletResponse response,
			String username,
			String message
	) throws IOException {
		HttpSession session = request.getSession(true);
		session.setAttribute(AuthConstants.FLASH_ERROR, message);
		session.setAttribute(AuthConstants.LAST_USERNAME, username);
		response.sendRedirect(request.getContextPath() + "/forgot-password");
	}

	private void consumeFlash(HttpSession session, HttpServletRequest request, String sessionName, String requestName) {
		Object value = session.getAttribute(sessionName);
		if (value != null) {
			request.setAttribute(requestName, value);
			session.removeAttribute(sessionName);
		}
	}

	private String safeTrim(String value) {
		return value == null ? "" : value.trim();
	}

	private LocalDate parseDate(HttpServletRequest request) {
		String isoDate = request.getParameter("dateOfBirth");
		if (isoDate != null && !isoDate.isBlank()) {
			return parseIsoDate(isoDate);
		}

		try {
			int year = Integer.parseInt(request.getParameter("dateYear"));
			int month = Integer.parseInt(request.getParameter("dateMonth"));
			int day = Integer.parseInt(request.getParameter("dateDay"));
			return LocalDate.of(year, month, day);
		} catch (DateTimeException | NumberFormatException e) {
			return null;
		}
	}

	private LocalDate parseIsoDate(String value) {
		try {
			return value == null || value.isBlank() ? null : LocalDate.parse(value);
		} catch (DateTimeParseException e) {
			return null;
		}
	}

	private char[] toPasswordChars(String value) {
		return value == null ? new char[0] : value.toCharArray();
	}
}
