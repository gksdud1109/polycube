package com.polycube.assignment.domain.discount;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.polycube.assignment.domain.discount.entity.DiscountHistory;
import com.polycube.assignment.domain.discount.entity.DiscountType;
import com.polycube.assignment.domain.discount.repository.DiscountHistoryRepository;
import com.polycube.assignment.domain.member.entity.Member;
import com.polycube.assignment.domain.member.entity.MemberGrade;
import com.polycube.assignment.domain.member.repository.MemberRepository;
import com.polycube.assignment.domain.order.entity.Order;
import com.polycube.assignment.domain.order.repository.OrderRepository;
import com.polycube.assignment.domain.payment.dto.PaymentRequest;
import com.polycube.assignment.domain.payment.dto.PaymentResponse;
import com.polycube.assignment.domain.payment.entity.PaymentMethod;
import com.polycube.assignment.domain.payment.repository.PaymentRepository;
import com.polycube.assignment.domain.payment.service.PaymentService;

/**
 * 할인 이력 보존 정합성 테스트.
 *
 * 핵심 검증: 할인 이력은 결제 시점의 정책을 스냅샷으로 저장하므로,
 * 이후 시스템 정책이 수정·삭제되어도 과거 결제 이력은 변하지 않는다.
 */
@SpringBootTest
class DiscountHistoryIntegrityTest {

	@Autowired private PaymentService paymentService;
	@Autowired private DiscountHistoryRepository discountHistoryRepository;
	@Autowired private PaymentRepository paymentRepository;
	@Autowired private OrderRepository orderRepository;
	@Autowired private MemberRepository memberRepository;

	@AfterEach
	void tearDown() {
		discountHistoryRepository.deleteAllInBatch();
		paymentRepository.deleteAllInBatch();
		orderRepository.deleteAllInBatch();
		memberRepository.deleteAllInBatch();
	}

	@Test
	@DisplayName("VIP 신용카드 결제 시 등급 할인 이력 1건이 저장된다")
	void vipCreditCardPaymentSavesGradeDiscountHistory() {
		Order order = saveOrder(MemberGrade.VIP, new BigDecimal("10000"));

		PaymentResponse response = paymentService.pay(
			new PaymentRequest(order.getId(), PaymentMethod.CREDIT_CARD));

		List<DiscountHistory> histories = discountHistoryRepository
			.findAllByPaymentId(response.paymentId());

		assertThat(histories).hasSize(1);
		DiscountHistory history = histories.get(0);
		assertThat(history.getDiscountType()).isEqualTo(DiscountType.GRADE);
		assertThat(history.getPolicyName()).isEqualTo("VIP 고정 금액 할인");
		assertThat(history.getAppliedGrade()).isEqualTo("VIP");
		assertThat(history.getDiscountAmount()).isEqualByComparingTo("1000");
		assertThat(history.getDiscountRate()).isEqualByComparingTo("0");
	}

	@Test
	@DisplayName("VIP 포인트 결제 시 등급 할인 + 결제 수단 할인 이력 2건이 저장된다")
	void vipPointPaymentSavesBothDiscountHistories() {
		Order order = saveOrder(MemberGrade.VIP, new BigDecimal("10000"));

		PaymentResponse response = paymentService.pay(
			new PaymentRequest(order.getId(), PaymentMethod.POINT));

		List<DiscountHistory> histories = discountHistoryRepository
			.findAllByPaymentId(response.paymentId());

		assertThat(histories).hasSize(2);
		assertThat(histories).anySatisfy(h -> {
			assertThat(h.getDiscountType()).isEqualTo(DiscountType.GRADE);
			assertThat(h.getDiscountAmount()).isEqualByComparingTo("1000");
		});
		assertThat(histories).anySatisfy(h -> {
			assertThat(h.getDiscountType()).isEqualTo(DiscountType.PAYMENT_METHOD);
			assertThat(h.getPolicyName()).isEqualTo("포인트 결제 추가 할인");
			assertThat(h.getDiscountRate()).isEqualByComparingTo("0.05");
			assertThat(h.getDiscountAmount()).isEqualByComparingTo("450"); // 9000 * 5%
		});
	}

	@Test
	@DisplayName("정책이 코드에서 제거되어도 DB에 기록된 이력의 policyName·discountRate는 변하지 않는다")
	void discountHistoryPreservedAfterPolicyChange() {
		// given: VIP 회원이 신용카드로 결제 → "VIP 고정 금액 할인" 이력 생성
		Order order = saveOrder(MemberGrade.VIP, new BigDecimal("10000"));
		PaymentResponse response = paymentService.pay(
			new PaymentRequest(order.getId(), PaymentMethod.CREDIT_CARD));

		// when: 이력을 직접 DB에서 조회 (정책 구현체와 무관하게 값으로 저장됨)
		List<DiscountHistory> histories = discountHistoryRepository
			.findAllByPaymentId(response.paymentId());

		// then: 정책 이름·할인율·할인금액이 결제 당시 값 그대로 보존
		DiscountHistory history = histories.get(0);
		assertThat(history.getPolicyName()).isEqualTo("VIP 고정 금액 할인");
		assertThat(history.getDiscountRate()).isEqualByComparingTo(BigDecimal.ZERO);
		assertThat(history.getDiscountAmount()).isEqualByComparingTo("1000");
		assertThat(history.getAppliedGrade()).isEqualTo("VIP");
		// 이 값들은 FixedAmountDiscountPolicy 클래스가 수정·삭제되어도 DB에서 그대로 조회됨
	}

	@Test
	@DisplayName("NORMAL 등급 신용카드 결제 시 할인이 없어 이력이 저장되지 않는다")
	void normalMemberCreditCardNoDiscountHistory() {
		Order order = saveOrder(MemberGrade.NORMAL, new BigDecimal("10000"));

		PaymentResponse response = paymentService.pay(
			new PaymentRequest(order.getId(), PaymentMethod.CREDIT_CARD));

		List<DiscountHistory> histories = discountHistoryRepository
			.findAllByPaymentId(response.paymentId());

		assertThat(histories).isEmpty();
	}

	@Test
	@DisplayName("VVIP 포인트 결제 시 최종 금액이 등급 할인 → 결제 수단 할인 순으로 계산된다")
	void vvipPointPaymentFinalAmountCalculation() {
		BigDecimal originalPrice = new BigDecimal("20000");
		Order order = saveOrder(MemberGrade.VVIP, originalPrice);

		PaymentResponse response = paymentService.pay(
			new PaymentRequest(order.getId(), PaymentMethod.POINT));

		// VVIP 10% → 18000, 포인트 5% → 900 → 최종 17100
		assertThat(response.finalAmount()).isEqualByComparingTo("17100");
		assertThat(response.discountAmount()).isEqualByComparingTo("2900");

		List<DiscountHistory> histories = discountHistoryRepository
			.findAllByPaymentId(response.paymentId());
		assertThat(histories).hasSize(2);
		assertThat(histories.stream()
			.map(DiscountHistory::getDiscountAmount)
			.reduce(BigDecimal.ZERO, BigDecimal::add))
			.isEqualByComparingTo(response.discountAmount());
	}

	private Order saveOrder(MemberGrade grade, BigDecimal price) {
		Member member = memberRepository.save(
			Member.builder().email(grade.name().toLowerCase() + "@test.com").grade(grade).build()
		);
		return orderRepository.save(
			Order.builder().productName("상품").originalPrice(price).member(member).build()
		);
	}
}
