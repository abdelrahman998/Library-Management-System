package com.library.repository;

import com.library.entity.SystemUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SystemUserRepository extends JpaRepository<SystemUser, Long> {

    Optional<SystemUser> findByUsername(String username);

    List<SystemUser> findByActiveTrue();

    List<SystemUser> findByRole(SystemUser.Role role);
    
    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
