package com.projectj.api.restaurant.service;

import com.projectj.api.catalog.domain.ResourceEntity;
import com.projectj.api.catalog.service.CatalogLookupService;
import com.projectj.api.catalog.service.RecipeCatalogService;
import com.projectj.api.catalog.service.SheetRecipe;
import com.projectj.api.catalog.service.SheetRecipeIngredient;
import com.projectj.api.common.exception.BusinessException;
import com.projectj.api.common.exception.ErrorCode;
import com.projectj.api.player.domain.PlayerEntity;
import com.projectj.api.player.dto.PlayerSnapshotResponse;
import com.projectj.api.player.repository.PlayerRepository;
import com.projectj.api.player.service.PlayerResourceService;
import com.projectj.api.player.service.PlayerSnapshotService;
import com.projectj.api.player.service.PlayerSupportService;
import com.projectj.api.restaurant.dto.SelectRecipeRequest;
import com.projectj.api.restaurant.dto.ServiceRunResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class RestaurantService{

	private final CatalogLookupService catalogLookupService;
	private final RecipeCatalogService recipeCatalogService;
	private final PlayerSupportService playerSupportService;
	private final PlayerResourceService playerResourceService;
	private final PlayerSnapshotService playerSnapshotService;
	private final PlayerRepository playerRepository;

	public RestaurantService(
		CatalogLookupService catalogLookupService,
		RecipeCatalogService recipeCatalogService,
		PlayerSupportService playerSupportService,
		PlayerResourceService playerResourceService,
		PlayerSnapshotService playerSnapshotService,
		PlayerRepository playerRepository
	){
		this.catalogLookupService = catalogLookupService;
		this.recipeCatalogService = recipeCatalogService;
		this.playerSupportService = playerSupportService;
		this.playerResourceService = playerResourceService;
		this.playerSnapshotService = playerSnapshotService;
		this.playerRepository = playerRepository;
	}

	@Transactional
	public PlayerSnapshotResponse selectRecipe(String playerId, SelectRecipeRequest request){
		PlayerEntity player = playerSupportService.getPlayerForUpdate(playerId);
		playerSupportService.requireInHub(player, "Recipe selection is only available in Hub.");
		SheetRecipe recipe = recipeCatalogService.getRecipeById(request.recipeId());
		player.setSelectedRecipeId(recipe.recipeId());
		playerRepository.save(player);
		return playerSnapshotService.buildSnapshot(player);
	}

	@Transactional
	public ServiceRunResponse runService(String playerId){
		PlayerEntity player = playerSupportService.getPlayerForUpdate(playerId);
		playerSupportService.requireInHub(player, "Service is only available in Hub.");
		if(player.getSelectedRecipeId() == null){
			throw new BusinessException(ErrorCode.RECIPE_NOT_SELECTED, "Select a recipe before running service.");
		}

		SheetRecipe recipe = recipeCatalogService.getRecipeById(player.getSelectedRecipeId());
		List<ResolvedIngredient> ingredients = resolveIngredients(recipe.ingredients());
		Map<String, Integer> inventoryQuantities = playerResourceService.getInventoryQuantityMap(player);
		int cookableCount = calculateCookableCount(inventoryQuantities, ingredients);
		int soldCount = Math.min(cookableCount, player.getServiceCapacity());
		int earnedGold = soldCount * recipe.price();
		int earnedReputation = 0;

		if(soldCount > 0){
			for(ResolvedIngredient ingredient : ingredients){
				playerResourceService.removeInventory(player, ingredient.resource(), ingredient.quantity() * soldCount);
			}
		}

		player.setGold(player.getGold() + earnedGold);
		player.setReputation(player.getReputation() + earnedReputation);
		playerRepository.save(player);

		return new ServiceRunResponse(
			recipe.recipeId(),
			player.getServiceCapacity(),
			cookableCount,
			soldCount,
			earnedGold,
			earnedReputation,
			playerSnapshotService.buildSnapshot(player)
		);
	}

	private List<ResolvedIngredient> resolveIngredients(List<SheetRecipeIngredient> recipeIngredients){
		Map<String, Integer> requiredQuantities = new LinkedHashMap<>();
		for(SheetRecipeIngredient ingredient : recipeIngredients){
			requiredQuantities.merge(ingredient.ingredientName(), ingredient.quantity(), Integer::sum);
		}
		return requiredQuantities.entrySet()
			.stream()
			.map(entry -> new ResolvedIngredient(catalogLookupService.getResourceByName(entry.getKey()), entry.getValue()))
			.toList();
	}

	private int calculateCookableCount(Map<String, Integer> inventoryQuantities, List<ResolvedIngredient> ingredients){
		if(ingredients.isEmpty()){
			return 0;
		}
		int cookableCount = Integer.MAX_VALUE;
		for(ResolvedIngredient ingredient : ingredients){
			int inventoryQuantity = inventoryQuantities.getOrDefault(ingredient.resource().getCode(), 0);
			cookableCount = Math.min(cookableCount, inventoryQuantity / ingredient.quantity());
		}
		return cookableCount == Integer.MAX_VALUE ? 0 : cookableCount;
	}

	private record ResolvedIngredient(ResourceEntity resource, int quantity){
	}

}
