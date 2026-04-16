package com.polycube.assignment.domain.discount.policy;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Component;

import com.polycube.assignment.domain.payment.entity.PaymentMethod;

@Component
public class PointPaymentDiscountPolicy implements PaymentMethodDiscountPolicy {

	private static final BigDecimal RATE = new BigDecimal("0.05");

	@Override
	public boolean supports(PaymentMethod method) {
		return method == PaymentMethod.POINT;
	}

	@Override
	public BigDecimal calculateDiscount(BigDecimal baseAmount) {
		return baseAmount.multiply(RATE).setScale(0, RoundingMode.DOWN);
	}

	@Override
	public String policyName() {
		return "포인트 결제 추가 할인";
	}

	@Override
	public BigDecimal discountRate() {
		return RATE;
	}
}
