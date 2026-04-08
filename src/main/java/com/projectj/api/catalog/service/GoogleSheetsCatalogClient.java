package com.projectj.api.catalog.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.projectj.api.common.exception.BusinessException;
import com.projectj.api.common.exception.ErrorCode;
import com.projectj.api.config.GoogleSheetsProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class GoogleSheetsCatalogClient{

	private static final String GOOGLE_SHEETS_SCOPE = "https://www.googleapis.com/auth/spreadsheets.readonly";

	private final GoogleSheetsProperties properties;
	private final RestClient restClient;

	public GoogleSheetsCatalogClient(GoogleSheetsProperties properties, RestClient.Builder restClientBuilder){
		this.properties = properties;
		this.restClient = restClientBuilder.baseUrl("https://sheets.googleapis.com/v4").build();
	}

	GoogleSheetTab resolveSheet(Long sheetGid, String sheetName){
		SpreadsheetMetadataResponse response = fetchMetadata();
		List<SheetProperties> sheets = response.sheets().stream()
			.map(SheetMetadata::properties)
			.toList();
		if(sheetName != null && !sheetName.isBlank()){
			return sheets.stream()
				.filter(sheet -> sheetName.equals(sheet.title()))
				.findFirst()
				.map(sheet -> new GoogleSheetTab(sheet.sheetId(), sheet.title()))
				.orElseThrow(() -> new BusinessException(
					ErrorCode.GOOGLE_SHEET_NOT_FOUND,
					"Google Sheet tab was not found: " + sheetName
				));
		}
		if(sheetGid != null){
			return sheets.stream()
				.filter(sheet -> sheet.sheetId() == sheetGid.longValue())
				.findFirst()
				.map(sheet -> new GoogleSheetTab(sheet.sheetId(), sheet.title()))
				.orElseThrow(() -> new BusinessException(
					ErrorCode.GOOGLE_SHEET_NOT_FOUND,
					"Google Sheet tab was not found for gid: " + sheetGid
				));
		}
		if(sheets.isEmpty()){
			throw new BusinessException(ErrorCode.GOOGLE_SHEET_NOT_FOUND, "Google Sheet document has no tabs.");
		}
		SheetProperties firstSheet = sheets.getFirst();
		return new GoogleSheetTab(firstSheet.sheetId(), firstSheet.title());
	}

	List<List<String>> fetchRows(String sheetTitle){
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

}
