package com.projectj.api.restaurant.dto;

import com.projectj.api.player.dto.PlayerSnapshotResponse;

public record ServiceRunResponse(
	String recipeCode,
	int requestedCapacity,
	int cookableCount,
	int soldCount,
	int earnedGold,
	int earnedReputation,
	boolean skipped,
	PlayerSnapshotResponse snapshot
){
}
