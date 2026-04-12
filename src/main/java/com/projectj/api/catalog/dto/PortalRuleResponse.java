package com.projectj.api.catalog.dto;

public record PortalRuleResponse(
	String code,
	String name,
	String fromRegionCode,
	String toRegionCode,
	String requiredToolCode,
	int requiredReputation,
	String requiredUpgradeCode
){
}
