package com.library.management.dto.response;

import com.library.management.entity.BorrowStatus;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BorrowRecordResponse {
    private Long id;
    private BookResponse book;
    private MemberResponse member;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private BorrowStatus status;
    private FineResponse fine;
}
