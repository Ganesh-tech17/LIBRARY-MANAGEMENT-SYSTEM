package com.library.management.repository;

import com.library.management.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookRepository extends JpaRepository<Book, Long> {

    boolean existsByIsbn(String isbn);

    /**
     * Flexible search across title, author name and category name.
     * Any parameter may be null, in which case it is ignored.
     */
    @Query("""
            SELECT b FROM Book b
            JOIN b.author a
            JOIN b.category c
            WHERE (:title IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%')))
              AND (:author IS NULL OR LOWER(a.name) LIKE LOWER(CONCAT('%', :author, '%')))
              AND (:category IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :category, '%')))
              AND (:isbn IS NULL OR b.isbn = :isbn)
            """)
    Page<Book> searchBooks(@Param("title") String title,
                            @Param("author") String author,
                            @Param("category") String category,
                            @Param("isbn") String isbn,
                            Pageable pageable);

    @Query("SELECT b FROM Book b WHERE b.availableCopies > 0")
    Page<Book> findAvailableBooks(Pageable pageable);
}
