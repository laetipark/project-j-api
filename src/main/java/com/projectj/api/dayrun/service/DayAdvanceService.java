package com.projectj.api.dayrun.service;

import com.projectj.api.catalog.service.CatalogLookupService;
import com.projectj.api.player.domain.PlayerEntity;
import com.projectj.api.player.domain.PlayerPhase;
import com.projectj.api.player.dto.PlayerSnapshotResponse;
import com.projectj.api.player.service.PlayerSnapshotService;
import com.projectj.api.player.service.PlayerSupportService;
import com.projectj.api.player.repository.PlayerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DayAdvanceService{

	private final PlayerSupportService playerSupportService;
	private final CatalogLookupService catalogLookupService;
	private final DayRunService dayRunService;
	private final PlayerSnapshotService playerSnapshotService;
	private final PlayerRepository playerRepository;

	public DayAdvanceService(
		PlayerSupportService playerSupportService,
		CatalogLookupService catalogLookupService,
		DayRunService dayRunService,
		PlayerSnapshotService playerSnapshotService,
		PlayerRepository playerRepository
	){
		this.playerSupportService = playerSupportService;
		this.catalogLookupService = catalogLookupService;
		this.dayRunService = dayRunService;
		this.playerSnapshotService = playerSnapshotService;
		this.playerRepository = playerRepository;
	}

	@Transactional
	public PlayerSnapshotResponse nextDay(String playerId){
		PlayerEntity player = playerSupportService.getPlayerForUpdate(playerId);
		playerSupportService.requirePhase(player, "The next day can only begin from settlement.", PlayerPhase.SETTLEMENT);
		player.setCurrentDay(player.getCurrentDay() + 1);
		player.setCurrentPhase(PlayerPhase.MORNING_EXPLORE);
		player.setCurrentRegion(catalogLookupService.getGameSettings().getStartRegion());
		player.setSelectedRecipe(null);
		playerRepository.save(player);
		dayRunService.createDayRun(player);
		return playerSnapshotService.buildSnapshot(player);
	}

}
