package com.polycube.assignment.global.error;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class ExternalApiException extends RuntimeException{

	private final String code;
	private final HttpStatus httpStatus;

	public ExternalApiException(String code, ErrorCode errorCode) {
		super(errorCode.getMessage());
		this.code = code;
		this.httpStatus = errorCode.getHttpStatus();
	}

	public ExternalApiException(String code, String message, HttpStatus httpStatus) {
		super(message);
		this.code = code;
		this.httpStatus = httpStatus;
	}
}

