package com.projectj.api.player.dto;

import com.projectj.api.dayrun.dto.DayRunSummaryResponse;
import com.projectj.api.upgrade.dto.AvailableUpgradeResponse;

import java.util.List;

public record PlayerSnapshotResponse(
	String playerId,
	String displayName,
	int currentDay,
	String currentPhase,
	String currentRegion,
	int gold,
	int reputation,
	int serviceCapacity,
	int inventorySlotLimit,
	String selectedRecipe,
	List<ResourceAmountResponse> inventoryResources,
	List<ResourceAmountResponse> storageResources,
	List<String> unlockedTools,
	List<AvailableUpgradeResponse> availableUpgrades,
	DayRunSummaryResponse currentDayRun,
	DayRunSummaryResponse lastSettlementSummary
){
}
