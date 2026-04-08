package com.projectj.api.catalog.service;

import com.projectj.api.catalog.domain.GameSettingEntity;
import com.projectj.api.catalog.domain.IngredientEntity;
import com.projectj.api.catalog.domain.PortalRuleEntity;
import com.projectj.api.catalog.domain.RegionEntity;
import com.projectj.api.catalog.domain.ResourceEntity;
import com.projectj.api.catalog.domain.ToolEntity;
import com.projectj.api.catalog.domain.UpgradeEntity;
import com.projectj.api.catalog.domain.UpgradeResourceCostEntity;
import com.projectj.api.catalog.dto.BootstrapResponse;
import com.projectj.api.catalog.dto.GameSettingsResponse;
import com.projectj.api.catalog.dto.IngredientDefinitionResponse;
import com.projectj.api.catalog.dto.PortalRuleResponse;
import com.projectj.api.catalog.dto.RecipeIngredientResponse;
import com.projectj.api.catalog.dto.RecipeDefinitionResponse;
import com.projectj.api.catalog.dto.RegionDefinitionResponse;
import com.projectj.api.catalog.dto.ResourceDefinitionResponse;
import com.projectj.api.catalog.dto.ToolDefinitionResponse;
import com.projectj.api.catalog.dto.UpgradeCostResponse;
import com.projectj.api.catalog.dto.UpgradeDefinitionResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BootstrapService{

	private final CatalogLookupService catalogLookupService;
	private final RecipeCatalogService recipeCatalogService;

	public BootstrapService(CatalogLookupService catalogLookupService, RecipeCatalogService recipeCatalogService){
		this.catalogLookupService = catalogLookupService;
		this.recipeCatalogService = recipeCatalogService;
	}

	public BootstrapResponse getBootstrap(){
		List<ResourceDefinitionResponse> resources = catalogLookupService.getActiveResources()
			.stream()
			.map(this::toResourceResponse)
			.toList();
		List<IngredientDefinitionResponse> ingredients = catalogLookupService.getActiveIngredients()
			.stream()
			.map(this::toIngredientResponse)
			.toList();
		List<ToolDefinitionResponse> tools = catalogLookupService.getActiveTools()
			.stream()
			.map(this::toToolResponse)
			.toList();
		List<RegionDefinitionResponse> regions = catalogLookupService.getActiveRegions()
			.stream()
			.map(this::toRegionResponse)
			.toList();
		List<PortalRuleResponse> portalRules = catalogLookupService.getActivePortalRules()
			.stream()
			.map(this::toPortalRuleResponse)
			.toList();
		List<RecipeDefinitionResponse> recipes = recipeCatalogService.getRecipes()
			.stream()
			.map(this::toRecipeResponse)
			.toList();
		List<UpgradeDefinitionResponse> upgrades = catalogLookupService.getActiveUpgrades()
			.stream()
			.map(this::toUpgradeResponse)
			.toList();
		GameSettingsResponse settings = toSettingsResponse(catalogLookupService.getGameSettings());
		return new BootstrapResponse(resources, ingredients, tools, regions, portalRules, recipes, upgrades, settings);
	}

	private ResourceDefinitionResponse toResourceResponse(ResourceEntity resource){
		return new ResourceDefinitionResponse(resource.getCode(), resource.getName());
	}

	private ToolDefinitionResponse toToolResponse(ToolEntity tool){
		return new ToolDefinitionResponse(tool.getCode(), tool.getName(), tool.isDefaultUnlocked());
	}

	private IngredientDefinitionResponse toIngredientResponse(IngredientEntity ingredient){
		return new IngredientDefinitionResponse(
			ingredient.getIngredientId(),
			ingredient.getIngredientName(),
			ingredient.getDifficulty(),
			ingredient.getSupplySource(),
			ingredient.getAcquisitionSource(),
			ingredient.getAcquisitionMethod(),
			ingredient.getAcquisitionTool(),
			ingredient.getBuyPrice(),
			ingredient.getSellPrice(),
			ingredient.getMemo()
		);
	}

	private RegionDefinitionResponse toRegionResponse(RegionEntity region){
		return new RegionDefinitionResponse(region.getCode(), region.getName());
	}

	private PortalRuleResponse toPortalRuleResponse(PortalRuleEntity portalRule){
		return new PortalRuleResponse(
			portalRule.getCode(),
			portalRule.getName(),
			portalRule.getFromRegion().getCode(),
			portalRule.getToRegion().getCode(),
			portalRule.getRequiredTool() != null ? portalRule.getRequiredTool().getCode() : null,
			portalRule.getRequiredReputation()
		);
	}

	private RecipeDefinitionResponse toRecipeResponse(SheetRecipe recipe){
		return new RecipeDefinitionResponse(
			recipe.recipeId(),
			recipe.recipeName(),
			recipe.supplySource(),
			recipe.difficulty(),
			recipe.cookingMethod(),
			recipe.ingredients().stream().map(this::toRecipeIngredientResponse).toList(),
			recipe.price(),
			recipe.memo()
		);
	}

	private RecipeIngredientResponse toRecipeIngredientResponse(SheetRecipeIngredient ingredient){
		return new RecipeIngredientResponse(ingredient.ingredientId(), ingredient.ingredientName(), ingredient.quantity());
	}

	private UpgradeDefinitionResponse toUpgradeResponse(UpgradeEntity upgrade){
		List<UpgradeCostResponse> resourceCosts = catalogLookupService.getUpgradeCosts(upgrade.getId())
			.stream()
			.map(this::toUpgradeCostResponse)
			.toList();
		return new UpgradeDefinitionResponse(
			upgrade.getCode(),
			upgrade.getName(),
			upgrade.getUpgradeType().name(),
			upgrade.getTargetValue(),
			upgrade.getTool() != null ? upgrade.getTool().getCode() : null,
			upgrade.getGoldCost(),
			upgrade.getPrerequisiteUpgrade() != null ? upgrade.getPrerequisiteUpgrade().getCode() : null,
			resourceCosts
		);
	}

	private UpgradeCostResponse toUpgradeCostResponse(UpgradeResourceCostEntity resourceCost){
		return new UpgradeCostResponse(resourceCost.getResource().getCode(), resourceCost.getQuantity());
	}

	private GameSettingsResponse toSettingsResponse(GameSettingEntity settings){
		return new GameSettingsResponse(
			settings.getStartRegion().getCode(),
			settings.getStartGold(),
			settings.getStartReputation(),
			settings.getDefaultServiceCapacity(),
			settings.getDefaultInventorySlotLimit()
		);
	}

}
