package com.polycube.assignment.domain.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.polycube.assignment.domain.member.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
