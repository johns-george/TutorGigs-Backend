package com.tutorapp.webapp.Auth.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tutorapp.webapp.Auth.Entity.PasswordKey;

@Repository
public interface PasswordKeyRepository extends JpaRepository<PasswordKey, String> {
    Optional<PasswordKey> findById(String id);
}
