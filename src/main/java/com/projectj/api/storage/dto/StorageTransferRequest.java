package com.projectj.api.storage.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record StorageTransferRequest(
	@NotBlank(message = "resourceCode is required.")
	String resourceCode,
	@Min(value = 1, message = "quantity must be at least 1.")
	int quantity
){
}
