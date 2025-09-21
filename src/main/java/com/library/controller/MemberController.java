package com.library.controller;

import com.library.dto.MemberRequest;
import com.library.dto.MemberResponse;
import com.library.entity.Member;
import com.library.entity.Member.MembershipStatus;
import com.library.mapper.MemberMapper;
import com.library.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final MemberMapper memberMapper;

    @PostMapping
    public ResponseEntity<MemberResponse> createMember(@Valid @RequestBody MemberRequest request) {
        Member member = memberMapper.toEntity(request);
        Member createdMember = memberService.createMember(member);
        return ResponseEntity.status(HttpStatus.CREATED).body(memberMapper.toResponse(createdMember));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MemberResponse> getMemberById(@PathVariable Long id) {
        Optional<Member> member = memberService.getMemberById(id);
        return ResponseEntity.ok(memberMapper.toResponse(member.orElse(null)));
    }

    @GetMapping("/membership/{membershipId}")
    public ResponseEntity<MemberResponse> getMemberByMembershipId(@PathVariable String membershipId) {
        Optional<Member> member = memberService.getMemberByMembershipId(membershipId);
        return ResponseEntity.ok(memberMapper.toResponse(member.orElse(null)));
    }

    @GetMapping
    public ResponseEntity<Page<MemberResponse>> getAllMembers(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<Member> members = memberService.getAllMembers(pageable);
        return ResponseEntity.ok(members.map(memberMapper::toResponse));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<MemberResponse>> searchMembers(
            @RequestParam String name,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<Member> members = memberService.searchMembersByName(name, pageable);
        return ResponseEntity.ok(members.map(memberMapper::toResponse));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Page<MemberResponse>> getMembersByStatus(
            @PathVariable MembershipStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<Member> members = memberService.getMembersByStatus(status, pageable);
        return ResponseEntity.ok(members.map(memberMapper::toResponse));
    }

    @GetMapping("/active")
    public ResponseEntity<List<MemberResponse>> getActiveMembers() {
        List<Member> members = memberService.getActiveMembers();
        return ResponseEntity.ok(members.stream()
                .map(memberMapper::toResponse)
                .toList());
    }

    @GetMapping("/expired")
    public ResponseEntity<List<MemberResponse>> getExpiredMemberships() {
        List<Member> members = memberService.getExpiredMemberships();
        return ResponseEntity.ok(members.stream()
                .map(memberMapper::toResponse)
                .toList());
    }

    @PutMapping("/{id}")
    public ResponseEntity<MemberResponse> updateMember(
            @PathVariable Long id,
            @Valid @RequestBody MemberRequest request) {
        Member member = memberMapper.toEntity(request);
        member.setId(id);
        Member updatedMember = memberService.updateMember(id, member);
        return ResponseEntity.ok(memberMapper.toResponse(updatedMember));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMember(@PathVariable Long id) {
        memberService.deleteMember(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmailExists(@RequestParam String email) {
        boolean exists = memberService.existsByEmail(email);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/check-membership-id")
    public ResponseEntity<Boolean> checkMembershipIdExists(@RequestParam String membershipId) {
        boolean exists = memberService.existsByMembershipId(membershipId);
        return ResponseEntity.ok(exists);
    }
}
