package com.projectj.api.upgrade.dto;

import com.projectj.api.player.dto.PlayerSnapshotResponse;

public record UpgradePurchaseResponse(
	String upgradeCode,
	PlayerSnapshotResponse snapshot
){
}
