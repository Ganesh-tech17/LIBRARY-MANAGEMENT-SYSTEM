package com.library.management.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FineResponse {
    private Long id;
    private Long borrowRecordId;
    private BigDecimal amount;
    private Integer daysLate;
    private Boolean paid;
    private LocalDate calculatedDate;
}
