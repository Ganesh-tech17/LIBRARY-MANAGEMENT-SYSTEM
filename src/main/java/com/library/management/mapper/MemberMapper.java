package com.library.management.mapper;

import com.library.management.dto.request.MemberRequest;
import com.library.management.dto.response.MemberResponse;
import com.library.management.entity.Member;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class MemberMapper {

    public Member toEntity(MemberRequest request) {
        return Member.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .membershipDate(LocalDate.now())
                .active(true)
                .build();
    }

    public void updateEntity(Member member, MemberRequest request) {
        member.setName(request.getName());
        member.setEmail(request.getEmail());
        member.setPhone(request.getPhone());
    }

    public MemberResponse toResponse(Member member) {
        return MemberResponse.builder()
                .id(member.getId())
                .name(member.getName())
                .email(member.getEmail())
                .phone(member.getPhone())
                .membershipDate(member.getMembershipDate())
                .active(member.getActive())
                .build();
    }
}
