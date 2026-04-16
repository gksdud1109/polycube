package com.polycube.assignment.domain.discount.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.polycube.assignment.domain.discount.dto.DiscountResult;
import com.polycube.assignment.domain.discount.policy.PaymentMethodDiscountPolicy;
import com.polycube.assignment.domain.payment.entity.PaymentMethod;

@Service
public class PaymentMethodDiscountService {

	private final List<PaymentMethodDiscountPolicy> policies;

	public PaymentMethodDiscountService(List<PaymentMethodDiscountPolicy> policies) {
		this.policies = policies;
	}

	/**
	 * 결제 수단 추가 할인 내역 반환.
	 * baseAmount는 등급 할인 적용 후 금액이며, 할인은 이 금액을 기준으로 계산된다.
	 */
	public List<DiscountResult> calculateDiscountDetails(PaymentMethod method, BigDecimal baseAmount) {
		return policies.stream()
			.filter(policy -> policy.supports(method))
			.map(policy -> new DiscountResult(
				policy.policyName(),
				policy.discountRate(),
				policy.calculateDiscount(baseAmount)
			))
			.toList();
	}
}
