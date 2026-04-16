package com.polycube.assignment.domain.payment.service;

import java.math.BigDecimal;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.polycube.assignment.domain.discount.service.DiscountService;
import com.polycube.assignment.domain.order.entity.Order;
import com.polycube.assignment.domain.order.repository.OrderRepository;
import com.polycube.assignment.domain.payment.dto.PaymentRequest;
import com.polycube.assignment.domain.payment.dto.PaymentResponse;
import com.polycube.assignment.domain.payment.entity.Payment;
import com.polycube.assignment.domain.payment.exception.PaymentErrorCode;
import com.polycube.assignment.domain.payment.repository.PaymentRepository;
import com.polycube.assignment.global.error.BusinessException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

	private final PaymentRepository paymentRepository;
	private final OrderRepository orderRepository;
	private final DiscountService discountService;

	@Transactional
	public PaymentResponse pay(PaymentRequest request) {
		Order order = orderRepository.findById(request.orderId())
			.orElseThrow(() -> new BusinessException(PaymentErrorCode.ORDER_NOT_FOUND));

		BigDecimal originalPrice = order.getOriginalPrice();

		// 할인가 계산
		BigDecimal discountAmount = discountService.calculateDiscount(
			order.getMember().getGrade(), originalPrice);

		// 최종 금액 = 원가 - 할인가
		BigDecimal finalAmount = originalPrice.subtract(discountAmount);

		// 주문 상태 업데이트 & 멱등체크
		int updated = orderRepository.markPaid(order.getId());
		if (updated == 0) {
			return paymentRepository.findByOrderId(order.getId())
				.map(PaymentResponse::from)
				.orElseThrow(() -> new BusinessException(PaymentErrorCode.ALREADY_PAID));
		}

		Payment payment = Payment.create(order, discountAmount, finalAmount, request.method());

		try {
			Payment saved = paymentRepository.saveAndFlush(payment);
			return PaymentResponse.from(saved);
		} catch (DataIntegrityViolationException ex) {
			return paymentRepository.findByOrderId(order.getId())
				.map(PaymentResponse::from)
				.orElseThrow(() -> ex);
		}
	}
}
