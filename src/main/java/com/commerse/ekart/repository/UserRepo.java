package com.commerse.ekart.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.commerse.ekart.entity.User;

public interface UserRepo extends JpaRepository<User, Integer> {

	Optional<User> findByEmail(String email);
	Optional<User> findByUserName(String userName);
	boolean existsByEmail(String email);
	List<User> findByIsEmailVerifiedFalse();

}
