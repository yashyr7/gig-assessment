package com.gigassessment.security;

import java.io.IOException;

import com.gigassessment.auth.AuthConstants;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebFilter("/core/*")
public class AuthFilter implements Filter {
	
	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) req;
		HttpServletResponse httpResponse = (HttpServletResponse) resp;
		
		HttpSession session = httpRequest.getSession(false);
		
		if (session == null || session.getAttribute(AuthConstants.AUTHENTICATED_USER) == null) {
			httpResponse.sendRedirect(httpRequest.getContextPath() + "/login");
			return;
		}
		
		chain.doFilter(req, resp);
	}
}
