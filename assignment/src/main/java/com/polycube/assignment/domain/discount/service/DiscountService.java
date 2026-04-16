package com.polycube.assignment.domain.discount.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.polycube.assignment.domain.discount.policy.DiscountPolicy;
import com.polycube.assignment.domain.member.entity.MemberGrade;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DiscountService {

	private final List<DiscountPolicy> policies;

	public BigDecimal calculateDiscount(MemberGrade grade, BigDecimal originalPrice) {
		return policies.stream()
			.filter(policy -> policy.supports(grade))
			.map(policy -> policy.calculateDiscount(originalPrice))
			.findFirst()
			.orElse(BigDecimal.ZERO);
	}
}
