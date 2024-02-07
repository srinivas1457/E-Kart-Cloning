package com.commerse.ekart.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.commerse.ekart.requestdto.UserRequest;
import com.commerse.ekart.responsedto.UserResponse;
import com.commerse.ekart.service.AuthService;
import com.commerse.ekart.util.ResponseStructure;

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

}
