package com.polycube.assignment.domain.payment.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.polycube.assignment.domain.payment.dto.PaymentRequest;
import com.polycube.assignment.domain.payment.dto.PaymentResponse;
import com.polycube.assignment.domain.payment.service.PaymentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

	private final PaymentService paymentService;

	@PostMapping
	public ResponseEntity<PaymentResponse> pay(@Valid @RequestBody PaymentRequest request) {
		PaymentResponse response = paymentService.pay(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
}
