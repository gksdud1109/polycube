package com.polycube.assignment.domain.discount.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.polycube.assignment.domain.discount.entity.DiscountHistory;

public interface DiscountHistoryRepository extends JpaRepository<DiscountHistory, Long> {

	List<DiscountHistory> findAllByPaymentId(Long paymentId);
}
