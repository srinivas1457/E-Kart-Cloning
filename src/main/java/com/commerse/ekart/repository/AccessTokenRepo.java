package com.commerse.ekart.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.commerse.ekart.entity.AccessToken;

public interface AccessTokenRepo extends JpaRepository<AccessToken, Long> {

	Optional<AccessToken> findByToken(String at);

	List<AccessToken> findAllByExpirationBefore(LocalDateTime now);

}
