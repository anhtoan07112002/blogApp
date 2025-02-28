package com.blogApp.blogcommon.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class PageRequest {
    @Min(value = 0, message = "Page number cannot be less than 0")
    private int page = 0;

    @Min(value = 1, message = "Page size cannot be less than 1")
    @Max(value = 100, message = "Page size cannot be greater than 100")
    private int size = 10;

    private String sortBy = "id";
    private String sortDir = "desc";
}
