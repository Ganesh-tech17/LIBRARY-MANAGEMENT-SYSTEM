package com.library.management.repository;

import com.library.management.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByEmailIgnoreCase(String email);
    Optional<Member> findByEmailIgnoreCase(String email);
}
