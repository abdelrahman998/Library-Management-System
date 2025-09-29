package com.library.service;

import com.library.entity.Member;
import com.library.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    /**
     * Retrieve all members with pagination
     */
    public Page<Member> getAllMembers(Pageable pageable) {
        return memberRepository.findAll(pageable);
    }

    /**
     * Find member by ID
     */
    public Optional<Member> getMemberById(Long id) {
        return memberRepository.findById(id);
    }

    /**
     * Find member by membership ID
     */
    public Optional<Member> getMemberByMembershipId(String membershipId) {
        return memberRepository.findByMembershipId(membershipId);
    }

    /**
     * Find member by email
     */
    public Optional<Member> getMemberByEmail(String email) {
        return memberRepository.findByEmail(email);
    }

    /**
     * Create a new member
     */
    public Member createMember(Member member) {
        // Validate unique constraints
        if (memberRepository.existsByMembershipId(member.getMembershipId())) {
            throw new RuntimeException("Membership ID already exists: " + member.getMembershipId());
        }
        if (memberRepository.existsByEmail(member.getEmail())) {
            throw new RuntimeException("Email already exists: " + member.getEmail());
        }

        // Set default dates if not provided
        if (member.getMembershipDate() == null) {
            member.setMembershipDate(LocalDate.now());
        }
        if (member.getMembershipExpiry() == null) {
            member.setMembershipExpiry(LocalDate.now().plusYears(1));
        }
        if (member.getStatus() == null) {
            member.setStatus(Member.MembershipStatus.ACTIVE);
        }

        return memberRepository.save(member);
    }

    /**
     * Update an existing member
     */
    public Member updateMember(Long id, Member memberDetails) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Member not found with id: " + id));

        // Update fields
        member.setFirstName(memberDetails.getFirstName());
        member.setLastName(memberDetails.getLastName());
        member.setEmail(memberDetails.getEmail());
        member.setPhoneNumber(memberDetails.getPhoneNumber());
        member.setAddress(memberDetails.getAddress());
        member.setDateOfBirth(memberDetails.getDateOfBirth());
        member.setMembershipExpiry(memberDetails.getMembershipExpiry());
        member.setStatus(memberDetails.getStatus());

        return memberRepository.save(member);
    }

    /**
     * Delete a member
     */
    public void deleteMember(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Member not found with id: " + id));

        memberRepository.delete(member);
    }

    /**
     * Search members by name (first name or last name, case-insensitive)
     */
    public Page<Member> searchMembersByName(String name, Pageable pageable) {
        return memberRepository.findByNameContainingIgnoreCase(name, pageable);
    }

    /**
     * Get members by membership status
     */
    @Transactional(readOnly = true)
    public Page<Member> getMembersByStatus(Member.MembershipStatus status, Pageable pageable) {
        return memberRepository.findByStatus(status, pageable);
    }

    /**
     * Get members with expired memberships
     */
    public List<Member> getExpiredMemberships() {
        return memberRepository.findByMembershipExpiryBefore(LocalDate.now());
    }

    /**
     * Get members whose memberships are expiring within specified days
     */
    public List<Member> getExpiringMemberships(int daysAhead) {
        LocalDate futureDate = LocalDate.now().plusDays(daysAhead);
        return memberRepository.findByMembershipExpiryBetween(LocalDate.now(), futureDate);
    }

    /**
     * Get members whose memberships expire between two dates
     */
    public List<Member> getMembershipExpiringBetween(LocalDate startDate, LocalDate endDate) {
        return memberRepository.findByMembershipExpiryBetween(startDate, endDate);
    }

    /**
     * Renew member's membership
     */
    public Member renewMembership(Long memberId, int extensionYears) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found with id: " + memberId));

        // Extend membership from current expiry date or today (whichever is later)
        LocalDate currentExpiry = member.getMembershipExpiry();
        LocalDate baseDate = currentExpiry.isAfter(LocalDate.now()) ? currentExpiry : LocalDate.now();

        member.setMembershipExpiry(baseDate.plusYears(extensionYears));
        member.setStatus(Member.MembershipStatus.ACTIVE);

        return memberRepository.save(member);
    }

    /**
     * Suspend a member
     */
    public Member suspendMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found with id: " + memberId));

        member.setStatus(Member.MembershipStatus.SUSPENDED);
        return memberRepository.save(member);
    }

    /**
     * Reactivate a suspended member
     */
    public Member reactivateMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found with id: " + memberId));

        // Check if membership is not expired
        if (member.getMembershipExpiry().isBefore(LocalDate.now())) {
            throw new RuntimeException("Cannot reactivate member with expired membership. Please renew first.");
        }

        member.setStatus(Member.MembershipStatus.ACTIVE);
        return memberRepository.save(member);
    }

    /**
     * Cancel a member's membership
     */
    public Member cancelMembership(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found with id: " + memberId));

        member.setStatus(Member.MembershipStatus.CANCELLED);
        return memberRepository.save(member);
    }

    /**
     * Update membership status for expired members
     */
    public int updateExpiredMemberships() {
        List<Member> expiredMembers = getExpiredMemberships();
        int updateCount = 0;

        for (Member member : expiredMembers) {
            if (member.getStatus() == Member.MembershipStatus.ACTIVE) {
                member.setStatus(Member.MembershipStatus.EXPIRED);
                memberRepository.save(member);
                updateCount++;
            }
        }

        return updateCount;
    }

    /**
     * Check if membership ID exists
     */
    public boolean existsByMembershipId(String membershipId) {
        return memberRepository.existsByMembershipId(membershipId);
    }

    /**
     * Check if email exists
     */
    public boolean existsByEmail(String email) {
        return memberRepository.existsByEmail(email);
    }

    /**
     * Get all active members
     * @return List of active members
     */
    @Transactional(readOnly = true)
    public List<Member> getActiveMembers() {
        return memberRepository.findByStatus(Member.MembershipStatus.ACTIVE);
    }

    /**
     * Check if member is eligible for borrowing
     */
    public boolean isMemberEligibleForBorrowing(Long memberId) {
        return memberRepository.findById(memberId)
                .map(member -> member.getStatus() == Member.MembershipStatus.ACTIVE &&
                        member.getMembershipExpiry().isAfter(LocalDate.now()))
                .orElse(false);
    }

    /**
     * Get total member count
     */
    public long getTotalMemberCount() {
        return memberRepository.count();
    }

    /**
     * Get member count by status
     */
    public long getMemberCountByStatus(Member.MembershipStatus status) {
        return memberRepository.findByStatus(status).size();
    }

    /**
     * Get active member count
     */
    public long getActiveMemberCount() {
        return getMemberCountByStatus(Member.MembershipStatus.ACTIVE);
    }

    /**
     * Get count of memberships expiring within specified days
     */
    public long getExpiringMembershipCount(int daysAhead) {
        return getExpiringMemberships(daysAhead).size();
    }

    /**
     * Validate member information
     */
    public void validateMemberData(Member member) {
        if (member.getMembershipId() == null || member.getMembershipId().trim().isEmpty()) {
            throw new RuntimeException("Membership ID is required");
        }
        if (member.getFirstName() == null || member.getFirstName().trim().isEmpty()) {
            throw new RuntimeException("First name is required");
        }
        if (member.getLastName() == null || member.getLastName().trim().isEmpty()) {
            throw new RuntimeException("Last name is required");
        }
        if (member.getEmail() == null || member.getEmail().trim().isEmpty()) {
            throw new RuntimeException("Email is required");
        }
        // Basic email validation
        if (!member.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new RuntimeException("Invalid email format");
        }
    }

    /**
     * Generate next membership ID (simple implementation)
     */
    public String generateNextMembershipId() {
        long count = memberRepository.count();
        return String.format("LM%03d", count + 1);
    }
}
