package com.projectj.api.player.service;

import com.projectj.api.catalog.domain.GameSettingEntity;
import com.projectj.api.catalog.domain.ToolEntity;
import com.projectj.api.catalog.service.CatalogLookupService;
import com.projectj.api.player.domain.PlayerEntity;
import com.projectj.api.player.dto.CreatePlayerRequest;
import com.projectj.api.player.dto.CreatePlayerResponse;
import com.projectj.api.player.repository.PlayerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class PlayerLifecycleService{

	private final CatalogLookupService catalogLookupService;
	private final PlayerRepository playerRepository;
	private final PlayerResourceService playerResourceService;
	private final PlayerSnapshotService playerSnapshotService;

	public PlayerLifecycleService(
		CatalogLookupService catalogLookupService,
		PlayerRepository playerRepository,
		PlayerResourceService playerResourceService,
		PlayerSnapshotService playerSnapshotService
	){
		this.catalogLookupService = catalogLookupService;
		this.playerRepository = playerRepository;
		this.playerResourceService = playerResourceService;
		this.playerSnapshotService = playerSnapshotService;
	}

	@Transactional
	public CreatePlayerResponse createPlayer(CreatePlayerRequest request){
		GameSettingEntity settings = catalogLookupService.getGameSettings();
		PlayerEntity player = new PlayerEntity();
		player.setPublicId(UUID.randomUUID().toString());
		player.setDisplayName(request != null ? request.displayName() : null);
		player.setCurrentRegion(settings.getStartRegion());
		player.setGold(settings.getStartGold());
		player.setReputation(settings.getStartReputation());
		player.setServiceCapacity(settings.getDefaultServiceCapacity());
		player.setInventorySlotLimit(settings.getDefaultInventorySlotLimit());
		player.setSelectedRecipeId(null);
		player = playerRepository.save(player);

		for(ToolEntity tool : catalogLookupService.getDefaultUnlockedTools()){
			playerResourceService.unlockTool(player, tool);
		}

		return new CreatePlayerResponse(player.getPublicId(), playerSnapshotService.buildSnapshot(player));
	}

}
