package com.projectj.api.catalog.service;

import com.projectj.api.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GoogleSheetRecipeRowMapperTest{

	private final GoogleSheetRecipeRowMapper mapper = new GoogleSheetRecipeRowMapper();

	@Test
	void mapUsesHeaderNamesAndKeepsRecipeIdsFromSheetIdColumn(){
		List<String> header = List.of(
			"메모",
			"재료 2",
			"레시피명",
			"가격",
			"id",
			"난이도",
			"재료 1",
			"조리법",
			"수급처",
			"재료 3",
			"재료 4",
			"재료 5",
			"재료 6",
			"재료 7"
		);
		List<String> firstRecipeRow = List.of(
			"한정 판매",
			"당근",
			"김밥",
			"1,250",
			"food_041",
			"☆2",
			"김",
			"칼+도마",
			"깊은 숲",
			"밥",
			"",
			"",
			"",
			""
		);
		List<String> secondRecipeRow = List.of(
			"",
			"",
			"육개장",
			"",
			"food_069",
			"",
			"소고기",
			"냄비",
			"바람언덕",
			"",
			"",
			"",
			"",
			""
		);

		List<RawSheetRecipe> response = mapper.map(List.of(header, firstRecipeRow, secondRecipeRow));

		assertEquals(2, response.size());
		assertEquals(2, response.get(0).rowNumber());
		assertEquals("food_041", response.get(0).recipeId());
		assertEquals("김밥", response.get(0).recipeName());
		assertEquals(2, response.get(0).difficulty());
		assertEquals(List.of("김", "당근", "밥"), response.get(0).ingredientNames());
		assertEquals(1250, response.get(0).price());
		assertEquals("한정 판매", response.get(0).memo());

		assertEquals(3, response.get(1).rowNumber());
		assertEquals("food_069", response.get(1).recipeId());
		assertEquals("육개장", response.get(1).recipeName());
		assertEquals(0, response.get(1).difficulty());
		assertEquals(List.of("소고기"), response.get(1).ingredientNames());
		assertEquals(0, response.get(1).price());
	}

	@Test
	void mapFailsWhenIdHeaderIsMissing(){
		List<List<String>> rows = List.of(
			List.of("레시피명", "난이도", "가격", "수급처", "조리법", "재료 1", "재료 2", "재료 3", "재료 4", "재료 5", "재료 6", "재료 7", "메모")
		);

		assertThrows(BusinessException.class, () -> mapper.map(rows));
	}

	@Test
	void mapFailsWhenRecipeRowHasNoIdValue(){
		List<List<String>> rows = List.of(
			List.of("id", "레시피명", "난이도", "가격", "수급처", "조리법", "재료 1", "재료 2", "재료 3", "재료 4", "재료 5", "재료 6", "재료 7", "메모"),
			List.of("", "김밥", "☆2", "900", "깊은 숲", "칼+도마", "김", "", "", "", "", "", "", "")
		);

		assertThrows(BusinessException.class, () -> mapper.map(rows));
	}

}
