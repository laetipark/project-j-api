package com.projectj.api.restaurant.service;

import com.projectj.api.catalog.domain.RecipeEntity;
import com.projectj.api.catalog.domain.RecipeIngredientEntity;
import com.projectj.api.catalog.service.CatalogLookupService;
import com.projectj.api.common.domain.EconomyLogEntity;
import com.projectj.api.common.domain.EconomyLogType;
import com.projectj.api.common.exception.BusinessException;
import com.projectj.api.common.exception.ErrorCode;
import com.projectj.api.common.repository.EconomyLogRepository;
import com.projectj.api.dayrun.domain.DayRunEntity;
import com.projectj.api.dayrun.service.DayRunService;
import com.projectj.api.player.domain.PlayerEntity;
import com.projectj.api.player.domain.PlayerPhase;
import com.projectj.api.player.dto.PlayerSnapshotResponse;
import com.projectj.api.player.service.PlayerResourceService;
import com.projectj.api.player.service.PlayerSnapshotService;
import com.projectj.api.player.service.PlayerSupportService;
import com.projectj.api.player.repository.PlayerRepository;
import com.projectj.api.restaurant.domain.ServiceLogEntity;
import com.projectj.api.restaurant.dto.SelectRecipeRequest;
import com.projectj.api.restaurant.dto.ServiceRunResponse;
import com.projectj.api.restaurant.repository.ServiceLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class RestaurantService{

	private final CatalogLookupService catalogLookupService;
	private final PlayerSupportService playerSupportService;
	private final PlayerResourceService playerResourceService;
	private final PlayerSnapshotService playerSnapshotService;
	private final DayRunService dayRunService;
	private final ServiceLogRepository serviceLogRepository;
	private final EconomyLogRepository economyLogRepository;
	private final PlayerRepository playerRepository;

	public RestaurantService(
		CatalogLookupService catalogLookupService,
		PlayerSupportService playerSupportService,
		PlayerResourceService playerResourceService,
		PlayerSnapshotService playerSnapshotService,
		DayRunService dayRunService,
		ServiceLogRepository serviceLogRepository,
		EconomyLogRepository economyLogRepository,
		PlayerRepository playerRepository
	){
		this.catalogLookupService = catalogLookupService;
		this.playerSupportService = playerSupportService;
		this.playerResourceService = playerResourceService;
		this.playerSnapshotService = playerSnapshotService;
		this.dayRunService = dayRunService;
		this.serviceLogRepository = serviceLogRepository;
		this.economyLogRepository = economyLogRepository;
		this.playerRepository = playerRepository;
	}

	@Transactional
	public PlayerSnapshotResponse selectRecipe(String playerId, SelectRecipeRequest request){
		PlayerEntity player = playerSupportService.getPlayerForUpdate(playerId);
		playerSupportService.requirePhase(player, "Recipes can only be selected during afternoon service.", PlayerPhase.AFTERNOON_SERVICE);
		playerSupportService.requireInHub(player, "Recipe selection is only available in Hub.");
		RecipeEntity recipe = catalogLookupService.getRecipeByCode(request.recipeCode());
		player.setSelectedRecipe(recipe);
		playerRepository.save(player);
		dayRunService.recordRecipeSelection(dayRunService.getCurrentDayRun(player), recipe);
		return playerSnapshotService.buildSnapshot(player);
	}

	@Transactional
	public ServiceRunResponse runService(String playerId){
		PlayerEntity player = playerSupportService.getPlayerForUpdate(playerId);
		playerSupportService.requirePhase(player, "Service can only be run during afternoon service.", PlayerPhase.AFTERNOON_SERVICE);
		playerSupportService.requireInHub(player, "Service is only available in Hub.");
		if(player.getSelectedRecipe() == null){
			throw new BusinessException(ErrorCode.RECIPE_NOT_SELECTED, "Select a recipe before running service.");
		}

		RecipeEntity recipe = player.getSelectedRecipe();
		List<RecipeIngredientEntity> ingredients = catalogLookupService.getRecipeIngredients(recipe.getId());
		Map<String, Integer> inventoryQuantities = playerResourceService.getInventoryQuantityMap(player);
		int cookableCount = calculateCookableCount(inventoryQuantities, ingredients);
		int soldCount = Math.min(cookableCount, player.getServiceCapacity());
		int earnedGold = soldCount * recipe.getSellPrice();
		int earnedReputation = soldCount * recipe.getReputationReward();

		if(soldCount > 0){
			for(RecipeIngredientEntity ingredient : ingredients){
				playerResourceService.removeInventory(player, ingredient.getResource(), ingredient.getQuantity() * soldCount);
			}
		}

		player.setGold(player.getGold() + earnedGold);
		player.setReputation(player.getReputation() + earnedReputation);
		player.setCurrentPhase(PlayerPhase.SETTLEMENT);
		playerRepository.save(player);

		DayRunEntity dayRun = dayRunService.getCurrentDayRun(player);
		dayRunService.recordServiceResult(dayRun, recipe, false, soldCount, earnedGold, earnedReputation);

		ServiceLogEntity serviceLog = new ServiceLogEntity();
		serviceLog.setPlayer(player);
		serviceLog.setDayRun(dayRun);
		serviceLog.setRecipe(recipe);
		serviceLog.setRequestedCapacity(player.getServiceCapacity());
		serviceLog.setCookableCount(cookableCount);
		serviceLog.setSoldCount(soldCount);
		serviceLog.setEarnedGold(earnedGold);
		serviceLog.setEarnedReputation(earnedReputation);
		serviceLog.setSkipped(false);
		serviceLogRepository.save(serviceLog);

		EconomyLogEntity economyLog = new EconomyLogEntity();
		economyLog.setPlayer(player);
		economyLog.setDayRun(dayRun);
		economyLog.setLogType(EconomyLogType.SERVICE_REWARD);
		economyLog.setGoldDelta(earnedGold);
		economyLog.setReasonCode("SERVICE_REWARD");
		economyLog.setNote(recipe.getCode());
		economyLogRepository.save(economyLog);

		return new ServiceRunResponse(
			recipe.getCode(),
			player.getServiceCapacity(),
			cookableCount,
			soldCount,
			earnedGold,
			earnedReputation,
			false,
			playerSnapshotService.buildSnapshot(player)
		);
	}

	@Transactional
	public ServiceRunResponse skipService(String playerId){
		PlayerEntity player = playerSupportService.getPlayerForUpdate(playerId);
		playerSupportService.requirePhase(player, "Service can only be skipped during afternoon service.", PlayerPhase.AFTERNOON_SERVICE);
		playerSupportService.requireInHub(player, "Service skip is only available in Hub.");
		RecipeEntity recipe = player.getSelectedRecipe();
		player.setCurrentPhase(PlayerPhase.SETTLEMENT);
		playerRepository.save(player);

		DayRunEntity dayRun = dayRunService.getCurrentDayRun(player);
		dayRunService.recordServiceResult(dayRun, recipe, true, 0, 0, 0);

		ServiceLogEntity serviceLog = new ServiceLogEntity();
		serviceLog.setPlayer(player);
		serviceLog.setDayRun(dayRun);
		serviceLog.setRecipe(recipe);
		serviceLog.setRequestedCapacity(player.getServiceCapacity());
		serviceLog.setCookableCount(0);
		serviceLog.setSoldCount(0);
		serviceLog.setEarnedGold(0);
		serviceLog.setEarnedReputation(0);
		serviceLog.setSkipped(true);
		serviceLogRepository.save(serviceLog);

		return new ServiceRunResponse(
			recipe != null ? recipe.getCode() : null,
			player.getServiceCapacity(),
			0,
			0,
			0,
			0,
			true,
			playerSnapshotService.buildSnapshot(player)
		);
	}

	private int calculateCookableCount(Map<String, Integer> inventoryQuantities, List<RecipeIngredientEntity> ingredients){
		if(ingredients.isEmpty()){
			return 0;
		}
		int cookableCount = Integer.MAX_VALUE;
		for(RecipeIngredientEntity ingredient : ingredients){
			int inventoryQuantity = inventoryQuantities.getOrDefault(ingredient.getResource().getCode(), 0);
			cookableCount = Math.min(cookableCount, inventoryQuantity / ingredient.getQuantity());
		}
		return cookableCount == Integer.MAX_VALUE ? 0 : cookableCount;
	}

}
