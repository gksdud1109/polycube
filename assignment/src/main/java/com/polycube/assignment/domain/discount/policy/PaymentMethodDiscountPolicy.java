package com.polycube.assignment.domain.discount.policy;

import java.math.BigDecimal;

import com.polycube.assignment.domain.payment.entity.PaymentMethod;

public interface PaymentMethodDiscountPolicy {

	boolean supports(PaymentMethod method);

	/**
	 * @param baseAmount 등급 할인 적용 후 금액 (추가 할인의 기준)
	 */
	BigDecimal calculateDiscount(BigDecimal baseAmount);

	String policyName();

	BigDecimal discountRate();
}
