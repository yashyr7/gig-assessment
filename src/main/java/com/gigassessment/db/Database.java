package com.gigassessment.db;

import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public final class Database {
	private static DataSource dataSource;
	private static final String JNDI_NAME = "java:comp/env/jdbc/GigAssessmentDB";
	
	private Database() {}
	
	public static Connection getConnection() throws SQLException {
		return getDataSource().getConnection();
	}
	
	private static DataSource getDataSource() {
		if (dataSource == null) {
			try {
				InitialContext context = new InitialContext();
				dataSource = (DataSource) context.lookup(JNDI_NAME);
			} catch (NamingException e) {
				throw new IllegalStateException("Database connection pool is not configured.", e);
			}
		}
		return dataSource;
	}
}
