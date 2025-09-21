package com.library.repository;

import com.library.entity.Publisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PublisherRepository extends JpaRepository<Publisher, Long> {

    // Find a publisher by exact name
    Optional<Publisher> findByName(String name);

    // Check if a publisher exists with the given name
    boolean existsByName(String name);

    // Search publishers by name (case-insensitive, partial match)
    Page<Publisher> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
