package com.projectj.api.player.service;

import com.projectj.api.catalog.domain.GameSettingEntity;
import com.projectj.api.catalog.domain.ToolEntity;
import com.projectj.api.catalog.service.CatalogLookupService;
import com.projectj.api.common.domain.EconomyLogEntity;
import com.projectj.api.common.domain.EconomyLogType;
import com.projectj.api.common.repository.EconomyLogRepository;
import com.projectj.api.dayrun.service.DayRunService;
import com.projectj.api.player.domain.PlayerEntity;
import com.projectj.api.player.domain.PlayerPhase;
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
	private final DayRunService dayRunService;
	private final PlayerSnapshotService playerSnapshotService;
	private final EconomyLogRepository economyLogRepository;

	public PlayerLifecycleService(
		CatalogLookupService catalogLookupService,
		PlayerRepository playerRepository,
		PlayerResourceService playerResourceService,
		DayRunService dayRunService,
		PlayerSnapshotService playerSnapshotService,
		EconomyLogRepository economyLogRepository
	){
		this.catalogLookupService = catalogLookupService;
		this.playerRepository = playerRepository;
		this.playerResourceService = playerResourceService;
		this.dayRunService = dayRunService;
		this.playerSnapshotService = playerSnapshotService;
		this.economyLogRepository = economyLogRepository;
	}

	@Transactional
	public CreatePlayerResponse createPlayer(CreatePlayerRequest request){
		GameSettingEntity settings = catalogLookupService.getGameSettings();
		PlayerEntity player = new PlayerEntity();
		player.setPublicId(UUID.randomUUID().toString());
		player.setDisplayName(request != null ? request.displayName() : null);
		player.setCurrentDay(settings.getStartDay());
		player.setCurrentPhase(PlayerPhase.MORNING_EXPLORE);
		player.setCurrentRegion(settings.getStartRegion());
		player.setGold(settings.getStartGold());
		player.setReputation(settings.getStartReputation());
		player.setServiceCapacity(settings.getDefaultServiceCapacity());
		player.setInventorySlotLimit(settings.getDefaultInventorySlotLimit());
		player.setSelectedRecipe(null);
		player = playerRepository.save(player);

		for(ToolEntity tool : catalogLookupService.getDefaultUnlockedTools()){
			playerResourceService.unlockTool(player, tool);
		}
		dayRunService.createDayRun(player);

		EconomyLogEntity economyLog = new EconomyLogEntity();
		economyLog.setPlayer(player);
		economyLog.setLogType(EconomyLogType.PLAYER_CREATE);
		economyLog.setGoldDelta(0);
		economyLog.setReasonCode("PLAYER_CREATE");
		economyLog.setNote("Player initialized.");
		economyLogRepository.save(economyLog);

		return new CreatePlayerResponse(player.getPublicId(), playerSnapshotService.buildSnapshot(player));
	}

}
