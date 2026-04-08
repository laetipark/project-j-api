package com.projectj.api.catalog.dto;

import java.util.List;

public record GoogleSheetRecipeRowResponse(
	int rowNumber,
	String recipeId,
	String recipeName,
	String supplySource,
	int difficulty,
	String cookingMethod,
	List<RecipeIngredientResponse> ingredients,
	int price,
	String memo
){
}
