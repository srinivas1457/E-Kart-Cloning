package com.commerse.ekart.util;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageStructure {
	private String to;
	private String subject;
	private Date sentDate;
	private String text;

}
