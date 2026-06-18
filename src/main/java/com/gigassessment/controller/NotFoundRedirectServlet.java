package com.gigassessment.controller;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/not-found")
public class NotFoundRedirectServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		redirectToLogin(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		redirectToLogin(req, resp);
	}

	private void redirectToLogin(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.sendRedirect(req.getContextPath() + "/login");
	}
}
