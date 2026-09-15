package com.library.management.repository;

import com.library.management.entity.BorrowRecord;
import com.library.management.entity.BorrowStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long> {

    List<BorrowRecord> findByMemberId(Long memberId);

    Page<BorrowRecord> findByMemberId(Long memberId, Pageable pageable);

    Optional<BorrowRecord> findByBookIdAndMemberIdAndStatus(Long bookId, Long memberId, BorrowStatus status);

    long countByBookIdAndStatus(Long bookId, BorrowStatus status);

    @Query("""
            SELECT br FROM BorrowRecord br
            WHERE br.status = com.library.management.entity.BorrowStatus.BORROWED
              AND br.dueDate < :today
            """)
    List<BorrowRecord> findOverdueRecords(@Param("today") LocalDate today);

    @Query("""
            SELECT br FROM BorrowRecord br
            WHERE br.status = com.library.management.entity.BorrowStatus.BORROWED
              AND br.dueDate < :today
            """)
    Page<BorrowRecord> findOverdueRecords(@Param("today") LocalDate today, Pageable pageable);
}
