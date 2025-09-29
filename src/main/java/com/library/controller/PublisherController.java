package com.library.controller;

import com.library.entity.Publisher;
import com.library.service.PublisherService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/publishers")
@RequiredArgsConstructor
public class PublisherController {

    private final PublisherService publisherService;

    // Get all publishers with pagination
    @GetMapping
    public Page<Publisher> getAllPublishers(Pageable pageable) {
        return publisherService.getAllPublishers(pageable);
    }

    // Get publisher by ID
    @GetMapping("/{id}")
    public ResponseEntity<Publisher> getPublisherById(@PathVariable Long id) {
        return publisherService.getPublisherById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Get publisher by name
    @GetMapping("/name/{name}")
    public ResponseEntity<Publisher> getPublisherByName(@PathVariable String name) {
        return publisherService.getPublisherByName(name)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Create a new publisher
    @PostMapping
    public Publisher createPublisher(@RequestBody Publisher publisher) {
        return publisherService.createPublisher(publisher);
    }

    // Update an existing publisher
    @PutMapping("/{id}")
    public ResponseEntity<Publisher> updatePublisher(
            @PathVariable Long id,
            @RequestBody Publisher publisherDetails) {
        try {
            Publisher updatedPublisher = publisherService.updatePublisher(id, publisherDetails);
            return ResponseEntity.ok(updatedPublisher);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Delete a publisher
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePublisher(@PathVariable Long id) {
        try {
            publisherService.deletePublisher(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Search publishers by name
    @GetMapping("/search")
    public Page<Publisher> searchPublishers(
            @RequestParam String name,
            Pageable pageable) {
        return publisherService.searchPublishersByName(name, pageable);
    }

    // Get publisher statistics
    @GetMapping("/statistics")
    public PublisherService.PublisherStats getPublisherStatistics() {
        return publisherService.getPublisherStatistics();
    }

    // Get publishers with most books
    @GetMapping("/most-active")
    public List<Publisher> getMostActivePublishers(
            @RequestParam(defaultValue = "5") int limit) {
        return publisherService.getMostActivePublishers(limit);
    }

    // Get publishers with no books
    @GetMapping("/without-books")
    public List<Publisher> getPublishersWithoutBooks() {
        return publisherService.getPublishersWithoutBooks();
    }
}