package com.library.dto;

import com.library.entity.BorrowingTransaction;
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
public class BorrowingResponse {
    private Long id;
    private Long bookId;
    private Long memberId;
    private Long issuedById;
    private Long returnedToId;
    private LocalDateTime borrowDate;
    private LocalDate dueDate;
    private LocalDateTime returnDate;
    private Double fineAmount;
    private BorrowingTransaction.TransactionStatus status;
    private String notes;
}