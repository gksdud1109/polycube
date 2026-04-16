package com.polycube.assignment.domain.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.polycube.assignment.domain.order.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {

	@Modifying
	@Query("""
		UPDATE Order o
		SET o.status = 'PAID'
		WHERE o.id = :orderID AND o.status = 'READY'
		""")
	int markPaid(@Param("orderId") Long orderId);
}
