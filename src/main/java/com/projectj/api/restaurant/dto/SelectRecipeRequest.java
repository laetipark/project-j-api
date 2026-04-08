package com.projectj.api.restaurant.dto;

import jakarta.validation.constraints.NotBlank;

public record SelectRecipeRequest(
	@NotBlank(message = "recipeId is required.")
	String recipeId
){
}
