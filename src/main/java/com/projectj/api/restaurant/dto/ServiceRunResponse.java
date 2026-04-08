package com.projectj.api.restaurant.dto;

import com.projectj.api.player.dto.PlayerSnapshotResponse;

public record ServiceRunResponse(
	String recipeId,
	int requestedCapacity,
	int cookableCount,
	int soldCount,
	int earnedGold,
	int earnedReputation,
	PlayerSnapshotResponse snapshot
){
}
