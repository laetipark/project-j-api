package com.projectj.api.exploration.dto;

import jakarta.validation.constraints.NotBlank;

public record TravelRequest(
	@NotBlank(message = "portalCode is required.")
	String portalCode
){
}
