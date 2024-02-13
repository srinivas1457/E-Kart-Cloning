package com.commerse.ekart.authexceptionhandller;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UserNotLoggedInException extends RuntimeException {
	private String message;

}
