package com.library.repository;

import com.library.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    // Check if book exists by ISBN
    boolean existsByIsbn(String isbn);

    // Search by title (case-insensitive)
    Page<Book> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    // Find available books (availableCopies > 0)
    @Query("SELECT b FROM Book b WHERE b.availableCopies > 0")
    Page<Book> findAvailableBooks(Pageable pageable);

}
