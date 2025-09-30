package com.library.service;

import com.library.entity.Author;
import com.library.repository.AuthorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthorService {

    private final AuthorRepository authorRepository;

    /**
     * Retrieve all authors with pagination
     */
    public Page<Author> getAllAuthors(Pageable pageable) {
        return authorRepository.findAll(pageable);
    }

    /**
     * Find author by ID
     */
    public Optional<Author> getAuthorById(Long id) {
        return authorRepository.findById(id);
    }

    /**
     * Create a new author
     */
    public Author createAuthor(Author author) {
        validateAuthorData(author);
        return authorRepository.save(author);
    }

    /**
     * Update an existing author
     */
    public Author updateAuthor(Long id, Author authorDetails) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Author not found with id: " + id));

        // Validate updated data
        validateAuthorData(authorDetails);

        // Update fields
        author.setFirstName(authorDetails.getFirstName());
        author.setLastName(authorDetails.getLastName());
        author.setBiography(authorDetails.getBiography());
        author.setBirthDate(authorDetails.getBirthDate());
        author.setDeathDate(authorDetails.getDeathDate());
        author.setNationality(authorDetails.getNationality());

        return authorRepository.save(author);
    }

    /**
     * Delete an author
     */
    public void deleteAuthor(Long id) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Author not found with id: " + id));

        // Check if author has books before deletion
        if (author.getBooks() != null && !author.getBooks().isEmpty()) {
            throw new RuntimeException("Cannot delete author who has books. Remove books first or reassign to other authors.");
        }

        authorRepository.delete(author);
    }

    /**
     * Search authors by name (case-insensitive)
     */
    public Page<Author> searchAuthorsByName(String name, Pageable pageable) {
        return authorRepository.findByNameContainingIgnoreCase(name, pageable);
    }

    /**
     * Find authors by nationality
     */
    public List<Author> getAuthorsByNationality(String nationality) {
        return authorRepository.findByNationality(nationality);
    }

    /**
     * Find authors by full name
     */
    public List<Author> getAuthorsByFullName(String fullName) {
        return authorRepository.findByFullName(fullName);
    }

    /**
     * Get all living authors (no death date)
     */
    public List<Author> getLivingAuthors() {
        return authorRepository.findAll().stream()
                .filter(author -> author.getDeathDate() == null)
                .toList();
    }

    /**
     * Get all deceased authors (has death date)
     */
    public List<Author> getDeceasedAuthors() {
        return authorRepository.findAll().stream()
                .filter(author -> author.getDeathDate() != null)
                .toList();
    }

    /**
     * Get authors born in a specific year
     */
    public List<Author> getAuthorsByBirthYear(int year) {
        return authorRepository.findAll().stream()
                .filter(author -> author.getBirthDate() != null && author.getBirthDate().getYear() == year)
                .toList();
    }

    /**
     * Get authors born between two dates
     */
    public List<Author> getAuthorsBornBetween(LocalDate startDate, LocalDate endDate) {
        return authorRepository.findAll().stream()
                .filter(author -> author.getBirthDate() != null &&
                        !author.getBirthDate().isBefore(startDate) &&
                        !author.getBirthDate().isAfter(endDate))
                .toList();
    }

    /**
     * Get authors by age range (for living authors)
     */
    public List<Author> getAuthorsByAgeRange(int minAge, int maxAge) {
        LocalDate today = LocalDate.now();
        return getLivingAuthors().stream()
                .filter(author -> {
                    if (author.getBirthDate() == null) return false;
                    int age = Period.between(author.getBirthDate(), today).getYears();
                    return age >= minAge && age <= maxAge;
                })
                .toList();
    }

    /**
     * Calculate author's age (if alive) or age at death
     */
    public Integer getAuthorAge(Long authorId) {
        Author author = authorRepository.findById(authorId)
                .orElseThrow(() -> new RuntimeException("Author not found with id: " + authorId));

        if (author.getBirthDate() == null) {
            return null;
        }

        LocalDate endDate = author.getDeathDate() != null ? author.getDeathDate() : LocalDate.now();
        return Period.between(author.getBirthDate(), endDate).getYears();
    }

    /**
     * Check if author is alive
     */
    public boolean isAuthorAlive(Long authorId) {
        return authorRepository.findById(authorId)
                .map(author -> author.getDeathDate() == null)
                .orElse(false);
    }

    /**
     * Get total number of authors
     */
    public long getTotalAuthorCount() {
        return authorRepository.count();
    }

    /**
     * Get count of authors by nationality
     */
    public long getAuthorCountByNationality(String nationality) {
        return getAuthorsByNationality(nationality).size();
    }

    /**
     * Get count of living authors
     */
    public long getLivingAuthorCount() {
        return getLivingAuthors().size();
    }

    /**
     * Get all distinct nationalities
     */
    public List<String> getAllNationalities() {
        return authorRepository.findAll().stream()
                .map(Author::getNationality)
                .filter(nationality -> nationality != null && !nationality.trim().isEmpty())
                .distinct()
                .sorted()
                .toList();
    }

    /**
     * Get authors with most books
     */
    public List<Author> getMostProlificAuthors(int limit) {
        return authorRepository.findAll().stream()
                .filter(author -> author.getBooks() != null)
                .sorted((a1, a2) -> Integer.compare(a2.getBooks().size(), a1.getBooks().size()))
                .limit(limit)
                .toList();
    }

    /**
     * Search authors by partial name match
     */
    public List<Author> findAuthorsByPartialName(String partialName) {
        return authorRepository.findAll().stream()
                .filter(author ->
                        (author.getFirstName() != null &&
                                author.getFirstName().toLowerCase().contains(partialName.toLowerCase())) ||
                                (author.getLastName() != null &&
                                        author.getLastName().toLowerCase().contains(partialName.toLowerCase())))
                .toList();
    }

    /**
     * Check if author name already exists
     */
    public boolean authorExists(String firstName, String lastName) {
        String fullName = firstName + " " + lastName;
        return !getAuthorsByFullName(fullName).isEmpty();
    }

    /**
     * Get authors born in a specific decade
     */
    public List<Author> getAuthorsByDecade(int startYear) {
        int endYear = startYear + 9;
        LocalDate startDate = LocalDate.of(startYear, 1, 1);
        LocalDate endDate = LocalDate.of(endYear, 12, 31);
        return getAuthorsBornBetween(startDate, endDate);
    }


    /**
     * Validate author data
     */
    private void validateAuthorData(Author author) {
        if (author.getFirstName() == null || author.getFirstName().trim().isEmpty()) {
            throw new RuntimeException("First name is required");
        }
        if (author.getLastName() == null || author.getLastName().trim().isEmpty()) {
            throw new RuntimeException("Last name is required");
        }

        // Validate dates
        if (author.getBirthDate() != null && author.getDeathDate() != null) {
            if (author.getBirthDate().isAfter(author.getDeathDate())) {
                throw new RuntimeException("Birth date cannot be after death date");
            }
        }

        // Validate birth date is not in the future
        if (author.getBirthDate() != null && author.getBirthDate().isAfter(LocalDate.now())) {
            throw new RuntimeException("Birth date cannot be in the future");
        }

        // Validate death date is not in the future
        if (author.getDeathDate() != null && author.getDeathDate().isAfter(LocalDate.now())) {
            throw new RuntimeException("Death date cannot be in the future");
        }
    }

    /**
     * Inner class for author statistics
     */
    public static class AuthorStats {
        private final Long totalAuthors;
        private final Long livingAuthors;
        private final Long deceasedAuthors;
        private final Double averageAgeLiving;
        private final String mostCommonNationality;

        public AuthorStats(Long totalAuthors, Long livingAuthors, Long deceasedAuthors,
                           Double averageAgeLiving, String mostCommonNationality) {
            this.totalAuthors = totalAuthors;
            this.livingAuthors = livingAuthors;
            this.deceasedAuthors = deceasedAuthors;
            this.averageAgeLiving = averageAgeLiving;
            this.mostCommonNationality = mostCommonNationality;
        }

        // Getters
        public Long getTotalAuthors() { return totalAuthors; }
        public Long getLivingAuthors() { return livingAuthors; }
        public Long getDeceasedAuthors() { return deceasedAuthors; }
        public Double getAverageAgeLiving() { return averageAgeLiving; }
        public String getMostCommonNationality() { return mostCommonNationality; }
    }
}
