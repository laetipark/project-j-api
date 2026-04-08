package com.projectj.api.exploration.service;

import com.projectj.api.catalog.domain.PortalRuleEntity;
import com.projectj.api.catalog.domain.RegionEntity;
import com.projectj.api.catalog.domain.ResourceEntity;
import com.projectj.api.catalog.domain.ResourceGatherRuleEntity;
import com.projectj.api.catalog.service.CatalogLookupService;
import com.projectj.api.common.exception.BusinessException;
import com.projectj.api.common.exception.ErrorCode;
import com.projectj.api.exploration.dto.GatherRequest;
import com.projectj.api.exploration.dto.GatherResponse;
import com.projectj.api.exploration.dto.TravelRequest;
import com.projectj.api.player.domain.PlayerEntity;
import com.projectj.api.player.dto.PlayerSnapshotResponse;
import com.projectj.api.player.repository.PlayerRepository;
import com.projectj.api.player.service.PlayerResourceService;
import com.projectj.api.player.service.PlayerSnapshotService;
import com.projectj.api.player.service.PlayerSupportService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExplorationService{

	private final CatalogLookupService catalogLookupService;
	private final PlayerSupportService playerSupportService;
	private final PlayerResourceService playerResourceService;
	private final PlayerSnapshotService playerSnapshotService;
	private final PlayerRepository playerRepository;

	public ExplorationService(
		CatalogLookupService catalogLookupService,
		PlayerSupportService playerSupportService,
		PlayerResourceService playerResourceService,
		PlayerSnapshotService playerSnapshotService,
		PlayerRepository playerRepository
	){
		this.catalogLookupService = catalogLookupService;
		this.playerSupportService = playerSupportService;
		this.playerResourceService = playerResourceService;
		this.playerSnapshotService = playerSnapshotService;
		this.playerRepository = playerRepository;
	}

	@Transactional
	public PlayerSnapshotResponse travel(String playerId, TravelRequest request){
		PlayerEntity player = playerSupportService.getPlayerForUpdate(playerId);
		PortalRuleEntity portalRule = catalogLookupService.getPortalRuleByCode(request.portalCode());
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
		if(!player.getCurrentRegion().getCode().equals(request.regionCode())){
			return failGather(player, request, "The requested region does not match the player's current region.");
		}

		ResourceGatherRuleEntity gatherRule = catalogLookupService.findGatherRule(request.regionCode(), request.resourceCode()).orElse(null);
		if(gatherRule == null){
			return failGather(player, request, "The resource cannot be gathered in this region.");
		}
		if(gatherRule.getRequiredTool() != null && !playerResourceService.hasTool(player, gatherRule.getRequiredTool())){
			return failGather(player, request, "The required tool is not unlocked.");
		}

		try{
			playerResourceService.addInventory(player, requestedResource, request.quantity());
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
			return failGather(player, request, exception.getMessage());
		}
	}

	private GatherResponse failGather(
		PlayerEntity player,
		GatherRequest request,
		String message
	){
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

}
