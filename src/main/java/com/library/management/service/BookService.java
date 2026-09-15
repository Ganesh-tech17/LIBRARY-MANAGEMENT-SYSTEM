package com.library.management.service;

import com.library.management.dto.request.BookRequest;
import com.library.management.dto.response.BookResponse;
import com.library.management.entity.Author;
import com.library.management.entity.Book;
import com.library.management.entity.Category;
import com.library.management.exception.BusinessException;
import com.library.management.exception.ResourceNotFoundException;
import com.library.management.mapper.BookMapper;
import com.library.management.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class BookService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;
    private final AuthorService authorService;
    private final CategoryService categoryService;

    public BookResponse create(BookRequest request) {
        if (bookRepository.existsByIsbn(request.getIsbn())) {
            throw new BusinessException("A book with ISBN '" + request.getIsbn() + "' already exists");
        }
        Author author = authorService.getAuthorOrThrow(request.getAuthorId());
        Category category = categoryService.getCategoryOrThrow(request.getCategoryId());

        Book book = Book.builder()
                .title(request.getTitle())
                .isbn(request.getIsbn())
                .publishedYear(request.getPublishedYear())
                .totalCopies(request.getTotalCopies())
                .availableCopies(request.getTotalCopies())
                .author(author)
                .category(category)
                .build();

        return bookMapper.toResponse(bookRepository.save(book));
    }

    @Transactional(readOnly = true)
    public Page<BookResponse> search(String title, String author, String category, String isbn, Pageable pageable) {
        return bookRepository
                .searchBooks(blankToNull(title), blankToNull(author), blankToNull(category), blankToNull(isbn), pageable)
                .map(bookMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<BookResponse> findAll(Pageable pageable) {
        return bookRepository.findAll(pageable).map(bookMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public BookResponse findById(Long id) {
        return bookMapper.toResponse(getBookOrThrow(id));
    }

    public BookResponse update(Long id, BookRequest request) {
        Book book = getBookOrThrow(id);

        if (!book.getIsbn().equals(request.getIsbn()) && bookRepository.existsByIsbn(request.getIsbn())) {
            throw new BusinessException("A book with ISBN '" + request.getIsbn() + "' already exists");
        }

        int borrowedCopies = book.getTotalCopies() - book.getAvailableCopies();
        if (request.getTotalCopies() < borrowedCopies) {
            throw new BusinessException("Total copies cannot be less than copies currently borrowed (" + borrowedCopies + ")");
        }

        Author author = authorService.getAuthorOrThrow(request.getAuthorId());
        Category category = categoryService.getCategoryOrThrow(request.getCategoryId());

        book.setTitle(request.getTitle());
        book.setIsbn(request.getIsbn());
        book.setPublishedYear(request.getPublishedYear());
        book.setAvailableCopies(request.getTotalCopies() - borrowedCopies);
        book.setTotalCopies(request.getTotalCopies());
        book.setAuthor(author);
        book.setCategory(category);

        return bookMapper.toResponse(bookRepository.save(book));
    }

    public void delete(Long id) {
        Book book = getBookOrThrow(id);
        if (!book.getAvailableCopies().equals(book.getTotalCopies())) {
            throw new BusinessException("Cannot delete a book that currently has copies on loan");
        }
        bookRepository.delete(book);
    }

    @Transactional(readOnly = true)
    public Book getBookOrThrow(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Book", id));
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
