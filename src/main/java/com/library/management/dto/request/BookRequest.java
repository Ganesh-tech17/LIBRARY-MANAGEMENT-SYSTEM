package com.library.management.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @NotBlank(message = "ISBN is required")
    @Pattern(regexp = "^[0-9Xx-]{10,20}$", message = "ISBN must be a valid 10-20 character ISBN")
    private String isbn;

    @Min(value = 1450, message = "Published year must be a realistic year")
    private Integer publishedYear;

    @NotNull(message = "Total copies is required")
    @Min(value = 1, message = "Total copies must be at least 1")
    private Integer totalCopies;

    @NotNull(message = "Author id is required")
    private Long authorId;

    @NotNull(message = "Category id is required")
    private Long categoryId;
}
