package com.projectj.api.catalog.service;

import java.util.List;

public record SheetRecipe(
	int rowNumber,
	String recipeId,
	String recipeName,
	String supplySource,
	int difficulty,
	String cookingMethod,
	List<SheetRecipeIngredient> ingredients,
	int price,
	String memo
){
}
