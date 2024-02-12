package com.commerse.ekart.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.commerse.ekart.entity.RefreshToken;

public interface RefreshTokenRepo extends JpaRepository<RefreshToken, Long>{
	Optional<RefreshToken> findByToken(String at);

	List<RefreshToken> findAllByExpirationBefore(LocalDateTime now);
}
