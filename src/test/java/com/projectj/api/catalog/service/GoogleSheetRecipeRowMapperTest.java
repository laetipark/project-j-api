package com.projectj.api.catalog.service;

import com.projectj.api.catalog.dto.GoogleSheetRecipeRowResponse;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class GoogleSheetRecipeRowMapperTest{

	private final GoogleSheetRecipeRowMapper mapper = new GoogleSheetRecipeRowMapper();

	@Test
	void mapExtractsRecipeColumnsAndOptionalPriceMemo(){
		List<String> recipeRow = new ArrayList<>();
		for(int index = 0; index < 42; index++){
			recipeRow.add("");
		}
		recipeRow.set(0, "☆2");
		recipeRow.set(1, "김밥");
		recipeRow.set(2, "깊은 숲");
		recipeRow.set(3, "칼+도마");
		recipeRow.set(4, "시금치");
		recipeRow.set(5, "당근");
		recipeRow.set(6, "김");
		recipeRow.set(7, "밥");
		recipeRow.set(40, "1,250");
		recipeRow.set(41, "한정 판매");

		List<List<String>> rows = List.of(
			List.of("난이도", "레시피명", "수급처", "조리법", "재료 1", "재료 2", "재료 3", "재료 4", "재료 5", "재료 6", "재료 7"),
			recipeRow,
			List.of("", "떡국", "바람언덕", "", "소고기")
		);

		List<GoogleSheetRecipeRowResponse> response = mapper.map(rows);

		assertEquals(2, response.size());
		assertEquals(2, response.get(0).rowNumber());
		assertEquals("김밥", response.get(0).recipeName());
		assertEquals(2, response.get(0).difficulty());
		assertEquals(List.of("시금치", "당근", "김", "밥"), response.get(0).ingredients());
		assertEquals("1,250", response.get(0).priceText());
		assertEquals(1250, response.get(0).price());
		assertEquals("한정 판매", response.get(0).memo());

		assertEquals(3, response.get(1).rowNumber());
		assertEquals("떡국", response.get(1).recipeName());
		assertNull(response.get(1).difficulty());
		assertEquals(List.of("소고기"), response.get(1).ingredients());
		assertNull(response.get(1).price());
	}

}
