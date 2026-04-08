package com.projectj.api.catalog.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
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
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class GoogleSheetsRecipeCatalogService implements RecipeCatalogService{

	private static final String GOOGLE_SHEETS_SCOPE = "https://www.googleapis.com/auth/spreadsheets.readonly";
	private static final Logger log = LoggerFactory.getLogger(GoogleSheetsRecipeCatalogService.class);

	private final GoogleSheetsProperties properties;
	private final GoogleSheetIngredientRowMapper ingredientRowMapper;
	private final GoogleSheetRecipeRowMapper recipeRowMapper;
	private final RecipeSheetPersistenceService recipeSheetPersistenceService;
	private final RestClient restClient;
	private volatile CachedRecipeCatalogSnapshot cachedSnapshot;

	public GoogleSheetsRecipeCatalogService(
		GoogleSheetsProperties properties,
		GoogleSheetIngredientRowMapper ingredientRowMapper,
		GoogleSheetRecipeRowMapper recipeRowMapper,
		RecipeSheetPersistenceService recipeSheetPersistenceService,
		RestClient.Builder restClientBuilder
	){
		this.properties = properties;
		this.ingredientRowMapper = ingredientRowMapper;
		this.recipeRowMapper = recipeRowMapper;
		this.recipeSheetPersistenceService = recipeSheetPersistenceService;
		this.restClient = restClientBuilder.baseUrl("https://sheets.googleapis.com/v4").build();
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
		SheetProperties ingredientSheetProperties = resolveSheet(properties.getIngredientSheetGid(), properties.getIngredientSheetName());
		List<SheetIngredient> ingredients = ingredientRowMapper.map(fetchRows(ingredientSheetProperties.title()));
		validateIngredients(ingredients);

		SheetProperties recipeSheetProperties = resolveSheet(properties.getRecipeSheetGid(), properties.getRecipeSheetName());
		List<RawSheetRecipe> rawRecipes = recipeRowMapper.map(fetchRows(recipeSheetProperties.title()));
		validateRawRecipes(rawRecipes);

		List<SheetRecipe> recipes = resolveRecipes(rawRecipes, ingredients);
		recipeSheetPersistenceService.synchronize(ingredients, recipes);

		return new CachedRecipeCatalogSnapshot(
			properties.getSpreadsheetId(),
			recipeSheetProperties.sheetId(),
			recipeSheetProperties.title(),
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

	private SheetProperties resolveSheet(Long sheetGid, String sheetName){
		SpreadsheetMetadataResponse response = fetchMetadata();
		List<SheetProperties> sheets = response.sheets().stream()
			.map(SheetMetadata::properties)
			.toList();
		if(sheetName != null && !sheetName.isBlank()){
			return sheets.stream()
				.filter(sheet -> sheetName.equals(sheet.title()))
				.findFirst()
				.orElseThrow(() -> new BusinessException(
					ErrorCode.GOOGLE_SHEET_NOT_FOUND,
					"Google Sheet tab was not found: " + sheetName
				));
		}
		if(sheetGid != null){
			return sheets.stream()
				.filter(sheet -> sheet.sheetId() == sheetGid.longValue())
				.findFirst()
				.orElseThrow(() -> new BusinessException(
					ErrorCode.GOOGLE_SHEET_NOT_FOUND,
					"Google Sheet tab was not found for gid: " + sheetGid
				));
		}
		if(sheets.isEmpty()){
			throw new BusinessException(ErrorCode.GOOGLE_SHEET_NOT_FOUND, "Google Sheet document has no tabs.");
		}
		return sheets.getFirst();
	}

	private SpreadsheetMetadataResponse fetchMetadata(){
		String accessToken = getAccessToken();
		try{
			SpreadsheetMetadataResponse response = restClient.get()
				.uri(uriBuilder -> uriBuilder
					.path("/spreadsheets/{spreadsheetId}")
					.queryParam("fields", "sheets(properties(sheetId,title))")
					.build(properties.getSpreadsheetId()))
				.headers(headers -> headers.setBearerAuth(accessToken))
				.retrieve()
				.body(SpreadsheetMetadataResponse.class);
			if(response == null || response.sheets() == null){
				throw new BusinessException(ErrorCode.GOOGLE_SHEETS_FETCH_FAILED, "Google Sheets metadata response was empty.");
			}
			return response;
		}catch(RestClientException exception){
			throw new BusinessException(
				ErrorCode.GOOGLE_SHEETS_FETCH_FAILED,
				"Failed to fetch Google Sheets metadata: " + exception.getMessage()
			);
		}
	}

	private List<List<String>> fetchRows(String sheetTitle){
		String accessToken = getAccessToken();
		String range = quoteSheetTitle(sheetTitle);
		try{
			BatchValueRangeResponse response = restClient.get()
				.uri(uriBuilder -> uriBuilder
					.path("/spreadsheets/{spreadsheetId}/values:batchGet")
					.queryParam("ranges", range)
					.queryParam("majorDimension", "ROWS")
					.build(properties.getSpreadsheetId()))
				.headers(headers -> headers.setBearerAuth(accessToken))
				.retrieve()
				.body(BatchValueRangeResponse.class);
			if(response == null || response.valueRanges() == null || response.valueRanges().isEmpty()){
				throw new BusinessException(ErrorCode.GOOGLE_SHEETS_FETCH_FAILED, "Google Sheets values response was empty.");
			}
			ValueRangeResponse valueRange = response.valueRanges().getFirst();
			return valueRange.values() == null ? List.of() : valueRange.values();
		}catch(RestClientException exception){
			throw new BusinessException(
				ErrorCode.GOOGLE_SHEETS_FETCH_FAILED,
				"Failed to fetch Google Sheets rows: " + exception.getMessage()
			);
		}
	}

	private String getAccessToken(){
		Path credentialsPath = resolveCredentialsPath();
		try(InputStream inputStream = Files.newInputStream(credentialsPath)){
			GoogleCredentials credentials = GoogleCredentials.fromStream(inputStream)
				.createScoped(List.of(GOOGLE_SHEETS_SCOPE));
			AccessToken accessToken = credentials.getAccessToken();
			if(accessToken == null){
				accessToken = credentials.refreshAccessToken();
			}
			return accessToken.getTokenValue();
		}catch(IOException exception){
			throw new BusinessException(
				ErrorCode.GOOGLE_SHEETS_AUTH_FAILED,
				"Failed to load Google Sheets credentials. Use a service account JSON file shared with the spreadsheet: "
					+ exception.getMessage()
			);
		}
	}

	private Path resolveCredentialsPath(){
		Path path = Paths.get(properties.getCredentialsPath());
		if(path.isAbsolute()){
			return path;
		}
		return Paths.get("").toAbsolutePath().resolve(path).normalize();
	}

	private String quoteSheetTitle(String sheetTitle){
		String escaped = sheetTitle.replace("'", "''");
		return "'" + escaped + "'";
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	private record SpreadsheetMetadataResponse(List<SheetMetadata> sheets){
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	private record SheetMetadata(SheetProperties properties){
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	private record SheetProperties(long sheetId, String title){
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	private record BatchValueRangeResponse(List<ValueRangeResponse> valueRanges){
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	private record ValueRangeResponse(List<List<String>> values){
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
