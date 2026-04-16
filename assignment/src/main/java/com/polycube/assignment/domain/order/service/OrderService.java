package com.polycube.assignment.domain.order.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.polycube.assignment.domain.member.entity.Member;
import com.polycube.assignment.domain.member.repository.MemberRepository;
import com.polycube.assignment.domain.order.dto.CreateOrderRequest;
import com.polycube.assignment.domain.order.dto.OrderResponse;
import com.polycube.assignment.domain.order.entity.Order;
import com.polycube.assignment.domain.order.exception.OrderErrorCode;
import com.polycube.assignment.domain.order.repository.OrderRepository;
import com.polycube.assignment.global.error.BusinessException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

	private final OrderRepository orderRepository;
	private final MemberRepository memberRepository;

	@Transactional
	public OrderResponse createOrder(CreateOrderRequest request) {
		Member member = memberRepository.findById(request.memberId())
			.orElseThrow(() -> new BusinessException(OrderErrorCode.MEMBER_NOT_FOUND));

		Order order = Order.builder()
			.productName(request.productName())
			.originalPrice(request.originalPrice())
			.member(member)
			.build();

		Order saved = orderRepository.save(order);
		return OrderResponse.from(saved);
	}

	public OrderResponse getOrder(Long orderId) {
		Order order = orderRepository.findById(orderId)
			.orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));
		return OrderResponse.from(order);
	}
}
