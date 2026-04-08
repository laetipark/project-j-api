package com.projectj.api.catalog.service;

import com.projectj.api.catalog.dto.GoogleSheetRecipeRowResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class GoogleSheetRecipeRowMapper{

	private static final int COLUMN_DIFFICULTY = 0;
	private static final int COLUMN_RECIPE_NAME = 1;
	private static final int COLUMN_SUPPLY_SOURCE = 2;
	private static final int COLUMN_COOKING_METHOD = 3;
	private static final int INGREDIENT_START = 4;
	private static final int INGREDIENT_END = 10;
	private static final int COLUMN_PRICE = 40;
	private static final int COLUMN_MEMO = 41;
	private static final Pattern NUMBER_PATTERN = Pattern.compile("(\\d+)");

	public List<GoogleSheetRecipeRowResponse> map(List<List<String>> rows){
		List<GoogleSheetRecipeRowResponse> responses = new ArrayList<>();
		for(int index = 1; index < rows.size(); index++){
			List<String> row = rows.get(index);
			String recipeName = cell(row, COLUMN_RECIPE_NAME);
			if(recipeName == null){
				continue;
			}
			String difficultyLabel = cell(row, COLUMN_DIFFICULTY);
			String priceText = cell(row, COLUMN_PRICE);
			responses.add(
				new GoogleSheetRecipeRowResponse(
					index + 1,
					difficultyLabel,
					parseNumber(difficultyLabel),
					recipeName,
					cell(row, COLUMN_SUPPLY_SOURCE),
					cell(row, COLUMN_COOKING_METHOD),
					extractIngredients(row),
					priceText,
					parseNumber(priceText),
					cell(row, COLUMN_MEMO)
				)
			);
		}
		return responses;
	}

	private List<String> extractIngredients(List<String> row){
		List<String> ingredients = new ArrayList<>();
		for(int columnIndex = INGREDIENT_START; columnIndex <= INGREDIENT_END; columnIndex++){
			String ingredient = cell(row, columnIndex);
			if(ingredient != null){
				ingredients.add(ingredient);
			}
		}
		return ingredients;
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

	private Integer parseNumber(String value){
		if(value == null){
			return null;
		}
		Matcher matcher = NUMBER_PATTERN.matcher(value.replace(",", ""));
		if(!matcher.find()){
			return null;
		}
		return Integer.parseInt(matcher.group(1));
	}

}
