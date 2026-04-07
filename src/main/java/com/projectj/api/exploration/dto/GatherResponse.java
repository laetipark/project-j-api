package com.projectj.api.exploration.dto;

import com.projectj.api.player.dto.PlayerSnapshotResponse;

public record GatherResponse(
	boolean success,
	String message,
	String regionCode,
	String resourceCode,
	int quantityRequested,
	int quantityGranted,
	PlayerSnapshotResponse snapshot
){
}
