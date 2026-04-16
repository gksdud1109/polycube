package com.polycube.assignment.domain.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.polycube.assignment.domain.order.entity.Order;

public record OrderResponse(
	Long orderId,
	String productName,
	BigDecimal originalPrice,
	Long memberId,
	String memberName,
	LocalDateTime createdAt
) {
	public static OrderResponse from(Order order) {
		return new OrderResponse(
			order.getId(),
			order.getProductName(),
			order.getOriginalPrice(),
			order.getMember().getId(),
			order.getMember().getEmail(),
			order.getCreatedAt()
		);
	}
}
