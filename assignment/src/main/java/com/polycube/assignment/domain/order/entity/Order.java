package com.polycube.assignment.domain.order.entity;

import java.math.BigDecimal;

import com.polycube.assignment.domain.member.entity.Member;
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

@Entity
@Getter
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {

	@Column(nullable = false)
	private String productName;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal originalPrice;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private OrderStatus status;

	@Builder
	public Order(String productName, BigDecimal originalPrice, Member member) {
		this.productName = productName;
		this.originalPrice = originalPrice;
		this.member = member;
		this.status = OrderStatus.READY;
	}

}
