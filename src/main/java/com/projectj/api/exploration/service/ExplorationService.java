package com.projectj.api.exploration.service;

import com.projectj.api.catalog.domain.PortalRuleEntity;
import com.projectj.api.catalog.domain.RegionEntity;
import com.projectj.api.catalog.domain.ResourceEntity;
import com.projectj.api.catalog.domain.ResourceGatherRuleEntity;
import com.projectj.api.catalog.service.CatalogLookupService;
import com.projectj.api.common.exception.BusinessException;
import com.projectj.api.common.exception.ErrorCode;
import com.projectj.api.dayrun.domain.DayRunEntity;
import com.projectj.api.dayrun.service.DayRunService;
import com.projectj.api.exploration.domain.GatherLogEntity;
import com.projectj.api.exploration.dto.GatherRequest;
import com.projectj.api.exploration.dto.GatherResponse;
import com.projectj.api.exploration.dto.TravelRequest;
import com.projectj.api.exploration.repository.GatherLogRepository;
import com.projectj.api.player.domain.PlayerEntity;
import com.projectj.api.player.domain.PlayerPhase;
import com.projectj.api.player.dto.PlayerSnapshotResponse;
import com.projectj.api.player.service.PlayerResourceService;
import com.projectj.api.player.service.PlayerSnapshotService;
import com.projectj.api.player.service.PlayerSupportService;
import com.projectj.api.player.repository.PlayerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExplorationService{

	private final CatalogLookupService catalogLookupService;
	private final PlayerSupportService playerSupportService;
	private final PlayerResourceService playerResourceService;
	private final PlayerSnapshotService playerSnapshotService;
	private final DayRunService dayRunService;
	private final GatherLogRepository gatherLogRepository;
	private final PlayerRepository playerRepository;

	public ExplorationService(
		CatalogLookupService catalogLookupService,
		PlayerSupportService playerSupportService,
		PlayerResourceService playerResourceService,
		PlayerSnapshotService playerSnapshotService,
		DayRunService dayRunService,
		GatherLogRepository gatherLogRepository,
		PlayerRepository playerRepository
	){
		this.catalogLookupService = catalogLookupService;
		this.playerSupportService = playerSupportService;
		this.playerResourceService = playerResourceService;
		this.playerSnapshotService = playerSnapshotService;
		this.dayRunService = dayRunService;
		this.gatherLogRepository = gatherLogRepository;
		this.playerRepository = playerRepository;
	}

	@Transactional
	public PlayerSnapshotResponse travel(String playerId, TravelRequest request){
		PlayerEntity player = playerSupportService.getPlayerForUpdate(playerId);
		PortalRuleEntity portalRule = catalogLookupService.getPortalRuleByCode(request.portalCode());
		playerSupportService.requirePhase(player, "This portal is not available in the current phase.", portalRule.getRequiredPhase());
		if(!player.getCurrentRegion().getCode().equals(portalRule.getFromRegion().getCode())){
			throw new BusinessException(ErrorCode.PORTAL_NOT_ACCESSIBLE, "The player is not at the portal origin region.");
		}
		if(portalRule.getRequiredTool() != null && !playerResourceService.hasTool(player, portalRule.getRequiredTool())){
			throw new BusinessException(ErrorCode.TOOL_REQUIRED, "Required tool is not unlocked: " + portalRule.getRequiredTool().getCode());
		}
		if(player.getReputation() < portalRule.getRequiredReputation()){
			throw new BusinessException(ErrorCode.REPUTATION_REQUIRED, "Required reputation is not met.");
		}
		player.setCurrentRegion(portalRule.getToRegion());
		playerRepository.save(player);
		return playerSnapshotService.buildSnapshot(player);
	}

	@Transactional
	public GatherResponse gather(String playerId, GatherRequest request){
		PlayerEntity player = playerSupportService.getPlayerForUpdate(playerId);
		RegionEntity requestedRegion = catalogLookupService.getRegionByCode(request.regionCode());
		ResourceEntity requestedResource = catalogLookupService.getResourceByCode(request.resourceCode());
		DayRunEntity dayRun = dayRunService.getCurrentDayRun(player);

		if(player.getCurrentPhase() != PlayerPhase.MORNING_EXPLORE){
			return failGather(player, dayRun, requestedRegion, requestedResource, request, "Gathering is only available during morning exploration.");
		}
		if(!player.getCurrentRegion().getCode().equals(request.regionCode())){
			return failGather(player, dayRun, requestedRegion, requestedResource, request, "The requested region does not match the player's current region.");
		}

		ResourceGatherRuleEntity gatherRule = catalogLookupService.findGatherRule(request.regionCode(), request.resourceCode()).orElse(null);
		if(gatherRule == null){
			return failGather(player, dayRun, requestedRegion, requestedResource, request, "The resource cannot be gathered in this region.");
		}
		if(gatherRule.getRequiredTool() != null && !playerResourceService.hasTool(player, gatherRule.getRequiredTool())){
			return failGather(player, dayRun, requestedRegion, requestedResource, request, "The required tool is not unlocked.");
		}

		try{
			playerResourceService.addInventory(player, requestedResource, request.quantity());
			saveGatherLog(player, dayRun, requestedRegion, requestedResource, request.quantity(), request.quantity(), true, null);
			dayRunService.recordGatherResult(dayRun, true, request.quantity());
			return new GatherResponse(
				true,
				"Gather succeeded.",
				request.regionCode(),
				request.resourceCode(),
				request.quantity(),
				request.quantity(),
				playerSnapshotService.buildSnapshot(player)
			);
		}catch(BusinessException exception){
			return failGather(player, dayRun, requestedRegion, requestedResource, request, exception.getMessage());
		}
	}

	@Transactional
	public PlayerSnapshotResponse skipExploration(String playerId){
		PlayerEntity player = playerSupportService.getPlayerForUpdate(playerId);
		playerSupportService.requirePhase(player, "Exploration can only be skipped from morning exploration.", PlayerPhase.MORNING_EXPLORE);
		playerSupportService.requireInHub(player, "Return to Hub before ending exploration.");
		player.setCurrentPhase(PlayerPhase.AFTERNOON_SERVICE);
		playerRepository.save(player);
		return playerSnapshotService.buildSnapshot(player);
	}

	private GatherResponse failGather(
		PlayerEntity player,
		DayRunEntity dayRun,
		RegionEntity requestedRegion,
		ResourceEntity requestedResource,
		GatherRequest request,
		String message
	){
		saveGatherLog(player, dayRun, requestedRegion, requestedResource, request.quantity(), 0, false, message);
		dayRunService.recordGatherResult(dayRun, false, 0);
		return new GatherResponse(
			false,
			message,
			request.regionCode(),
			request.resourceCode(),
			request.quantity(),
			0,
			playerSnapshotService.buildSnapshot(player)
		);
	}

	private void saveGatherLog(
		PlayerEntity player,
		DayRunEntity dayRun,
		RegionEntity region,
		ResourceEntity resource,
		int requestedQuantity,
		int grantedQuantity,
		boolean success,
		String failureReason
	){
		GatherLogEntity gatherLog = new GatherLogEntity();
		gatherLog.setPlayer(player);
		gatherLog.setDayRun(dayRun);
		gatherLog.setRegion(region);
		gatherLog.setResource(resource);
		gatherLog.setQuantityRequested(requestedQuantity);
		gatherLog.setQuantityGranted(grantedQuantity);
		gatherLog.setSuccess(success);
		gatherLog.setFailureReason(failureReason);
		gatherLogRepository.save(gatherLog);
	}

}
