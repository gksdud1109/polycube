package com.polycube.assignment.domain.payment.dto;

import com.polycube.assignment.domain.payment.entity.PaymentMethod;

import jakarta.validation.constraints.NotNull;

public record PaymentRequest(
	@NotNull(message = "주문 ID는 필수입니다.")
	Long orderId,

	@NotNull(message = "결제 수단은 필수입니다.")
	PaymentMethod method
) {
}
