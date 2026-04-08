package com.projectj.api.catalog.dto;

public record RecipeIngredientResponse(
	String ingredientId,
	String ingredientName,
	int quantity
){
}
