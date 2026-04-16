package com.polycube.assignment.global.response;

import com.polycube.assignment.global.error.BusinessException;
import com.polycube.assignment.global.error.ErrorCode;

public record ErrorResponse(
	int status,
	String code,
	String message
) {
	public static ErrorResponse from(BusinessException ex) {
		ErrorCode errorCode = ex.getErrorCode();
		return new ErrorResponse(
			errorCode.getHttpStatus().value(),
			toCodeName(errorCode),
			ex.getMessage()
		);
	}

	public static ErrorResponse from(ErrorCode errorCode) {
		return new ErrorResponse(
			errorCode.getHttpStatus().value(),
			toCodeName(errorCode),
			errorCode.getMessage()
		);
	}

	private static String toCodeName(ErrorCode errorCode) {
		return (errorCode instanceof Enum<?> e) ? e.name() : errorCode.getClass().getSimpleName();
	}
}

