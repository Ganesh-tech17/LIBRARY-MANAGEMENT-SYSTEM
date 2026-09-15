package com.library.management.mapper;

import com.library.management.dto.response.BorrowRecordResponse;
import com.library.management.dto.response.FineResponse;
import com.library.management.entity.BorrowRecord;
import com.library.management.entity.Fine;
import org.springframework.stereotype.Component;

@Component
public class BorrowRecordMapper {

    private final BookMapper bookMapper;
    private final MemberMapper memberMapper;

    public BorrowRecordMapper(BookMapper bookMapper, MemberMapper memberMapper) {
        this.bookMapper = bookMapper;
        this.memberMapper = memberMapper;
    }

    public BorrowRecordResponse toResponse(BorrowRecord record) {
        return BorrowRecordResponse.builder()
                .id(record.getId())
                .book(record.getBook() != null ? bookMapper.toResponse(record.getBook()) : null)
                .member(record.getMember() != null ? memberMapper.toResponse(record.getMember()) : null)
                .borrowDate(record.getBorrowDate())
                .dueDate(record.getDueDate())
                .returnDate(record.getReturnDate())
                .status(record.getStatus())
                .fine(record.getFine() != null ? toFineResponse(record.getFine()) : null)
                .build();
    }

    public FineResponse toFineResponse(Fine fine) {
        return FineResponse.builder()
                .id(fine.getId())
                .borrowRecordId(fine.getBorrowRecord().getId())
                .amount(fine.getAmount())
                .daysLate(fine.getDaysLate())
                .paid(fine.getPaid())
                .calculatedDate(fine.getCalculatedDate())
                .build();
    }
}
