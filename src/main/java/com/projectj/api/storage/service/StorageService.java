package com.projectj.api.storage.service;

import com.projectj.api.catalog.service.CatalogLookupService;
import com.projectj.api.common.exception.BusinessException;
import com.projectj.api.common.exception.ErrorCode;
import com.projectj.api.player.domain.PlayerEntity;
import com.projectj.api.player.dto.PlayerSnapshotResponse;
import com.projectj.api.player.service.PlayerResourceService;
import com.projectj.api.player.service.PlayerSnapshotService;
import com.projectj.api.player.service.PlayerSupportService;
import com.projectj.api.storage.dto.StorageTransferRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StorageService{

	private final CatalogLookupService catalogLookupService;
	private final PlayerSupportService playerSupportService;
	private final PlayerResourceService playerResourceService;
	private final PlayerSnapshotService playerSnapshotService;

	public StorageService(
		CatalogLookupService catalogLookupService,
		PlayerSupportService playerSupportService,
		PlayerResourceService playerResourceService,
		PlayerSnapshotService playerSnapshotService
	){
		this.catalogLookupService = catalogLookupService;
		this.playerSupportService = playerSupportService;
		this.playerResourceService = playerResourceService;
		this.playerSnapshotService = playerSnapshotService;
	}

	@Transactional
	public PlayerSnapshotResponse deposit(String playerId, StorageTransferRequest request){
		PlayerEntity player = playerSupportService.getPlayerForUpdate(playerId);
		ensureHubStorage(player);
		var resource = catalogLookupService.getResourceByCode(request.resourceCode());
		playerResourceService.removeInventory(player, resource, request.quantity());
		playerResourceService.addStorage(player, resource, request.quantity());
		return playerSnapshotService.buildSnapshot(player);
	}

	@Transactional
	public PlayerSnapshotResponse withdraw(String playerId, StorageTransferRequest request){
		PlayerEntity player = playerSupportService.getPlayerForUpdate(playerId);
		ensureHubStorage(player);
		var resource = catalogLookupService.getResourceByCode(request.resourceCode());
		playerResourceService.removeStorage(player, resource, request.quantity());
		playerResourceService.addInventory(player, resource, request.quantity());
		return playerSnapshotService.buildSnapshot(player);
	}

	private void ensureHubStorage(PlayerEntity player){
		if(!playerSupportService.isInHub(player)){
			throw new BusinessException(ErrorCode.STORAGE_ONLY_IN_HUB, "Storage is only available in Hub.");
		}
	}

}
