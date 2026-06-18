package com.gigassessment.controller;

import java.io.IOException;

import com.gigassessment.auth.AuthConstants;
import com.gigassessment.auth.AuthService;
import com.gigassessment.auth.AuthService.SignupResult;
import com.gigassessment.auth.AuthUser;
import com.gigassessment.auth.PasswordHasher;
import com.gigassessment.security.CsrfTokenService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/signup")
public class SignupServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private final AuthService authService = new AuthService();

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		if (isAuthenticated(req)) {
			resp.sendRedirect(req.getContextPath() + "/core/dashboard");
			return;
		}

		HttpSession session = req.getSession(true);
		consumeFlash(session, req, AuthConstants.FLASH_ERROR, "error");
		consumeFlash(session, req, AuthConstants.LAST_USERNAME, "lastUsername");

		req.setAttribute("csrfToken", CsrfTokenService.ensureToken(session));
		req.getRequestDispatcher("/WEB-INF/views/signup.jsp").forward(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		if (!CsrfTokenService.isValid(req)) {
			resp.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		String username = safeTrim(req.getParameter("username"));
		char[] password = toPasswordChars(req.getParameter("password"));
		char[] confirmedPassword = toPasswordChars(req.getParameter("confirmedPassword"));

		try {
			SignupResult result = authService.register(username, password, confirmedPassword);

			switch (result.status()) {
			case SUCCESS -> completeSignup(req, resp, result.user());

			case INVALID_USERNAME -> redirectWithError(
					req,
					resp,
					username,
					"Username is required and must be 128 characters or fewer.");

			case WEAK_PASSWORD -> redirectWithError(
					req,
					resp,
					username,
					"Password must be at least 8 characters.");

			case PASSWORD_MISMATCH -> redirectWithError(
					req,
					resp,
					username,
					"Passwords do not match.");

			case USERNAME_TAKEN -> redirectWithError(
					req,
					resp,
					username,
					"That username is already in use. Try logging in or choose another one.");

			case SYSTEM_ERROR -> {
				getServletContext().log("Signup failed because the authentication system had an internal error.");
				redirectWithError(req, resp, username, "Signup is temporarily unavailable. Please try again later.");
			}

			default -> throw new IllegalArgumentException("Unexpected value: " + result.status());
			}
		} finally {
			PasswordHasher.clear(password);
			PasswordHasher.clear(confirmedPassword);
		}
	}

	private boolean isAuthenticated(HttpServletRequest req) {
		HttpSession session = req.getSession(false);
		return session != null && session.getAttribute(AuthConstants.AUTHENTICATED_USER) != null;
	}

	private void completeSignup(
			HttpServletRequest request,
			HttpServletResponse response,
			AuthUser user
	) throws IOException {
		HttpSession oldSession = request.getSession(false);
		if (oldSession != null) {
			oldSession.invalidate();
		}

		HttpSession session = request.getSession(true);
		session.setMaxInactiveInterval(AuthConstants.SESSION_TIMEOUT_SECONDS);
		session.setAttribute(AuthConstants.AUTHENTICATED_USER, user);
		CsrfTokenService.rotateToken(session);

		response.sendRedirect(request.getContextPath() + "/core/dashboard");
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

		response.sendRedirect(request.getContextPath() + "/signup");
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

	private char[] toPasswordChars(String value) {
		return value == null ? new char[0] : value.toCharArray();
	}
}
