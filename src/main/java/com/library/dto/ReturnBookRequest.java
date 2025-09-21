package com.library.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReturnBookRequest {
    @NotNull(message = "Returned by user ID is required")
    private Long returnedToUserId;
}