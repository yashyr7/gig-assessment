package com.gigassessment.auth;

import java.io.Console;

import com.gigassessment.auth.PasswordHasher.PasswordCredential;

public final class PasswordHashTool {
	private PasswordHashTool() {
	}

	public static void main(String[] args) {
		char[] password = readPassword(args);
		try {
			PasswordCredential credential = PasswordHasher.hash(password);
			System.out.println(credential.encode());
		} finally {
			PasswordHasher.clear(password);
		}
	}

	private static char[] readPassword(String[] args) {
		if (args.length == 1) {
			return args[0].toCharArray();
		}

		if (args.length > 1) {
			throw new IllegalArgumentException("Pass no arguments, or pass exactly one password argument.");
		}

		Console console = System.console();
		if (console == null) {
			throw new IllegalStateException("Run from a terminal or pass one password argument.");
		}

		char[] password = console.readPassword("Password to hash: ");
		if (password == null || password.length == 0) {
			throw new IllegalArgumentException("Password cannot be blank.");
		}

		return password;
	}
}