package com.projectj.api.catalog.dto;

public record IngredientDefinitionResponse(
	String ingredientId,
	String ingredientName,
	int difficulty,
	String supplySource,
	String acquisitionSource,
	String acquisitionMethod,
	String acquisitionTool,
	int buyPrice,
	int sellPrice,
	String memo
){
}
