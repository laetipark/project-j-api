package com.projectj.api.catalog.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.projectj.api.catalog.dto.GoogleSheetRecipeCatalogResponse;
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
import java.util.List;

@Service
public class GoogleSheetsRecipeCatalogService{

	private static final String GOOGLE_SHEETS_SCOPE = "https://www.googleapis.com/auth/spreadsheets.readonly";
	private static final Logger log = LoggerFactory.getLogger(GoogleSheetsRecipeCatalogService.class);

	private final GoogleSheetsProperties properties;
	private final GoogleSheetRecipeRowMapper rowMapper;
	private final RestClient restClient;
	private volatile CachedRecipeCatalogSnapshot cachedSnapshot;

	public GoogleSheetsRecipeCatalogService(
		GoogleSheetsProperties properties,
		GoogleSheetRecipeRowMapper rowMapper,
		RestClient.Builder restClientBuilder
	){
		this.properties = properties;
		this.rowMapper = rowMapper;
		this.restClient = restClientBuilder.baseUrl("https://sheets.googleapis.com/v4").build();
	}

	public GoogleSheetRecipeCatalogResponse getRecipes(){
		validateConfiguration();
		validateRecipeTarget();
		CachedRecipeCatalogSnapshot snapshot = cachedSnapshot;
		if(snapshot != null){
			return snapshot.toResponse();
		}
		return refreshSnapshot().toResponse();
	}

	@EventListener(ApplicationReadyEvent.class)
	public void warmUp(){
		refreshSnapshotSafely();
	}

	@Scheduled(cron = "${app.google-sheets.refresh-cron:2 6 * * * *}", zone = "Asia/Seoul")
	public void refreshOnSchedule(){
		refreshSnapshotSafely();
	}

	private void validateConfiguration(){
		if(!properties.isConfigured()){
			throw new BusinessException(
				ErrorCode.GOOGLE_SHEETS_NOT_CONFIGURED,
				"Google Sheets integration is not configured. Set GOOGLE_CREDENTIALS_PATH and GOOGLE_SHEETS_ID."
			);
		}
	}

	private void validateRecipeTarget(){
		if(!properties.hasRecipeTarget()){
			throw new BusinessException(
				ErrorCode.GOOGLE_SHEETS_NOT_CONFIGURED,
				"Set GOOGLE_SHEETS_RECIPE_GID or GOOGLE_SHEETS_RECIPE_SHEET_NAME to choose which tab to sync."
			);
		}
	}

	private void refreshSnapshotSafely(){
		if(!properties.isConfigured() || !properties.hasRecipeTarget()){
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
		SheetProperties sheetProperties = resolveSheet(properties.getRecipeSheetGid(), properties.getRecipeSheetName());
		List<List<String>> rows = fetchRows(sheetProperties.title());
		return new CachedRecipeCatalogSnapshot(
			properties.getSpreadsheetId(),
			sheetProperties.sheetId(),
			sheetProperties.title(),
			Instant.now(),
			rowMapper.map(rows)
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
		String range = quoteSheetTitle(sheetTitle) + "!A:AP";
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
		List<com.projectj.api.catalog.dto.GoogleSheetRecipeRowResponse> recipes
	){

		private GoogleSheetRecipeCatalogResponse toResponse(){
			return new GoogleSheetRecipeCatalogResponse(spreadsheetId, sheetGid, sheetTitle, syncedAt, recipes);
		}

	}

}
