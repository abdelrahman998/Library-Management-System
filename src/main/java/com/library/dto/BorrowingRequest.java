package com.library.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BorrowingRequest {
    @NotNull(message = "Book ID is required")
    private Long bookId;

    @NotNull(message = "Member ID is required")
    private Long memberId;

    @NotNull(message = "Issued by user ID is required")
    private Long issuedByUserId;
}