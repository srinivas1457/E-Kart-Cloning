package com.commerse.ekart.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseStructure<T> {
	private int statusCode;
	private String message;
	private T data;
}
