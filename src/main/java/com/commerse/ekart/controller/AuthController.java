package com.commerse.ekart.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.commerse.ekart.requestdto.AuthRequest;
import com.commerse.ekart.requestdto.OtpModel;
import com.commerse.ekart.requestdto.UserRequest;
import com.commerse.ekart.responsedto.AuthResponse;
import com.commerse.ekart.responsedto.UserResponse;
import com.commerse.ekart.service.AuthService;
import com.commerse.ekart.util.ResponseStructure;
import com.commerse.ekart.util.SimpleResponseStructure;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/ekart/v1")
@AllArgsConstructor
public class AuthController {
	
	private AuthService authService;
	
	@PostMapping("/register")
	public ResponseEntity<ResponseStructure<UserResponse>> registerUser(@RequestBody UserRequest userRequest){
		return authService.registerUser(userRequest);
	}
	
	
	@PostMapping("/verify-otp")
	public ResponseEntity<ResponseStructure<UserResponse>> verifyOTP(@RequestBody OtpModel otpModel){
		return authService.verifyOTP(otpModel);
	}
	
	@PostMapping("/login")
	public ResponseEntity<ResponseStructure<AuthResponse>>login(@CookieValue(name="rt",required = false) String refreshToken,@CookieValue(name="at",required =false) String accessToken,@RequestBody AuthRequest authRequest,HttpServletResponse response){
		return authService.login(refreshToken,accessToken,authRequest,response);
	}
	
	////////////////********** Traditional Approach*******************//////////////
//	@PostMapping("/logout")
//	public ResponseEntity<String> logOut(HttpServletRequest request ,HttpServletResponse response){
//		return authService.logOut(request,response);
//	}
	////******2nd Approach*****/////
	@PostMapping("/logout")
	public ResponseEntity<SimpleResponseStructure> logOut(@CookieValue(name="rt",required = false) String refreshToken,@CookieValue(name="at",required =false) String accessToken ,HttpServletResponse response){
		return authService.logOut(refreshToken,accessToken,response);
	}
	
	@PostMapping("/revoke-other-devices")
	public ResponseEntity<SimpleResponseStructure> revokeOther(@CookieValue(name="at",required = false)String accessToken,@CookieValue(name="rt",required =false) String refreshToken,
			HttpServletResponse response){
		return authService.revokeOther(accessToken, refreshToken, response);
	}
	
	@PostMapping("/revoke-all-devices")
	public ResponseEntity<SimpleResponseStructure> revokeAll(@CookieValue(name="at",required = false)String accessToken,@CookieValue(name="rt",required =false) String refreshToken,
			HttpServletResponse response){
		return authService.revokeAll(accessToken, refreshToken, response);
	}
	
	@PostMapping("/refresh-login")
	public ResponseEntity<SimpleResponseStructure> refreshLogin(@CookieValue(name="at",required = false)String accessToken,@CookieValue(name="rt",required =false) String refreshToken,
			HttpServletResponse response){
		return authService.refreshLogin(accessToken, refreshToken, response);
	}
	

}
