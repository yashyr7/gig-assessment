package com.gigassessment.controller;

import java.io.IOException;

import com.gigassessment.auth.AuthConstants;
import com.gigassessment.auth.AuthUser;
import com.gigassessment.security.CsrfTokenService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/core/dashboard")
public class DashboardServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		HttpSession session = req.getSession(false);
		AuthUser user = null;
		
		if(session != null) {
			user = (AuthUser)session.getAttribute(AuthConstants.AUTHENTICATED_USER);
		}
		
		if (user == null) {
			resp.sendRedirect(req.getContextPath() + "/login");
			return;
		}
		
		req.setAttribute("username", user.getUsername());
		req.setAttribute("csrfToken", CsrfTokenService.ensureToken(session));
		req.getRequestDispatcher("/WEB-INF/views/dashboard.jsp").forward(req, resp);
		
	}
}
