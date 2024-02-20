package com.commerse.ekart.requestdto;

import com.commerse.ekart.enums.UserRole;

import lombok.Data;


@Data
public class UserRequest {
	private String email;
	private String password;
	private String userRole;
}
