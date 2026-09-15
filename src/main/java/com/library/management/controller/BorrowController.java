package com.library.management.controller;

import com.library.management.dto.request.IssueBookRequest;
import com.library.management.dto.response.BorrowRecordResponse;
import com.library.management.dto.response.OverdueReportResponse;
import com.library.management.service.BorrowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/borrow")
@RequiredArgsConstructor
@Tag(name = "Borrowing", description = "Issue/return books, fines and overdue reporting")
public class BorrowController {

    private final BorrowService borrowService;

    @PostMapping("/issue")
    @Operation(summary = "Issue (checkout) a book to a member")
    public ResponseEntity<BorrowRecordResponse> issueBook(@Valid @RequestBody IssueBookRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(borrowService.issueBook(request));
    }

    @PostMapping("/return/{borrowRecordId}")
    @Operation(summary = "Return a borrowed book; automatically calculates and stores any late fine")
    public ResponseEntity<BorrowRecordResponse> returnBook(@PathVariable Long borrowRecordId) {
        return ResponseEntity.ok(borrowService.returnBook(borrowRecordId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single borrow record by id")
    public ResponseEntity<BorrowRecordResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(borrowService.findById(id));
    }

    @GetMapping("/{id}/fine")
    @Operation(summary = "Calculate (or retrieve) the fine for a borrow record")
    public ResponseEntity<Map<String, Object>> calculateFine(@PathVariable Long id) {
        BigDecimal fine = borrowService.calculateFine(id);
        return ResponseEntity.ok(Map.of("borrowRecordId", id, "fineAmount", fine));
    }

    @GetMapping("/overdue")
    @Operation(summary = "Report of all currently overdue borrow records")
    public ResponseEntity<List<OverdueReportResponse>> overdueReport() {
        return ResponseEntity.ok(borrowService.getOverdueReport());
    }

    @GetMapping("/member/{memberId}")
    @Operation(summary = "Borrowing history for a specific member (paginated)")
    public ResponseEntity<Page<BorrowRecordResponse>> memberHistory(
            @PathVariable Long memberId,
            @PageableDefault(size = 20, sort = "borrowDate") Pageable pageable) {
        return ResponseEntity.ok(borrowService.getMemberHistory(memberId, pageable));
    }
}
