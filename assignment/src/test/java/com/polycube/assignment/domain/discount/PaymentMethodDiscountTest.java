package com.polycube.assignment.domain.discount;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.polycube.assignment.domain.discount.policy.FixedAmountDiscountPolicy;
import com.polycube.assignment.domain.discount.policy.PointPaymentDiscountPolicy;
import com.polycube.assignment.domain.discount.policy.RateDiscountPolicy;
import com.polycube.assignment.domain.discount.service.DiscountService;
import com.polycube.assignment.domain.discount.service.PaymentMethodDiscountService;
import com.polycube.assignment.domain.member.entity.MemberGrade;
import com.polycube.assignment.domain.payment.entity.PaymentMethod;

/**
 * 등급 할인 + 결제 수단 할인 중복 적용 검증.
 * 요구사항: 포인트 결제 시 등급 할인 후 금액에서 추가 5% 할인
 */
class PaymentMethodDiscountTest {

	private final DiscountService discountService = new DiscountService(List.of(
		new FixedAmountDiscountPolicy(),
		new RateDiscountPolicy()
	));
	private final PaymentMethodDiscountService methodDiscountService =
		new PaymentMethodDiscountService(List.of(new PointPaymentDiscountPolicy()));

	@Nested
	@DisplayName("VIP + 포인트 결제")
	class VipWithPoint {

		@Test
		@DisplayName("VIP 1,000원 고정 할인 후 금액에서 5% 추가 할인이 순서대로 적용된다")
		void vipGradeDiscountThenPointDiscount() {
			BigDecimal originalPrice = new BigDecimal("10000");

			// 1단계: 등급 할인
			BigDecimal gradeDiscount = discountService.calculateDiscount(MemberGrade.VIP, originalPrice);
			BigDecimal afterGrade = originalPrice.subtract(gradeDiscount); // 9000

			// 2단계: 결제 수단 할인 (등급 할인 후 금액 기준)
			BigDecimal methodDiscount = methodDiscountService
				.calculateDiscountDetails(PaymentMethod.POINT, afterGrade)
				.stream()
				.map(r -> r.discountAmount())
				.reduce(BigDecimal.ZERO, BigDecimal::add);

			BigDecimal finalAmount = afterGrade.subtract(methodDiscount);

			assertThat(gradeDiscount).isEqualByComparingTo("1000");
			assertThat(methodDiscount).isEqualByComparingTo("450");   // 9000 * 5% = 450
			assertThat(finalAmount).isEqualByComparingTo("8550");
		}

		@Test
		@DisplayName("결제 수단 할인은 등급 할인 후 금액 기준이므로 원가 기준보다 할인액이 작다")
		void methodDiscountBasedOnAfterGradePrice() {
			BigDecimal originalPrice = new BigDecimal("10000");

			BigDecimal gradeDiscount = discountService.calculateDiscount(MemberGrade.VIP, originalPrice);
			BigDecimal afterGrade = originalPrice.subtract(gradeDiscount);

			BigDecimal methodDiscountOnAfterGrade = methodDiscountService
				.calculateDiscountDetails(PaymentMethod.POINT, afterGrade).get(0).discountAmount();

			BigDecimal methodDiscountOnOriginal = originalPrice
				.multiply(new BigDecimal("0.05")).setScale(0, java.math.RoundingMode.DOWN);

			// 등급 할인 후 금액(9000) 기준이 원가(10000) 기준보다 할인액이 작음
			assertThat(methodDiscountOnAfterGrade).isLessThan(methodDiscountOnOriginal);
		}
	}

	@Nested
	@DisplayName("VVIP + 포인트 결제")
	class VvipWithPoint {

		@Test
		@DisplayName("VVIP 10% 할인 후 금액에서 추가 5% 할인이 적용된다")
		void vvipGradeDiscountThenPointDiscount() {
			BigDecimal originalPrice = new BigDecimal("20000");

			BigDecimal gradeDiscount = discountService.calculateDiscount(MemberGrade.VVIP, originalPrice);
			BigDecimal afterGrade = originalPrice.subtract(gradeDiscount); // 18000

			BigDecimal methodDiscount = methodDiscountService
				.calculateDiscountDetails(PaymentMethod.POINT, afterGrade)
				.get(0).discountAmount();

			BigDecimal finalAmount = afterGrade.subtract(methodDiscount);

			assertThat(gradeDiscount).isEqualByComparingTo("2000");  // 20000 * 10%
			assertThat(methodDiscount).isEqualByComparingTo("900");  // 18000 * 5%
			assertThat(finalAmount).isEqualByComparingTo("17100");
		}
	}

	@Nested
	@DisplayName("신용카드 결제")
	class WithCreditCard {

		@Test
		@DisplayName("신용카드 결제 시 결제 수단 추가 할인이 없다")
		void noCreditCardDiscount() {
			BigDecimal price = new BigDecimal("10000");

			var results = methodDiscountService
				.calculateDiscountDetails(PaymentMethod.CREDIT_CARD, price);

			assertThat(results).isEmpty();
		}
	}
}
