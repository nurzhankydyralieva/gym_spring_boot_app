package com.epam.xstack.repository;

import com.epam.xstack.models.entity.AuthUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
@Repository
public interface UserRepository extends JpaRepository<AuthUser, UUID> {
    Optional<AuthUser> findByUserName(String userName);
}
