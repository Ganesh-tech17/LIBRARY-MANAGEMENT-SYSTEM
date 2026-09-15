package com.library.management.service;

import com.library.management.dto.request.IssueBookRequest;
import com.library.management.dto.response.BorrowRecordResponse;
import com.library.management.dto.response.OverdueReportResponse;
import com.library.management.entity.*;
import com.library.management.exception.BusinessException;
import com.library.management.exception.ResourceNotFoundException;
import com.library.management.mapper.BorrowRecordMapper;
import com.library.management.repository.BorrowRecordRepository;
import com.library.management.repository.FineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BorrowService {

    private final BorrowRecordRepository borrowRecordRepository;
    private final FineRepository fineRepository;
    private final BorrowRecordMapper borrowRecordMapper;
    private final BookService bookService;
    private final MemberService memberService;

    @Value("${library.borrow.loan-period-days:14}")
    private int loanPeriodDays;

    @Value("${library.borrow.fine-per-day:5.00}")
    private BigDecimal finePerDay;

    /**
     * Issue (checkout) a book to a member.
     */
    public BorrowRecordResponse issueBook(IssueBookRequest request) {
        Book book = bookService.getBookOrThrow(request.getBookId());
        Member member = memberService.getMemberOrThrow(request.getMemberId());

        if (!Boolean.TRUE.equals(member.getActive())) {
            throw new BusinessException("Member '" + member.getName() + "' is not active and cannot borrow books");
        }
        if (book.getAvailableCopies() == null || book.getAvailableCopies() <= 0) {
            throw new BusinessException("No available copies of '" + book.getTitle() + "' to issue");
        }

        LocalDate today = LocalDate.now();
        BorrowRecord record = BorrowRecord.builder()
                .book(book)
                .member(member)
                .borrowDate(today)
                .dueDate(today.plusDays(loanPeriodDays))
                .status(BorrowStatus.BORROWED)
                .build();

        book.setAvailableCopies(book.getAvailableCopies() - 1);

        BorrowRecord saved = borrowRecordRepository.save(record);
        return borrowRecordMapper.toResponse(saved);
    }

    /**
     * Return a borrowed book. Calculates and persists a fine if returned late.
     */
    public BorrowRecordResponse returnBook(Long borrowRecordId) {
        BorrowRecord record = getBorrowRecordOrThrow(borrowRecordId);

        if (record.getStatus() == BorrowStatus.RETURNED) {
            throw new BusinessException("This book has already been returned");
        }

        LocalDate today = LocalDate.now();
        record.setReturnDate(today);
        record.setStatus(BorrowStatus.RETURNED);

        Book book = record.getBook();
        book.setAvailableCopies(book.getAvailableCopies() + 1);

        long daysLate = ChronoUnit.DAYS.between(record.getDueDate(), today);
        if (daysLate > 0) {
            BigDecimal amount = finePerDay.multiply(BigDecimal.valueOf(daysLate))
                    .setScale(2, RoundingMode.HALF_UP);

            Fine fine = Fine.builder()
                    .borrowRecord(record)
                    .amount(amount)
                    .daysLate((int) daysLate)
                    .paid(false)
                    .calculatedDate(today)
                    .build();
            record.setFine(fine);
            fineRepository.save(fine);
        }

        BorrowRecord saved = borrowRecordRepository.save(record);
        return borrowRecordMapper.toResponse(saved);
    }

    /**
     * Calculate the (potentially projected) fine for a borrow record without persisting.
     * If the book is still out, projects the fine as of today; if already returned, uses
     * the stored fine.
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateFine(Long borrowRecordId) {
        BorrowRecord record = getBorrowRecordOrThrow(borrowRecordId);

        if (record.getFine() != null) {
            return record.getFine().getAmount();
        }

        LocalDate referenceDate = record.getReturnDate() != null ? record.getReturnDate() : LocalDate.now();
        long daysLate = ChronoUnit.DAYS.between(record.getDueDate(), referenceDate);
        if (daysLate <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return finePerDay.multiply(BigDecimal.valueOf(daysLate)).setScale(2, RoundingMode.HALF_UP);
    }

    @Transactional(readOnly = true)
    public List<OverdueReportResponse> getOverdueReport() {
        LocalDate today = LocalDate.now();
        return borrowRecordRepository.findOverdueRecords(today).stream()
                .map(record -> {
                    long daysOverdue = ChronoUnit.DAYS.between(record.getDueDate(), today);
                    double estimatedFine = finePerDay.multiply(BigDecimal.valueOf(daysOverdue))
                            .setScale(2, RoundingMode.HALF_UP).doubleValue();
                    return OverdueReportResponse.builder()
                            .borrowRecordId(record.getId())
                            .bookTitle(record.getBook().getTitle())
                            .memberName(record.getMember().getName())
                            .memberEmail(record.getMember().getEmail())
                            .borrowDate(record.getBorrowDate())
                            .dueDate(record.getDueDate())
                            .daysOverdue(daysOverdue)
                            .estimatedFine(estimatedFine)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<BorrowRecordResponse> getMemberHistory(Long memberId, Pageable pageable) {
        memberService.getMemberOrThrow(memberId);
        return borrowRecordRepository.findByMemberId(memberId, pageable).map(borrowRecordMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public BorrowRecordResponse findById(Long id) {
        return borrowRecordMapper.toResponse(getBorrowRecordOrThrow(id));
    }

    private BorrowRecord getBorrowRecordOrThrow(Long id) {
        return borrowRecordRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("BorrowRecord", id));
    }
}
