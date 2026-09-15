package com.library.management.mapper;

import com.library.management.dto.response.BookResponse;
import com.library.management.entity.Book;
import org.springframework.stereotype.Component;

@Component
public class BookMapper {

    private final AuthorMapper authorMapper;
    private final CategoryMapper categoryMapper;

    public BookMapper(AuthorMapper authorMapper, CategoryMapper categoryMapper) {
        this.authorMapper = authorMapper;
        this.categoryMapper = categoryMapper;
    }

    public BookResponse toResponse(Book book) {
        return BookResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .isbn(book.getIsbn())
                .publishedYear(book.getPublishedYear())
                .totalCopies(book.getTotalCopies())
                .availableCopies(book.getAvailableCopies())
                .author(book.getAuthor() != null ? authorMapper.toResponse(book.getAuthor()) : null)
                .category(book.getCategory() != null ? categoryMapper.toResponse(book.getCategory()) : null)
                .build();
    }
}
