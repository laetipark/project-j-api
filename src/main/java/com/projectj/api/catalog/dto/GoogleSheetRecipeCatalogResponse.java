package com.projectj.api.catalog.dto;

import java.time.Instant;
import java.util.List;

public record GoogleSheetRecipeCatalogResponse(
	String spreadsheetId,
	long sheetGid,
	String sheetTitle,
	Instant syncedAt,
	List<GoogleSheetRecipeRowResponse> recipes
){
}
