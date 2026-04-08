package com.projectj.api.catalog.service;

import com.projectj.api.common.exception.BusinessException;
import com.projectj.api.common.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class GoogleSheetIngredientRowMapper{

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
	private static final Pattern NUMBER_PATTERN = Pattern.compile("(\\d+)");

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
					requiredCell(row, headerIndexes.get(HEADER_ID), HEADER_ID, rowNumber),
					ingredientName,
					parseRequiredNumber(cell(row, headerIndexes.get(HEADER_DIFFICULTY)), HEADER_DIFFICULTY, rowNumber),
					cell(row, headerIndexes.get(HEADER_SUPPLY_SOURCE)),
					cell(row, headerIndexes.get(HEADER_ACQUISITION_SOURCE)),
					cell(row, headerIndexes.get(HEADER_ACQUISITION_METHOD)),
					cell(row, headerIndexes.get(HEADER_ACQUISITION_TOOL)),
					parseRequiredNumber(cell(row, headerIndexes.get(HEADER_BUY_PRICE)), HEADER_BUY_PRICE, rowNumber),
					parseRequiredNumber(cell(row, headerIndexes.get(HEADER_SELL_PRICE)), HEADER_SELL_PRICE, rowNumber),
					cell(row, headerIndexes.get(HEADER_MEMO))
				)
			);
		}
		return ingredients;
	}

	private Map<String, Integer> buildHeaderIndexes(List<String> headerRow){
		Map<String, Integer> indexes = new LinkedHashMap<>();
		indexes.put(HEADER_ID, requiredHeaderIndex(headerRow, HEADER_ID));
		indexes.put(HEADER_DIFFICULTY, requiredHeaderIndex(headerRow, HEADER_DIFFICULTY));
		indexes.put(HEADER_INGREDIENT_NAME, requiredHeaderIndex(headerRow, HEADER_INGREDIENT_NAME));
		indexes.put(HEADER_SUPPLY_SOURCE, requiredHeaderIndex(headerRow, HEADER_SUPPLY_SOURCE));
		indexes.put(HEADER_ACQUISITION_SOURCE, requiredHeaderIndex(headerRow, HEADER_ACQUISITION_SOURCE));
		indexes.put(HEADER_ACQUISITION_METHOD, requiredHeaderIndex(headerRow, HEADER_ACQUISITION_METHOD));
		indexes.put(HEADER_ACQUISITION_TOOL, requiredHeaderIndex(headerRow, HEADER_ACQUISITION_TOOL));
		indexes.put(HEADER_BUY_PRICE, requiredHeaderIndex(headerRow, HEADER_BUY_PRICE));
		indexes.put(HEADER_SELL_PRICE, requiredHeaderIndex(headerRow, HEADER_SELL_PRICE));
		indexes.put(HEADER_MEMO, requiredHeaderIndex(headerRow, HEADER_MEMO));
		return indexes;
	}

	private int requiredHeaderIndex(List<String> headerRow, String headerName){
		for(int index = 0; index < headerRow.size(); index++){
			String value = headerRow.get(index);
			if(value != null && headerName.equals(value.trim())){
				return index;
			}
		}
		throw new BusinessException(
			ErrorCode.GOOGLE_SHEETS_INVALID_FORMAT,
			"Google Sheets ingredient header is missing: " + headerName
		);
	}

	private String requiredCell(List<String> row, int index, String headerName, int rowNumber){
		String value = cell(row, index);
		if(value == null){
			throw new BusinessException(
				ErrorCode.GOOGLE_SHEETS_INVALID_FORMAT,
				"Google Sheets ingredient value is required. header=%s row=%d".formatted(headerName, rowNumber)
			);
		}
		return value;
	}

	private String cell(List<String> row, int index){
		if(index >= row.size()){
			return null;
		}
		String value = row.get(index);
		if(value == null){
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	private int parseRequiredNumber(String value, String headerName, int rowNumber){
		if(value == null){
			return 0;
		}
		Matcher matcher = NUMBER_PATTERN.matcher(value.replace(",", ""));
		if(!matcher.find()){
			throw new BusinessException(
				ErrorCode.GOOGLE_SHEETS_INVALID_FORMAT,
				"Google Sheets ingredient value is invalid. header=%s row=%d value=%s".formatted(headerName, rowNumber, value)
			);
		}
		return Integer.parseInt(matcher.group(1));
	}

}
