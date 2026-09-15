package com.library.management.mapper;

import com.library.management.dto.request.AuthorRequest;
import com.library.management.dto.response.AuthorResponse;
import com.library.management.entity.Author;
import org.springframework.stereotype.Component;

@Component
public class AuthorMapper {

    public Author toEntity(AuthorRequest request) {
        return Author.builder()
                .name(request.getName())
                .nationality(request.getNationality())
                .biography(request.getBiography())
                .build();
    }

    public void updateEntity(Author author, AuthorRequest request) {
        author.setName(request.getName());
        author.setNationality(request.getNationality());
        author.setBiography(request.getBiography());
    }

    public AuthorResponse toResponse(Author author) {
        return AuthorResponse.builder()
                .id(author.getId())
                .name(author.getName())
                .nationality(author.getNationality())
                .biography(author.getBiography())
                .build();
    }
}
