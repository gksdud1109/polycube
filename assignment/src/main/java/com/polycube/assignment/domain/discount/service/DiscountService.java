package com.polycube.assignment.domain.discount.service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.polycube.assignment.domain.discount.policy.DiscountPolicy;
import com.polycube.assignment.domain.member.entity.MemberGrade;

@Service
public class DiscountService {

	private final List<DiscountPolicy> policies;

	public DiscountService(List<DiscountPolicy> policies) {
		this.policies = policies.stream()
			.sorted(Comparator
				.comparingInt(DiscountPolicy::priority)
				.thenComparing(policy -> policy.getClass().getName()))
			.toList();
	}

	public BigDecimal calculateDiscount(MemberGrade grade, BigDecimal originalPrice) {
		BigDecimal totalDiscount = policies.stream()
			.filter(policy -> policy.supports(grade))
			.map(policy -> policy.calculateDiscount(originalPrice))
			.reduce(BigDecimal.ZERO, BigDecimal::add);

		return totalDiscount.min(originalPrice);
	}
}
