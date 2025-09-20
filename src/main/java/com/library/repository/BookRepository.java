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

    // Find by ISBN
    Optional<Book> findByIsbn(String isbn);

    // Check if book exists by ISBN
    boolean existsByIsbn(String isbn);

    // Search by title (case-insensitive)
    Page<Book> findByTitleContainingIgnoreCase(String title, Pageable pageable);

//    // Search by author name (using join with authors)
//    @Query("SELECT DISTINCT b FROM Book b JOIN b.authors a WHERE LOWER(a.name) LIKE LOWER(concat('%', :authorName,'%'))")
//    Page<Book> findByAuthorNameContainingIgnoreCase(@Param("authorName") String authorName, Pageable pageable);
//
//    // Find by category name (using join with categories)
//    @Query("SELECT b FROM Book b JOIN b.categories c WHERE c.name = :categoryName")
//    Page<Book> findByCategoryName(@Param("categoryName") String categoryName, Pageable pageable);
//
//    // Find by publication year
//    Page<Book> findByPublicationYear(Integer year, Pageable pageable);
//
//    // Find by publisher name
//    @Query("SELECT b FROM Book b JOIN b.publisher p WHERE p.name = :publisherName")
//    Page<Book> findByPublisherName(@Param("publisherName") String publisherName, Pageable pageable);

    // Find available books (availableCopies > 0)
    @Query("SELECT b FROM Book b WHERE b.availableCopies > 0")
    Page<Book> findAvailableBooks(Pageable pageable);

    // Find by language
    List<Book> findByLanguage(String language);

    // Find books with available copies
    @Query("SELECT b FROM Book b WHERE b.availableCopies > 0")
    List<Book> findAvailableBooks();

//    // Find books by multiple criteria
//    // In BookRepository.java
//    @Query("SELECT DISTINCT b FROM Book b " +
//            "LEFT JOIN b.authors a " +
//            "LEFT JOIN b.categories c " +
//            "WHERE (:title IS NULL OR LOWER(b.title) LIKE LOWER(concat('%', :title,'%'))) " +
//            "AND (:authorName IS NULL OR LOWER(a.firstName) LIKE LOWER(concat('%', :authorName,'%')) " +
//            "     OR LOWER(a.lastName) LIKE LOWER(concat('%', :authorName,'%'))) " +
//            "AND (:categoryName IS NULL OR c.name = :categoryName) " +
//            "AND (:publisherName IS NULL OR b.publisher.name = :publisherName) " +
//            "AND (:publicationYear IS NULL OR b.publicationYear = :publicationYear) " +
//            "AND (:language IS NULL OR b.language = :language) " +
//            "AND (:availableOnly = FALSE OR b.availableCopies > 0)")
//    Page<Book> searchBooks(
//            @Param("title") String title,
//            @Param("authorName") String authorName,
//            @Param("categoryName") String categoryName,
//            @Param("publisherName") String publisherName,
//            @Param("publicationYear") Integer publicationYear,
//            @Param("language") String language,
//            @Param("availableOnly") boolean availableOnly,
//            Pageable pageable
//    );
    // Count books by availability
    @Query("SELECT COUNT(b) FROM Book b WHERE b.availableCopies > 0")
    long countAvailableBooks();

    // Find books that need restocking (available copies below threshold)
    @Query("SELECT b FROM Book b WHERE b.availableCopies <= :threshold")
    List<Book> findBooksNeedingRestock(@Param("threshold") int threshold);
}
