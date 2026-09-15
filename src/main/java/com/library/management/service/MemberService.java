package com.library.management.service;

import com.library.management.dto.request.MemberRequest;
import com.library.management.dto.response.MemberResponse;
import com.library.management.entity.Member;
import com.library.management.exception.BusinessException;
import com.library.management.exception.ResourceNotFoundException;
import com.library.management.mapper.MemberMapper;
import com.library.management.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberMapper memberMapper;

    public MemberResponse create(MemberRequest request) {
        if (memberRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new BusinessException("A member with email '" + request.getEmail() + "' already exists");
        }
        Member member = memberMapper.toEntity(request);
        return memberMapper.toResponse(memberRepository.save(member));
    }

    @Transactional(readOnly = true)
    public Page<MemberResponse> findAll(Pageable pageable) {
        return memberRepository.findAll(pageable).map(memberMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public MemberResponse findById(Long id) {
        return memberMapper.toResponse(getMemberOrThrow(id));
    }

    public MemberResponse update(Long id, MemberRequest request) {
        Member member = getMemberOrThrow(id);
        if (!member.getEmail().equalsIgnoreCase(request.getEmail())
                && memberRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new BusinessException("A member with email '" + request.getEmail() + "' already exists");
        }
        memberMapper.updateEntity(member, request);
        return memberMapper.toResponse(memberRepository.save(member));
    }

    public MemberResponse setActive(Long id, boolean active) {
        Member member = getMemberOrThrow(id);
        member.setActive(active);
        return memberMapper.toResponse(memberRepository.save(member));
    }

    public void delete(Long id) {
        Member member = getMemberOrThrow(id);
        memberRepository.delete(member);
    }

    @Transactional(readOnly = true)
    public Member getMemberOrThrow(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Member", id));
    }
}
