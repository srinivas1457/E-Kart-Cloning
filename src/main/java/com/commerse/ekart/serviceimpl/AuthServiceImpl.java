package com.commerse.ekart.serviceimpl;


import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.commerse.ekart.authexceptionhandller.IllegalRequestException;
import com.commerse.ekart.authexceptionhandller.UserAlreadyExistsByEmailException;
import com.commerse.ekart.authexceptionhandller.UserNotLoggedInException;
import com.commerse.ekart.cache.CacheStore;
import com.commerse.ekart.entity.AccessToken;
import com.commerse.ekart.entity.Customer;
import com.commerse.ekart.entity.RefreshToken;
import com.commerse.ekart.entity.Seller;
import com.commerse.ekart.entity.User;
import com.commerse.ekart.repository.AccessTokenRepo;
import com.commerse.ekart.repository.CustomerRepo;
import com.commerse.ekart.repository.RefreshTokenRepo;
import com.commerse.ekart.repository.SellerRepo;
import com.commerse.ekart.repository.UserRepo;
import com.commerse.ekart.requestdto.AuthRequest;
import com.commerse.ekart.requestdto.OtpModel;
import com.commerse.ekart.requestdto.UserRequest;
import com.commerse.ekart.responsedto.AuthResponse;
import com.commerse.ekart.responsedto.UserResponse;
import com.commerse.ekart.security.JwtService;
import com.commerse.ekart.service.AuthService;
import com.commerse.ekart.util.CookieManager;
import com.commerse.ekart.util.MessageStructure;
import com.commerse.ekart.util.ResponseStructure;
import com.commerse.ekart.util.SimpleResponseStructure;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {
	private UserRepo userRepo;

	private SellerRepo sellerRepo;

	private CustomerRepo customerRepo;

	private ResponseStructure<UserResponse> structure;
	private ResponseStructure<AuthResponse> authStructure;

	private PasswordEncoder passwordEncoder;

	// storing user and otp data in cache memory for otp register validation
	private CacheStore<String> otpCacheStore;
	private CacheStore<User> userCacheStore;

	private JavaMailSender javaMailSender; // Automatically bean created by spring boot mail

	private AuthenticationManager authenticationManager;

	private CookieManager cookieManager;

	private JwtService jwtService;

	private AccessTokenRepo accessTokenRepo;

	private RefreshTokenRepo refreshTokenRepo;

	@Value("${myapp.refresh.expiry}")
	private int refreshExpiryInSeconds;

	@Value("${myapp.access.expiry}")
	private int accessExpiryInSeconds;


	public AuthServiceImpl(UserRepo userRepo, SellerRepo sellerRepo, CustomerRepo customerRepo,
			ResponseStructure<UserResponse> structure, ResponseStructure<AuthResponse> authStructure,
			PasswordEncoder passwordEncoder, CacheStore<String> otpCacheStore, CacheStore<User> userCacheStore,
			JavaMailSender javaMailSender, AuthenticationManager authenticationManager, CookieManager cookieManager,
			JwtService jwtService, AccessTokenRepo accessTokenRepo, RefreshTokenRepo refreshTokenRepo) {
		super();
		this.userRepo = userRepo;
		this.sellerRepo = sellerRepo;
		this.customerRepo = customerRepo;
		this.structure = structure;
		this.authStructure = authStructure;
		this.passwordEncoder = passwordEncoder;
		this.otpCacheStore = otpCacheStore;
		this.userCacheStore = userCacheStore;
		this.javaMailSender = javaMailSender;
		this.authenticationManager = authenticationManager;
		this.cookieManager = cookieManager;
		this.jwtService = jwtService;
		this.accessTokenRepo = accessTokenRepo;
		this.refreshTokenRepo = refreshTokenRepo;
	}


	

	

	@Override
	public ResponseEntity<ResponseStructure<UserResponse>> registerUser(UserRequest userRequest) {

		if (userRepo.existsByEmail(userRequest.getEmail()))
			throw new UserAlreadyExistsByEmailException("User already exists with the specified email");
		String oTP = generateOTP();
		User user = maptoRespectiveChild(userRequest);
		userCacheStore.add(userRequest.getEmail(), user);
		otpCacheStore.add(userRequest.getEmail(), oTP);
		try {
			sendOTPtoMail(user, oTP);
		} catch (MessagingException e) {
			log.error("The Email address doesn't exist");
		}
		return new ResponseEntity<ResponseStructure<UserResponse>>(structure
				.setStatusCode(HttpStatus.ACCEPTED.value())
				.setMessage("Please verify through OTP sent to your email Id ").setData(mapToUserResponse(user)),
				HttpStatus.ACCEPTED);

	}

	
	@Override
	public ResponseEntity<ResponseStructure<UserResponse>> verifyOTP(OtpModel otpModel) {
		User user = userCacheStore.get(otpModel.getEmail());
		String otp = otpCacheStore.get(otpModel.getEmail());

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
				structure.setStatusCode(HttpStatus.ACCEPTED.value()).setMessage("Registred Successfully!!")
						.setData(mapToUserResponse(user)),
				HttpStatus.ACCEPTED);
	}

	@Override
	public void deleteNonVerifiedUsers() {
		List<User> deleteList = userRepo.findByIsEmailVerifiedFalse();
		if (!deleteList.isEmpty()) {
			userRepo.deleteAll(deleteList);
		}
	}
	
	@Override
	public ResponseEntity<ResponseStructure<AuthResponse>> login(AuthRequest authRequest,HttpServletResponse response) {
		String userName=authRequest.getEmail().split("@")[0];

		UsernamePasswordAuthenticationToken token=new UsernamePasswordAuthenticationToken(userName, authRequest.getPassword());

		Authentication authentication = authenticationManager.authenticate(token);
		if(!authentication.isAuthenticated())
			throw new UsernameNotFoundException("Failed to Authenticate the User");

//		 generating the cookies and authresponse and returning to the client.
		else {
			return userRepo.findByUserName(userName).map(user->{

				grantAccess(response, user);

				return ResponseEntity.ok(authStructure.setStatusCode(HttpStatus.OK.value())
						.setData(AuthResponse.builder()
								.userId(user.getUserId())
								.userName(userName)
								.userRole(user.getUserRole().name())
								.isAuthenticated(true)
								.accessExpiration(LocalDateTime.now().plusSeconds(accessExpiryInSeconds))
								.refreshExpiration(LocalDateTime.now().plusSeconds(refreshExpiryInSeconds))
								.build())
						.setMessage("Login Successfull...!!!!!!!"));

			}).get();
		}
	}
		

	@Override
	public ResponseEntity<String> logOut(HttpServletRequest request, HttpServletResponse response) {
		
		String at=null;
		String rt=null;
		Cookie[] cookies=request.getCookies();
		for(Cookie cookie:cookies) {
			if(cookie.getName().equals("at")) at=cookie.getValue();
			if(cookie.getName().equals("rt")) rt=cookie.getValue();
		}
		
		accessTokenRepo.findByToken(at).ifPresent(accessToken ->{
			accessToken.setBlocked(true);
			accessTokenRepo.save(accessToken);
		});
		refreshTokenRepo.findByToken(rt).ifPresent(refresToken ->{
			refresToken.setBlocked(true);
			refreshTokenRepo.save(refresToken);
		});
		response.addCookie(cookieManager.invalidate(new Cookie("at", "")));
		response.addCookie(cookieManager.invalidate(new Cookie("rt","")));
		return new ResponseEntity<String>("logout Successfully",HttpStatus.OK);
	}

	@Override
	public ResponseEntity<SimpleResponseStructure> logOut(String refreshToken, String accessToken,
			HttpServletResponse response) {
		if(refreshToken==null && accessToken==null) {
			throw new UserNotLoggedInException("Please Log In First");
		}
		accessTokenRepo.findByToken(accessToken).ifPresent(accessToken1 ->{
			accessToken1.setBlocked(true);
			accessTokenRepo.save(accessToken1);
		});
		refreshTokenRepo.findByToken(refreshToken).ifPresent(refresToken ->{
			refresToken.setBlocked(true);
			refreshTokenRepo.save(refresToken);
		});
		response.addCookie(cookieManager.invalidate(new Cookie("at","")));
		response.addCookie(cookieManager.invalidate(new Cookie("rt","")));
		
		SimpleResponseStructure structure=new SimpleResponseStructure();
		structure.setStatusCode(HttpStatus.OK.value());
		structure.setMessage("logout Successfully");
		return new ResponseEntity<SimpleResponseStructure>(structure,HttpStatus.OK);
	}

//	@Override
//	public ResponseEntity<String> verifyOTP(OtpModel otpModel) {
//		String exOTP=userCacheStore.get("key");
//		if(exOTP!=null) {
//			if(exOTP.equals(oTP))return new ResponseEntity<String>(exOTP,HttpStatus.OK);
//		else return new ResponseEntity<String>("OTP is Invalid",HttpStatus.OK);
//		}else return new ResponseEntity<String>("OTP is Expired",HttpStatus.OK);
//	}



	///////////////////////// **********************************************//////////////////////////////////

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
		user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
		user.setUserRole(userRequest.getUserRole());

		return (T) user;
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
		helper.setText(messageStructure.getText(), true); // this true allows this message contains html

		javaMailSender.send(mimeMessage);
	}

	private void sendOTPtoMail(User user, String oTP) throws MessagingException {
		sendMail(MessageStructure.builder().to(user.getEmail()).subject("Complete your Registration to eKart")
				.sentDate(new Date())
				.text("hey, " + user.getUserName()
						+ " Good to see you intrested in eKart, complete your registration using the OTP <br><h1>" + oTP
						+ "</h1> Note:OTP Expires in With in 5 Minutes<br>" + "with best regards <br> eKart")
				.build());
	}

	private void sendRegistrationSuccessMail(User user) throws MessagingException {
		sendMail(MessageStructure.builder().to(user.getEmail()).subject("Successfully Registred in eKart")
				.sentDate(new Date())
				.text("welcome To eKart Shopping , " + user.getUserName() + "<br> Successfully Registered").build());
	}

	private void grantAccess(HttpServletResponse response,User user) {

		// generating access and refresh tokens
		String accessToken = jwtService.generateAccessToken(user.getUserName());
		String refreshToken = jwtService.generateRefreshToken(user.getUserName());


		// adding access and referesh tokens cookies to the response
		response.addCookie(cookieManager.configure(new Cookie("at", accessToken), accessExpiryInSeconds));
		response.addCookie(cookieManager.configure(new Cookie("rt", refreshToken), refreshExpiryInSeconds));

		// saving the access and refresh cookie in the database

		accessTokenRepo.save(AccessToken.builder()
				.token(accessToken)
				.isBlocked(false)
				.expiration(LocalDateTime.now().plusSeconds(accessExpiryInSeconds))
				.user(user)
				.build());

		refreshTokenRepo.save(RefreshToken.builder()
				.token(refreshToken)
				.isBlocked(false)
				.expiration(LocalDateTime.now().plusSeconds(refreshExpiryInSeconds))
				.user(user)
				.build());
	}
	
	
	
	
	



	


}
