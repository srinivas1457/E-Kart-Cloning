package com.commerse.ekart.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.commerse.ekart.entity.User;

public interface CustomerRepo extends JpaRepository<User, Integer> {

	Optional<User> findByEmail(String email) ;
		
}
