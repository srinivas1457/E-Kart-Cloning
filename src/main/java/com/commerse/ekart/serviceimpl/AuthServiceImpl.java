package com.commerse.ekart.serviceimpl;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.commerse.ekart.authexceptionhandller.IllegalRequestException;
import com.commerse.ekart.authexceptionhandller.UserAlreadyExistsByEmailException;
import com.commerse.ekart.entity.Customer;
import com.commerse.ekart.entity.Seller;
import com.commerse.ekart.entity.User;
import com.commerse.ekart.repository.SellerRepo;
import com.commerse.ekart.repository.UserRepo;
import com.commerse.ekart.repository.customerRepo;
import com.commerse.ekart.requestdto.UserRequest;
import com.commerse.ekart.responsedto.UserResponse;
import com.commerse.ekart.service.AuthService;
import com.commerse.ekart.util.ResponseStructure;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {

	private UserRepo userRepo;

	
	private customerRepo customerRepo;

	private SellerRepo sellerRepo;

	private ResponseStructure<UserResponse> responseStructure;

	@Override
	public ResponseEntity<ResponseStructure<UserResponse>> registerUser(UserRequest userRequest) {

		 User user= userRepo.findByEmail(userRequest.getEmail()).map(u->{
			  if (u.isEmailVerified())throw new UserAlreadyExistsByEmailException("User already exists with the specified email");
			  else{
				// send an email to the client with otp
			}
			  return u;
		  }).orElseGet(()-> saveUser(userRequest));
		  
		  return new ResponseEntity<ResponseStructure<UserResponse>>(responseStructure.setStatusCode(HttpStatus.ACCEPTED.value())
					.setMessage("Please verify through OTP sent on your email Id").setData(mapToUserResponse(user)),HttpStatus.ACCEPTED);	

	}

	private UserResponse mapToUserResponse(User user) {
		return UserResponse.builder().userId(user.getUserId()).userName(user.getUserName()).email(user.getEmail())
				.userRole(user.getUserRole()).isDeleted(user.isDeleted()).isEmailVerified(user.isEmailVerified()).build();
	}

	private User saveUser(UserRequest userRequest) {
		User user=null;
		switch (userRequest.getUserRole()) {
		case CUSTOMER -> {
			user = customerRepo.save(maptoRespectiveChild(userRequest));
		}
		case SELLER -> {
			user = sellerRepo.save(maptoRespectiveChild(userRequest));
		}
		default -> new IllegalRequestException("Invalid User Role");
		}
		return user;
	}

	private <T extends User> T maptoRespectiveChild(UserRequest userRequest) {
		User user = null;
		switch (userRequest.getUserRole()) {
		case CUSTOMER -> {
			user = new Customer();
		}
		case SELLER -> {
			user = new Seller();
		}
		default -> new IllegalRequestException("Invalid User Role");
		}
//		String name=userRequest.getEmail().substring(0,userRequest.getEmail().indexOf('@'));
		user.setUserName(userRequest.getEmail().split("@")[0]);
		user.setEmail(userRequest.getEmail());
		user.setPassword(userRequest.getPassword());
		user.setUserRole(userRequest.getUserRole());
		
		return (T)user;
	}
	
	@Override
	public void deleteNonVerifiedUsers() {
		List<User> deleteList = userRepo.findByIsEmailVerifiedFalse();
		if(!deleteList.isEmpty()) {
			userRepo.deleteAll(deleteList);
		}
	}

}
