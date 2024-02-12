package com.commerse.ekart.service;

import java.time.LocalDateTime;

import org.springframework.http.ResponseEntity;

import com.commerse.ekart.requestdto.AuthRequest;
import com.commerse.ekart.requestdto.OtpModel;
import com.commerse.ekart.requestdto.UserRequest;
import com.commerse.ekart.responsedto.AuthResponse;
import com.commerse.ekart.responsedto.UserResponse;
import com.commerse.ekart.util.ResponseStructure;
import com.commerse.ekart.util.SimpleResponseStructure;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

	ResponseEntity<ResponseStructure<UserResponse>> registerUser(UserRequest userRequest);

	void deleteNonVerifiedUsers();

	ResponseEntity<ResponseStructure<UserResponse>> verifyOTP(OtpModel otpModel);

	ResponseEntity<ResponseStructure<AuthResponse>> login(AuthRequest authRequest, HttpServletResponse response);

	ResponseEntity<String> logOut(HttpServletRequest request, HttpServletResponse response);

	ResponseEntity<SimpleResponseStructure> logOut(String refreshToken, String accessToken, HttpServletResponse response);
	
	public void cleanupExpiredAccessTokens();
	
	public void cleanupExpiredRefreshTokens();

	ResponseEntity<SimpleResponseStructure> revokeOther(String accessToken, String refreshToken,
			HttpServletResponse response);

	ResponseEntity<SimpleResponseStructure> revokeAll(String accessToken, String refreshToken,
			HttpServletResponse response);

}
