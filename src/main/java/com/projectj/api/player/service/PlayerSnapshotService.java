package com.projectj.api.player.service;

import com.projectj.api.dayrun.domain.DayRunEntity;
import com.projectj.api.dayrun.dto.DayRunSummaryResponse;
import com.projectj.api.dayrun.service.DayRunService;
import com.projectj.api.player.domain.PlayerEntity;
import com.projectj.api.player.dto.PlayerSnapshotResponse;
import com.projectj.api.player.dto.ResourceAmountResponse;
import com.projectj.api.upgrade.service.UpgradeAvailabilityService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlayerSnapshotService{

	private final PlayerSupportService playerSupportService;
	private final PlayerResourceService playerResourceService;
	private final UpgradeAvailabilityService upgradeAvailabilityService;
	private final DayRunService dayRunService;

	public PlayerSnapshotService(
		PlayerSupportService playerSupportService,
		PlayerResourceService playerResourceService,
		UpgradeAvailabilityService upgradeAvailabilityService,
		DayRunService dayRunService
	){
		this.playerSupportService = playerSupportService;
		this.playerResourceService = playerResourceService;
		this.upgradeAvailabilityService = upgradeAvailabilityService;
		this.dayRunService = dayRunService;
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
		DayRunSummaryResponse currentDayRun = toDayRunSummary(dayRunService.getCurrentDayRun(player));
		DayRunSummaryResponse lastSettlementSummary = toDayRunSummary(dayRunService.resolveLastSettlement(player));
		return new PlayerSnapshotResponse(
			player.getPublicId(),
			player.getDisplayName(),
			player.getCurrentDay(),
			player.getCurrentPhase().getCode(),
			player.getCurrentRegion().getCode(),
			player.getGold(),
			player.getReputation(),
			player.getServiceCapacity(),
			player.getInventorySlotLimit(),
			player.getSelectedRecipe() != null ? player.getSelectedRecipe().getCode() : null,
			inventoryResources,
			storageResources,
			unlockedTools,
			upgradeAvailabilityService.getAvailableUpgrades(player),
			currentDayRun,
			lastSettlementSummary
		);
	}

	private DayRunSummaryResponse toDayRunSummary(DayRunEntity dayRun){
		if(dayRun == null){
			return null;
		}
		return new DayRunSummaryResponse(
			dayRun.getDayNumber(),
			dayRun.getSelectedRecipe() != null ? dayRun.getSelectedRecipe().getCode() : null,
			dayRun.getGatherSuccessCount(),
			dayRun.getGatherFailureCount(),
			dayRun.getTotalGatheredQuantity(),
			dayRun.isServiceSkipped(),
			dayRun.getSoldCount(),
			dayRun.getEarnedGold(),
			dayRun.getEarnedReputation()
		);
	}

}
