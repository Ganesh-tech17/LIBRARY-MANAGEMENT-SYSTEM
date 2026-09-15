package com.library.management.service;

import com.library.management.dto.request.AuthorRequest;
import com.library.management.dto.response.AuthorResponse;
import com.library.management.entity.Author;
import com.library.management.exception.ResourceNotFoundException;
import com.library.management.mapper.AuthorMapper;
import com.library.management.repository.AuthorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthorService {

    private final AuthorRepository authorRepository;
    private final AuthorMapper authorMapper;

    public AuthorResponse create(AuthorRequest request) {
        Author author = authorMapper.toEntity(request);
        return authorMapper.toResponse(authorRepository.save(author));
    }

    @Transactional(readOnly = true)
    public List<AuthorResponse> findAll() {
        return authorRepository.findAll().stream()
                .map(authorMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AuthorResponse findById(Long id) {
        return authorMapper.toResponse(getAuthorOrThrow(id));
    }

    public AuthorResponse update(Long id, AuthorRequest request) {
        Author author = getAuthorOrThrow(id);
        authorMapper.updateEntity(author, request);
        return authorMapper.toResponse(authorRepository.save(author));
    }

    public void delete(Long id) {
        Author author = getAuthorOrThrow(id);
        authorRepository.delete(author);
    }

    @Transactional(readOnly = true)
    public Author getAuthorOrThrow(Long id) {
        return authorRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Author", id));
    }
}
