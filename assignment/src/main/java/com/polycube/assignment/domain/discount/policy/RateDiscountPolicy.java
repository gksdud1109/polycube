package com.polycube.assignment.domain.discount.policy;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Component;

import com.polycube.assignment.domain.member.entity.MemberGrade;

@Component
public class RateDiscountPolicy implements DiscountPolicy {

	private static final BigDecimal RATE = new BigDecimal("0.10");

	@Override
	public boolean supports(MemberGrade grade) {
		return grade == MemberGrade.VVIP;
	}

	@Override
	public BigDecimal calculateDiscount(BigDecimal originalPrice) {
		return originalPrice.multiply(RATE).setScale(0, RoundingMode.DOWN);
	}
}
