package com.library.dto;

import com.library.entity.Member.MembershipStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class MemberRequest {
    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    private String phoneNumber;
    private String address;
    private LocalDate dateOfBirth;
    private LocalDate membershipExpiry;
    private MembershipStatus status;
}
