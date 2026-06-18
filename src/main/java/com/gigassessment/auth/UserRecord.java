package com.gigassessment.auth;

import java.time.Instant;
import java.time.LocalDate;

public record UserRecord(
		long id,
		String username,
		LocalDate dateOfBirth,
		String passwordCredential,
		int failedLoginCount,
		Instant lockedUntil
) {
	public boolean isLocked() {
		return lockedUntil != null && lockedUntil.isAfter(Instant.now());
	}
}
