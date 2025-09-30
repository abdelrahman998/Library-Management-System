package com.library.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "borrowing_transactions")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BorrowingTransaction implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issued_by_user_id")
    private SystemUser issuedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "returned_to_user_id")
    private SystemUser returnedTo;

    @CreatedDate
    private LocalDateTime borrowDate;

    private LocalDate dueDate;
    private LocalDateTime returnDate;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status = TransactionStatus.BORROWED;

    private Double fineAmount = 0.0;
    private String notes;

    public BorrowingTransaction(Book book, Member member, SystemUser issuedBy) {
        this.book = book;
        this.member = member;
        this.issuedBy = issuedBy;
        this.borrowDate = LocalDateTime.now();
        this.dueDate = LocalDate.now().plusWeeks(2); // 2 weeks borrowing period
    }

    public boolean isOverdue() {
        return status == TransactionStatus.BORROWED && LocalDate.now().isAfter(dueDate);
    }

    public enum TransactionStatus {
        BORROWED, RETURNED, OVERDUE, LOST
    }
}
