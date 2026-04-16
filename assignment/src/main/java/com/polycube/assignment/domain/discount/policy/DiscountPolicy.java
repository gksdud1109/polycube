package com.polycube.assignment.domain.discount.policy;

import java.math.BigDecimal;

import com.polycube.assignment.domain.member.entity.MemberGrade;

public interface DiscountPolicy {

	boolean supports(MemberGrade grade);

	BigDecimal calculateDiscount(BigDecimal originalPrice);
}
