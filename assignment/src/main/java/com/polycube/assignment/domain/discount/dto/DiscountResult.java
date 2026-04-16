package com.polycube.assignment.domain.discount.dto;

import java.math.BigDecimal;

/**
 * 할인 정책 1건의 적용 결과.
 * 이력 저장을 위해 정책명·할인율을 스냅샷으로 함께 담는다.
 * 정책이 코드에서 수정·삭제되더라도 이 객체가 DB에 기록되므로 이력이 보존된다.
 */
public record DiscountResult(
	String policyName,
	BigDecimal discountRate,
	BigDecimal discountAmount
) {
}
