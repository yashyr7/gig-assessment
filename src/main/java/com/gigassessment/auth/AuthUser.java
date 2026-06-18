package com.gigassessment.auth;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

public class AuthUser implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;
	
	private final String username;
	private final Instant authenticatedAt;
	
	public AuthUser(String username) {
		this.username = username;
		this.authenticatedAt = Instant.now();
	}
	
	public String getUsername() {
		return username;
	}
	
	public Instant getAuthenticatedAt() {
		return authenticatedAt;
	}
}
