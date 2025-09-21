package com.library.repository;

import com.library.entity.Author;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {

    // Search authors by name (first name or last name) containing the given string (case-insensitive)
    @Query("SELECT a FROM Author a WHERE LOWER(a.firstName) LIKE LOWER(concat('%', :name, '%')) OR " +
            "LOWER(a.lastName) LIKE LOWER(concat('%', :name, '%'))")
    Page<Author> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);

    // Find authors by exact nationality
    List<Author> findByNationality(String nationality);

    // Find authors by full name (first name + last name)
    @Query("SELECT a FROM Author a WHERE CONCAT(a.firstName, ' ', a.lastName) = :fullName")
    List<Author> findByFullName(@Param("fullName") String fullName);
}
