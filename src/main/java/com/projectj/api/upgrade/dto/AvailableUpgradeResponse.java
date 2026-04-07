package com.projectj.api.upgrade.dto;

import java.util.List;

public record AvailableUpgradeResponse(
	String upgradeCode,
	boolean purchasable,
	int goldCost,
	int missingGold,
	List<MissingResourceResponse> missingResources,
	List<String> blockedReasons
){
}
