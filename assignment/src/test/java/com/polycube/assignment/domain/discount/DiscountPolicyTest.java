package com.polycube.assignment.domain.discount;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.polycube.assignment.domain.discount.policy.DiscountPolicy;
import com.polycube.assignment.domain.discount.policy.FixedAmountDiscountPolicy;
import com.polycube.assignment.domain.discount.policy.RateDiscountPolicy;
import com.polycube.assignment.domain.discount.service.DiscountService;
import com.polycube.assignment.domain.member.entity.MemberGrade;

class DiscountPolicyTest {

	private DiscountService discountService;

	@BeforeEach
	void setUp() {
		List<DiscountPolicy> policies = List.of(
			new FixedAmountDiscountPolicy(),
			new RateDiscountPolicy()
		);
		discountService = new DiscountService(policies);
	}

	@Nested
	@DisplayName("NORMAL 등급")
	class NormalGradeTest {

		@Test
		@DisplayName("할인이 적용되지 않는다")
		void noDiscount() {
			BigDecimal price = new BigDecimal("10000");
			BigDecimal discount = discountService.calculateDiscount(MemberGrade.NORMAL, price);
			assertThat(discount).isEqualByComparingTo(BigDecimal.ZERO);
		}
	}

	@Nested
	@DisplayName("VIP 등급")
	class VipGradeTest {

		@Test
		@DisplayName("1,000원 고정 금액 할인이 적용된다")
		void fixedDiscount() {
			BigDecimal price = new BigDecimal("10000");
			BigDecimal discount = discountService.calculateDiscount(MemberGrade.VIP, price);
			assertThat(discount).isEqualByComparingTo(new BigDecimal("1000"));
		}

		@Test
		@DisplayName("주문 금액이 1,000원 미만이면 주문 금액만큼만 할인된다")
		void fixedDiscountExceedsPrice() {
			BigDecimal price = new BigDecimal("500");
			BigDecimal discount = discountService.calculateDiscount(MemberGrade.VIP, price);
			assertThat(discount).isEqualByComparingTo(new BigDecimal("500"));
		}
	}

	@Nested
	@DisplayName("VVIP 등급")
	class VvipGradeTest {

		@Test
		@DisplayName("주문 금액의 10% 할인이 적용된다")
		void rateDiscount() {
			BigDecimal price = new BigDecimal("20000");
			BigDecimal discount = discountService.calculateDiscount(MemberGrade.VVIP, price);
			assertThat(discount).isEqualByComparingTo(new BigDecimal("2000"));
		}

		@Test
		@DisplayName("소수점 이하 금액은 절사된다")
		void rateDiscountRoundsDown() {
			BigDecimal price = new BigDecimal("15555");
			BigDecimal discount = discountService.calculateDiscount(MemberGrade.VVIP, price);
			assertThat(discount).isEqualByComparingTo(new BigDecimal("1555"));
		}
	}

	@Test
	@DisplayName("여러 할인 정책이 동시에 적용되면 우선순위와 무관하게 모두 합산되고 과할인은 방지된다")
	void multiplePoliciesAreAppliedDeterministically() {
		DiscountService stackedDiscountService = new DiscountService(List.of(
			new TestFixedDiscountPolicy(2, MemberGrade.VIP, new BigDecimal("700")),
			new TestFixedDiscountPolicy(1, MemberGrade.VIP, new BigDecimal("500"))
		));

		BigDecimal price = new BigDecimal("1000");
		BigDecimal discount = stackedDiscountService.calculateDiscount(MemberGrade.VIP, price);

		assertThat(discount).isEqualByComparingTo(price);
	}

	private static class TestFixedDiscountPolicy implements DiscountPolicy {

		private final int priority;
		private final MemberGrade grade;
		private final BigDecimal amount;

		private TestFixedDiscountPolicy(int priority, MemberGrade grade, BigDecimal amount) {
			this.priority = priority;
			this.grade = grade;
			this.amount = amount;
		}

		@Override
		public int priority() {
			return priority;
		}

		@Override
		public boolean supports(MemberGrade grade) {
			return this.grade == grade;
		}

		@Override
		public BigDecimal calculateDiscount(BigDecimal originalPrice) {
			return amount;
		}
	}
}
