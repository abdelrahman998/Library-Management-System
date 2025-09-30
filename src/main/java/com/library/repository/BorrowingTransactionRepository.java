package com.library.repository;

import com.library.entity.Book;
import com.library.entity.BorrowingTransaction;
import com.library.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BorrowingTransactionRepository extends JpaRepository<BorrowingTransaction, Long> {

    // Find all transactions for a member with pagination
    Page<BorrowingTransaction> findByMemberId(Long memberId, Pageable pageable);

    // Find all transactions for a book with pagination
    Page<BorrowingTransaction> findByBookId(Long bookId, Pageable pageable);

    // Find transactions by member and status
    List<BorrowingTransaction> findByMemberAndStatus(Member member, BorrowingTransaction.TransactionStatus status);

    // Count active borrowings for a member
    @Query("SELECT COUNT(b) FROM BorrowingTransaction b WHERE b.member = :member AND b.status = 'BORROWED'")
    Long countActiveBorrowingsByMember(@Param("member") Member member);

    // Find overdue transactions
    @Query("SELECT b FROM BorrowingTransaction b WHERE b.dueDate < :currentDate AND b.status = 'BORROWED'")
    List<BorrowingTransaction> findOverdueTransactions(@Param("currentDate") LocalDate currentDate);

}
