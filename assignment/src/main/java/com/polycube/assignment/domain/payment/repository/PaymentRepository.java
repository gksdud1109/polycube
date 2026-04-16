package com.polycube.assignment.domain.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.polycube.assignment.domain.payment.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

	boolean existsByOrderId(Long orderId);
}
