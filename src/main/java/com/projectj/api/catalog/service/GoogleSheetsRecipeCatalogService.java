package com.projectj.api.catalog.service;

import com.projectj.api.catalog.dto.GoogleSheetRecipeCatalogResponse;
import com.projectj.api.catalog.dto.GoogleSheetRecipeRowResponse;
import com.projectj.api.catalog.dto.RecipeIngredientResponse;
import com.projectj.api.common.exception.BusinessException;
import com.projectj.api.common.exception.ErrorCode;
import com.projectj.api.config.GoogleSheetsProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class GoogleSheetsRecipeCatalogService implements RecipeCatalogService{

	private static final Logger log = LoggerFactory.getLogger(GoogleSheetsRecipeCatalogService.class);

	private final GoogleSheetsProperties properties;
	private final GoogleSheetsCatalogClient googleSheetsCatalogClient;
	private final GoogleSheetIngredientRowMapper ingredientRowMapper;
	private final GoogleSheetRecipeRowMapper recipeRowMapper;
	private final RecipeSheetPersistenceService recipeSheetPersistenceService;
	private volatile CachedRecipeCatalogSnapshot cachedSnapshot;

	public GoogleSheetsRecipeCatalogService(
		GoogleSheetsProperties properties,
		GoogleSheetsCatalogClient googleSheetsCatalogClient,
		GoogleSheetIngredientRowMapper ingredientRowMapper,
		GoogleSheetRecipeRowMapper recipeRowMapper,
		RecipeSheetPersistenceService recipeSheetPersistenceService
	){
		this.properties = properties;
		this.googleSheetsCatalogClient = googleSheetsCatalogClient;
		this.ingredientRowMapper = ingredientRowMapper;
		this.recipeRowMapper = recipeRowMapper;
		this.recipeSheetPersistenceService = recipeSheetPersistenceService;
	}

	@Override
	public List<SheetRecipe> getRecipes(){
		return getCurrentSnapshot().recipes();
	}

	@Override
	public SheetRecipe getRecipeById(String recipeId){
		return getCurrentSnapshot().recipes()
			.stream()
			.filter(recipe -> recipe.recipeId().equals(recipeId))
			.findFirst()
			.orElseThrow(() -> new BusinessException(ErrorCode.RECIPE_NOT_FOUND, "Recipe was not found: " + recipeId));
	}

	public GoogleSheetRecipeCatalogResponse getRecipeCatalogResponse(){
		return getCurrentSnapshot().toResponse();
	}

	public GoogleSheetRecipeCatalogResponse refreshRecipes(){
		validateConfiguration();
		validateIngredientTarget();
		validateRecipeTarget();
		CachedRecipeCatalogSnapshot snapshot = refreshSnapshot();
		cachedSnapshot = snapshot;
		return snapshot.toResponse();
	}

	@EventListener(ApplicationReadyEvent.class)
	public void warmUp(){
		refreshSnapshotSafely();
	}

	@Scheduled(cron = "${app.google-sheets.refresh-cron:2 6 * * * *}", zone = "Asia/Seoul")
	public void refreshOnSchedule(){
		refreshSnapshotSafely();
	}

	private CachedRecipeCatalogSnapshot getCurrentSnapshot(){
		validateConfiguration();
		validateIngredientTarget();
		validateRecipeTarget();
		CachedRecipeCatalogSnapshot snapshot = cachedSnapshot;
		if(snapshot != null){
			return snapshot;
		}
		CachedRecipeCatalogSnapshot refreshedSnapshot = refreshSnapshot();
		cachedSnapshot = refreshedSnapshot;
		return refreshedSnapshot;
	}

	private void validateConfiguration(){
		if(!properties.isConfigured()){
			throw new BusinessException(
				ErrorCode.GOOGLE_SHEETS_NOT_CONFIGURED,
				"Google Sheets integration is not configured. Set GOOGLE_CREDENTIALS_PATH and GOOGLE_SHEETS_ID."
			);
		}
	}

	private void validateIngredientTarget(){
		if(!properties.hasIngredientTarget()){
			throw new BusinessException(
				ErrorCode.GOOGLE_SHEETS_NOT_CONFIGURED,
				"Set GOOGLE_SHEETS_INGREDIENT_GID or GOOGLE_SHEETS_INGREDIENT_SHEET_NAME to choose which ingredient tab to sync."
			);
		}
	}

	private void validateRecipeTarget(){
		if(!properties.hasRecipeTarget()){
			throw new BusinessException(
				ErrorCode.GOOGLE_SHEETS_NOT_CONFIGURED,
				"Set GOOGLE_SHEETS_RECIPE_GID or GOOGLE_SHEETS_RECIPE_SHEET_NAME to choose which recipe tab to sync."
			);
		}
	}

	private void refreshSnapshotSafely(){
		if(!properties.isConfigured() || !properties.hasIngredientTarget() || !properties.hasRecipeTarget()){
			return;
		}
		try{
			cachedSnapshot = refreshSnapshot();
		}catch(BusinessException exception){
			if(cachedSnapshot == null){
				throw exception;
			}
			log.warn("Google Sheets refresh failed. Keeping previous snapshot. reason={}", exception.getMessage());
		}
	}

	private CachedRecipeCatalogSnapshot refreshSnapshot(){
		GoogleSheetTab ingredientSheet = googleSheetsCatalogClient.resolveSheet(
			properties.getIngredientSheetGid(),
			properties.getIngredientSheetName()
		);
		List<SheetIngredient> ingredients = ingredientRowMapper.map(googleSheetsCatalogClient.fetchRows(ingredientSheet.title()));
		validateIngredients(ingredients);

		GoogleSheetTab recipeSheet = googleSheetsCatalogClient.resolveSheet(
			properties.getRecipeSheetGid(),
			properties.getRecipeSheetName()
		);
		List<RawSheetRecipe> rawRecipes = recipeRowMapper.map(googleSheetsCatalogClient.fetchRows(recipeSheet.title()));
		validateRawRecipes(rawRecipes);

		List<SheetRecipe> recipes = resolveRecipes(rawRecipes, ingredients);
		recipeSheetPersistenceService.synchronize(ingredients, recipes);

		return new CachedRecipeCatalogSnapshot(
			properties.getSpreadsheetId(),
			recipeSheet.sheetId(),
			recipeSheet.title(),
			Instant.now(),
			recipes
		);
	}

	private void validateIngredients(List<SheetIngredient> ingredients){
		Set<String> ingredientIds = new HashSet<>();
		Set<String> ingredientNames = new HashSet<>();
		for(SheetIngredient ingredient : ingredients){
			if(!ingredientIds.add(ingredient.ingredientId())){
				throw new BusinessException(
					ErrorCode.GOOGLE_SHEETS_INVALID_FORMAT,
					"Duplicate ingredient id was found in Google Sheets: " + ingredient.ingredientId()
				);
			}
			if(!ingredientNames.add(ingredient.ingredientName())){
				throw new BusinessException(
					ErrorCode.GOOGLE_SHEETS_INVALID_FORMAT,
					"Duplicate ingredient name was found in Google Sheets: " + ingredient.ingredientName()
				);
			}
		}
	}

	private void validateRawRecipes(List<RawSheetRecipe> recipes){
		Set<String> recipeIds = new HashSet<>();
		Set<String> recipeNames = new HashSet<>();
		for(RawSheetRecipe recipe : recipes){
			if(!recipeIds.add(recipe.recipeId())){
				throw new BusinessException(
					ErrorCode.GOOGLE_SHEETS_INVALID_FORMAT,
					"Duplicate recipe id was found in Google Sheets: " + recipe.recipeId()
				);
			}
			if(!recipeNames.add(recipe.recipeName())){
				throw new BusinessException(
					ErrorCode.GOOGLE_SHEETS_INVALID_FORMAT,
					"Duplicate recipe name was found in Google Sheets: " + recipe.recipeName()
				);
			}
		}
	}

	private List<SheetRecipe> resolveRecipes(List<RawSheetRecipe> rawRecipes, List<SheetIngredient> ingredients){
		Map<String, SheetIngredient> ingredientsByName = new LinkedHashMap<>();
		for(SheetIngredient ingredient : ingredients){
			ingredientsByName.put(ingredient.ingredientName(), ingredient);
		}

		return rawRecipes.stream()
			.map(recipe -> toRecipe(recipe, ingredientsByName))
			.toList();
	}

	private SheetRecipe toRecipe(RawSheetRecipe rawRecipe, Map<String, SheetIngredient> ingredientsByName){
		Map<String, Integer> quantitiesByIngredientName = new LinkedHashMap<>();
		for(String ingredientName : rawRecipe.ingredientNames()){
			SheetIngredient ingredient = ingredientsByName.get(ingredientName);
			if(ingredient == null){
				throw new BusinessException(
					ErrorCode.GOOGLE_SHEETS_INVALID_FORMAT,
					"Google Sheets recipe ingredient is not mapped to an active ingredient name: " + ingredientName
				);
			}
			quantitiesByIngredientName.merge(ingredientName, 1, Integer::sum);
		}

		List<SheetRecipeIngredient> recipeIngredients = quantitiesByIngredientName.entrySet()
			.stream()
			.map(entry -> {
				SheetIngredient ingredient = ingredientsByName.get(entry.getKey());
				return new SheetRecipeIngredient(ingredient.ingredientId(), ingredient.ingredientName(), entry.getValue());
			})
			.toList();

		return new SheetRecipe(
			rawRecipe.rowNumber(),
			rawRecipe.recipeId(),
			rawRecipe.recipeName(),
			rawRecipe.supplySource(),
			rawRecipe.difficulty(),
			rawRecipe.cookingMethod(),
			recipeIngredients,
			rawRecipe.price(),
			rawRecipe.memo()
		);
	}

	private record CachedRecipeCatalogSnapshot(
		String spreadsheetId,
		long sheetGid,
		String sheetTitle,
		Instant syncedAt,
		List<SheetRecipe> recipes
	){

		private GoogleSheetRecipeCatalogResponse toResponse(){
			List<GoogleSheetRecipeRowResponse> rows = recipes.stream()
				.map(recipe -> new GoogleSheetRecipeRowResponse(
					recipe.rowNumber(),
					recipe.recipeId(),
					recipe.recipeName(),
					recipe.supplySource(),
					recipe.difficulty(),
					recipe.cookingMethod(),
					recipe.ingredients().stream()
						.map(ingredient -> new RecipeIngredientResponse(
							ingredient.ingredientId(),
							ingredient.ingredientName(),
							ingredient.quantity()
						))
						.toList(),
					recipe.price(),
					recipe.memo()
				))
				.toList();
			return new GoogleSheetRecipeCatalogResponse(spreadsheetId, sheetGid, sheetTitle, syncedAt, rows);
		}

	}

}
