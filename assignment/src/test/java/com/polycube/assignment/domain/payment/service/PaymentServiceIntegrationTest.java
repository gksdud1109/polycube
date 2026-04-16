package com.polycube.assignment.domain.payment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.polycube.assignment.domain.member.entity.Member;
import com.polycube.assignment.domain.member.entity.MemberGrade;
import com.polycube.assignment.domain.member.repository.MemberRepository;
import com.polycube.assignment.domain.order.entity.Order;
import com.polycube.assignment.domain.order.entity.OrderStatus;
import com.polycube.assignment.domain.order.repository.OrderRepository;
import com.polycube.assignment.domain.payment.dto.PaymentRequest;
import com.polycube.assignment.domain.payment.dto.PaymentResponse;
import com.polycube.assignment.domain.payment.entity.PaymentMethod;
import com.polycube.assignment.domain.payment.exception.PaymentErrorCode;
import com.polycube.assignment.domain.payment.repository.PaymentRepository;
import com.polycube.assignment.global.error.BusinessException;

@SpringBootTest
class PaymentServiceIntegrationTest {

	@Autowired
	private PaymentService paymentService;

	@Autowired
	private PaymentRepository paymentRepository;

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private MemberRepository memberRepository;

	@AfterEach
	void tearDown() {
		paymentRepository.deleteAllInBatch();
		orderRepository.deleteAllInBatch();
		memberRepository.deleteAllInBatch();
	}

	@Test
	@DisplayName("결제 성공 시 주문 상태가 PAID로 변경되고 결제 내역이 저장된다")
	void payPersistsPaymentAndMarksOrderPaid() {
		Order order = saveOrder(MemberGrade.VIP, new BigDecimal("10000"));

		PaymentResponse response = paymentService.pay(new PaymentRequest(order.getId(), PaymentMethod.CREDIT_CARD));

		assertThat(response.discountAmount()).isEqualByComparingTo(new BigDecimal("1000"));
		assertThat(response.finalAmount()).isEqualByComparingTo(new BigDecimal("9000"));
		assertThat(response.method()).isEqualTo(PaymentMethod.CREDIT_CARD);

		Order savedOrder = orderRepository.findById(order.getId()).orElseThrow();
		assertThat(savedOrder.getStatus()).isEqualTo(OrderStatus.PAID);
		assertThat(paymentRepository.findByOrderId(order.getId())).isPresent();
	}

	@Test
	@DisplayName("이미 성공한 동일 주문 결제 요청은 기존 성공 결과를 재반환한다")
	void payReturnsExistingPaymentForDuplicateRequest() {
		Order order = saveOrder(MemberGrade.NORMAL, new BigDecimal("12000"));

		PaymentResponse first = paymentService.pay(new PaymentRequest(order.getId(), PaymentMethod.POINT));
		PaymentResponse second = paymentService.pay(new PaymentRequest(order.getId(), PaymentMethod.POINT));

		assertThat(second.paymentId()).isEqualTo(first.paymentId());
		assertThat(second.finalAmount()).isEqualByComparingTo(first.finalAmount());
		assertThat(paymentRepository.count()).isEqualTo(1);
	}

	@Test
	@DisplayName("존재하지 않는 주문 결제 시 예외가 발생한다")
	void payFailsWhenOrderDoesNotExist() {
		assertThatThrownBy(() -> paymentService.pay(new PaymentRequest(9999L, PaymentMethod.CREDIT_CARD)))
			.isInstanceOf(BusinessException.class)
			.extracting(ex -> ((BusinessException) ex).getErrorCode())
			.isEqualTo(PaymentErrorCode.ORDER_NOT_FOUND);
	}

	private Order saveOrder(MemberGrade grade, BigDecimal originalPrice) {
		Member member = memberRepository.save(
			Member.builder()
				.email(grade.name().toLowerCase() + "@example.com")
				.grade(grade)
				.build()
		);

		return orderRepository.save(
			Order.builder()
				.productName("coffee")
				.originalPrice(originalPrice)
				.member(member)
				.build()
		);
	}
}
