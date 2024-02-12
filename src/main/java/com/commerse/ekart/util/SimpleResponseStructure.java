package com.commerse.ekart.util;

import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@Data
public class SimpleResponseStructure {
	private int statusCode;
	private String message;

}
