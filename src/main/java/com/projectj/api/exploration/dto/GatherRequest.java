package com.projectj.api.exploration.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record GatherRequest(
	@NotBlank(message = "regionCode is required.")
	String regionCode,
	@NotBlank(message = "resourceCode is required.")
	String resourceCode,
	@Min(value = 1, message = "quantity must be at least 1.")
	int quantity
){
}
