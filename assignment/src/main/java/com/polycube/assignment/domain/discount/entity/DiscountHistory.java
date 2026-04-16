package com.polycube.assignment.domain.discount.entity;

import java.math.BigDecimal;

import com.polycube.assignment.domain.discount.dto.DiscountResult;
import com.polycube.assignment.domain.payment.entity.Payment;
import com.polycube.assignment.global.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 결제 시 적용된 할인 정책의 스냅샷.
 * 정책명·등급·할인율·할인금액을 값으로 저장하므로
 * 이후 정책이 수정·삭제되어도 결제 당시 이력이 보존된다.
 */
@Entity
@Getter
@Table(name = "discount_histories")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DiscountHistory extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "payment_id", nullable = false)
	private Payment payment;

	@Column(nullable = false)
	private String appliedGrade;    // 결제 시점 회원 등급 스냅샷

	@Column(nullable = false)
	private String policyName;      // 적용 정책명 스냅샷

	@Column(nullable = false, precision = 5, scale = 4)
	private BigDecimal discountRate;    // 할인율 스냅샷 (고정금액 정책은 0)

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal discountAmount;  // 실제 적용된 할인 금액

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private DiscountType discountType;

	@Builder
	private DiscountHistory(Payment payment, String appliedGrade, String policyName,
		BigDecimal discountRate, BigDecimal discountAmount, DiscountType discountType) {
		this.payment = payment;
		this.appliedGrade = appliedGrade;
		this.policyName = policyName;
		this.discountRate = discountRate;
		this.discountAmount = discountAmount;
		this.discountType = discountType;
	}

	public static DiscountHistory ofGrade(Payment payment, String grade, DiscountResult result) {
		return DiscountHistory.builder()
			.payment(payment)
			.appliedGrade(grade)
			.policyName(result.policyName())
			.discountRate(result.discountRate())
			.discountAmount(result.discountAmount())
			.discountType(DiscountType.GRADE)
			.build();
	}

	public static DiscountHistory ofPaymentMethod(Payment payment, String grade, DiscountResult result) {
		return DiscountHistory.builder()
			.payment(payment)
			.appliedGrade(grade)
			.policyName(result.policyName())
			.discountRate(result.discountRate())
			.discountAmount(result.discountAmount())
			.discountType(DiscountType.PAYMENT_METHOD)
			.build();
	}
}
