package com.polycube.assignment.domain.order.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateOrderRequest(
	@NotBlank(message = "상품명은 필수입니다.")
	String productName,

	@NotNull(message = "주문 금액은 필수입니다.")
	@Positive(message = "주문 금액은 0보다 커야 합니다.")
	BigDecimal originalPrice,

	@NotNull(message = "회원 ID는 필수입니다.")
	Long memberId
) {
}
