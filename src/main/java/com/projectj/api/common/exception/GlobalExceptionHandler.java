package com.projectj.api.common.exception;

import com.projectj.api.common.api.ApiError;
import com.projectj.api.common.api.ApiResponse;
import com.projectj.api.common.api.FieldValidationError;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler{

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException exception){
		ApiError error = new ApiError(exception.getErrorCode().getCode(), exception.getMessage(), List.of());
		return ResponseEntity.status(exception.getErrorCode().getHttpStatus()).body(ApiResponse.failure(error));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException exception){
		List<FieldValidationError> fieldErrors = exception.getBindingResult()
			.getFieldErrors()
			.stream()
			.map(this::toFieldValidationError)
			.toList();
		ApiError error = new ApiError(ErrorCode.VALIDATION_ERROR.getCode(), "Validation failed.", fieldErrors);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.failure(error));
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException exception){
		List<FieldValidationError> fieldErrors = exception.getConstraintViolations()
			.stream()
			.map(violation -> new FieldValidationError(violation.getPropertyPath().toString(), violation.getMessage()))
			.toList();
		ApiError error = new ApiError(ErrorCode.VALIDATION_ERROR.getCode(), "Validation failed.", fieldErrors);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.failure(error));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleUnexpectedException(Exception exception){
		ApiError error = new ApiError("INTERNAL_SERVER_ERROR", exception.getMessage(), List.of());
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.failure(error));
	}

	private FieldValidationError toFieldValidationError(FieldError fieldError){
		return new FieldValidationError(fieldError.getField(), fieldError.getDefaultMessage());
	}

}
