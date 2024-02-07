package com.commerse.ekart.authexceptionhandller;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class IllegalRequestException extends RuntimeException{
	private String message;
}
