package com.polycube.assignment.domain.discount.policy;

import java.math.BigDecimal;

import com.polycube.assignment.domain.member.entity.MemberGrade;

public interface DiscountPolicy {

	default int priority() {
		return 0;
	}

	boolean supports(MemberGrade grade);

	BigDecimal calculateDiscount(BigDecimal originalPrice);

	String policyName();

	BigDecimal discountRate();
}
