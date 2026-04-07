package com.projectj.api.catalog.service;

import com.projectj.api.catalog.domain.GameSettingEntity;
import com.projectj.api.catalog.domain.PortalRuleEntity;
import com.projectj.api.catalog.domain.RecipeEntity;
import com.projectj.api.catalog.domain.RecipeIngredientEntity;
import com.projectj.api.catalog.domain.RegionEntity;
import com.projectj.api.catalog.domain.ResourceEntity;
import com.projectj.api.catalog.domain.ResourceGatherRuleEntity;
import com.projectj.api.catalog.domain.ToolEntity;
import com.projectj.api.catalog.domain.UpgradeEntity;
import com.projectj.api.catalog.domain.UpgradeResourceCostEntity;
import com.projectj.api.catalog.repository.GameSettingRepository;
import com.projectj.api.catalog.repository.PortalRuleRepository;
import com.projectj.api.catalog.repository.RecipeIngredientRepository;
import com.projectj.api.catalog.repository.RecipeRepository;
import com.projectj.api.catalog.repository.RegionRepository;
import com.projectj.api.catalog.repository.ResourceGatherRuleRepository;
import com.projectj.api.catalog.repository.ResourceRepository;
import com.projectj.api.catalog.repository.ToolRepository;
import com.projectj.api.catalog.repository.UpgradeRepository;
import com.projectj.api.catalog.repository.UpgradeResourceCostRepository;
import com.projectj.api.common.exception.BusinessException;
import com.projectj.api.common.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CatalogLookupService{

	private final GameSettingRepository gameSettingRepository;
	private final ToolRepository toolRepository;
	private final RegionRepository regionRepository;
	private final ResourceRepository resourceRepository;
	private final ResourceGatherRuleRepository resourceGatherRuleRepository;
	private final RecipeRepository recipeRepository;
	private final RecipeIngredientRepository recipeIngredientRepository;
	private final PortalRuleRepository portalRuleRepository;
	private final UpgradeRepository upgradeRepository;
	private final UpgradeResourceCostRepository upgradeResourceCostRepository;

	public CatalogLookupService(
		GameSettingRepository gameSettingRepository,
		ToolRepository toolRepository,
		RegionRepository regionRepository,
		ResourceRepository resourceRepository,
		ResourceGatherRuleRepository resourceGatherRuleRepository,
		RecipeRepository recipeRepository,
		RecipeIngredientRepository recipeIngredientRepository,
		PortalRuleRepository portalRuleRepository,
		UpgradeRepository upgradeRepository,
		UpgradeResourceCostRepository upgradeResourceCostRepository
	){
		this.gameSettingRepository = gameSettingRepository;
		this.toolRepository = toolRepository;
		this.regionRepository = regionRepository;
		this.resourceRepository = resourceRepository;
		this.resourceGatherRuleRepository = resourceGatherRuleRepository;
		this.recipeRepository = recipeRepository;
		this.recipeIngredientRepository = recipeIngredientRepository;
		this.portalRuleRepository = portalRuleRepository;
		this.upgradeRepository = upgradeRepository;
		this.upgradeResourceCostRepository = upgradeResourceCostRepository;
	}

	public GameSettingEntity getGameSettings(){
		return gameSettingRepository.findTopByOrderByIdAsc()
			.orElseThrow(() -> new BusinessException(ErrorCode.GAME_SETTINGS_NOT_FOUND, "Game settings were not found."));
	}

	public List<ToolEntity> getActiveTools(){
		return toolRepository.findAllByActiveTrueOrderByIdAsc();
	}

	public List<RegionEntity> getActiveRegions(){
		return regionRepository.findAllByActiveTrueOrderByIdAsc();
	}

	public List<ResourceEntity> getActiveResources(){
		return resourceRepository.findAllByActiveTrueOrderByIdAsc();
	}

	public List<PortalRuleEntity> getActivePortalRules(){
		return portalRuleRepository.findAllByActiveTrueOrderByIdAsc();
	}

	public List<RecipeEntity> getActiveRecipes(){
		return recipeRepository.findAllByActiveTrueOrderByIdAsc();
	}

	public List<UpgradeEntity> getActiveUpgrades(){
		return upgradeRepository.findAllByActiveTrueOrderByIdAsc();
	}

	public List<RecipeIngredientEntity> getRecipeIngredients(Long recipeId){
		return recipeIngredientRepository.findByRecipeIdOrderByIdAsc(recipeId);
	}

	public List<UpgradeResourceCostEntity> getUpgradeCosts(Long upgradeId){
		return upgradeResourceCostRepository.findByUpgradeIdOrderByIdAsc(upgradeId);
	}

	public RegionEntity getRegionByCode(String code){
		return regionRepository.findByCode(code)
			.orElseThrow(() -> new BusinessException(ErrorCode.REGION_NOT_FOUND, "Region was not found: " + code));
	}

	public ResourceEntity getResourceByCode(String code){
		return resourceRepository.findByCode(code)
			.orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Resource was not found: " + code));
	}

	public ToolEntity getToolByCode(String code){
		return toolRepository.findByCode(code)
			.orElseThrow(() -> new BusinessException(ErrorCode.TOOL_NOT_FOUND, "Tool was not found: " + code));
	}

	public RecipeEntity getRecipeByCode(String code){
		return recipeRepository.findByCode(code)
			.orElseThrow(() -> new BusinessException(ErrorCode.RECIPE_NOT_FOUND, "Recipe was not found: " + code));
	}

	public PortalRuleEntity getPortalRuleByCode(String code){
		return portalRuleRepository.findByCodeAndActiveTrue(code)
			.orElseThrow(() -> new BusinessException(ErrorCode.PORTAL_RULE_NOT_FOUND, "Portal rule was not found: " + code));
	}

	public UpgradeEntity getUpgradeByCode(String code){
		return upgradeRepository.findByCodeAndActiveTrue(code)
			.orElseThrow(() -> new BusinessException(ErrorCode.UPGRADE_NOT_FOUND, "Upgrade was not found: " + code));
	}

	public Optional<ResourceGatherRuleEntity> findGatherRule(String regionCode, String resourceCode){
		return resourceGatherRuleRepository.findActiveRule(regionCode, resourceCode);
	}

	public List<ToolEntity> getDefaultUnlockedTools(){
		return toolRepository.findByDefaultUnlockedTrueAndActiveTrueOrderByIdAsc();
	}

}
