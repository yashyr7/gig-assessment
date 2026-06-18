package com.gigassessment.auth;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.util.Optional;

import com.gigassessment.db.Database;

public final class UserRepository {

	public Optional<UserRecord> findByUsername(String username) {
		String sql = """
				SELECT id, username, password_hash, failed_login_count, locked_until
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

				return Optional.of(new UserRecord(
						resultSet.getLong("id"),
						resultSet.getString("username"),
						resultSet.getString("password_hash"),
						resultSet.getInt("failed_login_count"),
						lockedUntil
				));
			}
		} catch (SQLException e) {
			throw new IllegalStateException("Unable to load user: " + username, e);
		}
	}

	public boolean createUser(String username, String passwordCredential) {
		String sql = """
				INSERT INTO users (username, password_hash)
				VALUES (?, ?)
				""";

		try (
				Connection connection = Database.getConnection();
				PreparedStatement statement = connection.prepareStatement(sql);
		) {
			statement.setString(1, username);
			statement.setString(2, passwordCredential);

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
