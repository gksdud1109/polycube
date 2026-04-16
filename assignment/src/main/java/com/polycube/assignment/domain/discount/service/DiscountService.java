package com.polycube.assignment.domain.discount.service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.polycube.assignment.domain.discount.dto.DiscountResult;
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

	/**
	 * 이력 저장용: 적용된 정책별 DiscountResult(정책명·할인율·할인금액 스냅샷) 목록 반환.
	 * 각 정책의 할인 금액은 합산 전 개별 값이며, 총액 상한은 호출부에서 처리한다.
	 */
	public List<DiscountResult> calculateDiscountDetails(MemberGrade grade, BigDecimal originalPrice) {
		return policies.stream()
			.filter(policy -> policy.supports(grade))
			.map(policy -> new DiscountResult(
				policy.policyName(),
				policy.discountRate(),
				policy.calculateDiscount(originalPrice)
			))
			.toList();
	}

	public BigDecimal calculateDiscount(MemberGrade grade, BigDecimal originalPrice) {
		BigDecimal total = calculateDiscountDetails(grade, originalPrice).stream()
			.map(DiscountResult::discountAmount)
			.reduce(BigDecimal.ZERO, BigDecimal::add);
		return total.min(originalPrice);
	}
}
