package com.library.service;

import com.library.entity.Book;
import com.library.entity.BorrowingTransaction;
import com.library.entity.Member;
import com.library.entity.SystemUser;
import com.library.repository.BorrowingTransactionRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class BorrowingService {

    private final BorrowingTransactionRepository borrowingRepository;
    private final BookService bookService;
    private final MemberService memberService;
    private final SystemUserService userService;

    // Configuration constants
    private static final int MAX_BOOKS_PER_MEMBER = 5;
    private static final int DEFAULT_BORROWING_PERIOD_DAYS = 14;
    private static final double DAILY_FINE_RATE = 0.50; // $0.50 per day

    /**
     * Borrow a book for a member
     */
    public BorrowingTransaction borrowBook(Long bookId, Long memberId, Long issuedByUserId) {
        // Validate book exists and is available
        Book book = bookService.getBookById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + bookId));

        if (!book.isAvailable()) {
            throw new RuntimeException("Book is not available for borrowing");
        }

        // Validate member exists and is eligible
        Member member = memberService.getMemberById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found with id: " + memberId));

        if (!memberService.isMemberEligibleForBorrowing(memberId)) {
            throw new RuntimeException("Member is not eligible for borrowing (inactive or expired membership)");
        }

        // Validate user exists
        SystemUser issuedBy = userService.getUserById(issuedByUserId);
        if (issuedBy == null) {
            throw new RuntimeException("User not found with id: " + issuedByUserId);
        }
        // Check borrowing limits
        Long activeBorrowings = borrowingRepository.countActiveBorrowingsByMember(member);
        if (activeBorrowings >= MAX_BOOKS_PER_MEMBER) {
            throw new RuntimeException("Member has reached maximum borrowing limit of " + MAX_BOOKS_PER_MEMBER + " books");
        }

        // Create borrowing transaction
        BorrowingTransaction transaction = new BorrowingTransaction();
        transaction.setBook(book);
        transaction.setMember(member);
        transaction.setIssuedBy(issuedBy);
        transaction.setBorrowDate(LocalDateTime.now());
        transaction.setDueDate(LocalDate.now().plusDays(DEFAULT_BORROWING_PERIOD_DAYS));
        transaction.setStatus(BorrowingTransaction.TransactionStatus.BORROWED);
        transaction.setFineAmount(0.0);

        BorrowingTransaction savedTransaction = borrowingRepository.save(transaction);

        // Update book availability
        bookService.updateBookAvailability(bookId, -1);

        return savedTransaction;
    }

    /**
     * Return a borrowed book
     */
    public BorrowingTransaction returnBook(Long transactionId, Long returnedToUserId) {
        BorrowingTransaction transaction = borrowingRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + transactionId));

        if (transaction.getStatus() != BorrowingTransaction.TransactionStatus.BORROWED) {
            throw new RuntimeException("Book is not currently borrowed or already returned");
        }

        SystemUser returnedTo = userService.getUserById(returnedToUserId);
        if (returnedTo == null) {
            throw new RuntimeException("User not found with id: " + returnedToUserId);
        }
        // Calculate fine if overdue
        LocalDate today = LocalDate.now();
        if (today.isAfter(transaction.getDueDate())) {
            long overdueDays = ChronoUnit.DAYS.between(transaction.getDueDate(), today);
            double fine = overdueDays * DAILY_FINE_RATE;
            transaction.setFineAmount(fine);
            transaction.setStatus(BorrowingTransaction.TransactionStatus.OVERDUE);
        }

        // Update transaction
        transaction.setReturnDate(LocalDateTime.now());
        transaction.setReturnedTo(returnedTo);
        if (transaction.getStatus() == BorrowingTransaction.TransactionStatus.BORROWED) {
            transaction.setStatus(BorrowingTransaction.TransactionStatus.RETURNED);
        }

        BorrowingTransaction updatedTransaction = borrowingRepository.save(transaction);

        // Update book availability
        bookService.updateBookAvailability(transaction.getBook().getId(), 1);

        return updatedTransaction;
    }

    /**
     * Get all borrowing transactions with pagination
     */
    public Page<BorrowingTransaction> getAllTransactions(Pageable pageable) {
        return borrowingRepository.findAll(pageable);
    }

    /**
     * Find transaction by ID
     */
    public Optional<BorrowingTransaction> getTransactionById(Long id) {
        return borrowingRepository.findById(id);
    }

    /**
     * Get transactions by member with pagination
     */
    public Page<BorrowingTransaction> getTransactionsByMember(Long memberId, Pageable pageable) {
        return borrowingRepository.findByMemberId(memberId, pageable);
    }

    /**
     * Get transactions by book with pagination
     */
    public Page<BorrowingTransaction> getTransactionsByBook(Long bookId, Pageable pageable) {
        return borrowingRepository.findByBookId(bookId, pageable);
    }

    /**
     * Get all overdue transactions
     */
    public List<BorrowingTransaction> getOverdueTransactions() {
        return borrowingRepository.findOverdueTransactions(LocalDate.now());
    }

    /**
     * Get active borrowings for a specific member
     */
    public List<BorrowingTransaction> getActiveBorrowingsByMember(Long memberId) {
        Member member = memberService.getMemberById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found with id: " + memberId));

        return borrowingRepository.findByMemberAndStatus(member, BorrowingTransaction.TransactionStatus.BORROWED);
    }

    /**
     * Extend due date for a borrowing transaction
     */
    public BorrowingTransaction extendDueDate(Long transactionId, int additionalDays) {
        if (additionalDays <= 0) {
            throw new RuntimeException("Additional days must be positive");
        }

        BorrowingTransaction transaction = borrowingRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + transactionId));

        if (transaction.getStatus() != BorrowingTransaction.TransactionStatus.BORROWED) {
            throw new RuntimeException("Can only extend due date for active borrowings");
        }

        LocalDate newDueDate = transaction.getDueDate().plusDays(additionalDays);
        transaction.setDueDate(newDueDate);

        return borrowingRepository.save(transaction);
    }

    /**
     * Mark book as lost
     */
    public BorrowingTransaction markAsLost(Long transactionId, double replacementCost) {
        BorrowingTransaction transaction = borrowingRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + transactionId));

        if (transaction.getStatus() == BorrowingTransaction.TransactionStatus.RETURNED) {
            throw new RuntimeException("Cannot mark returned book as lost");
        }

        transaction.setStatus(BorrowingTransaction.TransactionStatus.LOST);
        transaction.setFineAmount(replacementCost);
        transaction.setReturnDate(LocalDateTime.now());

        BorrowingTransaction updatedTransaction = borrowingRepository.save(transaction);

        // Update book availability (book is permanently lost)
        bookService.updateBookAvailability(transaction.getBook().getId(), 1);
        // Remove one copy from total since it's lost
        Book book = transaction.getBook();
        book.setTotalCopies(book.getTotalCopies() - 1);

        return updatedTransaction;
    }

    /**
     * Get borrowing statistics for a member
     */
    public BorrowingStats getMemberBorrowingStats(Long memberId) {
        Member member = memberService.getMemberById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found with id: " + memberId));

        Long activeBorrowings = borrowingRepository.countActiveBorrowingsByMember(member);

        // Get all transactions for this member to calculate stats
        List<BorrowingTransaction> allTransactions = borrowingRepository.findByMemberAndStatus(member, null);

        long totalBorrowings = allTransactions.size();
        long returnedBooks = allTransactions.stream()
                .mapToLong(t -> t.getStatus() == BorrowingTransaction.TransactionStatus.RETURNED ? 1 : 0)
                .sum();
        long overdueBooks = allTransactions.stream()
                .mapToLong(t -> t.getStatus() == BorrowingTransaction.TransactionStatus.OVERDUE ? 1 : 0)
                .sum();
        double totalFines = allTransactions.stream()
                .mapToDouble(BorrowingTransaction::getFineAmount)
                .sum();

        return new BorrowingStats(activeBorrowings, totalBorrowings, returnedBooks, overdueBooks, totalFines);
    }

    /**
     * Check if member can borrow more books
     */
    public boolean canMemberBorrowMore(Long memberId) {
        if (!memberService.isMemberEligibleForBorrowing(memberId)) {
            return false;
        }

        Member member = memberService.getMemberById(memberId)
                .orElse(null);
        if (member == null) {
            return false;
        }

        Long activeBorrowings = borrowingRepository.countActiveBorrowingsByMember(member);
        return activeBorrowings < MAX_BOOKS_PER_MEMBER;
    }

    /**
     * Get remaining borrowing capacity for a member
     */
    public int getRemainingBorrowingCapacity(Long memberId) {
        Member member = memberService.getMemberById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found with id: " + memberId));

        Long activeBorrowings = borrowingRepository.countActiveBorrowingsByMember(member);
        return Math.max(0, MAX_BOOKS_PER_MEMBER - activeBorrowings.intValue());
    }

    /**
     * Inner class for borrowing statistics
     */
    @Data
    public static class BorrowingStats {
        private final Long activeBorrowings;
        private final Long totalBorrowings;
        private final Long returnedBooks;
        private final Long overdueBooks;
        private final Double totalFines;

        public BorrowingStats(Long activeBorrowings, Long totalBorrowings, Long returnedBooks,
                              Long overdueBooks, Double totalFines) {
            this.activeBorrowings = activeBorrowings;
            this.totalBorrowings = totalBorrowings;
            this.returnedBooks = returnedBooks;
            this.overdueBooks = overdueBooks;
            this.totalFines = totalFines;
        }

    }
}
