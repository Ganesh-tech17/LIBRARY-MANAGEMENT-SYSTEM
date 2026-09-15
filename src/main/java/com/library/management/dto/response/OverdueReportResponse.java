package com.library.management.dto.response;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OverdueReportResponse {
    private Long borrowRecordId;
    private String bookTitle;
    private String memberName;
    private String memberEmail;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private long daysOverdue;
    private double estimatedFine;
}
