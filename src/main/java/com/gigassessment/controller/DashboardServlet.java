package com.gigassessment.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Optional;

import com.gigassessment.auth.AuthConstants;
import com.gigassessment.auth.AuthUser;
import com.gigassessment.auth.UserRecord;
import com.gigassessment.auth.UserRepository;
import com.gigassessment.security.CsrfTokenService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/core/dashboard")
public class DashboardServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final int RICH_BY_AGE = 30;
	private static final DateTimeFormatter DATE_FORMATTER =
			DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG);

	private final UserRepository userRepository = new UserRepository();
		
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		HttpSession session = req.getSession(false);
		AuthUser user = null;
		
		if(session != null) {
			user = (AuthUser)session.getAttribute(AuthConstants.AUTHENTICATED_USER);
		}
		
		if (user == null) {
			resp.sendRedirect(req.getContextPath() + "/login");
			return;
		}
		
		DashboardView dashboardView = loadDashboardView(user);

		req.setAttribute("username", dashboardView.username());
		req.setAttribute("dateOfBirth", dashboardView.dateOfBirth());
		req.setAttribute("age", dashboardView.age());
		req.setAttribute("yearsLeft", dashboardView.yearsLeft());
		req.setAttribute("progressPercent", dashboardView.progressPercent());
		req.setAttribute("csrfToken", CsrfTokenService.ensureToken(session));
		req.getRequestDispatcher("/WEB-INF/views/dashboard.jsp").forward(req, resp);
			
	}

	private DashboardView loadDashboardView(AuthUser user) {
		try {
			Optional<UserRecord> optionalUser = userRepository.findByUsername(user.getUsername());
			if (optionalUser.isPresent() && optionalUser.get().dateOfBirth() != null) {
				UserRecord userRecord = optionalUser.get();
				LocalDate today = LocalDate.now();
				int age = Period.between(userRecord.dateOfBirth(), today).getYears();
				int yearsLeft = Math.max(0, RICH_BY_AGE - age);
				int progressPercent = Math.min(100, Math.max(0, age * 100 / RICH_BY_AGE));

				return new DashboardView(
						userRecord.username(),
						DATE_FORMATTER.format(userRecord.dateOfBirth()),
						age,
						yearsLeft,
						progressPercent
				);
			}
		} catch (IllegalStateException e) {
			getServletContext().log("Unable to load dashboard profile for user: " + user.getUsername(), e);
		}

		return new DashboardView(user.getUsername(), "an unknown date", 0, RICH_BY_AGE, 0);
	}

	private record DashboardView(
			String username,
			String dateOfBirth,
			int age,
			int yearsLeft,
			int progressPercent
	) {
	}
}
