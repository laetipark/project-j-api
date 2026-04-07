package com.projectj.api.common.api;

public record FieldValidationError(
	String field,
	String message
){
}
