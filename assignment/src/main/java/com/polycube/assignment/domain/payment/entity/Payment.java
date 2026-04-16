package com.polycube.assignment.domain.payment.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.polycube.assignment.domain.order.entity.Order;
import com.polycube.assignment.global.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "payments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseEntity {

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id", nullable = false, unique = true)
	private Order order;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal discountAmount;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal finalAmount;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PaymentMethod method;

	@Column(nullable = false)
	private LocalDateTime paidAt;

	private Payment(Order order, BigDecimal discountAmount, BigDecimal finalAmount,
		PaymentMethod method, LocalDateTime paidAt) {
		this.order = order;
		this.discountAmount = discountAmount;
		this.finalAmount = finalAmount;
		this.method = method;
		this.paidAt = paidAt;
	}

	public static Payment create(Order order, BigDecimal discountAmount, BigDecimal finalAmount,
		PaymentMethod method) {
		return new Payment(order, discountAmount, finalAmount, method, LocalDateTime.now());
	}
}
