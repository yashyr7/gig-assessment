package com.gigassessment.auth;

import java.time.Instant;

public record UserRecord(
		long id,
		String username,
		String passwordCredential,
		int failedLoginCount,
		Instant lockedUntil
) {
	public boolean isLocked() {
		return lockedUntil != null && lockedUntil.isAfter(Instant.now());
	}
}
