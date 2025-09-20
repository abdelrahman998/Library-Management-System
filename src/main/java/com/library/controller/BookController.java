package com.library.controller;

import com.library.dto.BookRequest;
import com.library.dto.BookResponse;
import com.library.entity.Book;
import com.library.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {
    private final BookService bookService;

    @PostMapping
    public ResponseEntity<BookResponse> createBook(@Valid @RequestBody BookRequest request) {
        Book book = mapToEntity(request);
        Book createdBook = bookService.createBook(book);
        return new ResponseEntity<>(mapToResponse(createdBook), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> getBookById(@PathVariable Long id) {
        return bookService.getBookById(id)
                .map(book -> ResponseEntity.ok(mapToResponse(book)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<Page<BookResponse>> getAllBooks(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(bookService.getAllBooks(pageable)
                .map(this::mapToResponse));
    }

//    @GetMapping("/search")
//    public ResponseEntity<Page<BookResponse>> searchBooks(
//            @RequestParam(required = false) String title,
//            @RequestParam(required = false) String author,
//            @RequestParam(required = false) String category,
//            @RequestParam(required = false) String publisher,
//            @RequestParam(required = false) Integer year,
//            @RequestParam(required = false) String language,
//            @RequestParam(required = false, defaultValue = "false") boolean availableOnly,
//            @PageableDefault(size = 20) Pageable pageable) {
//
//        Page<Book> books = bookService.searchBooks(
//                title,
//                author,
//                category,
//                publisher,
//                year,
//                language,
//                availableOnly,
//                pageable
//        );
//
//        return ResponseEntity.ok(books.map(this::mapToResponse));
//    }

    @PutMapping("/{id}")
    public ResponseEntity<BookResponse> updateBook(
            @PathVariable Long id,
            @Valid @RequestBody BookRequest request) {
        Book book = mapToEntity(request);
        book.setId(id);
        Book updatedBook = bookService.updateBook(id, book);
        return ResponseEntity.ok(mapToResponse(updatedBook));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/add-copies/{copies}")
    public ResponseEntity<BookResponse> addCopies(
            @PathVariable Long id,
            @PathVariable int copies) {
        Book updatedBook = bookService.addCopies(id, copies);
        return ResponseEntity.ok(mapToResponse(updatedBook));
    }

    @PostMapping("/{id}/remove-copies/{copies}")
    public ResponseEntity<BookResponse> removeCopies(
            @PathVariable Long id,
            @PathVariable int copies) {
        Book updatedBook = bookService.removeCopies(id, copies);
        return ResponseEntity.ok(mapToResponse(updatedBook));
    }

    @GetMapping("/available")
    public ResponseEntity<Page<BookResponse>> getAvailableBooks(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(bookService.getAvailableBooks(pageable)
                .map(this::mapToResponse));
    }

//    @GetMapping("/by-category/{category}")
//    public ResponseEntity<Page<BookResponse>> getBooksByCategory(
//            @PathVariable String category,
//            @PageableDefault(size = 20) Pageable pageable) {
//        return ResponseEntity.ok(bookService.getBooksByCategory(category, pageable)
//                .map(this::mapToResponse));
//    }
//
//    @GetMapping("/by-author/{author}")
//    public ResponseEntity<Page<BookResponse>> getBooksByAuthor(
//            @PathVariable String author,
//            @PageableDefault(size = 20) Pageable pageable) {
//        return ResponseEntity.ok(bookService.searchBooksByAuthor(author, pageable)
//                .map(this::mapToResponse));
//    }

    // Helper methods to map between entity and DTO
    private Book mapToEntity(BookRequest request) {
        // Implementation depends on your entity structure
        // This is a simplified version
        Book book = new Book();
        book.setTitle(request.getTitle());
        book.setIsbn(request.getIsbn());
        book.setEdition(request.getEdition());
        book.setPublicationYear(request.getPublicationYear());
        book.setLanguage(request.getLanguage());
        book.setSummary(request.getSummary());
        book.setCoverImageUrl(request.getCoverImageUrl());
        book.setTotalCopies(request.getTotalCopies());
        book.setAvailableCopies(request.getAvailableCopies());
        // Note: You'll need to set authors, publisher, and categories using their services
        return book;
    }

    private BookResponse mapToResponse(Book book) {
        return BookResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .isbn(book.getIsbn())
                .edition(book.getEdition())
                .publicationYear(book.getPublicationYear())
                .language(book.getLanguage())
                .summary(book.getSummary())
                .coverImageUrl(book.getCoverImageUrl())
                .totalCopies(book.getTotalCopies())
                .availableCopies(book.getAvailableCopies())
                // Note: You'll need to map authors, publisher, and categories
                .createdAt(book.getCreatedAt())
                .updatedAt(book.getUpdatedAt())
                .build();
    }
}
