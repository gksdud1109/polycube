package com.polycube.assignment.domain.discount.policy;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.polycube.assignment.domain.member.entity.MemberGrade;

@Component
public class FixedAmountDiscountPolicy implements DiscountPolicy {

	private static final BigDecimal DISCOUNT_AMOUNT = new BigDecimal("1000");

	@Override
	public boolean supports(MemberGrade grade) {
		return grade == MemberGrade.VIP;
	}

	@Override
	public BigDecimal calculateDiscount(BigDecimal originalPrice) {
		return originalPrice.compareTo(DISCOUNT_AMOUNT) < 0
			? originalPrice
			: DISCOUNT_AMOUNT;
	}

	@Override
	public String policyName() {
		return "VIP 고정 금액 할인";
	}

	@Override
	public BigDecimal discountRate() {
		return BigDecimal.ZERO;
	}
}
