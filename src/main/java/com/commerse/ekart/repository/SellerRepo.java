package com.commerse.ekart.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.commerse.ekart.entity.User;

public interface SellerRepo extends JpaRepository<User, Integer>{

}
