package com.library.dto;

import lombok.Data;

@Data
public class AuthorResponse {
    private Long id;
    private String name;
    private String biography;
}
