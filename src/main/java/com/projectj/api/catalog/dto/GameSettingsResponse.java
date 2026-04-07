package com.projectj.api.catalog.dto;

public record GameSettingsResponse(
	int startDay,
	String startRegionCode,
	int startGold,
	int startReputation,
	int defaultServiceCapacity,
	int defaultInventorySlotLimit
){
}
