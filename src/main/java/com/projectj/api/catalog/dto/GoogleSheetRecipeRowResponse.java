package com.projectj.api.catalog.dto;

import java.util.List;

public record GoogleSheetRecipeRowResponse(
	int rowNumber,
	String difficultyLabel,
	Integer difficulty,
	String recipeName,
	String supplySource,
	String cookingMethod,
	List<String> ingredients,
	String priceText,
	Integer price,
	String memo
){
}
