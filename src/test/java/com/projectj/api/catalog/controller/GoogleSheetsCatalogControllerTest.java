package com.projectj.api.catalog.controller;

import com.projectj.api.catalog.dto.GoogleSheetRecipeCatalogResponse;
import com.projectj.api.catalog.dto.GoogleSheetRecipeRowResponse;
import com.projectj.api.catalog.dto.RecipeIngredientResponse;
import com.projectj.api.catalog.service.GoogleSheetsRecipeCatalogService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GoogleSheetsCatalogController.class)
class GoogleSheetsCatalogControllerTest{

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private GoogleSheetsRecipeCatalogService googleSheetsRecipeCatalogService;

	@Test
	void refreshRecipesCallsServiceAndReturnsLatestSnapshot() throws Exception{
		GoogleSheetRecipeCatalogResponse response = new GoogleSheetRecipeCatalogResponse(
			"spreadsheet-123",
			123456789L,
			"레시피",
			Instant.parse("2026-04-08T03:00:00Z"),
			List.of(
				new GoogleSheetRecipeRowResponse(
					2,
					"food_041",
					"김밥",
					"깊은 숲",
					2,
					"칼+도마",
					List.of(
						new RecipeIngredientResponse("ingredient_006", "김", 1),
						new RecipeIngredientResponse("ingredient_002", "밥", 1)
					),
					1250,
					"한정"
				)
			)
		);
		given(googleSheetsRecipeCatalogService.refreshRecipes()).willReturn(response);

		mockMvc.perform(post("/api/v1/catalog/google-sheets/recipes/refresh"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.spreadsheetId").value("spreadsheet-123"))
			.andExpect(jsonPath("$.data.sheetGid").value(123456789))
			.andExpect(jsonPath("$.data.sheetTitle").value("레시피"))
			.andExpect(jsonPath("$.data.recipes[0].rowNumber").value(2))
			.andExpect(jsonPath("$.data.recipes[0].recipeId").value("food_041"))
			.andExpect(jsonPath("$.data.recipes[0].recipeName").value("김밥"))
			.andExpect(jsonPath("$.data.recipes[0].ingredients[0].ingredientId").value("ingredient_006"))
			.andExpect(jsonPath("$.data.recipes[0].ingredients[0].ingredientName").value("김"))
			.andExpect(jsonPath("$.data.recipes[0].ingredients[0].quantity").value(1))
			.andExpect(jsonPath("$.data.recipes[0].price").value(1250));

		verify(googleSheetsRecipeCatalogService).refreshRecipes();
	}

}
