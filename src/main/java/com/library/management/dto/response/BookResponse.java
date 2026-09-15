package com.library.management.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookResponse {
    private Long id;
    private String title;
    private String isbn;
    private Integer publishedYear;
    private Integer totalCopies;
    private Integer availableCopies;
    private AuthorResponse author;
    private CategoryResponse category;
}
