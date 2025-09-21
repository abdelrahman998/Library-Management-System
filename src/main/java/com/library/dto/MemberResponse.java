package com.library.dto;

import com.library.entity.Member.MembershipStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberResponse {
    private Long id;
    private String membershipId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String address;
    private LocalDate dateOfBirth;
    private LocalDate membershipDate;
    private LocalDate membershipExpiry;
    private MembershipStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
