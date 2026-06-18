package com.gigassessment.security;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebFilter("/*")
public class SecurityHeadersFilter implements Filter{
	private static final String CSP = "default-src 'self'; "
			+ "base-uri 'self'; "
			+ "frame-ancestors 'none'; "
			+ "form-action 'self'; "
			+ "object-src 'none'; "
			+ "script-src 'self'; "
			+ "style-src 'self'; "
			+ "img-src 'self'";
	
	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) req;
		HttpServletResponse httpResponse = (HttpServletResponse) resp;

		httpResponse.setHeader("X-Content-Type-Options", "nosniff");
		httpResponse.setHeader("X-Frame-Options", "DENY");
		httpResponse.setHeader("Referrer-Policy", "no-referrer");
		httpResponse.setHeader("Permissions-Policy", "geolocation=(), microphone=(), camera=()");
		httpResponse.setHeader("Content-Security-Policy", CSP);

		if (shouldDisableCaching(httpRequest)) {
			httpResponse.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
			httpResponse.setHeader("Pragma", "no-cache");
			httpResponse.setDateHeader("Expires", 0);
		}

		if (httpRequest.isSecure()) {
			httpResponse.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
		}

		chain.doFilter(req, resp);
	}

	private boolean shouldDisableCaching(HttpServletRequest request) {
		String path = request.getRequestURI().substring(request.getContextPath().length());
		return !path.startsWith("/css/");
	}
}
