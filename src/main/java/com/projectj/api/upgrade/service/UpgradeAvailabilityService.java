package com.projectj.api.upgrade.service;

import com.projectj.api.catalog.domain.UpgradeEntity;
import com.projectj.api.catalog.domain.UpgradeResourceCostEntity;
import com.projectj.api.catalog.service.CatalogLookupService;
import com.projectj.api.player.domain.PlayerEntity;
import com.projectj.api.player.service.PlayerResourceService;
import com.projectj.api.player.service.PlayerSupportService;
import com.projectj.api.player.repository.PlayerUpgradePurchaseRepository;
import com.projectj.api.upgrade.domain.UpgradeType;
import com.projectj.api.upgrade.dto.AvailableUpgradeResponse;
import com.projectj.api.upgrade.dto.MissingResourceResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class UpgradeAvailabilityService{

	private final CatalogLookupService catalogLookupService;
	private final PlayerResourceService playerResourceService;
	private final PlayerUpgradePurchaseRepository playerUpgradePurchaseRepository;

	public UpgradeAvailabilityService(
		CatalogLookupService catalogLookupService,
		PlayerResourceService playerResourceService,
		PlayerUpgradePurchaseRepository playerUpgradePurchaseRepository
	){
		this.catalogLookupService = catalogLookupService;
		this.playerResourceService = playerResourceService;
		this.playerUpgradePurchaseRepository = playerUpgradePurchaseRepository;
	}

	public List<AvailableUpgradeResponse> getAvailableUpgrades(PlayerEntity player){
		Set<Long> purchasedUpgradeIds = Set.copyOf(playerUpgradePurchaseRepository.findUpgradeIdsByPlayerId(player.getId()));
		Map<String, Integer> combinedResources = playerResourceService.getCombinedQuantityMap(player);
		List<String> unlockedTools = playerResourceService.getUnlockedTools(player)
			.stream()
			.map(playerTool -> playerTool.getTool().getCode())
			.toList();

		List<AvailableUpgradeResponse> responses = new ArrayList<>();
		for(UpgradeEntity upgrade : catalogLookupService.getActiveUpgrades()){
			if(purchasedUpgradeIds.contains(upgrade.getId())){
				continue;
			}
			if(shouldHideAppliedUpgrade(player, upgrade, unlockedTools)){
				continue;
			}
			responses.add(toAvailability(player, upgrade, purchasedUpgradeIds, combinedResources));
		}
		return responses;
	}

	public AvailableUpgradeResponse getAvailability(PlayerEntity player, UpgradeEntity upgrade){
		Set<Long> purchasedUpgradeIds = Set.copyOf(playerUpgradePurchaseRepository.findUpgradeIdsByPlayerId(player.getId()));
		Map<String, Integer> combinedResources = playerResourceService.getCombinedQuantityMap(player);
		return toAvailability(player, upgrade, purchasedUpgradeIds, combinedResources);
	}

	private boolean shouldHideAppliedUpgrade(PlayerEntity player, UpgradeEntity upgrade, List<String> unlockedTools){
		if(upgrade.getUpgradeType() == UpgradeType.INVENTORY_SLOT && upgrade.getTargetValue() != null){
			return player.getInventorySlotLimit() >= upgrade.getTargetValue();
		}
		if(upgrade.getUpgradeType() == UpgradeType.TOOL_UNLOCK && upgrade.getTool() != null){
			return unlockedTools.contains(upgrade.getTool().getCode());
		}
		return false;
	}

	private AvailableUpgradeResponse toAvailability(
		PlayerEntity player,
		UpgradeEntity upgrade,
		Set<Long> purchasedUpgradeIds,
		Map<String, Integer> combinedResources
	){
		List<String> blockedReasons = new ArrayList<>();
		if(upgrade.getPrerequisiteUpgrade() != null && !purchasedUpgradeIds.contains(upgrade.getPrerequisiteUpgrade().getId())){
			blockedReasons.add("prerequisite_not_met");
		}
		if(!PlayerSupportService.HUB_REGION_CODE.equals(player.getCurrentRegion().getCode())){
			blockedReasons.add("not_in_hub");
		}

		int missingGold = Math.max(0, upgrade.getGoldCost() - player.getGold());
		List<MissingResourceResponse> missingResources = new ArrayList<>();
		for(UpgradeResourceCostEntity resourceCost : catalogLookupService.getUpgradeCosts(upgrade.getId())){
			int ownedQuantity = combinedResources.getOrDefault(resourceCost.getResource().getCode(), 0);
			int missingQuantity = Math.max(0, resourceCost.getQuantity() - ownedQuantity);
			if(missingQuantity > 0){
				missingResources.add(
					new MissingResourceResponse(
						resourceCost.getResource().getCode(),
						resourceCost.getQuantity(),
						ownedQuantity,
						missingQuantity
					)
				);
			}
		}

		boolean purchasable = blockedReasons.isEmpty() && missingGold == 0 && missingResources.isEmpty();
		return new AvailableUpgradeResponse(
			upgrade.getCode(),
			purchasable,
			upgrade.getGoldCost(),
			missingGold,
			missingResources,
			blockedReasons
		);
	}

}
