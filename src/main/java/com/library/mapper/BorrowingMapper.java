package com.library.mapper;

import com.library.dto.BorrowingResponse;
import com.library.entity.BorrowingTransaction;
import org.springframework.stereotype.Component;

@Component
public class BorrowingMapper {
    
    public BorrowingResponse toResponse(BorrowingTransaction transaction) {
        if (transaction == null) {
            return null;
        }
        
        return BorrowingResponse.builder()
                .id(transaction.getId())
                .bookId(transaction.getBook() != null ? transaction.getBook().getId() : null)
                .memberId(transaction.getMember() != null ? transaction.getMember().getId() : null)
                .issuedById(transaction.getIssuedBy() != null ? transaction.getIssuedBy().getId() : null)
                .returnedToId(transaction.getReturnedTo() != null ? transaction.getReturnedTo().getId() : null)
                .borrowDate(transaction.getBorrowDate())
                .dueDate(transaction.getDueDate())
                .returnDate(transaction.getReturnDate())
                .fineAmount(transaction.getFineAmount())
                .status(transaction.getStatus())
                .notes(transaction.getNotes())
                .build();
    }
}