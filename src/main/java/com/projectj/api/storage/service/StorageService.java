package com.projectj.api.storage.service;

import com.projectj.api.catalog.domain.ResourceEntity;
import com.projectj.api.catalog.service.CatalogLookupService;
import com.projectj.api.common.exception.BusinessException;
import com.projectj.api.common.exception.ErrorCode;
import com.projectj.api.player.domain.PlayerEntity;
import com.projectj.api.player.dto.PlayerSnapshotResponse;
import com.projectj.api.player.service.PlayerResourceService;
import com.projectj.api.player.service.PlayerSnapshotService;
import com.projectj.api.player.service.PlayerSupportService;
import com.projectj.api.storage.domain.StorageActionType;
import com.projectj.api.storage.domain.StorageLogEntity;
import com.projectj.api.storage.dto.StorageTransferRequest;
import com.projectj.api.storage.repository.StorageLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StorageService{

	private final CatalogLookupService catalogLookupService;
	private final PlayerSupportService playerSupportService;
	private final PlayerResourceService playerResourceService;
	private final PlayerSnapshotService playerSnapshotService;
	private final StorageLogRepository storageLogRepository;

	public StorageService(
		CatalogLookupService catalogLookupService,
		PlayerSupportService playerSupportService,
		PlayerResourceService playerResourceService,
		PlayerSnapshotService playerSnapshotService,
		StorageLogRepository storageLogRepository
	){
		this.catalogLookupService = catalogLookupService;
		this.playerSupportService = playerSupportService;
		this.playerResourceService = playerResourceService;
		this.playerSnapshotService = playerSnapshotService;
		this.storageLogRepository = storageLogRepository;
	}

	@Transactional
	public PlayerSnapshotResponse deposit(String playerId, StorageTransferRequest request){
		PlayerEntity player = playerSupportService.getPlayerForUpdate(playerId);
		ensureHubStorage(player);
		ResourceEntity resource = catalogLookupService.getResourceByCode(request.resourceCode());
		playerResourceService.removeInventory(player, resource, request.quantity());
		playerResourceService.addStorage(player, resource, request.quantity());
		saveStorageLog(player, resource, StorageActionType.DEPOSIT, request.quantity());
		return playerSnapshotService.buildSnapshot(player);
	}

	@Transactional
	public PlayerSnapshotResponse withdraw(String playerId, StorageTransferRequest request){
		PlayerEntity player = playerSupportService.getPlayerForUpdate(playerId);
		ensureHubStorage(player);
		ResourceEntity resource = catalogLookupService.getResourceByCode(request.resourceCode());
		playerResourceService.removeStorage(player, resource, request.quantity());
		playerResourceService.addInventory(player, resource, request.quantity());
		saveStorageLog(player, resource, StorageActionType.WITHDRAW, request.quantity());
		return playerSnapshotService.buildSnapshot(player);
	}

	private void ensureHubStorage(PlayerEntity player){
		if(!playerSupportService.isInHub(player)){
			throw new BusinessException(ErrorCode.STORAGE_ONLY_IN_HUB, "Storage is only available in Hub.");
		}
	}

	private void saveStorageLog(PlayerEntity player, ResourceEntity resource, StorageActionType actionType, int quantity){
		StorageLogEntity storageLog = new StorageLogEntity();
		storageLog.setPlayer(player);
		storageLog.setResource(resource);
		storageLog.setActionType(actionType);
		storageLog.setQuantity(quantity);
		storageLogRepository.save(storageLog);
	}

}
