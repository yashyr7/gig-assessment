package com.gigassessment.controller;

import java.io.IOException;

import com.gigassessment.auth.AuthConstants;
import com.gigassessment.security.CsrfTokenService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if (!CsrfTokenService.isValid(req)) {
			resp.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		HttpSession session = req.getSession(false);
		if (session != null) {
			session.invalidate();
		}

		HttpSession flashSession = req.getSession(true);
		flashSession.setAttribute(AuthConstants.FLASH_INFO, "You have been logged out.");
		resp.sendRedirect(req.getContextPath() + "/login");
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
	}

}
