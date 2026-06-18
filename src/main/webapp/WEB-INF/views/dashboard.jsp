<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="com.gigassessment.security.HtmlEscaper" %>
<%
String username = HtmlEscaper.escape(request.getAttribute("username"));
String dateOfBirth = HtmlEscaper.escape(request.getAttribute("dateOfBirth"));
String csrfToken = HtmlEscaper.escape(request.getAttribute("csrfToken"));
Integer age = (Integer) request.getAttribute("age");
Integer yearsLeft = (Integer) request.getAttribute("yearsLeft");
Integer progressPercent = (Integer) request.getAttribute("progressPercent");
int safeAge = age == null ? 0 : age;
int safeYearsLeft = yearsLeft == null ? 0 : yearsLeft;
int safeProgressPercent = progressPercent == null ? 0 : progressPercent;
%>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Dashboard</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
	<main class="dashboard-page">
		<header class="dashboard-topbar">
			<div class="dashboard-brand"></div>
			<form class="logout-form" action="${pageContext.request.contextPath}/logout" method="post">
				<input type="hidden" name="csrfToken" value="<%= csrfToken %>">
				<button class="secondary-button logout-button" type="submit">Logout</button>
			</form>
		</header>

		<section class="dashboard-hero" aria-labelledby="dashboard-title">
			<p class="dashboard-eyebrow">Age <%= safeAge %> / Goal 30</p>
			<h1 id="dashboard-title">
				Hey, <span class="shimmer-username"><%= username %></span>, you were born on <span><%= dateOfBirth %></span>.
				You've got <strong><%= safeYearsLeft %></strong> years to get rich.
			</h1>

			<div class="dashboard-progress" aria-label="Progress toward age 30">
				<progress class="dashboard-progress-bar" value="<%= safeProgressPercent %>" max="100"></progress>
				<div class="dashboard-progress-labels">
					<span>0</span>
					<span>30</span>
				</div>
			</div>
		</section>
	</main>
</body>
</html>
