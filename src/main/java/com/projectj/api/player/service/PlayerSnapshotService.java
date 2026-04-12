package com.projectj.api.player.service;

import com.projectj.api.player.domain.PlayerEntity;
import com.projectj.api.player.dto.PlayerSnapshotResponse;
import com.projectj.api.player.dto.ResourceAmountResponse;
import com.projectj.api.player.repository.PlayerUpgradePurchaseRepository;
import com.projectj.api.upgrade.service.UpgradeAvailabilityService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlayerSnapshotService{

	private final PlayerSupportService playerSupportService;
	private final PlayerResourceService playerResourceService;
	private final UpgradeAvailabilityService upgradeAvailabilityService;
	private final PlayerUpgradePurchaseRepository playerUpgradePurchaseRepository;

	public PlayerSnapshotService(
		PlayerSupportService playerSupportService,
		PlayerResourceService playerResourceService,
		UpgradeAvailabilityService upgradeAvailabilityService,
		PlayerUpgradePurchaseRepository playerUpgradePurchaseRepository
	){
		this.playerSupportService = playerSupportService;
		this.playerResourceService = playerResourceService;
		this.upgradeAvailabilityService = upgradeAvailabilityService;
		this.playerUpgradePurchaseRepository = playerUpgradePurchaseRepository;
	}

	public PlayerSnapshotResponse getSnapshot(String playerId){
		return buildSnapshot(playerSupportService.getPlayer(playerId));
	}

	public PlayerSnapshotResponse buildSnapshot(PlayerEntity player){
		List<ResourceAmountResponse> inventoryResources = playerResourceService.getInventory(player)
			.stream()
			.map(entry -> new ResourceAmountResponse(entry.getResource().getCode(), entry.getQuantity()))
			.toList();
		List<ResourceAmountResponse> storageResources = playerResourceService.getStorage(player)
			.stream()
			.map(entry -> new ResourceAmountResponse(entry.getResource().getCode(), entry.getQuantity()))
			.toList();
		List<String> unlockedTools = playerResourceService.getUnlockedTools(player)
			.stream()
			.map(entry -> entry.getTool().getCode())
			.toList();
		return new PlayerSnapshotResponse(
			player.getPublicId(),
			player.getDisplayName(),
			player.getCurrentRegion().getCode(),
			player.getGold(),
			player.getReputation(),
			player.getServiceCapacity(),
			player.getInventorySlotLimit(),
			player.getSelectedRecipeId(),
			inventoryResources,
			storageResources,
			unlockedTools,
			playerUpgradePurchaseRepository.findUpgradeCodesByPlayerId(player.getId()),
			upgradeAvailabilityService.getAvailableUpgrades(player)
		);
	}

}
