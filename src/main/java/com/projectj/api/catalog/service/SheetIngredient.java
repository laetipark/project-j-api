package com.projectj.api.catalog.service;

public record SheetIngredient(
	int rowNumber,
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
