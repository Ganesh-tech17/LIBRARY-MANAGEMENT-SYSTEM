package com.library.management.dto.response;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private LocalDate membershipDate;
    private Boolean active;
}
