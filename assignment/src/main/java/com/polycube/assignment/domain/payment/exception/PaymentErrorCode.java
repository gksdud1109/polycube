package com.polycube.assignment.domain.payment.exception;

import org.springframework.http.HttpStatus;

import com.polycube.assignment.global.error.ErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PaymentErrorCode implements ErrorCode {

	ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "결제할 주문을 찾을 수 없습니다."),
	ALREADY_PAID(HttpStatus.CONFLICT, "이미 결제 완료된 주문입니다.");

	private final HttpStatus httpStatus;
	private final String message;
}
