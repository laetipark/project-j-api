package com.projectj.api.catalog.dto;

import java.util.List;

public record BootstrapResponse(
	List<ResourceDefinitionResponse> resources,
	List<ToolDefinitionResponse> tools,
	List<RegionDefinitionResponse> regions,
	List<PortalRuleResponse> portalRules,
	List<RecipeDefinitionResponse> recipes,
	List<UpgradeDefinitionResponse> upgrades,
	GameSettingsResponse settings
){
}
