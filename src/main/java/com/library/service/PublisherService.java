package com.library.service;

import com.library.entity.Publisher;
import com.library.repository.PublisherRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@Transactional
@RequiredArgsConstructor
public class PublisherService {

    private final PublisherRepository publisherRepository;

    // Pattern for basic email validation
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    // Pattern for basic URL validation
    private static final Pattern URL_PATTERN = Pattern.compile("^(https?://)?(www\\.)?[a-zA-Z0-9-]+\\.[a-zA-Z]{2,}.*$");

    /**
     * Retrieve all publishers with pagination
     */
    public Page<Publisher> getAllPublishers(Pageable pageable) {
        return publisherRepository.findAll(pageable);
    }

    /**
     * Find publisher by ID
     */
    public Optional<Publisher> getPublisherById(Long id) {
        return publisherRepository.findById(id);
    }

    /**
     * Find publisher by name
     */
    public Optional<Publisher> getPublisherByName(String name) {
        return publisherRepository.findByName(name);
    }

    /**
     * Create a new publisher
     */
    public Publisher createPublisher(Publisher publisher) {
        // Validate unique name constraint
        if (publisherRepository.existsByName(publisher.getName())) {
            throw new RuntimeException("Publisher name already exists: " + publisher.getName());
        }

        // Validate publisher data
        validatePublisherData(publisher);

        return publisherRepository.save(publisher);
    }

    /**
     * Update an existing publisher
     */
    public Publisher updatePublisher(Long id, Publisher publisherDetails) {
        Publisher publisher = publisherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Publisher not found with id: " + id));

        // Validate updated data
        validatePublisherData(publisherDetails);

        // Check if name is being changed and if new name already exists
        if (!publisher.getName().equals(publisherDetails.getName()) &&
                publisherRepository.existsByName(publisherDetails.getName())) {
            throw new RuntimeException("Publisher name already exists: " + publisherDetails.getName());
        }

        // Update fields
        publisher.setName(publisherDetails.getName());
        publisher.setAddress(publisherDetails.getAddress());
        publisher.setWebsite(publisherDetails.getWebsite());
        publisher.setContactEmail(publisherDetails.getContactEmail());

        return publisherRepository.save(publisher);
    }

    /**
     * Delete a publisher
     */
    public void deletePublisher(Long id) {
        Publisher publisher = publisherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Publisher not found with id: " + id));

        // Check if publisher has books before deletion
        if (publisher.getBooks() != null && !publisher.getBooks().isEmpty()) {
            throw new RuntimeException("Cannot delete publisher who has books. Remove books first or reassign to other publishers.");
        }

        publisherRepository.delete(publisher);
    }

    /**
     * Search publishers by name (case-insensitive)
     */
    public Page<Publisher> searchPublishersByName(String name, Pageable pageable) {
        return publisherRepository.findByNameContainingIgnoreCase(name, pageable);
    }

    /**
     * Check if publisher name exists
     */
    public boolean existsByName(String name) {
        return publisherRepository.existsByName(name);
    }

    /**
     * Get all publishers as a list (for dropdowns, etc.)
     */
    public List<Publisher> getAllPublishersList() {
        return publisherRepository.findAll();
    }

    /**
     * Get publishers with the most books
     */
    public List<Publisher> getMostActivePublishers(int limit) {
        return publisherRepository.findAll().stream()
                .filter(publisher -> publisher.getBooks() != null)
                .sorted((p1, p2) -> Integer.compare(p2.getBooks().size(), p1.getBooks().size()))
                .limit(limit)
                .toList();
    }

    /**
     * Get publishers with no books
     */
    public List<Publisher> getPublishersWithoutBooks() {
        return publisherRepository.findAll().stream()
                .filter(publisher -> publisher.getBooks() == null || publisher.getBooks().isEmpty())
                .toList();
    }

    /**
     * Get total number of publishers
     */
    public long getTotalPublisherCount() {
        return publisherRepository.count();
    }

    /**
     * Get count of publishers with books
     */
    public long getActivePublisherCount() {
        return publisherRepository.findAll().stream()
                .filter(publisher -> publisher.getBooks() != null && !publisher.getBooks().isEmpty())
                .count();
    }

    /**
     * Get count of publishers without books
     */
    public long getInactivePublisherCount() {
        return getPublishersWithoutBooks().size();
    }

    /**
     * Get publisher statistics
     */
    public PublisherStats getPublisherStatistics() {
        List<Publisher> allPublishers = publisherRepository.findAll();

        long totalPublishers = allPublishers.size();
        long activePublishers = getActivePublisherCount();
        long inactivePublishers = getInactivePublisherCount();

        // Calculate average books per publisher
        double avgBooksPerPublisher = allPublishers.stream()
                .filter(publisher -> publisher.getBooks() != null)
                .mapToInt(publisher -> publisher.getBooks().size())
                .average()
                .orElse(0.0);

        // Find most prolific publisher
        String mostProlificPublisher = allPublishers.stream()
                .filter(publisher -> publisher.getBooks() != null && !publisher.getBooks().isEmpty())
                .max((p1, p2) -> Integer.compare(p1.getBooks().size(), p2.getBooks().size()))
                .map(Publisher::getName)
                .orElse("None");

        return new PublisherStats(totalPublishers, activePublishers, inactivePublishers,
                avgBooksPerPublisher, mostProlificPublisher);
    }

    /**
     * Search publishers by partial name match
     */
    public List<Publisher> findPublishersByPartialName(String partialName) {
        return publisherRepository.findAll().stream()
                .filter(publisher -> publisher.getName().toLowerCase().contains(partialName.toLowerCase()))
                .toList();
    }

    /**
     * Get publishers with contact information
     */
    public List<Publisher> getPublishersWithContactInfo() {
        return publisherRepository.findAll().stream()
                .filter(publisher ->
                        (publisher.getContactEmail() != null && !publisher.getContactEmail().trim().isEmpty()) ||
                                (publisher.getWebsite() != null && !publisher.getWebsite().trim().isEmpty()) ||
                                (publisher.getAddress() != null && !publisher.getAddress().trim().isEmpty()))
                .toList();
    }

    /**
     * Get publishers with complete information
     */
    public List<Publisher> getPublishersWithCompleteInfo() {
        return publisherRepository.findAll().stream()
                .filter(publisher ->
                        publisher.getName() != null && !publisher.getName().trim().isEmpty() &&
                                publisher.getContactEmail() != null && !publisher.getContactEmail().trim().isEmpty() &&
                                publisher.getWebsite() != null && !publisher.getWebsite().trim().isEmpty() &&
                                publisher.getAddress() != null && !publisher.getAddress().trim().isEmpty())
                .toList();
    }

    /**
     * Get publishers missing contact information
     */
    public List<Publisher> getPublishersMissingContactInfo() {
        return publisherRepository.findAll().stream()
                .filter(publisher ->
                        (publisher.getContactEmail() == null || publisher.getContactEmail().trim().isEmpty()) &&
                                (publisher.getWebsite() == null || publisher.getWebsite().trim().isEmpty()) &&
                                (publisher.getAddress() == null || publisher.getAddress().trim().isEmpty()))
                .toList();
    }

    /**
     * Update publisher contact information
     */
    public Publisher updateContactInfo(Long publisherId, String contactEmail, String website, String address) {
        Publisher publisher = publisherRepository.findById(publisherId)
                .orElseThrow(() -> new RuntimeException("Publisher not found with id: " + publisherId));

        // Validate contact information
        if (contactEmail != null && !contactEmail.trim().isEmpty() && !EMAIL_PATTERN.matcher(contactEmail).matches()) {
            throw new RuntimeException("Invalid email format");
        }

        if (website != null && !website.trim().isEmpty() && !URL_PATTERN.matcher(website).matches()) {
            throw new RuntimeException("Invalid website URL format");
        }

        publisher.setContactEmail(contactEmail);
        publisher.setWebsite(website);
        publisher.setAddress(address);

        return publisherRepository.save(publisher);
    }

    /**
     * Validate publisher data
     */
    private void validatePublisherData(Publisher publisher) {
        if (publisher.getName() == null || publisher.getName().trim().isEmpty()) {
            throw new RuntimeException("Publisher name is required");
        }

        // Validate email format if provided
        if (publisher.getContactEmail() != null && !publisher.getContactEmail().trim().isEmpty()) {
            if (!EMAIL_PATTERN.matcher(publisher.getContactEmail()).matches()) {
                throw new RuntimeException("Invalid email format");
            }
        }

        // Validate website URL format if provided
        if (publisher.getWebsite() != null && !publisher.getWebsite().trim().isEmpty()) {
            if (!URL_PATTERN.matcher(publisher.getWebsite()).matches()) {
                throw new RuntimeException("Invalid website URL format");
            }
        }

        // Ensure website starts with http:// or https:// if provided
        if (publisher.getWebsite() != null && !publisher.getWebsite().trim().isEmpty()) {
            String website = publisher.getWebsite().trim();
            if (!website.startsWith("http://") && !website.startsWith("https://")) {
                publisher.setWebsite("https://" + website);
            }
        }
    }

    /**
     * Merge duplicate publishers
     */
    public Publisher mergePublishers(Long primaryPublisherId, Long secondaryPublisherId) {
        Publisher primaryPublisher = publisherRepository.findById(primaryPublisherId)
                .orElseThrow(() -> new RuntimeException("Primary publisher not found with id: " + primaryPublisherId));

        Publisher secondaryPublisher = publisherRepository.findById(secondaryPublisherId)
                .orElseThrow(() -> new RuntimeException("Secondary publisher not found with id: " + secondaryPublisherId));

        // Move all books from secondary to primary publisher
        if (secondaryPublisher.getBooks() != null) {
            secondaryPublisher.getBooks().forEach(book -> book.setPublisher(primaryPublisher));
        }

        // Update primary publisher contact info if missing
        if (primaryPublisher.getContactEmail() == null || primaryPublisher.getContactEmail().trim().isEmpty()) {
            primaryPublisher.setContactEmail(secondaryPublisher.getContactEmail());
        }
        if (primaryPublisher.getWebsite() == null || primaryPublisher.getWebsite().trim().isEmpty()) {
            primaryPublisher.setWebsite(secondaryPublisher.getWebsite());
        }
        if (primaryPublisher.getAddress() == null || primaryPublisher.getAddress().trim().isEmpty()) {
            primaryPublisher.setAddress(secondaryPublisher.getAddress());
        }

        // Save primary publisher and delete secondary
        Publisher mergedPublisher = publisherRepository.save(primaryPublisher);
        publisherRepository.delete(secondaryPublisher);

        return mergedPublisher;
    }

    /**
     * Find potential duplicate publishers by name similarity
     */
    public List<PublisherDuplicate> findPotentialDuplicates() {
        List<Publisher> allPublishers = publisherRepository.findAll();
        List<PublisherDuplicate> duplicates = new java.util.ArrayList<>();

        for (int i = 0; i < allPublishers.size(); i++) {
            for (int j = i + 1; j < allPublishers.size(); j++) {
                Publisher p1 = allPublishers.get(i);
                Publisher p2 = allPublishers.get(j);

                double similarity = calculateNameSimilarity(p1.getName(), p2.getName());
                if (similarity > 0.8) { // 80% similarity threshold
                    duplicates.add(new PublisherDuplicate(p1, p2, similarity));
                }
            }
        }

        return duplicates;
    }

    /**
     * Calculate name similarity using Levenshtein distance
     */
    private double calculateNameSimilarity(String name1, String name2) {
        if (name1 == null || name2 == null) return 0.0;

        String n1 = name1.toLowerCase().trim();
        String n2 = name2.toLowerCase().trim();

        if (n1.equals(n2)) return 1.0;

        int maxLength = Math.max(n1.length(), n2.length());
        if (maxLength == 0) return 1.0;

        int distance = levenshteinDistance(n1, n2);
        return 1.0 - (double) distance / maxLength;
    }

    /**
     * Calculate Levenshtein distance between two strings
     */
    private int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    dp[i][j] = Math.min(
                            dp[i - 1][j - 1] + (s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1),
                            Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1)
                    );
                }
            }
        }

        return dp[s1.length()][s2.length()];
    }

    /**
     * Inner class for publisher statistics
     */
    @Data
    public static class PublisherStats {
        private final Long totalPublishers;
        private final Long activePublishers;
        private final Long inactivePublishers;
        private final Double averageBooksPerPublisher;
        private final String mostProlificPublisher;

        public PublisherStats(Long totalPublishers, Long activePublishers, Long inactivePublishers,
                              Double averageBooksPerPublisher, String mostProlificPublisher) {
            this.totalPublishers = totalPublishers;
            this.activePublishers = activePublishers;
            this.inactivePublishers = inactivePublishers;
            this.averageBooksPerPublisher = averageBooksPerPublisher;
            this.mostProlificPublisher = mostProlificPublisher;
        }
    }

    /**
     * Inner class for potential duplicate publishers
     */
    @Data
    public static class PublisherDuplicate {
        private final Publisher publisher1;
        private final Publisher publisher2;
        private final Double similarity;

        public PublisherDuplicate(Publisher publisher1, Publisher publisher2, Double similarity) {
            this.publisher1 = publisher1;
            this.publisher2 = publisher2;
            this.similarity = similarity;
        }
    }
}