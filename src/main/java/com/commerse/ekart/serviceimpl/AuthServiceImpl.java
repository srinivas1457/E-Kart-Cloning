package com.commerse.ekart.serviceimpl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import com.commerse.ekart.entity.User;
import com.commerse.ekart.enums.UserRole;
import com.commerse.ekart.exceptionhandling.UserNotFoundException;
import com.commerse.ekart.repository.customerRepo;
import com.commerse.ekart.requestdto.UserRequest;
import com.commerse.ekart.responsedto.UserResponse;
import com.commerse.ekart.service.AuthService;
import com.commerse.ekart.util.ResponseStructure;

public class AuthServiceImpl implements AuthService{
	
	@Autowired
	private customerRepo customerRepo;

	@Override
	public ResponseEntity<ResponseStructure<UserResponse>> registerUser(UserRequest userRequest) {
		if(UserRole.CUSTOMER.equals(userRequest.getUserRole())) {
			Optional<User> findByEmail = customerRepo.findByEmail(userRequest.getEmail());//.orElseThrow(()-> new UserNotFoundException("User Not Found By Email"));
		}
		return null;
	}

}
