package com.gigassessment.security;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.SessionCookieConfig;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class SessionCookieConfigListener implements ServletContextListener {
	@Override
	public void contextInitialized(ServletContextEvent event) {
		SessionCookieConfig cookieConfig = event.getServletContext().getSessionCookieConfig();
		cookieConfig.setName("id");
		cookieConfig.setHttpOnly(true);
		cookieConfig.setSecure(secureCookiesEnabled());
		cookieConfig.setPath(cookiePath(event));
		cookieConfig.setAttribute("SameSite", "Strict");
	}

	private String cookiePath(ServletContextEvent event) {
		String contextPath = event.getServletContext().getContextPath();
		return contextPath == null || contextPath.isBlank() ? "/" : contextPath;
	}

	private boolean secureCookiesEnabled() {
		String explicit = readSetting("AUTH_SECURE_COOKIES", "auth.secureCookies");
		if (explicit != null && !explicit.isBlank()) {
			return Boolean.parseBoolean(explicit);
		}

		String appEnvironment = readSetting("APP_ENV", "app.env");
		return "production".equalsIgnoreCase(appEnvironment);
	}

	private String readSetting(String environmentName, String propertyName) {
		String value = System.getenv(environmentName);
		if (value != null && !value.isBlank()) {
			return value;
		}
		return System.getProperty(propertyName);
	}
}
