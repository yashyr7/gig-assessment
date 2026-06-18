package com.gigassessment.auth;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Date;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

import com.gigassessment.db.Database;

public final class UserRepository {

	public Optional<UserRecord> findByUsername(String username) {
		String sql = """
				SELECT id, username, date_of_birth, password_hash, failed_login_count, locked_until
				FROM users
				WHERE username = ?
				LIMIT 1
				""";

		try (
				Connection connection = Database.getConnection();
				PreparedStatement statement = connection.prepareStatement(sql);
		) {
			statement.setString(1, username);

			try (ResultSet resultSet = statement.executeQuery()) {
				if (!resultSet.next()) {
					return Optional.empty();
				}

				Timestamp lockedUntilValue = resultSet.getTimestamp("locked_until");
				Instant lockedUntil = lockedUntilValue == null ? null : lockedUntilValue.toInstant();
				Date dateOfBirthValue = resultSet.getDate("date_of_birth");
				LocalDate dateOfBirth = dateOfBirthValue == null ? null : dateOfBirthValue.toLocalDate();

				return Optional.of(new UserRecord(
						resultSet.getLong("id"),
						resultSet.getString("username"),
						dateOfBirth,
						resultSet.getString("password_hash"),
						resultSet.getInt("failed_login_count"),
						lockedUntil
				));
			}
		} catch (SQLException e) {
			throw new IllegalStateException("Unable to load user: " + username, e);
		}
	}

	public boolean createUser(String username, LocalDate dateOfBirth, String passwordCredential) {
		String sql = """
				INSERT INTO users (username, date_of_birth, password_hash)
				VALUES (?, ?, ?)
				""";

		try (
				Connection connection = Database.getConnection();
				PreparedStatement statement = connection.prepareStatement(sql);
		) {
			statement.setString(1, username);
			statement.setDate(2, java.sql.Date.valueOf(dateOfBirth));
			statement.setString(3, passwordCredential);

			return statement.executeUpdate() == 1;
		} catch (SQLIntegrityConstraintViolationException e) {
			return false;
		} catch (SQLException e) {
			if ("23000".equals(e.getSQLState())) {
				return false;
			}
			throw new IllegalStateException("Unable to create user: " + username, e);
		}
	}

	public boolean updatePassword(String username, String passwordCredential) {
		String sql = """
				UPDATE users
				SET password_hash = ?,
				    failed_login_count = 0,
				    locked_until = NULL
				WHERE username = ?
				""";

		try (
				Connection connection = Database.getConnection();
				PreparedStatement statement = connection.prepareStatement(sql);
		) {
			statement.setString(1, passwordCredential);
			statement.setString(2, username);

			return statement.executeUpdate() == 1;
		} catch (SQLException e) {
			throw new IllegalStateException("Unable to update password for user: " + username, e);
		}
	}

	public void recordSuccessfulLogin(long userId) {
		String sql = """
				UPDATE users
				SET failed_login_count = 0,
				    locked_until = NULL,
				    last_login_at = CURRENT_TIMESTAMP
				WHERE id = ?
				""";

		try (
				Connection connection = Database.getConnection();
				PreparedStatement statement = connection.prepareStatement(sql);
		) {
			statement.setLong(1, userId);
			statement.executeUpdate();
		} catch (SQLException e) {
			throw new IllegalStateException("Unable to record successful login for userId: " + userId, e);
		}
	}

	public void recordFailedLogin(long userId, int failedCount, Instant lockedUntil) {
		String sql = """
				UPDATE users
				SET failed_login_count = ?,
				    locked_until = ?
				WHERE id = ?
				""";

		try (
				Connection connection = Database.getConnection();
				PreparedStatement statement = connection.prepareStatement(sql);
		) {
			statement.setInt(1, failedCount);

			if (lockedUntil == null) {
				statement.setNull(2, Types.TIMESTAMP);
			} else {
				statement.setTimestamp(2, Timestamp.from(lockedUntil));
			}

			statement.setLong(3, userId);
			statement.executeUpdate();
		} catch (SQLException e) {
			throw new IllegalStateException("Unable to record failed login for userId: " + userId, e);
		}
	}
}
