package com.projectj.api.upgrade.service;

import com.projectj.api.catalog.domain.UpgradeEntity;
import com.projectj.api.catalog.domain.UpgradeResourceCostEntity;
import com.projectj.api.catalog.service.CatalogLookupService;
import com.projectj.api.common.exception.BusinessException;
import com.projectj.api.common.exception.ErrorCode;
import com.projectj.api.player.domain.PlayerEntity;
import com.projectj.api.player.domain.PlayerUpgradePurchaseEntity;
import com.projectj.api.player.repository.PlayerRepository;
import com.projectj.api.player.repository.PlayerUpgradePurchaseRepository;
import com.projectj.api.player.service.PlayerResourceService;
import com.projectj.api.player.service.PlayerSnapshotService;
import com.projectj.api.player.service.PlayerSupportService;
import com.projectj.api.upgrade.domain.UpgradeType;
import com.projectj.api.upgrade.dto.AvailableUpgradeResponse;
import com.projectj.api.upgrade.dto.UpgradePurchaseResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpgradePurchaseService{

	private final CatalogLookupService catalogLookupService;
	private final PlayerSupportService playerSupportService;
	private final PlayerResourceService playerResourceService;
	private final PlayerSnapshotService playerSnapshotService;
	private final UpgradeAvailabilityService upgradeAvailabilityService;
	private final PlayerUpgradePurchaseRepository playerUpgradePurchaseRepository;
	private final PlayerRepository playerRepository;

	public UpgradePurchaseService(
		CatalogLookupService catalogLookupService,
		PlayerSupportService playerSupportService,
		PlayerResourceService playerResourceService,
		PlayerSnapshotService playerSnapshotService,
		UpgradeAvailabilityService upgradeAvailabilityService,
		PlayerUpgradePurchaseRepository playerUpgradePurchaseRepository,
		PlayerRepository playerRepository
	){
		this.catalogLookupService = catalogLookupService;
		this.playerSupportService = playerSupportService;
		this.playerResourceService = playerResourceService;
		this.playerSnapshotService = playerSnapshotService;
		this.upgradeAvailabilityService = upgradeAvailabilityService;
		this.playerUpgradePurchaseRepository = playerUpgradePurchaseRepository;
		this.playerRepository = playerRepository;
	}

	@Transactional
	public UpgradePurchaseResponse purchase(String playerId, String upgradeCode){
		PlayerEntity player = playerSupportService.getPlayerForUpdate(playerId);
		playerSupportService.requireInHub(player, "Upgrades can only be purchased in Hub.");
		UpgradeEntity upgrade = catalogLookupService.getUpgradeByCode(upgradeCode);
		if(playerUpgradePurchaseRepository.existsByPlayer_IdAndUpgrade_IdAndDeletedAtIsNull(player.getId(), upgrade.getId())){
			throw new BusinessException(ErrorCode.UPGRADE_ALREADY_PURCHASED, "Upgrade was already purchased.");
		}

		AvailableUpgradeResponse availability = upgradeAvailabilityService.getAvailability(player, upgrade);
		if(!availability.purchasable()){
			throw new BusinessException(ErrorCode.UPGRADE_NOT_AVAILABLE, "Upgrade is not currently purchasable.");
		}

		player.setGold(player.getGold() - upgrade.getGoldCost());
		for(UpgradeResourceCostEntity resourceCost : catalogLookupService.getUpgradeCosts(upgrade.getId())){
			playerResourceService.consumeCombinedResource(player, resourceCost.getResource(), resourceCost.getQuantity());
		}

		if(upgrade.getUpgradeType() == UpgradeType.INVENTORY_SLOT){
			if(upgrade.getTargetValue() == null){
				throw new BusinessException(ErrorCode.INVALID_REQUEST, "Inventory slot upgrade target is missing.");
			}
			player.setInventorySlotLimit(upgrade.getTargetValue());
		}else if(upgrade.getUpgradeType() == UpgradeType.TOOL_UNLOCK && upgrade.getTool() != null){
			playerResourceService.unlockTool(player, upgrade.getTool());
		}
		playerRepository.save(player);

		PlayerUpgradePurchaseEntity purchase = new PlayerUpgradePurchaseEntity();
		purchase.setPlayer(player);
		purchase.setUpgrade(upgrade);
		playerUpgradePurchaseRepository.save(purchase);

		return new UpgradePurchaseResponse(upgrade.getCode(), playerSnapshotService.buildSnapshot(player));
	}

}
