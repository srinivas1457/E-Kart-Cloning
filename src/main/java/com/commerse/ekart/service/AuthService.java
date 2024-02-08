package com.commerse.ekart.service;

import org.springframework.http.ResponseEntity;

import com.commerse.ekart.requestdto.OtpModel;
import com.commerse.ekart.requestdto.UserRequest;
import com.commerse.ekart.responsedto.UserResponse;
import com.commerse.ekart.util.ResponseStructure;

public interface AuthService {

	ResponseEntity<ResponseStructure<UserResponse>> registerUser(UserRequest userRequest);

	void deleteNonVerifiedUsers();

	ResponseEntity<ResponseStructure<UserResponse>> verifyOTP(OtpModel otpModel);

}
