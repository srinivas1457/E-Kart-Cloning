package com.commerse.ekart.util;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.commerse.ekart.service.AuthService;
import com.commerse.ekart.serviceimpl.AuthServiceImpl;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class ScheduledJobs {
	private AuthService authService;

	@Scheduled(cron = "0 0 0 ? * *")
	public void removeNonVerifiedUsers() {
		authService.deleteNonVerifiedUsers();
	}
	
	@Scheduled(fixedDelay = 3000L)
	public void callCleanupExpiredAccessTokens() {
		authService.cleanupExpiredAccessTokens();
	}
	
	@Scheduled(fixedDelay = 3000L)
	public void callCleanupExpiredRefreshTokens() {
		authService.cleanupExpiredRefreshTokens();
	}
}
