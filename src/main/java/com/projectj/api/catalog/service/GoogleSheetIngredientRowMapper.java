package com.projectj.api.catalog.service;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.projectj.api.catalog.service.GoogleSheetRowMapperSupport.cell;
import static com.projectj.api.catalog.service.GoogleSheetRowMapperSupport.parseRequiredNumber;
import static com.projectj.api.catalog.service.GoogleSheetRowMapperSupport.requiredCell;
import static com.projectj.api.catalog.service.GoogleSheetRowMapperSupport.requiredHeaderIndex;

@Component
public class GoogleSheetIngredientRowMapper{

	private static final String SHEET_TYPE = "ingredient";
	private static final String HEADER_ID = "id";
	private static final String HEADER_DIFFICULTY = "난이도";
	private static final String HEADER_INGREDIENT_NAME = "재료명";
	private static final String HEADER_SUPPLY_SOURCE = "수급처";
	private static final String HEADER_ACQUISITION_SOURCE = "획득처";
	private static final String HEADER_ACQUISITION_METHOD = "획득방식";
	private static final String HEADER_ACQUISITION_TOOL = "획득도구";
	private static final String HEADER_BUY_PRICE = "구매가격";
	private static final String HEADER_SELL_PRICE = "판매가격";
	private static final String HEADER_MEMO = "메모";

	public List<SheetIngredient> map(List<List<String>> rows){
		if(rows.isEmpty()){
			return List.of();
		}

		Map<String, Integer> headerIndexes = buildHeaderIndexes(rows.getFirst());
		List<SheetIngredient> ingredients = new ArrayList<>();
		for(int rowIndex = 1; rowIndex < rows.size(); rowIndex++){
			List<String> row = rows.get(rowIndex);
			String ingredientName = cell(row, headerIndexes.get(HEADER_INGREDIENT_NAME));
			if(ingredientName == null){
				continue;
			}

			int rowNumber = rowIndex + 1;
			ingredients.add(
				new SheetIngredient(
					rowNumber,
					requiredCell(row, headerIndexes.get(HEADER_ID), HEADER_ID, rowNumber, SHEET_TYPE),
					ingredientName,
					parseRequiredNumber(cell(row, headerIndexes.get(HEADER_DIFFICULTY)), HEADER_DIFFICULTY, rowNumber, SHEET_TYPE),
					cell(row, headerIndexes.get(HEADER_SUPPLY_SOURCE)),
					cell(row, headerIndexes.get(HEADER_ACQUISITION_SOURCE)),
					cell(row, headerIndexes.get(HEADER_ACQUISITION_METHOD)),
					cell(row, headerIndexes.get(HEADER_ACQUISITION_TOOL)),
					parseRequiredNumber(cell(row, headerIndexes.get(HEADER_BUY_PRICE)), HEADER_BUY_PRICE, rowNumber, SHEET_TYPE),
					parseRequiredNumber(cell(row, headerIndexes.get(HEADER_SELL_PRICE)), HEADER_SELL_PRICE, rowNumber, SHEET_TYPE),
					cell(row, headerIndexes.get(HEADER_MEMO))
				)
			);
		}
		return ingredients;
	}

	private Map<String, Integer> buildHeaderIndexes(List<String> headerRow){
		Map<String, Integer> indexes = new LinkedHashMap<>();
		indexes.put(HEADER_ID, requiredHeaderIndex(headerRow, HEADER_ID, SHEET_TYPE));
		indexes.put(HEADER_DIFFICULTY, requiredHeaderIndex(headerRow, HEADER_DIFFICULTY, SHEET_TYPE));
		indexes.put(HEADER_INGREDIENT_NAME, requiredHeaderIndex(headerRow, HEADER_INGREDIENT_NAME, SHEET_TYPE));
		indexes.put(HEADER_SUPPLY_SOURCE, requiredHeaderIndex(headerRow, HEADER_SUPPLY_SOURCE, SHEET_TYPE));
		indexes.put(HEADER_ACQUISITION_SOURCE, requiredHeaderIndex(headerRow, HEADER_ACQUISITION_SOURCE, SHEET_TYPE));
		indexes.put(HEADER_ACQUISITION_METHOD, requiredHeaderIndex(headerRow, HEADER_ACQUISITION_METHOD, SHEET_TYPE));
		indexes.put(HEADER_ACQUISITION_TOOL, requiredHeaderIndex(headerRow, HEADER_ACQUISITION_TOOL, SHEET_TYPE));
		indexes.put(HEADER_BUY_PRICE, requiredHeaderIndex(headerRow, HEADER_BUY_PRICE, SHEET_TYPE));
		indexes.put(HEADER_SELL_PRICE, requiredHeaderIndex(headerRow, HEADER_SELL_PRICE, SHEET_TYPE));
		indexes.put(HEADER_MEMO, requiredHeaderIndex(headerRow, HEADER_MEMO, SHEET_TYPE));
		return indexes;
	}

}
