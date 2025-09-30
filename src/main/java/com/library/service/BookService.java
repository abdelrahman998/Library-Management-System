package com.library.service;

import com.library.entity.Book;
import com.library.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;

    /**
     * Retrieve all books with pagination
     */
    public Page<Book> getAllBooks(Pageable pageable) {
        return bookRepository.findAll(pageable);
    }

    /**
     * Find book by ID
     */
    public Optional<Book> getBookById(Long id) {
        return bookRepository.findById(id);
    }

    /**
     * Create a new book
     */
    public Book createBook(Book book) {
        // Validate unique ISBN constraint if provided
        if (book.getIsbn() != null && bookRepository.existsByIsbn(book.getIsbn())) {
            throw new RuntimeException("Book with ISBN already exists: " + book.getIsbn());
        }

        // Set default values if not provided
        if (book.getTotalCopies() == null) {
            book.setTotalCopies(1);
        }
        if (book.getAvailableCopies() == null) {
            book.setAvailableCopies(book.getTotalCopies());
        }

        return bookRepository.save(book);
    }

    /**
     * Update an existing book
     */
    public Book updateBook(Long id, Book bookDetails) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + id));

        // Update fields
        book.setTitle(bookDetails.getTitle());
        book.setIsbn(bookDetails.getIsbn());
        book.setEdition(bookDetails.getEdition());
        book.setPublicationYear(bookDetails.getPublicationYear());
        book.setLanguage(bookDetails.getLanguage());
        book.setSummary(bookDetails.getSummary());
        book.setCoverImageUrl(bookDetails.getCoverImageUrl());
        book.setTotalCopies(bookDetails.getTotalCopies());
        book.setAvailableCopies(bookDetails.getAvailableCopies());
        book.setAuthors(bookDetails.getAuthors());
        book.setPublisher(bookDetails.getPublisher());
        book.setCategories(bookDetails.getCategories());

        return bookRepository.save(book);
    }

    /**
     * Delete a book
     */
    public void deleteBook(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + id));

        bookRepository.delete(book);
    }

    /**
     * Search books by title (case-insensitive)
     */
    public Page<Book> searchBooksByTitle(String title, Pageable pageable) {
        return bookRepository.findByTitleContainingIgnoreCase(title, pageable);
    }

    /**
     * Get all available books (books with available copies > 0)
     */
    public Page<Book> getAvailableBooks(Pageable pageable) {
        return bookRepository.findAvailableBooks(pageable);
    }

    /**
     * Update book availability when borrowing/returning
     */
    public void updateBookAvailability(Long bookId, int change) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + bookId));

        int newAvailableCopies = book.getAvailableCopies() + change;

        // Validate availability constraints
        if (newAvailableCopies < 0) {
            throw new RuntimeException("Cannot reduce available copies below zero");
        }
        if (newAvailableCopies > book.getTotalCopies()) {
            throw new RuntimeException("Available copies cannot exceed total copies");
        }

        book.setAvailableCopies(newAvailableCopies);
        bookRepository.save(book);
    }

    /**
     * Add copies to an existing book
     */
    public Book addCopies(Long bookId, int additionalCopies) {
        if (additionalCopies <= 0) {
            throw new RuntimeException("Additional copies must be positive");
        }

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + bookId));

        book.setTotalCopies(book.getTotalCopies() + additionalCopies);
        book.setAvailableCopies(book.getAvailableCopies() + additionalCopies);

        return bookRepository.save(book);
    }

    /**
     * Remove copies from an existing book
     */
    public Book removeCopies(Long bookId, int copiesToRemove) {
        if (copiesToRemove <= 0) {
            throw new RuntimeException("Copies to remove must be positive");
        }

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + bookId));

        int borrowedCopies = book.getTotalCopies() - book.getAvailableCopies();

        if (book.getTotalCopies() - copiesToRemove < borrowedCopies) {
            throw new RuntimeException("Cannot remove more copies than available (some copies are currently borrowed)");
        }

        book.setTotalCopies(book.getTotalCopies() - copiesToRemove);
        book.setAvailableCopies(Math.max(0, book.getAvailableCopies() - copiesToRemove));

        return bookRepository.save(book);
    }
}
