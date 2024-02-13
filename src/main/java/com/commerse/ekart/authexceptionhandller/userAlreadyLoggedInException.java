package com.commerse.ekart.authexceptionhandller;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter 
public class userAlreadyLoggedInException extends RuntimeException {
	private String message;
}
