package com.projectj.api.restaurant.dto;

import jakarta.validation.constraints.NotBlank;

public record SelectRecipeRequest(
	@NotBlank(message = "recipeCode is required.")
	String recipeCode
){
}
