package com.projectj.api.catalog.dto;

import java.util.List;

public record RecipeDefinitionResponse(
	String code,
	String name,
	int difficulty,
	int sellPrice,
	int reputationReward,
	List<RecipeIngredientResponse> ingredients
){
}
