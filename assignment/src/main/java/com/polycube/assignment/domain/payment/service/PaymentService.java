package com.polycube.assignment.domain.payment.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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

		if (paymentRepository.existsByOrderId(order.getId())) {
			throw new BusinessException(PaymentErrorCode.ALREADY_PAID);
		}

		BigDecimal originalPrice = order.getOriginalPrice();

		// 할인율 계산
		BigDecimal discountAmount = discountService.calculateDiscount(
			order.getMember().getGrade(), originalPrice);

		// 최종 가격 = 원가 - 할인가격
		BigDecimal finalAmount = originalPrice.subtract(discountAmount);

		// 상태 기반 업데이트 쿼리 OrderStatus READY -> PAID
		int updated = orderRepository.markPaid(order.getId());
		if(updated == 0){
			throw new BusinessException(PaymentErrorCode.ALREADY_PAID);
		}

		Payment payment = Payment.builder()
			.order(order)
			.discountAmount(discountAmount)
			.finalAmount(finalAmount)
			.method(request.method())
			.paidAt(LocalDateTime.now())
			.build();

		Payment saved = paymentRepository.save(payment);
		return PaymentResponse.from(saved);
	}


}
