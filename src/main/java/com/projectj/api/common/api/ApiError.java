package com.projectj.api.common.api;

import java.util.List;

public record ApiError(
	String code,
	String message,
	List<FieldValidationError> fieldErrors
){
}
