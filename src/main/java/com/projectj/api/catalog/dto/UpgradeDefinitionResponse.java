package com.projectj.api.catalog.dto;

import java.util.List;

public record UpgradeDefinitionResponse(
	String code,
	String name,
	String upgradeType,
	Integer targetValue,
	String toolCode,
	int goldCost,
	String prerequisiteUpgradeCode,
	List<UpgradeCostResponse> resourceCosts
){
}
