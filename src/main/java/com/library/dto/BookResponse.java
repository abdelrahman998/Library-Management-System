package com.library.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookResponse {

    private Long id;
    private String title;
    private String isbn;
    private String edition;
    private Integer publicationYear;
    private String language;
    private String summary;
    private String coverImageUrl;
    private Integer totalCopies;
    private Integer availableCopies;
    private Set<AuthorResponse> authors;
    private PublisherResponse publisher;
    private Set<CategoryResponse> categories;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
