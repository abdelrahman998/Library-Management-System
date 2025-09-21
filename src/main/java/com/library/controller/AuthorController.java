package com.library.controller;

import com.library.entity.Author;
import com.library.service.AuthorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/authors")
@RequiredArgsConstructor
public class AuthorController {

    private final AuthorService authorService;

    @GetMapping
    public ResponseEntity<Page<Author>> getAllAuthors(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(authorService.getAllAuthors(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Author> getAuthorById(@PathVariable Long id) {
        return authorService.getAuthorById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Author> createAuthor(@Valid @RequestBody Author author) {
        Author createdAuthor = authorService.createAuthor(author);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAuthor);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Author> updateAuthor(
            @PathVariable Long id,
            @Valid @RequestBody Author authorDetails) {
        try {
            Author updatedAuthor = authorService.updateAuthor(id, authorDetails);
            return ResponseEntity.ok(updatedAuthor);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAuthor(@PathVariable Long id) {
        try {
            authorService.deleteAuthor(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<Page<Author>> searchAuthors(
            @RequestParam String name,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(authorService.searchAuthorsByName(name, pageable));
    }

    @GetMapping("/nationality/{nationality}")
    public ResponseEntity<List<Author>> getAuthorsByNationality(
            @PathVariable String nationality) {
        return ResponseEntity.ok(authorService.getAuthorsByNationality(nationality));
    }

    @GetMapping("/name/{fullName}")
    public ResponseEntity<List<Author>> getAuthorsByFullName(
            @PathVariable String fullName) {
        return ResponseEntity.ok(authorService.getAuthorsByFullName(fullName));
    }

    @GetMapping("/living")
    public ResponseEntity<List<Author>> getLivingAuthors() {
        return ResponseEntity.ok(authorService.getLivingAuthors());
    }

    @GetMapping("/deceased")
    public ResponseEntity<List<Author>> getDeceasedAuthors() {
        return ResponseEntity.ok(authorService.getDeceasedAuthors());
    }

    @GetMapping("/birth-year/{year}")
    public ResponseEntity<List<Author>> getAuthorsByBirthYear(@PathVariable int year) {
        return ResponseEntity.ok(authorService.getAuthorsByBirthYear(year));
    }

    @GetMapping("/birth-range")
    public ResponseEntity<List<Author>> getAuthorsBornBetween(
            @RequestParam LocalDate start,
            @RequestParam LocalDate end) {
        return ResponseEntity.ok(authorService.getAuthorsBornBetween(start, end));
    }

    @GetMapping("/age-range")
    public ResponseEntity<List<Author>> getAuthorsByAgeRange(
            @RequestParam int minAge,
            @RequestParam int maxAge) {
        return ResponseEntity.ok(authorService.getAuthorsByAgeRange(minAge, maxAge));
    }

    @GetMapping("/{id}/age")
    public ResponseEntity<Integer> getAuthorAge(@PathVariable Long id) {
        Integer age = authorService.getAuthorAge(id);
        return age != null ?
                ResponseEntity.ok(age) :
                ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/alive")
    public ResponseEntity<Boolean> isAuthorAlive(@PathVariable Long id) {
        return ResponseEntity.ok(authorService.isAuthorAlive(id));
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getTotalAuthorCount() {
        return ResponseEntity.ok(authorService.getTotalAuthorCount());
    }

    @GetMapping("/count/nationality/{nationality}")
    public ResponseEntity<Long> getAuthorCountByNationality(
            @PathVariable String nationality) {
        return ResponseEntity.ok(authorService.getAuthorCountByNationality(nationality));
    }

    @GetMapping("/count/living")
    public ResponseEntity<Long> getLivingAuthorCount() {
        return ResponseEntity.ok(authorService.getLivingAuthorCount());
    }

    @GetMapping("/nationalities")
    public ResponseEntity<List<String>> getAllNationalities() {
        return ResponseEntity.ok(authorService.getAllNationalities());
    }

    @GetMapping("/prolific")
    public ResponseEntity<List<Author>> getMostProlificAuthors(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(authorService.getMostProlificAuthors(limit));
    }

    @GetMapping("/search/partial")
    public ResponseEntity<List<Author>> findAuthorsByPartialName(
            @RequestParam String name) {
        return ResponseEntity.ok(authorService.findAuthorsByPartialName(name));
    }

    @GetMapping("/exists")
    public ResponseEntity<Boolean> authorExists(
            @RequestParam String firstName,
            @RequestParam String lastName) {
        return ResponseEntity.ok(authorService.authorExists(firstName, lastName));
    }

    @GetMapping("/decade/{startYear}")
    public ResponseEntity<List<Author>> getAuthorsByDecade(@PathVariable int startYear) {
        return ResponseEntity.ok(authorService.getAuthorsByDecade(startYear));
    }

    @GetMapping("/stats")
    public ResponseEntity<AuthorService.AuthorStats> getAuthorStatistics() {
        return ResponseEntity.ok(authorService.getAuthorStatistics());
    }
}