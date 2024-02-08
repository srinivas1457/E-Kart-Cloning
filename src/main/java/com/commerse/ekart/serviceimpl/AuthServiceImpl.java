package com.commerse.ekart.serviceimpl;

import java.util.Date;
import java.util.List;
import java.util.Random;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.commerse.ekart.authexceptionhandller.IllegalRequestException;
import com.commerse.ekart.authexceptionhandller.UserAlreadyExistsByEmailException;
import com.commerse.ekart.cache.CacheStore;
import com.commerse.ekart.entity.Customer;
import com.commerse.ekart.entity.Seller;
import com.commerse.ekart.entity.User;
import com.commerse.ekart.repository.SellerRepo;
import com.commerse.ekart.repository.UserRepo;
import com.commerse.ekart.repository.customerRepo;
import com.commerse.ekart.requestdto.OtpModel;
import com.commerse.ekart.requestdto.UserRequest;
import com.commerse.ekart.responsedto.UserResponse;
import com.commerse.ekart.service.AuthService;
import com.commerse.ekart.util.MessageStructure;
import com.commerse.ekart.util.ResponseStructure;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

	private UserRepo userRepo;

	private customerRepo customerRepo;

	private SellerRepo sellerRepo;

	private CacheStore<String> oTPCacheStore;

	private CacheStore<User> userCacheStore;

	private ResponseStructure<UserResponse> responseStructure;

	private JavaMailSender javaMailSender;

	@Override
	public ResponseEntity<ResponseStructure<UserResponse>> registerUser(UserRequest userRequest) {

		if (userRepo.existsByEmail(userRequest.getEmail()))
			throw new UserAlreadyExistsByEmailException("User already exists with the specified email");
		String oTP = generateOTP();
		User user = maptoRespectiveChild(userRequest);
		userCacheStore.add(userRequest.getEmail(), user);
		oTPCacheStore.add(userRequest.getEmail(), oTP);
		
		try {
			sendOTPtoMail(user,oTP);
		} catch (MessagingException e) {
			log.error("The Email address doesn't exist");
		}
		return new ResponseEntity<ResponseStructure<UserResponse>>(responseStructure
				.setStatusCode(HttpStatus.ACCEPTED.value())
				.setMessage("Please verify through OTP sent to your email Id ").setData(mapToUserResponse(user)),
				HttpStatus.ACCEPTED);

	}

	private UserResponse mapToUserResponse(User user) {
		return UserResponse.builder().userId(user.getUserId()).userName(user.getUserName()).email(user.getEmail())
				.userRole(user.getUserRole()).isDeleted(user.isDeleted()).isEmailVerified(user.isEmailVerified())
				.build();
	}

	private User saveUser(UserRequest userRequest) {
		User user = null;
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

		return (T) user;
	}

	@Override
	public void deleteNonVerifiedUsers() {
		List<User> deleteList = userRepo.findByIsEmailVerifiedFalse();
		if (!deleteList.isEmpty()) {
			userRepo.deleteAll(deleteList);
		}
	}

	@Override
	public ResponseEntity<ResponseStructure<UserResponse>> verifyOTP(OtpModel otpModel) {
		User user = userCacheStore.get(otpModel.getEmail());
		String otp = oTPCacheStore.get(otpModel.getEmail());

		if (otp == null)
			throw new IllegalRequestException("OTP Expired");
		if (user == null)
			throw new IllegalRequestException("Registration Session Expired");
		if (!otp.equals(otpModel.getOTP()))
			throw new IllegalRequestException("Invalid OTP");

		user.setEmailVerified(true);
		userRepo.save(user);
		try {
			sendRegistrationSuccessMail(user);
		} catch (MessagingException e) {
			log.error("The Email address doesn't exist");
		}
		return new ResponseEntity<ResponseStructure<UserResponse>>(
				responseStructure.setStatusCode(HttpStatus.ACCEPTED.value()).setMessage("Registred Successfully!!")
						.setData(mapToUserResponse(user)),
				HttpStatus.ACCEPTED);
	}

//	@Override
//	public ResponseEntity<String> verifyOTP(OtpModel otpModel) {
//		String exOTP=userCacheStore.get("key");
//		if(exOTP!=null) {
//			if(exOTP.equals(oTP))return new ResponseEntity<String>(exOTP,HttpStatus.OK);
//		else return new ResponseEntity<String>("OTP is Invalid",HttpStatus.OK);
//		}else return new ResponseEntity<String>("OTP is Expired",HttpStatus.OK);
//	}

	private String generateOTP() {
		return String.valueOf(new Random().nextInt(100000, 999999));
	}

	@Async
	private void sendMail(MessageStructure messageStructure) throws MessagingException {
		MimeMessage mimeMessage = javaMailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true); // this true allows Multi Part File
		helper.setTo(messageStructure.getTo());
		helper.setSubject(messageStructure.getSubject());
		helper.setSentDate(messageStructure.getSentDate());
		helper.setText(messageStructure.getText(),true); // this true allows this message contains html

		javaMailSender.send(mimeMessage);
	}

	private void sendOTPtoMail(User user,String oTP) throws MessagingException {
		sendMail(MessageStructure.builder().to(user.getEmail()).subject("Complete your Registration to eKart")
				.sentDate(new Date())
				.text("hey, " + user.getUserName()
						+ " Good to see you intrested in eKart, complete your registration using the OTP <br><h1>"+oTP+"</h1> Note:OTP Expires in With in 5 Minutes<br>"
						+ "with best regards <br> eKart")
				.build());
	}
	
	private void sendRegistrationSuccessMail(User user) throws MessagingException {
		sendMail(MessageStructure.builder().to(user.getEmail()).subject("Successfully Registred in eKart")
				.sentDate(new Date())
				.text("welcome To eKart Shopping , " + user.getUserName()
						+ "<br> Successfully Registered")
				.build());
	}

}
