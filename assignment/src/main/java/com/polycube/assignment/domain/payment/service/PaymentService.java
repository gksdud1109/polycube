package com.polycube.assignment.domain.payment.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.polycube.assignment.domain.discount.dto.DiscountResult;
import com.polycube.assignment.domain.discount.entity.DiscountHistory;
import com.polycube.assignment.domain.discount.repository.DiscountHistoryRepository;
import com.polycube.assignment.domain.discount.service.DiscountService;
import com.polycube.assignment.domain.discount.service.PaymentMethodDiscountService;
import com.polycube.assignment.domain.member.entity.MemberGrade;
import com.polycube.assignment.domain.order.entity.Order;
import com.polycube.assignment.domain.order.repository.OrderRepository;
import com.polycube.assignment.domain.payment.dto.PaymentRequest;
import com.polycube.assignment.domain.payment.dto.PaymentResponse;
import com.polycube.assignment.domain.payment.entity.Payment;
import com.polycube.assignment.domain.payment.entity.PaymentMethod;
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
	private final PaymentMethodDiscountService paymentMethodDiscountService;
	private final DiscountHistoryRepository discountHistoryRepository;

	@Transactional
	public PaymentResponse pay(PaymentRequest request) {
		Order order = orderRepository.findById(request.orderId())
			.orElseThrow(() -> new BusinessException(PaymentErrorCode.ORDER_NOT_FOUND));

		BigDecimal originalPrice = order.getOriginalPrice();
		MemberGrade grade = order.getMember().getGrade();
		PaymentMethod method = request.method();

		// 1. 등급 할인 계산 (원가 기준)
		List<DiscountResult> gradeDiscounts = discountService.calculateDiscountDetails(grade, originalPrice);
		BigDecimal gradeDiscountTotal = sumDiscounts(gradeDiscounts).min(originalPrice);
		BigDecimal afterGradePrice = originalPrice.subtract(gradeDiscountTotal);

		// 2. 결제 수단 추가 할인 계산 (등급 할인 후 금액 기준)
		List<DiscountResult> methodDiscounts = paymentMethodDiscountService.calculateDiscountDetails(method, afterGradePrice);
		BigDecimal methodDiscountTotal = sumDiscounts(methodDiscounts).min(afterGradePrice);

		BigDecimal totalDiscount = gradeDiscountTotal.add(methodDiscountTotal);
		BigDecimal finalAmount = originalPrice.subtract(totalDiscount);

		// 3. 상태 기반 CAS — READY인 주문만 PAID로 원자적 전이
		int updated = orderRepository.markPaid(order.getId());
		if (updated == 0) {
			// 이미 처리된 요청이면 기존 결과를 재반환 (멱등성)
			return paymentRepository.findByOrderId(order.getId())
				.map(PaymentResponse::from)
				.orElseThrow(() -> new BusinessException(PaymentErrorCode.ALREADY_PAID));
		}

		Payment payment = Payment.create(order, totalDiscount, finalAmount, method);
		try {
			Payment saved = paymentRepository.saveAndFlush(payment);
			// 4. 할인 이력 저장 — 정책 스냅샷이므로 이후 정책 수정·삭제와 무관하게 보존됨
			saveDiscountHistories(saved, grade, gradeDiscounts, methodDiscounts);
			return PaymentResponse.from(saved);
		} catch (DataIntegrityViolationException ex) {
			// DB unique 제약 위반 시 2차 방어: 기존 성공 결과 재반환
			return paymentRepository.findByOrderId(order.getId())
				.map(PaymentResponse::from)
				.orElseThrow(() -> ex);
		}
	}

	private void saveDiscountHistories(Payment payment, MemberGrade grade,
		List<DiscountResult> gradeDiscounts, List<DiscountResult> methodDiscounts) {

		String gradeSnapshot = grade.name();
		List<DiscountHistory> histories = new ArrayList<>();

		gradeDiscounts.stream()
			.filter(r -> r.discountAmount().compareTo(BigDecimal.ZERO) > 0)
			.map(r -> DiscountHistory.ofGrade(payment, gradeSnapshot, r))
			.forEach(histories::add);

		methodDiscounts.stream()
			.filter(r -> r.discountAmount().compareTo(BigDecimal.ZERO) > 0)
			.map(r -> DiscountHistory.ofPaymentMethod(payment, gradeSnapshot, r))
			.forEach(histories::add);

		discountHistoryRepository.saveAll(histories);
	}

	private BigDecimal sumDiscounts(List<DiscountResult> results) {
		return results.stream()
			.map(DiscountResult::discountAmount)
			.reduce(BigDecimal.ZERO, BigDecimal::add);
	}
}
