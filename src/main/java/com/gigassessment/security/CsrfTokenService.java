package com.gigassessment.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import com.gigassessment.auth.AuthConstants;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public final class CsrfTokenService {
	public static final String REQUEST_PARAMETER = "csrfToken";
	private static final int TOKEN_BYTES = 32;
	private static final SecureRandom SECURE_RANDOM = new SecureRandom();
	private static final Base64.Encoder BASE64_ENCODER = Base64.getUrlEncoder().withoutPadding();
	
	private CsrfTokenService() {
	}

	public static String ensureToken(HttpServletRequest req) {
		return ensureToken(req.getSession(true));
	}
	
	public static String ensureToken(HttpSession session) {
		String existingToken = (String) session.getAttribute(AuthConstants.CSRF_TOKEN);
		if (existingToken != null && !existingToken.isBlank()) {
			return existingToken;
		}
		return rotateToken(session);
	}
	
	public static String rotateToken(HttpSession session) {
		byte[] randomBytes = new byte[TOKEN_BYTES];
		SECURE_RANDOM.nextBytes(randomBytes);
		String token = BASE64_ENCODER.encodeToString(randomBytes);
		session.setAttribute(AuthConstants.CSRF_TOKEN, token);
		return token;
	}
	
	public static boolean isValid(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		if (session == null) {
			return false;
		}

		String expectedToken = (String) session.getAttribute(AuthConstants.CSRF_TOKEN);
		String providedToken = request.getParameter(REQUEST_PARAMETER);
		if (expectedToken == null || providedToken == null) {
			return false;
		}

		byte[] expected = expectedToken.getBytes(StandardCharsets.UTF_8);
		byte[] provided = providedToken.getBytes(StandardCharsets.UTF_8);
		return MessageDigest.isEqual(expected, provided);
	}

}
