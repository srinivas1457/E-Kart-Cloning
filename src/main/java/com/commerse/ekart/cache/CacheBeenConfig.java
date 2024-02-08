package com.commerse.ekart.cache;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.commerse.ekart.entity.User;

@Configuration
public class CacheBeenConfig {
	@Bean
	public CacheStore<User> userCacheStore(){
		return new CacheStore<User>(Duration.ofMinutes(5));
	}
	@Bean
	public CacheStore<String> check(){
		return new CacheStore<String>(Duration.ofMinutes(5));
	}
}
