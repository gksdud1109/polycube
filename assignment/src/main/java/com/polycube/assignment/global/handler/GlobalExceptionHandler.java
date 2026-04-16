package com.polycube.assignment.global.handler;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.polycube.assignment.global.error.BusinessException;
import com.polycube.assignment.global.error.CommonErrorCode;
import com.polycube.assignment.global.error.ErrorCode;
import com.polycube.assignment.global.error.ExternalApiException;
import com.polycube.assignment.global.response.ErrorResponse;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
		ErrorCode code = ex.getErrorCode();
		String codeName = (code instanceof Enum<?> e) ? e.name() : code.getClass().getSimpleName();

		if (code.getHttpStatus().is5xxServerError()) {
			log.error("BusinessException: {} - {}", codeName, ex.getMessage());
		} else {
			log.warn("BusinessException: {} - {}", codeName, ex.getMessage());
		}

		return ResponseEntity
			.status(code.getHttpStatus())
			.body(ErrorResponse.from(ex));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
		String message = ex.getBindingResult().getFieldErrors().stream()
			.map(e -> e.getField() + ": " + e.getDefaultMessage())
			.reduce((a, b) -> a + ", " + b)
			.orElse(CommonErrorCode.INVALID_INPUT_VALUE.getMessage());

		log.warn("Validation failed: {}", message);

		return ResponseEntity
			.status(CommonErrorCode.INVALID_INPUT_VALUE.getHttpStatus())
			.contentType(MediaType.APPLICATION_JSON)
			.body(new ErrorResponse(
				CommonErrorCode.INVALID_INPUT_VALUE.getHttpStatus().value(),
				CommonErrorCode.INVALID_INPUT_VALUE.name(),
				message));
	}

	@ExceptionHandler({MethodArgumentTypeMismatchException.class, HttpMessageNotReadableException.class})
	public ResponseEntity<ErrorResponse> handleBadRequest(Exception ex) {
		log.warn("Bad request: {}", ex.getMessage());

		return ResponseEntity
			.status(CommonErrorCode.INVALID_INPUT_VALUE.getHttpStatus())
			.contentType(MediaType.APPLICATION_JSON)
			.body(ErrorResponse.from(CommonErrorCode.INVALID_INPUT_VALUE));
	}

	@ExceptionHandler(ExternalApiException.class)
	public ResponseEntity<ErrorResponse> handleExternalApi(ExternalApiException ex) {
		log.error("ExternalApiException: {} - {}", ex.getCode(), ex.getMessage());

		return ResponseEntity
			.status(ex.getHttpStatus())
			.contentType(MediaType.APPLICATION_JSON)
			.body(new ErrorResponse(ex.getHttpStatus().value(), ex.getCode(), ex.getMessage()));
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ErrorResponse> handleDuplicateKey(DataIntegrityViolationException ex) {
		log.warn("Data integrity violation: {}", ex.getMessage());

		return ResponseEntity
			.status(CommonErrorCode.DUPLICATE_KEY.getHttpStatus())
			.contentType(MediaType.APPLICATION_JSON)
			.body(ErrorResponse.from(CommonErrorCode.DUPLICATE_KEY));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleUnknown(Exception ex) {
		log.error("Unexpected error: {}", ex.getMessage(), ex);

		return ResponseEntity
			.status(CommonErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus())
			.contentType(MediaType.APPLICATION_JSON)
			.body(ErrorResponse.from(CommonErrorCode.INTERNAL_SERVER_ERROR));
	}
}
