package com.projectj.api.upgrade.dto;

public record MissingResourceResponse(
	String resourceCode,
	int requiredQuantity,
	int ownedQuantity,
	int missingQuantity
){
}
