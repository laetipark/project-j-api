package com.projectj.api.dayrun.dto;

public record DayRunSummaryResponse(
	int dayNumber,
	String selectedRecipeCode,
	int gatherSuccessCount,
	int gatherFailureCount,
	int totalGatheredQuantity,
	boolean serviceSkipped,
	int soldCount,
	int earnedGold,
	int earnedReputation
){
}
