package com.commerse.ekart.authexceptionhandller;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserNotFoundException extends RuntimeException{
 private String message;
}
