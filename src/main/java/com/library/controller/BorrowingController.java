package com.library.controller;

import com.library.dto.BorrowingRequest;
import com.library.dto.BorrowingResponse;
import com.library.dto.ReturnBookRequest;
import com.library.entity.BorrowingTransaction;
import com.library.mapper.BorrowingMapper;
import com.library.service.BorrowingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/borrowings")
@RequiredArgsConstructor
public class BorrowingController {

    private final BorrowingService borrowingService;
    private final BorrowingMapper borrowingMapper;

    @PostMapping
    public ResponseEntity<BorrowingResponse> borrowBook(@Valid @RequestBody BorrowingRequest request) {
        BorrowingTransaction transaction = borrowingService.borrowBook(
                request.getBookId(),
                request.getMemberId(),
                request.getIssuedByUserId()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(borrowingMapper.toResponse(transaction));
    }

    @PostMapping("/{id}/return")
    public ResponseEntity<BorrowingResponse> returnBook(
            @PathVariable Long id,
            @Valid @RequestBody ReturnBookRequest request) {

        BorrowingTransaction transaction = borrowingService.returnBook(
                id,
                request.getReturnedToUserId()
        );
        return ResponseEntity.ok(borrowingMapper.toResponse(transaction));
    }

    @GetMapping
    public ResponseEntity<Page<BorrowingResponse>> getAllBorrowings(
            @PageableDefault(size = 20) Pageable pageable) {

        return ResponseEntity.ok(
                borrowingService.getAllTransactions(pageable)
                        .map(borrowingMapper::toResponse)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<BorrowingResponse> getBorrowingById(@PathVariable Long id) {
        return ResponseEntity.of(
                borrowingService.getTransactionById(id)
                        .map(borrowingMapper::toResponse)
        );
    }

    @GetMapping("/member/{memberId}")
    public ResponseEntity<Page<BorrowingResponse>> getBorrowingsByMember(
            @PathVariable Long memberId,
            @PageableDefault(size = 20) Pageable pageable) {

        return ResponseEntity.ok(
                borrowingService.getTransactionsByMember(memberId, pageable)
                        .map(borrowingMapper::toResponse)
        );
    }

    @GetMapping("/book/{bookId}")
    public ResponseEntity<Page<BorrowingResponse>> getBorrowingsByBook(
            @PathVariable Long bookId,
            @PageableDefault(size = 20) Pageable pageable) {

        return ResponseEntity.ok(
                borrowingService.getTransactionsByBook(bookId, pageable)
                        .map(borrowingMapper::toResponse)
        );
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<BorrowingResponse>> getOverdueBorrowings() {
        return ResponseEntity.ok(
                borrowingService.getOverdueTransactions().stream()
                        .map(borrowingMapper::toResponse)
                        .collect(Collectors.toList())
        );
    }

    @GetMapping("/member/{memberId}/active")
    public ResponseEntity<List<BorrowingResponse>> getActiveBorrowingsByMember(
            @PathVariable Long memberId) {

        return ResponseEntity.ok(
                borrowingService.getActiveBorrowingsByMember(memberId).stream()
                        .map(borrowingMapper::toResponse)
                        .collect(Collectors.toList())
        );
    }

    @PostMapping("/{id}/extend")
    public ResponseEntity<BorrowingResponse> extendDueDate(
            @PathVariable Long id,
            @RequestParam int additionalDays) {

        BorrowingTransaction transaction = borrowingService.extendDueDate(id, additionalDays);
        return ResponseEntity.ok(borrowingMapper.toResponse(transaction));
    }

    @PostMapping("/{id}/mark-lost")
    public ResponseEntity<BorrowingResponse> markAsLost(
            @PathVariable Long id,
            @RequestParam double replacementCost) {

        BorrowingTransaction transaction = borrowingService.markAsLost(id, replacementCost);
        return ResponseEntity.ok(borrowingMapper.toResponse(transaction));
    }

    @GetMapping("/member/{memberId}/stats")
    public ResponseEntity<?> getMemberBorrowingStats(@PathVariable Long memberId) {
        return ResponseEntity.ok(borrowingService.getMemberBorrowingStats(memberId));
    }

    @GetMapping("/member/{memberId}/can-borrow")
    public ResponseEntity<Boolean> canMemberBorrowMore(@PathVariable Long memberId) {
        return ResponseEntity.ok(borrowingService.canMemberBorrowMore(memberId));
    }

    @GetMapping("/member/{memberId}/remaining-capacity")
    public ResponseEntity<Integer> getRemainingBorrowingCapacity(@PathVariable Long memberId) {
        return ResponseEntity.ok(borrowingService.getRemainingBorrowingCapacity(memberId));
    }
}