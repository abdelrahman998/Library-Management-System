package com.library.mapper;

import com.library.dto.MemberRequest;
import com.library.dto.MemberResponse;
import com.library.entity.Member;
import org.springframework.stereotype.Component;

@Component
public class MemberMapper {

    public Member toEntity(MemberRequest request) {
        if (request == null) {
            return null;
        }

        return Member.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .address(request.getAddress())
                .dateOfBirth(request.getDateOfBirth())
                .membershipExpiry(request.getMembershipExpiry())
                .status(request.getStatus())
                .build();
    }

    public MemberResponse toResponse(Member member) {
        if (member == null) {
            return null;
        }

        return MemberResponse.builder()
                .id(member.getId())
                .membershipId(member.getMembershipId())
                .firstName(member.getFirstName())
                .lastName(member.getLastName())
                .email(member.getEmail())
                .phoneNumber(member.getPhoneNumber())
                .address(member.getAddress())
                .dateOfBirth(member.getDateOfBirth())
                .membershipDate(member.getMembershipDate())
                .membershipExpiry(member.getMembershipExpiry())
                .status(member.getStatus())
                .createdAt(member.getCreatedAt())
                .updatedAt(member.getUpdatedAt())
                .build();
    }
}
