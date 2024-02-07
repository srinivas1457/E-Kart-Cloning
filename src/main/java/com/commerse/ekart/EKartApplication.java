package com.commerse.ekart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EKartApplication {

	public static void main(String[] args) {
		SpringApplication.run(EKartApplication.class, args);
	}

}
