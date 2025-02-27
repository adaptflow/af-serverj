package com.adaptflow.af_serverj.configuration.db.login.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.adaptflow.af_serverj.configuration.db.login.entity.User;

import jakarta.transaction.Transactional;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

	Optional<User> findByUsername(String username);

	Optional<User> findByEmail(String email);

	@Modifying
	@Transactional
	@Query("UPDATE User u SET u.lastLogin = :lastLogin WHERE u.id = :userId")
	void updateLastLogin(@Param("userId") UUID userId, @Param("lastLogin") long lastLogin);
}
