package com.library.repository;

import com.library.entity.SystemUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SystemUserRepository extends JpaRepository<SystemUser, Long> {

    Optional<SystemUser> findByUsername(String username);
    Optional<SystemUser> findByEmail(String email);
    List<SystemUser> findByActiveTrue();
    List<SystemUser> findByRole(SystemUser.Role role);
    
    @Query("SELECT COUNT(u) FROM SystemUser u WHERE u.role = :role")
    long countByRole(SystemUser.Role role);
    
    @Query("SELECT COUNT(u) FROM SystemUser u WHERE u.active = true")
    long countByActiveTrue();

    @Query("SELECT u FROM SystemUser u WHERE u.active = true AND u.role IN :roles")
    List<SystemUser> findByActiveAndRoleIn(List<SystemUser.Role> roles);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
