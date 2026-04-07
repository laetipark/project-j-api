package com.projectj.api;

import com.projectj.api.catalog.domain.RecipeEntity;
import com.projectj.api.catalog.service.CatalogLookupService;
import com.projectj.api.common.exception.BusinessException;
import com.projectj.api.dayrun.service.DayAdvanceService;
import com.projectj.api.exploration.dto.GatherRequest;
import com.projectj.api.exploration.dto.GatherResponse;
import com.projectj.api.exploration.dto.TravelRequest;
import com.projectj.api.exploration.service.ExplorationService;
import com.projectj.api.player.domain.PlayerEntity;
import com.projectj.api.player.dto.CreatePlayerRequest;
import com.projectj.api.player.dto.CreatePlayerResponse;
import com.projectj.api.player.dto.PlayerSnapshotResponse;
import com.projectj.api.player.repository.PlayerRepository;
import com.projectj.api.player.service.PlayerLifecycleService;
import com.projectj.api.player.service.PlayerResourceService;
import com.projectj.api.restaurant.dto.SelectRecipeRequest;
import com.projectj.api.restaurant.dto.ServiceRunResponse;
import com.projectj.api.restaurant.service.RestaurantService;
import com.projectj.api.storage.dto.StorageTransferRequest;
import com.projectj.api.storage.service.StorageService;
import com.projectj.api.upgrade.dto.UpgradePurchaseResponse;
import com.projectj.api.upgrade.service.UpgradePurchaseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class JongguRestaurantApiIntegrationTest{

	@Autowired
	private PlayerLifecycleService playerLifecycleService;

	@Autowired
	private PlayerRepository playerRepository;

	@Autowired
	private CatalogLookupService catalogLookupService;

	@Autowired
	private ExplorationService explorationService;

	@Autowired
	private RestaurantService restaurantService;

	@Autowired
	private DayAdvanceService dayAdvanceService;

	@Autowired
	private PlayerResourceService playerResourceService;

	@Autowired
	private StorageService storageService;

	@Autowired
	private UpgradePurchaseService upgradePurchaseService;

	@Test
	void phaseTransitionsFollowMorningServiceSettlementLoop(){
		CreatePlayerResponse player = createPlayer("phase-player");

		explorationService.travel(player.playerId(), new TravelRequest("GoToBeach"));
		explorationService.travel(player.playerId(), new TravelRequest("ReturnToHubFromBeach"));
		PlayerSnapshotResponse afternoonSnapshot = explorationService.skipExploration(player.playerId());
		assertEquals("afternoon_service", afternoonSnapshot.currentPhase());

		ServiceRunResponse settlementSnapshot = restaurantService.skipService(player.playerId());
		assertEquals("settlement", settlementSnapshot.snapshot().currentPhase());

		PlayerSnapshotResponse nextDaySnapshot = dayAdvanceService.nextDay(player.playerId());
		assertEquals(2, nextDaySnapshot.currentDay());
		assertEquals("morning_explore", nextDaySnapshot.currentPhase());
		assertEquals("Hub", nextDaySnapshot.currentRegion());
	}

	@Test
	void serviceRunUsesCookableCountAndServiceCapacityMinimum(){
		CreatePlayerResponse player = createPlayer("service-player");
		PlayerEntity playerEntity = loadPlayer(player.playerId());
		playerResourceService.addInventory(playerEntity, catalogLookupService.getResourceByCode("Fish"), 5);
		playerResourceService.addInventory(playerEntity, catalogLookupService.getResourceByCode("Seaweed"), 3);

		explorationService.skipExploration(player.playerId());
		restaurantService.selectRecipe(player.playerId(), new SelectRecipeRequest("SushiSet"));
		ServiceRunResponse response = restaurantService.runService(player.playerId());

		assertEquals("SushiSet", response.recipeCode());
		assertEquals(2, response.cookableCount());
		assertEquals(2, response.soldCount());
		assertEquals(36, response.earnedGold());
		assertEquals(2, response.earnedReputation());
		assertEquals("settlement", response.snapshot().currentPhase());
		assertEquals(36, response.snapshot().gold());
		assertEquals(2, response.snapshot().reputation());

		Map<String, Integer> inventoryMap = toQuantityMap(response.snapshot());
		assertEquals(1, inventoryMap.get("Fish"));
		assertEquals(1, inventoryMap.get("Seaweed"));
	}

	@Test
	void inventorySlotRuleCountsDistinctResourceTypes(){
		CreatePlayerResponse player = createPlayer("inventory-player");
		PlayerEntity playerEntity = loadPlayer(player.playerId());
		playerEntity.setInventorySlotLimit(2);
		playerRepository.save(playerEntity);

		explorationService.travel(player.playerId(), new TravelRequest("GoToBeach"));
		GatherResponse first = explorationService.gather(player.playerId(), new GatherRequest("Beach", "Fish", 1));
		GatherResponse second = explorationService.gather(player.playerId(), new GatherRequest("Beach", "Shell", 1));
		GatherResponse third = explorationService.gather(player.playerId(), new GatherRequest("Beach", "Seaweed", 1));

		assertTrue(first.success());
		assertTrue(second.success());
		assertFalse(third.success());
		assertTrue(third.message().contains("Inventory slot limit"));
		assertEquals(2, third.snapshot().inventoryResources().size());
	}

	@Test
	void storageDepositAndWithdrawMoveResourcesBetweenInventoryAndStorage(){
		CreatePlayerResponse player = createPlayer("storage-player");
		PlayerEntity playerEntity = loadPlayer(player.playerId());
		playerResourceService.addInventory(playerEntity, catalogLookupService.getResourceByCode("Herb"), 3);

		PlayerSnapshotResponse afterDeposit = storageService.deposit(player.playerId(), new StorageTransferRequest("Herb", 2));
		assertEquals(1, toQuantityMap(afterDeposit).get("Herb"));
		assertEquals(2, toStorageQuantityMap(afterDeposit).get("Herb"));

		PlayerSnapshotResponse afterWithdraw = storageService.withdraw(player.playerId(), new StorageTransferRequest("Herb", 1));
		assertEquals(2, toQuantityMap(afterWithdraw).get("Herb"));
		assertEquals(1, toStorageQuantityMap(afterWithdraw).get("Herb"));
	}

	@Test
	void upgradePurchaseConsumesCostsAndAppliesEffect(){
		CreatePlayerResponse player = createPlayer("upgrade-player");
		PlayerEntity playerEntity = loadPlayer(player.playerId());
		playerEntity.setGold(30);
		playerRepository.save(playerEntity);
		playerResourceService.addStorage(playerEntity, catalogLookupService.getResourceByCode("Shell"), 3);

		UpgradePurchaseResponse response = upgradePurchaseService.purchase(player.playerId(), "inventory_12_slots");

		assertEquals("inventory_12_slots", response.upgradeCode());
		assertEquals(12, response.snapshot().inventorySlotLimit());
		assertEquals(0, response.snapshot().gold());
		assertFalse(toStorageQuantityMap(response.snapshot()).containsKey("Shell"));
	}

	@Test
	void portalAccessChecksToolAndReputationConditions(){
		CreatePlayerResponse player = createPlayer("portal-player");

		BusinessException mineException = assertThrows(
			BusinessException.class,
			() -> explorationService.travel(player.playerId(), new TravelRequest("GoToAbandonedMine"))
		);
		assertEquals("TOOL_REQUIRED", mineException.getErrorCode().getCode());

		explorationService.travel(player.playerId(), new TravelRequest("GoToDeepForest"));
		BusinessException shortcutException = assertThrows(
			BusinessException.class,
			() -> explorationService.travel(player.playerId(), new TravelRequest("WindHillShortcut"))
		);
		assertEquals("REPUTATION_REQUIRED", shortcutException.getErrorCode().getCode());
	}

	private CreatePlayerResponse createPlayer(String displayName){
		return playerLifecycleService.createPlayer(new CreatePlayerRequest(displayName));
	}

	private PlayerEntity loadPlayer(String playerId){
		return playerRepository.findDetailedByPublicId(playerId).orElseThrow();
	}

	private Map<String, Integer> toQuantityMap(PlayerSnapshotResponse snapshot){
		return snapshot.inventoryResources().stream()
			.collect(Collectors.toMap(resource -> resource.resourceCode(), resource -> resource.quantity()));
	}

	private Map<String, Integer> toStorageQuantityMap(PlayerSnapshotResponse snapshot){
		return snapshot.storageResources().stream()
			.collect(Collectors.toMap(resource -> resource.resourceCode(), resource -> resource.quantity()));
	}

}
