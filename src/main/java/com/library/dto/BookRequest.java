package com.library.dto;

import lombok.Data;

import java.util.Set;

@Data
public class BookRequest {

    private String title;
    private String isbn;
    private String edition;
    private Integer publicationYear;
    private String language;
    private String summary;
    private String coverImageUrl;
    private Integer totalCopies;
    private Integer availableCopies;
    private Set<Long> authorIds;
    private Long publisherId;
    private Set<Long> categoryIds;
}
