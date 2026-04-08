package com.projectj.api.catalog.service;

import com.projectj.api.catalog.domain.GameSettingEntity;
import com.projectj.api.catalog.domain.IngredientEntity;
import com.projectj.api.catalog.domain.PortalRuleEntity;
import com.projectj.api.catalog.domain.RegionEntity;
import com.projectj.api.catalog.domain.ResourceEntity;
import com.projectj.api.catalog.domain.ResourceGatherRuleEntity;
import com.projectj.api.catalog.domain.ToolEntity;
import com.projectj.api.catalog.domain.UpgradeEntity;
import com.projectj.api.catalog.domain.UpgradeResourceCostEntity;
import com.projectj.api.catalog.repository.GameSettingRepository;
import com.projectj.api.catalog.repository.IngredientRepository;
import com.projectj.api.catalog.repository.PortalRuleRepository;
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
	private final IngredientRepository ingredientRepository;
	private final ToolRepository toolRepository;
	private final RegionRepository regionRepository;
	private final ResourceRepository resourceRepository;
	private final ResourceGatherRuleRepository resourceGatherRuleRepository;
	private final PortalRuleRepository portalRuleRepository;
	private final UpgradeRepository upgradeRepository;
	private final UpgradeResourceCostRepository upgradeResourceCostRepository;

	public CatalogLookupService(
		GameSettingRepository gameSettingRepository,
		IngredientRepository ingredientRepository,
		ToolRepository toolRepository,
		RegionRepository regionRepository,
		ResourceRepository resourceRepository,
		ResourceGatherRuleRepository resourceGatherRuleRepository,
		PortalRuleRepository portalRuleRepository,
		UpgradeRepository upgradeRepository,
		UpgradeResourceCostRepository upgradeResourceCostRepository
	){
		this.gameSettingRepository = gameSettingRepository;
		this.ingredientRepository = ingredientRepository;
		this.toolRepository = toolRepository;
		this.regionRepository = regionRepository;
		this.resourceRepository = resourceRepository;
		this.resourceGatherRuleRepository = resourceGatherRuleRepository;
		this.portalRuleRepository = portalRuleRepository;
		this.upgradeRepository = upgradeRepository;
		this.upgradeResourceCostRepository = upgradeResourceCostRepository;
	}

	public GameSettingEntity getGameSettings(){
		return gameSettingRepository.findTopByDeletedAtIsNullOrderByIdAsc()
			.orElseThrow(() -> new BusinessException(ErrorCode.GAME_SETTINGS_NOT_FOUND, "Game settings were not found."));
	}

	public List<ToolEntity> getActiveTools(){
		return toolRepository.findAllByActiveTrueAndDeletedAtIsNullOrderByIdAsc();
	}

	public List<IngredientEntity> getActiveIngredients(){
		return ingredientRepository.findAllByActiveTrueAndDeletedAtIsNullOrderByIdAsc();
	}

	public List<RegionEntity> getActiveRegions(){
		return regionRepository.findAllByActiveTrueAndDeletedAtIsNullOrderByIdAsc();
	}

	public List<ResourceEntity> getActiveResources(){
		return resourceRepository.findAllByActiveTrueAndDeletedAtIsNullOrderByIdAsc();
	}

	public List<PortalRuleEntity> getActivePortalRules(){
		return portalRuleRepository.findAllByActiveTrueAndDeletedAtIsNullOrderByIdAsc();
	}

	public List<UpgradeEntity> getActiveUpgrades(){
		return upgradeRepository.findAllByActiveTrueAndDeletedAtIsNullOrderByIdAsc();
	}

	public List<UpgradeResourceCostEntity> getUpgradeCosts(Long upgradeId){
		return upgradeResourceCostRepository.findByUpgradeIdAndDeletedAtIsNullOrderByIdAsc(upgradeId);
	}

	public RegionEntity getRegionByCode(String code){
		return regionRepository.findByCodeAndActiveTrueAndDeletedAtIsNull(code)
			.orElseThrow(() -> new BusinessException(ErrorCode.REGION_NOT_FOUND, "Region was not found: " + code));
	}

	public ResourceEntity getResourceByCode(String code){
		return resourceRepository.findByCodeAndActiveTrueAndDeletedAtIsNull(code)
			.orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Resource was not found: " + code));
	}

	public ResourceEntity getResourceByName(String name){
		return resourceRepository.findByNameAndActiveTrueAndDeletedAtIsNull(name)
			.orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Resource was not found: " + name));
	}

	public IngredientEntity getIngredientByName(String name){
		return ingredientRepository.findByIngredientNameAndActiveTrueAndDeletedAtIsNull(name)
			.orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Ingredient was not found: " + name));
	}

	public ToolEntity getToolByCode(String code){
		return toolRepository.findByCodeAndActiveTrueAndDeletedAtIsNull(code)
			.orElseThrow(() -> new BusinessException(ErrorCode.TOOL_NOT_FOUND, "Tool was not found: " + code));
	}

	public PortalRuleEntity getPortalRuleByCode(String code){
		return portalRuleRepository.findByCodeAndActiveTrueAndDeletedAtIsNull(code)
			.orElseThrow(() -> new BusinessException(ErrorCode.PORTAL_RULE_NOT_FOUND, "Portal rule was not found: " + code));
	}

	public UpgradeEntity getUpgradeByCode(String code){
		return upgradeRepository.findByCodeAndActiveTrueAndDeletedAtIsNull(code)
			.orElseThrow(() -> new BusinessException(ErrorCode.UPGRADE_NOT_FOUND, "Upgrade was not found: " + code));
	}

	public Optional<ResourceGatherRuleEntity> findGatherRule(String regionCode, String resourceCode){
		return resourceGatherRuleRepository.findActiveRule(regionCode, resourceCode);
	}

	public List<ToolEntity> getDefaultUnlockedTools(){
		return toolRepository.findByDefaultUnlockedTrueAndActiveTrueAndDeletedAtIsNullOrderByIdAsc();
	}

}
