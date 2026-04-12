package com.projectj.api.player.dto;

import com.projectj.api.upgrade.dto.AvailableUpgradeResponse;

import java.util.List;

public record PlayerSnapshotResponse(
	String playerId,
	String displayName,
	String currentRegion,
	int gold,
	int reputation,
	int serviceCapacity,
	int inventorySlotLimit,
	String selectedRecipeId,
	List<ResourceAmountResponse> inventoryResources,
	List<ResourceAmountResponse> storageResources,
	List<String> unlockedTools,
	List<String> purchasedUpgradeCodes,
	List<AvailableUpgradeResponse> availableUpgrades
){
}
