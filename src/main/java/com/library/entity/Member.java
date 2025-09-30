package com.library.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "members")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Member implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(unique = true, updatable = false)
    private String membershipId;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @Email
    @Column(unique = true)
    private String email;

    private String phoneNumber;
    private String address;

    private LocalDate dateOfBirth;
    private LocalDate membershipDate;
    private LocalDate membershipExpiry;

    @Enumerated(EnumType.STRING)
    private MembershipStatus status = MembershipStatus.ACTIVE;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<BorrowingTransaction> borrowingTransactions;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (this.membershipId == null) {
            this.membershipId = "M" + String.format("%05d", (int)(Math.random() * 100000));
        }
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public enum MembershipStatus {
        ACTIVE, SUSPENDED, EXPIRED, CANCELLED
    }
}
