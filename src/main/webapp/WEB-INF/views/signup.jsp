<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="com.gigassessment.security.HtmlEscaper" %>
<%@ page import="java.time.LocalDate" %>
<%
String errorMessage = HtmlEscaper.escape(request.getAttribute("error"));
String lastUsername = HtmlEscaper.escape(request.getAttribute("lastUsername"));
String csrfToken = HtmlEscaper.escape(request.getAttribute("csrfToken"));
int currentYear = LocalDate.now().getYear();
%>

<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Create Account</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
	<main class="auth-page">
		<section class="login-component" aria-labelledby="signup-title">
			<header class="login-header">
				<h1 id="signup-title">Create Account</h1>
			</header>

			<form class="login-form" action="${pageContext.request.contextPath}/signup" method="post">
				<input type="hidden" name="csrfToken" value="<%= csrfToken %>">

				<label class="visually-hidden" for="username">Username</label>
				<input
					class="input-box"
					id="username"
					name="username"
					type="text"
					placeholder="Choose a username"
					autocomplete="username"
					maxlength="128"
					value="<%= lastUsername %>"
					required>

				<div class="date-field">
					<label class="date-field-label" id="signup-dob-label">Date of birth</label>
					<div class="date-select-grid" aria-labelledby="signup-dob-label">
						<div class="date-select-wrap">
							<select class="date-select" name="dateMonth" aria-label="Birth month" autocomplete="bday-month" required>
								<option value="" selected disabled>Month</option>
								<option value="1">January</option>
								<option value="2">February</option>
								<option value="3">March</option>
								<option value="4">April</option>
								<option value="5">May</option>
								<option value="6">June</option>
								<option value="7">July</option>
								<option value="8">August</option>
								<option value="9">September</option>
								<option value="10">October</option>
								<option value="11">November</option>
								<option value="12">December</option>
							</select>
						</div>
						<div class="date-select-wrap">
							<select class="date-select" name="dateDay" aria-label="Birth day" autocomplete="bday-day" required>
								<option value="" selected disabled>Day</option>
								<% for (int day = 1; day <= 31; day++) { %>
									<option value="<%= day %>"><%= day %></option>
								<% } %>
							</select>
						</div>
						<div class="date-select-wrap">
							<select class="date-select" name="dateYear" aria-label="Birth year" autocomplete="bday-year" required>
								<option value="" selected disabled>Year</option>
								<% for (int year = currentYear; year >= 1900; year--) { %>
									<option value="<%= year %>"><%= year %></option>
								<% } %>
							</select>
						</div>
					</div>
				</div>

				<label class="visually-hidden" for="password">Password</label>
				<input
					class="input-box"
					id="password"
					name="password"
					type="password"
					placeholder="Create a password"
					autocomplete="new-password"
					minlength="5"
					required>

				<label class="visually-hidden" for="confirmedPassword">Confirm password</label>
				<input
					class="input-box"
					id="confirmedPassword"
					name="confirmedPassword"
					type="password"
					placeholder="Confirm your password"
					autocomplete="new-password"
					minlength="5"
					required>

				<p class="field-help">Password must be at least 5 characters.</p>

				<% if (!errorMessage.isBlank()) { %>
					<p class="error-message" role="alert"><%= errorMessage %></p>
				<% } %>

				<div class="button-row">
					<button class="primary-button" type="submit">Create Account</button>
					<a class="secondary-button button-link" href="${pageContext.request.contextPath}/login">Login</a>
				</div>
			</form>
		</section>
	</main>
</body>
</html>
