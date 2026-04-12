package com.projectj.api;

import com.projectj.api.catalog.dto.BootstrapResponse;
import com.projectj.api.catalog.dto.PortalRuleResponse;
import com.projectj.api.catalog.dto.UpgradeDefinitionResponse;
import com.projectj.api.catalog.service.BootstrapService;
import com.projectj.api.catalog.service.CatalogLookupService;
import com.projectj.api.catalog.service.RecipeCatalogService;
import com.projectj.api.catalog.service.SheetRecipe;
import com.projectj.api.catalog.service.SheetRecipeIngredient;
import com.projectj.api.common.exception.BusinessException;
import com.projectj.api.exploration.dto.GatherRequest;
import com.projectj.api.exploration.dto.GatherResponse;
import com.projectj.api.exploration.dto.TravelRequest;
import com.projectj.api.exploration.service.ExplorationService;
import com.projectj.api.player.domain.PlayerEntity;
import com.projectj.api.player.domain.PlayerInventoryEntity;
import com.projectj.api.player.domain.PlayerStorageEntity;
import com.projectj.api.player.dto.CreatePlayerRequest;
import com.projectj.api.player.dto.CreatePlayerResponse;
import com.projectj.api.player.dto.PlayerSnapshotResponse;
import com.projectj.api.player.repository.PlayerInventoryRepository;
import com.projectj.api.player.repository.PlayerRepository;
import com.projectj.api.player.repository.PlayerStorageRepository;
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
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
	private ExplorationService explorationService;

	@Autowired
	private RestaurantService restaurantService;

	@Autowired
	private CatalogLookupService catalogLookupService;

	@Autowired
	private BootstrapService bootstrapService;

	@Autowired
	private PlayerResourceService playerResourceService;

	@Autowired
	private PlayerInventoryRepository playerInventoryRepository;

	@Autowired
	private PlayerStorageRepository playerStorageRepository;

	@Autowired
	private StorageService storageService;

	@Autowired
	private UpgradePurchaseService upgradePurchaseService;

	@Test
	void serviceRunUsesSheetRecipeIdAndAggregatedIngredients(){
		CreatePlayerResponse player = createPlayer("service-player");
		PlayerEntity playerEntity = loadPlayer(player.playerId());
		playerResourceService.addInventory(playerEntity, catalogLookupService.getResourceByCode("Fish"), 5);
		playerResourceService.addInventory(playerEntity, catalogLookupService.getResourceByCode("Seaweed"), 3);

		PlayerSnapshotResponse afterSelect = restaurantService.selectRecipe(player.playerId(), new SelectRecipeRequest("food_041"));
		assertEquals("food_041", afterSelect.selectedRecipeId());

		ServiceRunResponse response = restaurantService.runService(player.playerId());

		assertEquals("food_041", response.recipeId());
		assertEquals(2, response.cookableCount());
		assertEquals(2, response.soldCount());
		assertEquals(50, response.earnedGold());
		assertEquals(0, response.earnedReputation());
		assertEquals("food_041", response.snapshot().selectedRecipeId());
		assertEquals(50, response.snapshot().gold());

		Map<String, Integer> inventoryMap = toQuantityMap(response.snapshot());
		assertEquals(1, inventoryMap.get("Fish"));
		assertEquals(1, inventoryMap.get("Seaweed"));
	}

	@Test
	void inventorySlotRuleCountsDistinctResourceTypesWithoutDayLoop(){
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
	void hubOnlyActionsAreBlockedOutsideHub(){
		CreatePlayerResponse player = createPlayer("hub-gate-player");
		PlayerEntity playerEntity = loadPlayer(player.playerId());
		playerEntity.setGold(30);
		playerRepository.save(playerEntity);
		playerResourceService.addInventory(playerEntity, catalogLookupService.getResourceByCode("Herb"), 3);
		playerResourceService.addStorage(playerEntity, catalogLookupService.getResourceByCode("Shell"), 3);

		explorationService.travel(player.playerId(), new TravelRequest("GoToBeach"));
		explorationService.travel(player.playerId(), new TravelRequest("GoToDeepForest"));

		BusinessException selectRecipeException = assertThrows(
			BusinessException.class,
			() -> restaurantService.selectRecipe(player.playerId(), new SelectRecipeRequest("food_021"))
		);
		assertEquals("INVALID_REGION", selectRecipeException.getErrorCode().getCode());

		BusinessException runServiceException = assertThrows(
			BusinessException.class,
			() -> restaurantService.runService(player.playerId())
		);
		assertEquals("INVALID_REGION", runServiceException.getErrorCode().getCode());

		BusinessException depositException = assertThrows(
			BusinessException.class,
			() -> storageService.deposit(player.playerId(), new StorageTransferRequest("Herb", 1))
		);
		assertEquals("STORAGE_ONLY_IN_HUB", depositException.getErrorCode().getCode());

		BusinessException withdrawException = assertThrows(
			BusinessException.class,
			() -> storageService.withdraw(player.playerId(), new StorageTransferRequest("Shell", 1))
		);
		assertEquals("STORAGE_ONLY_IN_HUB", withdrawException.getErrorCode().getCode());

		BusinessException upgradeException = assertThrows(
			BusinessException.class,
			() -> upgradePurchaseService.purchase(player.playerId(), "inventory_12_slots")
		);
		assertEquals("INVALID_REGION", upgradeException.getErrorCode().getCode());
	}

	@Test
	void inventoryRowsAreSoftDeletedAndRevived(){
		CreatePlayerResponse player = createPlayer("inventory-soft-delete-player");
		PlayerEntity playerEntity = loadPlayer(player.playerId());
		var herb = catalogLookupService.getResourceByCode("Herb");

		playerResourceService.addInventory(playerEntity, herb, 2);
		PlayerInventoryEntity initialRow = playerInventoryRepository.findByPlayer_IdAndResource_Id(playerEntity.getId(), herb.getId()).orElseThrow();

		playerResourceService.removeInventory(playerEntity, herb, 2);
		PlayerInventoryEntity deletedRow = playerInventoryRepository.findByPlayer_IdAndResource_Id(playerEntity.getId(), herb.getId()).orElseThrow();
		assertEquals(initialRow.getId(), deletedRow.getId());
		assertEquals(0, deletedRow.getQuantity());
		assertNotNull(deletedRow.getDeletedAt());
		assertTrue(playerResourceService.getInventory(playerEntity).isEmpty());

		playerResourceService.addInventory(playerEntity, herb, 3);
		PlayerInventoryEntity restoredRow = playerInventoryRepository.findByPlayer_IdAndResource_Id(playerEntity.getId(), herb.getId()).orElseThrow();
		assertEquals(initialRow.getId(), restoredRow.getId());
		assertEquals(3, restoredRow.getQuantity());
		assertNull(restoredRow.getDeletedAt());
	}

	@Test
	void storageRowsAreSoftDeletedAndRevived(){
		CreatePlayerResponse player = createPlayer("storage-soft-delete-player");
		PlayerEntity playerEntity = loadPlayer(player.playerId());
		var shell = catalogLookupService.getResourceByCode("Shell");

		playerResourceService.addStorage(playerEntity, shell, 2);
		PlayerStorageEntity initialRow = playerStorageRepository.findByPlayer_IdAndResource_Id(playerEntity.getId(), shell.getId()).orElseThrow();

		playerResourceService.removeStorage(playerEntity, shell, 2);
		PlayerStorageEntity deletedRow = playerStorageRepository.findByPlayer_IdAndResource_Id(playerEntity.getId(), shell.getId()).orElseThrow();
		assertEquals(initialRow.getId(), deletedRow.getId());
		assertEquals(0, deletedRow.getQuantity());
		assertNotNull(deletedRow.getDeletedAt());
		assertTrue(playerResourceService.getStorage(playerEntity).isEmpty());

		playerResourceService.addStorage(playerEntity, shell, 4);
		PlayerStorageEntity restoredRow = playerStorageRepository.findByPlayer_IdAndResource_Id(playerEntity.getId(), shell.getId()).orElseThrow();
		assertEquals(initialRow.getId(), restoredRow.getId());
		assertEquals(4, restoredRow.getQuantity());
		assertNull(restoredRow.getDeletedAt());
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
	void bootstrapIncludesBeachCenteredExplorationMapContract(){
		BootstrapResponse bootstrap = bootstrapService.getBootstrap();

		assertTrue(bootstrap.regions().stream().anyMatch(region -> region.code().equals("Sea")));
		assertTrue(bootstrap.regions().stream().anyMatch(region -> region.code().equals("Shortcut")));

		Map<String, PortalRuleResponse> portalRules = bootstrap.portalRules().stream()
			.collect(Collectors.toMap(PortalRuleResponse::code, portalRule -> portalRule));
		assertEquals("Beach", portalRules.get("GoToSea").fromRegionCode());
		assertEquals("Sea", portalRules.get("GoToSea").toRegionCode());
		assertEquals("Beach", portalRules.get("GoToDeepForest").fromRegionCode());
		assertEquals("WindHill", portalRules.get("GoToAbandonedMine").fromRegionCode());
		assertEquals("Lantern", portalRules.get("GoToAbandonedMine").requiredToolCode());
		assertEquals("unlock_shortcut", portalRules.get("GoToShortcutFromBeach").requiredUpgradeCode());
		assertEquals("unlock_shortcut", portalRules.get("GoToWindHillFromShortcut").requiredUpgradeCode());
		assertFalse(portalRules.containsKey("WindHillShortcut"));

		Map<String, UpgradeDefinitionResponse> upgrades = bootstrap.upgrades().stream()
			.collect(Collectors.toMap(UpgradeDefinitionResponse::code, upgrade -> upgrade));
		UpgradeDefinitionResponse shortcutUpgrade = upgrades.get("unlock_shortcut");
		assertNotNull(shortcutUpgrade);
		assertEquals("PORTAL_UNLOCK", shortcutUpgrade.upgradeType());
		assertEquals(30, shortcutUpgrade.goldCost());
		assertTrue(shortcutUpgrade.resourceCosts().isEmpty());
	}

	@Test
	void portalGraphUsesBeachAsExplorationConnectionHub(){
		CreatePlayerResponse player = createPlayer("portal-player");

		BusinessException directForestException = assertThrows(
			BusinessException.class,
			() -> explorationService.travel(player.playerId(), new TravelRequest("GoToDeepForest"))
		);
		assertEquals("PORTAL_NOT_ACCESSIBLE", directForestException.getErrorCode().getCode());

		BusinessException directWindHillException = assertThrows(
			BusinessException.class,
			() -> explorationService.travel(player.playerId(), new TravelRequest("GoToWindHill"))
		);
		assertEquals("PORTAL_NOT_ACCESSIBLE", directWindHillException.getErrorCode().getCode());

		BusinessException directMineException = assertThrows(
			BusinessException.class,
			() -> explorationService.travel(player.playerId(), new TravelRequest("GoToAbandonedMine"))
		);
		assertEquals("PORTAL_NOT_ACCESSIBLE", directMineException.getErrorCode().getCode());

		PlayerSnapshotResponse beach = explorationService.travel(player.playerId(), new TravelRequest("GoToBeach"));
		assertEquals("Beach", beach.currentRegion());
		PlayerSnapshotResponse sea = explorationService.travel(player.playerId(), new TravelRequest("GoToSea"));
		assertEquals("Sea", sea.currentRegion());
		PlayerSnapshotResponse backToBeach = explorationService.travel(player.playerId(), new TravelRequest("ReturnToBeachFromSea"));
		assertEquals("Beach", backToBeach.currentRegion());
		explorationService.travel(player.playerId(), new TravelRequest("GoToDeepForest"));
		PlayerSnapshotResponse windHill = explorationService.travel(player.playerId(), new TravelRequest("GoToWindHill"));
		assertEquals("WindHill", windHill.currentRegion());

		BusinessException mineException = assertThrows(
			BusinessException.class,
			() -> explorationService.travel(player.playerId(), new TravelRequest("GoToAbandonedMine"))
		);
		assertEquals("TOOL_REQUIRED", mineException.getErrorCode().getCode());
	}

	@Test
	void shortcutPortalRequiresPurchasedPortalUpgrade(){
		CreatePlayerResponse player = createPlayer("shortcut-player");

		explorationService.travel(player.playerId(), new TravelRequest("GoToBeach"));
		BusinessException shortcutException = assertThrows(
			BusinessException.class,
			() -> explorationService.travel(player.playerId(), new TravelRequest("GoToShortcutFromBeach"))
		);
		assertEquals("PORTAL_UPGRADE_REQUIRED", shortcutException.getErrorCode().getCode());
		explorationService.travel(player.playerId(), new TravelRequest("ReturnToHubFromBeach"));

		PlayerEntity playerEntity = loadPlayer(player.playerId());
		playerEntity.setGold(30);
		playerRepository.save(playerEntity);

		UpgradePurchaseResponse purchase = upgradePurchaseService.purchase(player.playerId(), "unlock_shortcut");
		assertEquals("unlock_shortcut", purchase.upgradeCode());
		assertTrue(purchase.snapshot().purchasedUpgradeCodes().contains("unlock_shortcut"));
		assertEquals(0, purchase.snapshot().gold());

		explorationService.travel(player.playerId(), new TravelRequest("GoToBeach"));
		PlayerSnapshotResponse shortcut = explorationService.travel(player.playerId(), new TravelRequest("GoToShortcutFromBeach"));
		assertEquals("Shortcut", shortcut.currentRegion());
		assertTrue(shortcut.purchasedUpgradeCodes().contains("unlock_shortcut"));
		PlayerSnapshotResponse windHill = explorationService.travel(player.playerId(), new TravelRequest("GoToWindHillFromShortcut"));
		assertEquals("WindHill", windHill.currentRegion());
	}

	@Test
	void seaNetGatherUsesExistingGatherContract(){
		CreatePlayerResponse player = createPlayer("sea-gather-player");

		explorationService.travel(player.playerId(), new TravelRequest("GoToBeach"));
		explorationService.travel(player.playerId(), new TravelRequest("GoToSea"));
		GatherResponse seaFish = explorationService.gather(player.playerId(), new GatherRequest("Sea", "Fish", 1));

		assertTrue(seaFish.success());
		assertEquals(1, toQuantityMap(seaFish.snapshot()).get("Fish"));

		explorationService.travel(player.playerId(), new TravelRequest("ReturnToBeachFromSea"));
		GatherResponse wrongRegion = explorationService.gather(player.playerId(), new GatherRequest("Sea", "Fish", 1));
		assertFalse(wrongRegion.success());
		assertTrue(wrongRegion.message().contains("current region"));
	}

	@Test
	void explorationScenesCanReturnDirectlyToHub(){
		CreatePlayerResponse beachPlayer = createPlayer("return-beach-player");
		explorationService.travel(beachPlayer.playerId(), new TravelRequest("GoToBeach"));
		assertEquals("Hub", explorationService.travel(beachPlayer.playerId(), new TravelRequest("ReturnToHubFromBeach")).currentRegion());

		CreatePlayerResponse seaPlayer = createPlayer("return-sea-player");
		explorationService.travel(seaPlayer.playerId(), new TravelRequest("GoToBeach"));
		explorationService.travel(seaPlayer.playerId(), new TravelRequest("GoToSea"));
		assertEquals("Hub", explorationService.travel(seaPlayer.playerId(), new TravelRequest("ReturnToHubFromSea")).currentRegion());

		CreatePlayerResponse forestPlayer = createPlayer("return-forest-player");
		explorationService.travel(forestPlayer.playerId(), new TravelRequest("GoToBeach"));
		explorationService.travel(forestPlayer.playerId(), new TravelRequest("GoToDeepForest"));
		assertEquals("Hub", explorationService.travel(forestPlayer.playerId(), new TravelRequest("ReturnToHubFromDeepForest")).currentRegion());

		CreatePlayerResponse windHillPlayer = createPlayer("return-windhill-player");
		explorationService.travel(windHillPlayer.playerId(), new TravelRequest("GoToBeach"));
		explorationService.travel(windHillPlayer.playerId(), new TravelRequest("GoToDeepForest"));
		explorationService.travel(windHillPlayer.playerId(), new TravelRequest("GoToWindHill"));
		assertEquals("Hub", explorationService.travel(windHillPlayer.playerId(), new TravelRequest("ReturnToHubFromWindHill")).currentRegion());

		CreatePlayerResponse shortcutPlayer = createPlayer("return-shortcut-player");
		PlayerEntity shortcutPlayerEntity = loadPlayer(shortcutPlayer.playerId());
		shortcutPlayerEntity.setGold(30);
		playerRepository.save(shortcutPlayerEntity);
		upgradePurchaseService.purchase(shortcutPlayer.playerId(), "unlock_shortcut");
		explorationService.travel(shortcutPlayer.playerId(), new TravelRequest("GoToBeach"));
		explorationService.travel(shortcutPlayer.playerId(), new TravelRequest("GoToShortcutFromBeach"));
		assertEquals("Hub", explorationService.travel(shortcutPlayer.playerId(), new TravelRequest("ReturnToHubFromShortcut")).currentRegion());

		CreatePlayerResponse minePlayer = createPlayer("return-mine-player");
		PlayerEntity minePlayerEntity = loadPlayer(minePlayer.playerId());
		minePlayerEntity.setGold(45);
		playerRepository.save(minePlayerEntity);
		playerResourceService.addStorage(minePlayerEntity, catalogLookupService.getResourceByCode("Mushroom"), 2);
		upgradePurchaseService.purchase(minePlayer.playerId(), "unlock_lantern");
		explorationService.travel(minePlayer.playerId(), new TravelRequest("GoToBeach"));
		explorationService.travel(minePlayer.playerId(), new TravelRequest("GoToDeepForest"));
		explorationService.travel(minePlayer.playerId(), new TravelRequest("GoToWindHill"));
		explorationService.travel(minePlayer.playerId(), new TravelRequest("GoToAbandonedMine"));
		assertEquals("Hub", explorationService.travel(minePlayer.playerId(), new TravelRequest("ReturnToHubFromAbandonedMine")).currentRegion());
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

	@TestConfiguration
	static class RecipeCatalogTestConfiguration{

		@Bean
		@Primary
		RecipeCatalogService recipeCatalogService(){
			List<SheetRecipe> recipes = List.of(
				new SheetRecipe(
					2,
					"food_021",
					"Fish Meal",
					"Beach",
					1,
					"Pan",
					List.of(
						new SheetRecipeIngredient("ingredient_fish", "Fish", 1),
						new SheetRecipeIngredient("ingredient_seaweed", "Seaweed", 1)
					),
					18,
					null
				),
				new SheetRecipe(
					3,
					"food_041",
					"Double Fish Meal",
					"Beach",
					2,
					"Pot",
					List.of(
						new SheetRecipeIngredient("ingredient_fish", "Fish", 2),
						new SheetRecipeIngredient("ingredient_seaweed", "Seaweed", 1)
					),
					25,
					null
				)
			);
			return new RecipeCatalogService(){
				@Override
				public List<SheetRecipe> getRecipes(){
					return recipes;
				}

				@Override
				public SheetRecipe getRecipeById(String recipeId){
					return recipes.stream()
						.filter(recipe -> recipe.recipeId().equals(recipeId))
						.findFirst()
						.orElseThrow();
				}
			};
		}

	}

}
