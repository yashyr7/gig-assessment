<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="com.gigassessment.security.HtmlEscaper" %>
<%
String username = HtmlEscaper.escape(request.getAttribute("username"));
String csrfToken = HtmlEscaper.escape(request.getAttribute("csrfToken"));
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
			<div class="dashboard-brand">Gig Assessment</div>
			<form class="logout-form" action="${pageContext.request.contextPath}/logout" method="post">
				<input type="hidden" name="csrfToken" value="<%= csrfToken %>">
				<button class="logout-button" type="submit">Logout</button>
			</form>
		</header>

		<section class="dashboard-card" aria-labelledby="dashboard-title">
			<p class="dashboard-eyebrow">Dashboard</p>
			<h1 id="dashboard-title">Hello <%= username %>, let's make some money</h1>
		</section>
	</main>
</body>
</html>
