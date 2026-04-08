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
public class GoogleSheetRecipeRowMapper{

	private static final String SHEET_TYPE = "recipe";
	private static final String HEADER_ID = "id";
	private static final String HEADER_DIFFICULTY = "\uB09C\uC774\uB3C4";
	private static final String HEADER_RECIPE_NAME = "\uB808\uC2DC\uD53C\uBA85";
	private static final String HEADER_SUPPLY_SOURCE = "\uC218\uAE09\uCC98";
	private static final String HEADER_COOKING_METHOD = "\uC870\uB9AC\uBC95";
	private static final String HEADER_PRICE = "\uAC00\uACA9";
	private static final String HEADER_MEMO = "\uBA54\uBAA8";
	private static final List<String> INGREDIENT_HEADERS = List.of(
		"\uC7AC\uB8CC 1",
		"\uC7AC\uB8CC 2",
		"\uC7AC\uB8CC 3",
		"\uC7AC\uB8CC 4",
		"\uC7AC\uB8CC 5",
		"\uC7AC\uB8CC 6",
		"\uC7AC\uB8CC 7"
	);

	public List<RawSheetRecipe> map(List<List<String>> rows){
		if(rows.isEmpty()){
			return List.of();
		}

		Map<String, Integer> headerIndexes = buildHeaderIndexes(rows.getFirst());
		List<RawSheetRecipe> recipes = new ArrayList<>();
		for(int rowIndex = 1; rowIndex < rows.size(); rowIndex++){
			List<String> row = rows.get(rowIndex);
			String recipeName = cell(row, headerIndexes.get(HEADER_RECIPE_NAME));
			if(recipeName == null){
				continue;
			}
			int rowNumber = rowIndex + 1;
			String recipeId = requiredCell(row, headerIndexes.get(HEADER_ID), HEADER_ID, rowNumber, SHEET_TYPE);
			recipes.add(
				new RawSheetRecipe(
					rowNumber,
					recipeId,
					recipeName,
					cell(row, headerIndexes.get(HEADER_SUPPLY_SOURCE)),
					parseRequiredNumber(cell(row, headerIndexes.get(HEADER_DIFFICULTY)), HEADER_DIFFICULTY, rowNumber, SHEET_TYPE),
					cell(row, headerIndexes.get(HEADER_COOKING_METHOD)),
					extractIngredients(row, headerIndexes),
					parseRequiredNumber(cell(row, headerIndexes.get(HEADER_PRICE)), HEADER_PRICE, rowNumber, SHEET_TYPE),
					cell(row, headerIndexes.get(HEADER_MEMO))
				)
			);
		}
		return recipes;
	}

	private Map<String, Integer> buildHeaderIndexes(List<String> headerRow){
		Map<String, Integer> indexes = new LinkedHashMap<>();
		indexes.put(HEADER_ID, requiredHeaderIndex(headerRow, HEADER_ID, SHEET_TYPE));
		indexes.put(HEADER_DIFFICULTY, requiredHeaderIndex(headerRow, HEADER_DIFFICULTY, SHEET_TYPE));
		indexes.put(HEADER_RECIPE_NAME, requiredHeaderIndex(headerRow, HEADER_RECIPE_NAME, SHEET_TYPE));
		indexes.put(HEADER_SUPPLY_SOURCE, requiredHeaderIndex(headerRow, HEADER_SUPPLY_SOURCE, SHEET_TYPE));
		indexes.put(HEADER_COOKING_METHOD, requiredHeaderIndex(headerRow, HEADER_COOKING_METHOD, SHEET_TYPE));
		indexes.put(HEADER_PRICE, requiredHeaderIndex(headerRow, HEADER_PRICE, SHEET_TYPE));
		indexes.put(HEADER_MEMO, requiredHeaderIndex(headerRow, HEADER_MEMO, SHEET_TYPE));
		for(String ingredientHeader : INGREDIENT_HEADERS){
			indexes.put(ingredientHeader, requiredHeaderIndex(headerRow, ingredientHeader, SHEET_TYPE));
		}
		return indexes;
	}

	private List<String> extractIngredients(List<String> row, Map<String, Integer> headerIndexes){
		List<String> ingredients = new ArrayList<>();
		for(String ingredientHeader : INGREDIENT_HEADERS){
			String ingredient = cell(row, headerIndexes.get(ingredientHeader));
			if(ingredient != null){
				ingredients.add(ingredient);
			}
		}
		return ingredients;
	}

}
