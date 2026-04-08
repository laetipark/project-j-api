package com.projectj.api.catalog.service;

import java.util.List;

public record RawSheetRecipe(
	int rowNumber,
	String recipeId,
	String recipeName,
	String supplySource,
	int difficulty,
	String cookingMethod,
	List<String> ingredientNames,
	int price,
	String memo
){
}
