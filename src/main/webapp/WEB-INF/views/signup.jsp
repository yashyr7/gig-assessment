<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="com.gigassessment.security.HtmlEscaper" %>
<%
String errorMessage = HtmlEscaper.escape(request.getAttribute("error"));
String lastUsername = HtmlEscaper.escape(request.getAttribute("lastUsername"));
String csrfToken = HtmlEscaper.escape(request.getAttribute("csrfToken"));
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

				<label class="visually-hidden" for="password">Password</label>
				<input
					class="input-box"
					id="password"
					name="password"
					type="password"
					placeholder="Create a password"
					autocomplete="new-password"
					minlength="8"
					required>

				<label class="visually-hidden" for="confirmedPassword">Confirm password</label>
				<input
					class="input-box"
					id="confirmedPassword"
					name="confirmedPassword"
					type="password"
					placeholder="Confirm your password"
					autocomplete="new-password"
					minlength="8"
					required>

				<p class="field-help">Password must be at least 8 characters.</p>

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
