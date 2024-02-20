package com.commerse.ekart.authexceptionhandller;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter 
public class UserAlreadyLoggedInException extends RuntimeException {
	private String message;
}
