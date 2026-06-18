package com.gigassessment.auth;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class PasswordHasher {
	public static final String ID = "pbkdf2_sha256";
	public static final int DEFAULT_ITERATIONS = 600_000;
	private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
	private static final int HASH_LENGTH_BITS = 256;
	private static final int SALT_LENGTH_BYTES = 16;
	private static final SecureRandom SECURE_RANDOM = new SecureRandom();
	private static final Base64.Encoder BASE64_ENCODER = Base64.getUrlEncoder().withoutPadding();
	private static final Base64.Decoder BASE64_DECODER = Base64.getUrlDecoder();


	private PasswordHasher() {
	}

	public static PasswordCredential hash(char[] password) {
		byte[] salt = new byte[SALT_LENGTH_BYTES];
		SECURE_RANDOM.nextBytes(salt);
		byte[] hash = pbkdf2(password, salt, DEFAULT_ITERATIONS);
		return new PasswordCredential(DEFAULT_ITERATIONS, BASE64_ENCODER.encodeToString(salt),
				BASE64_ENCODER.encodeToString(hash));
	}

	public static boolean verify(char[] password, PasswordCredential credential) {
		try {
			byte[] salt = BASE64_DECODER.decode(credential.salt());
			byte[] expectedHash = BASE64_DECODER.decode(credential.hash());
			byte[] actualHash = pbkdf2(password, salt, credential.iterations());
			return MessageDigest.isEqual(expectedHash, actualHash);
		} catch (IllegalArgumentException ex) {
			return false;
		}
	}


	private static byte[] pbkdf2(char[] password, byte[] salt, int iterations) {
		char[] safePassword = password == null ? new char[0] : password;
		PBEKeySpec spec = new PBEKeySpec(safePassword, salt, iterations, HASH_LENGTH_BITS);
		try {
			SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
			return factory.generateSecret(spec).getEncoded();
		} catch (GeneralSecurityException ex) {
			throw new IllegalStateException("Password hashing algorithm is not available.", ex);
		} finally {
			spec.clearPassword();
		}
	}

	public record PasswordCredential(int iterations, String salt, String hash) {
		public String encode() {
			return ID + "$" + iterations + "$" + salt + "$" + hash;
		}

		public static PasswordCredential parse(String encodedCredential) {
			if (encodedCredential == null || encodedCredential.isBlank()) {
				throw new IllegalArgumentException("Password credential is missing.");
			}

			String[] parts = encodedCredential.split("\\$");
			if (parts.length != 4 || !ID.equals(parts[0])) {
				throw new IllegalArgumentException("Password credential format is invalid.");
			}

			int parsedIterations = Integer.parseInt(parts[1]);
			if (parsedIterations < DEFAULT_ITERATIONS) {
				throw new IllegalArgumentException("Password credential uses too few iterations.");
			}

			return new PasswordCredential(parsedIterations, parts[2], parts[3]);
		}
	}

	public static void clear(char[] password) {
		if (password != null) {
			Arrays.fill(password, '\0');
		}
	}
}