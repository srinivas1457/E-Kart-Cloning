package com.commerse.ekart.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.commerse.ekart.entity.RefreshToken;
import com.commerse.ekart.entity.User;

public interface RefreshTokenRepo extends JpaRepository<RefreshToken, Long>{
	Optional<RefreshToken> findByToken(String at);

	List<RefreshToken> findAllByExpirationBefore(LocalDateTime now);

	List<RefreshToken> findByUserAndIsBlockedAndTokenNot(User user, boolean b, String refreshToken);

	List<RefreshToken> findByUserAndIsBlocked(User user, boolean b);


	boolean existsByTokenAndIsBlockedAndExpirationAfter(String refreshToken, boolean b, LocalDateTime now);
}
