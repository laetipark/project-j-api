package com.projectj.api.catalog.service;

import com.projectj.api.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GoogleSheetIngredientRowMapperTest{

	private final GoogleSheetIngredientRowMapper mapper = new GoogleSheetIngredientRowMapper();

	@Test
	void mapUsesIngredientHeadersAndKeepsSheetIds(){
		List<String> header = List.of("메모", "구매가격", "재료명", "id", "난이도", "수급처", "획득처", "획득방식", "획득도구", "판매가격");
		List<String> firstRow = List.of("기본 재료", "0", "김치", "ingredient_001", "☆0", "기본", "매장", "상시구비", "구매", "0");
		List<String> secondRow = List.of("", "0", "미역", "ingredient_014", "☆1", "바닷가", "바다", "어망 포인트", "어망", "0");

		List<SheetIngredient> result = mapper.map(List.of(header, firstRow, secondRow));

		assertEquals(2, result.size());
		assertEquals("ingredient_001", result.get(0).ingredientId());
		assertEquals("김치", result.get(0).ingredientName());
		assertEquals(0, result.get(0).difficulty());
		assertEquals("기본 재료", result.get(0).memo());
		assertEquals("ingredient_014", result.get(1).ingredientId());
		assertEquals("미역", result.get(1).ingredientName());
		assertEquals("어망", result.get(1).acquisitionTool());
	}

	@Test
	void mapFailsWhenIngredientIdHeaderIsMissing(){
		List<List<String>> rows = List.of(
			List.of("난이도", "재료명", "수급처", "획득처", "획득방식", "획득도구", "구매가격", "판매가격", "메모")
		);

		assertThrows(BusinessException.class, () -> mapper.map(rows));
	}

	@Test
	void mapFailsWhenIngredientRowHasNoIdValue(){
		List<List<String>> rows = List.of(
			List.of("id", "난이도", "재료명", "수급처", "획득처", "획득방식", "획득도구", "구매가격", "판매가격", "메모"),
			List.of("", "☆0", "김치", "기본", "매장", "상시구비", "구매", "0", "0", "")
		);

		assertThrows(BusinessException.class, () -> mapper.map(rows));
	}

}
