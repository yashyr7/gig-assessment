package com.gigassessment.auth;

public final class AuthConstants {
	public static final String AUTHENTICATED_USER = "authenticatedUser";
	public static final String CSRF_TOKEN = "csrfToken";
	public static final String FLASH_ERROR = "flashError";
	public static final String FLASH_INFO = "flashInfo";
	public static final String LAST_USERNAME = "lastUsername";
	public static final int SESSION_TIMEOUT_SECONDS = 15 * 60;
	
	private AuthConstants() {}
}
