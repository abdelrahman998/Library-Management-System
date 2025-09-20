package com.library.service;

import com.library.entity.Book;
import com.library.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BookService {

    @Autowired
    private BookRepository bookRepository;

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
     * Find book by ISBN
     */
    public Optional<Book> getBookByIsbn(String isbn) {
        return bookRepository.findByIsbn(isbn);
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
//
//    /**
//     * Search books by author name (case-insensitive)
//     */
//    public Page<Book> searchBooksByAuthor(String authorName, Pageable pageable) {
//        return bookRepository.findByAuthorNameContainingIgnoreCase(authorName, pageable);
//    }
//
//    /**
//     * Get books by category name
//     */
//    public Page<Book> getBooksByCategory(String categoryName, Pageable pageable) {
//        return bookRepository.findByCategoryName(categoryName, pageable);
//    }
//
//    /**
//     * Get books by publication year
//     */
//    public Page<Book> getBooksByPublicationYear(Integer year, Pageable pageable) {
//        return bookRepository.findByPublicationYear(year, pageable);
//    }
//
//    /**
//     * Get books by publisher name
//     */
//    public Page<Book> getBooksByPublisher(String publisherName, Pageable pageable) {
//        return bookRepository.findByPublisherName(publisherName, pageable);
//    }

    /**
     * Get all available books (books with available copies > 0)
     */
    public Page<Book> getAvailableBooks(Pageable pageable) {
        return bookRepository.findAvailableBooks(pageable);
    }

    /**
     * Get books by language
     */
    public List<Book> getBooksByLanguage(String language) {
        return bookRepository.findByLanguage(language);
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
     * Check if book exists by ISBN
     */
    public boolean existsByIsbn(String isbn) {
        return bookRepository.existsByIsbn(isbn);
    }

    /**
     * Check if book is available for borrowing
     */
    public boolean isBookAvailable(Long bookId) {
        return bookRepository.findById(bookId)
                .map(Book::isAvailable)
                .orElse(false);
    }

    /**
     * Get total number of books in the library
     */
    public long getTotalBookCount() {
        return bookRepository.count();
    }

    /**
     * Get total number of available books
     */
    public long getAvailableBookCount() {
        return bookRepository.findAll().stream()
                .filter(Book::isAvailable)
                .count();
    }

    /**
     * Get total copies count across all books
     */
    public long getTotalCopiesCount() {
        return bookRepository.findAll().stream()
                .mapToInt(book -> book.getTotalCopies() != null ? book.getTotalCopies() : 0)
                .sum();
    }

    /**
     * Get total available copies count across all books
     */
    public long getAvailableCopiesCount() {
        return bookRepository.findAll().stream()
                .mapToInt(book -> book.getAvailableCopies() != null ? book.getAvailableCopies() : 0)
                .sum();
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
