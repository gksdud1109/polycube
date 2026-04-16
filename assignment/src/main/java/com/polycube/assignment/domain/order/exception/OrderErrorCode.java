package com.polycube.assignment.domain.order.exception;

import org.springframework.http.HttpStatus;

import com.polycube.assignment.global.error.ErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderErrorCode implements ErrorCode {

	ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."),
	MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."),
	NOT_PAYABLE(HttpStatus.CONFLICT, "결제 가능한 상태의 주문이 아닙니다.");

	private final HttpStatus httpStatus;
	private final String message;
}
