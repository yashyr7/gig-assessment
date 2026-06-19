package com.gigassessment.auth;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

public final class AuthService {
	private static final int MAX_USERNAME_LENGTH = 128;
	private static final int MIN_PASSWORD_LENGTH = 5;
	private static final int MAX_PASSWORD_LENGTH = 1024;
	private static final int MAX_FAILED_ATTEMPTS = 5;
	private static final Duration LOCKOUT_DURATION = Duration.ofMinutes(5);

	private final UserRepository userRepository = new UserRepository();

	public LoginResult authenticate(String username, char[] password) {
		String normalizedUsername = username == null ? "" : username.trim();

		if (!isValidLoginShape(normalizedUsername, password)) {
			return LoginResult.invalid();
		}

		try {
			Optional<UserRecord> optionalUser = userRepository.findByUsername(normalizedUsername);

			if (optionalUser.isEmpty()) {
				return LoginResult.invalid();
			}

			UserRecord user = optionalUser.get();

			if (user.isLocked()) {
				return LoginResult.locked(Duration.between(Instant.now(), user.lockedUntil()));
			}

			PasswordHasher.PasswordCredential credential =
					PasswordHasher.PasswordCredential.parse(user.passwordCredential());

			if (PasswordHasher.verify(password, credential)) {
				userRepository.recordSuccessfulLogin(user.id());
				return LoginResult.success(new AuthUser(user.username()));
			}

			int failedLoginCount = nextFailedLoginCount(user);
			Instant lockedUntil = failedLoginCount >= MAX_FAILED_ATTEMPTS
					? Instant.now().plus(LOCKOUT_DURATION)
					: null;
			userRepository.recordFailedLogin(user.id(), failedLoginCount, lockedUntil);

			if (lockedUntil != null) {
				return LoginResult.locked(Duration.between(Instant.now(), lockedUntil));
			}

			return LoginResult.invalid();
		} catch (IllegalArgumentException | IllegalStateException e) {
			return LoginResult.systemError();
		}
	}

	public SignupResult register(String username, LocalDate dateOfBirth, char[] password, char[] confirmedPassword) {
		String normalizedUsername = username == null ? "" : username.trim();

		if (!isValidUsername(normalizedUsername)) {
			return SignupResult.invalidUsername();
		}

		if (!isValidDateOfBirth(dateOfBirth)) {
			return SignupResult.invalidDateOfBirth();
		}

		if (!isValidSignupPassword(password)) {
			return SignupResult.weakPassword();
		}

		if (!Arrays.equals(password, confirmedPassword)) {
			return SignupResult.passwordMismatch();
		}

		try {
			String passwordCredential = PasswordHasher.hash(password).encode();
			boolean created = userRepository.createUser(normalizedUsername, dateOfBirth, passwordCredential);

			if (!created) {
				return SignupResult.usernameTaken();
			}

			return SignupResult.success(new AuthUser(normalizedUsername));
		} catch (IllegalArgumentException | IllegalStateException e) {
			return SignupResult.systemError();
		}
	}

	public PasswordResetVerificationResult verifyPasswordResetIdentity(String username, LocalDate dateOfBirth) {
		String normalizedUsername = username == null ? "" : username.trim();

		if (!isValidUsername(normalizedUsername) || !isValidDateOfBirth(dateOfBirth)) {
			return PasswordResetVerificationResult.invalid();
		}

		try {
			Optional<UserRecord> optionalUser = userRepository.findByUsername(normalizedUsername);
			if (optionalUser.isEmpty()) {
				return PasswordResetVerificationResult.invalid();
			}

			UserRecord user = optionalUser.get();
			if (!dateOfBirth.equals(user.dateOfBirth())) {
				return PasswordResetVerificationResult.invalid();
			}

			return PasswordResetVerificationResult.success(user.username());
		} catch (IllegalArgumentException | IllegalStateException e) {
			return PasswordResetVerificationResult.systemError();
		}
	}

	public PasswordResetResult resetPassword(String username, char[] password, char[] confirmedPassword) {
		String normalizedUsername = username == null ? "" : username.trim();

		if (!isValidUsername(normalizedUsername)) {
			return PasswordResetResult.invalid();
		}

		if (!isValidSignupPassword(password)) {
			return PasswordResetResult.weakPassword();
		}

		if (!Arrays.equals(password, confirmedPassword)) {
			return PasswordResetResult.passwordMismatch();
		}

		try {
			String passwordCredential = PasswordHasher.hash(password).encode();
			boolean updated = userRepository.updatePassword(normalizedUsername, passwordCredential);

			if (!updated) {
				return PasswordResetResult.invalid();
			}

			return PasswordResetResult.success();
		} catch (IllegalArgumentException | IllegalStateException e) {
			return PasswordResetResult.systemError();
		}
	}

	private boolean isValidLoginShape(String username, char[] password) {
		return isValidUsername(username)
				&& password != null
				&& password.length > 0
				&& password.length <= MAX_PASSWORD_LENGTH;
	}

	private boolean isValidUsername(String username) {
		return !username.isBlank() && username.length() <= MAX_USERNAME_LENGTH;
	}

	private boolean isValidDateOfBirth(LocalDate dateOfBirth) {
		return dateOfBirth != null && !dateOfBirth.isAfter(LocalDate.now());
	}

	private boolean isValidSignupPassword(char[] password) {
		return password != null
				&& password.length >= MIN_PASSWORD_LENGTH
				&& password.length <= MAX_PASSWORD_LENGTH;
	}

	private int nextFailedLoginCount(UserRecord user) {
		if (user.lockedUntil() != null && !user.isLocked()) {
			return 1;
		}

		return Math.min(user.failedLoginCount() + 1, MAX_FAILED_ATTEMPTS);
	}

	public record LoginResult(
			Status status,
			AuthUser user,
			Duration remainingLockout
	) {
		public static LoginResult success(AuthUser user) {
			return new LoginResult(Status.SUCCESS, user, Duration.ZERO);
		}

		public static LoginResult invalid() {
			return new LoginResult(Status.INVALID, null, Duration.ZERO);
		}

		public static LoginResult locked(Duration remainingLockout) {
			return new LoginResult(Status.LOCKED, null, remainingLockout);
		}

		public static LoginResult systemError() {
			return new LoginResult(Status.SYSTEM_ERROR, null, Duration.ZERO);
		}
	}

	public enum Status {
		SUCCESS,
		INVALID,
		LOCKED,
		SYSTEM_ERROR
	}

	public record SignupResult(
			SignupStatus status,
			AuthUser user
	) {
		public static SignupResult success(AuthUser user) {
			return new SignupResult(SignupStatus.SUCCESS, user);
		}

		public static SignupResult invalidUsername() {
			return new SignupResult(SignupStatus.INVALID_USERNAME, null);
		}

		public static SignupResult invalidDateOfBirth() {
			return new SignupResult(SignupStatus.INVALID_DATE_OF_BIRTH, null);
		}

		public static SignupResult weakPassword() {
			return new SignupResult(SignupStatus.WEAK_PASSWORD, null);
		}

		public static SignupResult passwordMismatch() {
			return new SignupResult(SignupStatus.PASSWORD_MISMATCH, null);
		}

		public static SignupResult usernameTaken() {
			return new SignupResult(SignupStatus.USERNAME_TAKEN, null);
		}

		public static SignupResult systemError() {
			return new SignupResult(SignupStatus.SYSTEM_ERROR, null);
		}
	}

	public enum SignupStatus {
		SUCCESS,
		INVALID_USERNAME,
		INVALID_DATE_OF_BIRTH,
		WEAK_PASSWORD,
		PASSWORD_MISMATCH,
		USERNAME_TAKEN,
		SYSTEM_ERROR
	}

	public record PasswordResetVerificationResult(
			PasswordResetVerificationStatus status,
			String username
	) {
		public static PasswordResetVerificationResult success(String username) {
			return new PasswordResetVerificationResult(PasswordResetVerificationStatus.SUCCESS, username);
		}

		public static PasswordResetVerificationResult invalid() {
			return new PasswordResetVerificationResult(PasswordResetVerificationStatus.INVALID, null);
		}

		public static PasswordResetVerificationResult systemError() {
			return new PasswordResetVerificationResult(PasswordResetVerificationStatus.SYSTEM_ERROR, null);
		}
	}

	public enum PasswordResetVerificationStatus {
		SUCCESS,
		INVALID,
		SYSTEM_ERROR
	}

	public record PasswordResetResult(PasswordResetStatus status) {
		public static PasswordResetResult success() {
			return new PasswordResetResult(PasswordResetStatus.SUCCESS);
		}

		public static PasswordResetResult invalid() {
			return new PasswordResetResult(PasswordResetStatus.INVALID);
		}

		public static PasswordResetResult weakPassword() {
			return new PasswordResetResult(PasswordResetStatus.WEAK_PASSWORD);
		}

		public static PasswordResetResult passwordMismatch() {
			return new PasswordResetResult(PasswordResetStatus.PASSWORD_MISMATCH);
		}

		public static PasswordResetResult systemError() {
			return new PasswordResetResult(PasswordResetStatus.SYSTEM_ERROR);
		}
	}

	public enum PasswordResetStatus {
		SUCCESS,
		INVALID,
		WEAK_PASSWORD,
		PASSWORD_MISMATCH,
		SYSTEM_ERROR
	}
}
