package com.library.repository;

import com.library.entity.Member;
import com.library.entity.Member.MembershipStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    
    // Find member by membership ID
    Optional<Member> findByMembershipId(String membershipId);
    
    // Find member by email
    Optional<Member> findByEmail(String email);
    
    // Find members by status
    List<Member> findByStatus(MembershipStatus status);
    
    // Find members with expired memberships (before current date)
    List<Member> findByMembershipExpiryBefore(LocalDate date);
    
    // Search members by name (first or last name contains the given string, case-insensitive)
    @Query("SELECT m FROM Member m WHERE LOWER(m.firstName) LIKE LOWER(concat('%', :name, '%')) OR LOWER(m.lastName) LIKE LOWER(concat('%', :name, '%'))")
    Page<Member> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);
    
    // Check if a member with the given email exists (for validation)
    boolean existsByEmail(String email);
    
    // Check if a member with the given membership ID exists (for validation)
    boolean existsByMembershipId(String membershipId);
    
    // Find all members with pagination
    Page<Member> findAll(Pageable pageable);
    
    // Find members by status with pagination
    Page<Member> findByStatus(MembershipStatus status, Pageable pageable);
}
