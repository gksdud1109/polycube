package com.polycube.assignment.domain.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.polycube.assignment.domain.payment.entity.Payment;
import com.polycube.assignment.domain.payment.entity.PaymentMethod;

public record PaymentResponse(
	Long paymentId,
	Long orderId,
	String productName,
	BigDecimal originalPrice,
	BigDecimal discountAmount,
	BigDecimal finalAmount,
	PaymentMethod method,
	LocalDateTime paidAt
) {
	public static PaymentResponse from(Payment payment) {
		return new PaymentResponse(
			payment.getId(),
			payment.getOrder().getId(),
			payment.getOrder().getProductName(),
			payment.getOrder().getOriginalPrice(),
			payment.getDiscountAmount(),
			payment.getFinalAmount(),
			payment.getMethod(),
			payment.getPaidAt()
		);
	}
}
