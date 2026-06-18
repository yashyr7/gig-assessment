<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="com.gigassessment.security.HtmlEscaper" %>
<%
String errorMessage = HtmlEscaper.escape(request.getAttribute("error"));
String infoMessage = HtmlEscaper.escape(request.getAttribute("info"));
String lastUsername = HtmlEscaper.escape(request.getAttribute("lastUsername"));
String csrfToken = HtmlEscaper.escape(request.getAttribute("csrfToken"));
%>

<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Login</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
	<main class="auth-page">
		<section class="login-component" aria-labelledby="login-title">
			<header class="login-header">
				<h1 id="login-title">Welcome Back</h1>
			</header>

			<form class="login-form" action="${pageContext.request.contextPath}/login" method="post">
				<input type="hidden" name="csrfToken" value="<%= csrfToken %>">

				<label class="visually-hidden" for="username">Username</label>
				<input
					class="input-box"
					id="username"
					name="username"
					type="text"
					placeholder="Enter your username"
					autocomplete="username"
					value="<%= lastUsername %>"
					required>

				<label class="visually-hidden" for="password">Password</label>
				<input
					class="input-box"
					id="password"
					name="password"
					type="password"
					placeholder="Enter your password"
					autocomplete="current-password"
					required>

				<div class="auth-link-row">
					<a href="${pageContext.request.contextPath}/forgot-password">Forgot password?</a>
				</div>

				<% if (!errorMessage.isBlank()) { %>
					<p class="error-message" role="alert"><%= errorMessage %></p>
				<% } %>

				<% if (!infoMessage.isBlank()) { %>
					<p class="info-message"><%= infoMessage %></p>
				<% } %>

				<div class="button-row">
					<button class="primary-button" type="submit">Login</button>
					<a class="secondary-button button-link" href="${pageContext.request.contextPath}/signup">Sign Up</a>
				</div>
			</form>
		</section>
	</main>
</body>
</html>
