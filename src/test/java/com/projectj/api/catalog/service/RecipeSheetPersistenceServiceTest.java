package com.projectj.api.catalog.service;

import com.projectj.api.catalog.domain.IngredientEntity;
import com.projectj.api.catalog.domain.RecipeEntity;
import com.projectj.api.catalog.domain.RecipeIngredientEntity;
import com.projectj.api.catalog.repository.IngredientRepository;
import com.projectj.api.catalog.repository.RecipeIngredientRepository;
import com.projectj.api.catalog.repository.RecipeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest(showSql = false)
@ActiveProfiles("test")
class RecipeSheetPersistenceServiceTest{

	@Autowired
	private IngredientRepository ingredientRepository;

	@Autowired
	private RecipeRepository recipeRepository;

	@Autowired
	private RecipeIngredientRepository recipeIngredientRepository;

	private RecipeSheetPersistenceService recipeSheetPersistenceService;

	@BeforeEach
	void setUp(){
		recipeSheetPersistenceService = new RecipeSheetPersistenceService(
			ingredientRepository,
			recipeRepository,
			recipeIngredientRepository
		);
	}

	@Test
	void synchronizeInsertsUpdatesSoftDeletesAndRevivesRecipesIngredientsAndRecipeIngredients(){
		recipeSheetPersistenceService.synchronize(
			List.of(
				new SheetIngredient(2, "ingredient_001", "Kimchi", 0, "Base", "Store", "Buy", "Purchase", 0, 0, "default"),
				new SheetIngredient(3, "ingredient_002", "Rice", 0, "Base", "Store", "Buy", "Purchase", 0, 0, null),
				new SheetIngredient(4, "ingredient_003", "Seaweed", 1, "Beach", "Rock", "Gather", "Hoe", 0, 0, null)
			),
			List.of(
				new SheetRecipe(
					2,
					"food_001",
					"Kimchi Rice",
					"Hub",
					1,
					"Pan",
					List.of(
						new SheetRecipeIngredient("ingredient_001", "Kimchi", 1),
						new SheetRecipeIngredient("ingredient_002", "Rice", 1)
					),
					1200,
					"first"
				),
				new SheetRecipe(
					3,
					"food_041",
					"Kimbap",
					"Forest",
					2,
					"Board",
					List.of(
						new SheetRecipeIngredient("ingredient_003", "Seaweed", 1),
						new SheetRecipeIngredient("ingredient_002", "Rice", 1)
					),
					1500,
					null
				)
			)
		);

		IngredientEntity insertedIngredient = ingredientRepository.findByIngredientId("ingredient_001").orElseThrow();
		RecipeEntity insertedRecipe = recipeRepository.findByRecipeId("food_001").orElseThrow();
		RecipeEntity removableRecipe = recipeRepository.findByRecipeId("food_041").orElseThrow();
		Long removableRecipeRowId = removableRecipe.getId();
		List<RecipeIngredientEntity> insertedRecipeIngredients = recipeIngredientRepository.findByRecipe_IdAndDeletedAtIsNullOrderBySortOrderAsc(insertedRecipe.getId());

		assertEquals("Kimchi", insertedIngredient.getIngredientName());
		assertTrue(insertedIngredient.isActive());
		assertNull(insertedIngredient.getDeletedAt());
		assertEquals("Kimchi Rice", insertedRecipe.getRecipeName());
		assertTrue(insertedRecipe.isActive());
		assertNull(insertedRecipe.getDeletedAt());
		assertEquals(2, insertedRecipeIngredients.size());
		assertEquals("ingredient_001", insertedRecipeIngredients.get(0).getIngredient().getIngredientId());
		assertEquals(1, insertedRecipeIngredients.get(0).getQuantity());
		assertEquals(0, insertedRecipeIngredients.get(0).getSortOrder());

		recipeSheetPersistenceService.synchronize(
			List.of(
				new SheetIngredient(2, "ingredient_001", "Kimchi Deluxe", 1, "Base", "Store", "Buy", "Purchase", 10, 2, "updated"),
				new SheetIngredient(3, "ingredient_002", "Rice", 0, "Base", "Store", "Buy", "Purchase", 0, 0, null)
			),
			List.of(
				new SheetRecipe(
					2,
					"food_001",
					"Kimchi Fried Rice",
					"Hub",
					3,
					"Pan",
					List.of(
						new SheetRecipeIngredient("ingredient_001", "Kimchi Deluxe", 1),
						new SheetRecipeIngredient("ingredient_002", "Rice", 2)
					),
					1600,
					"updated"
				)
			)
		);

		IngredientEntity updatedIngredient = ingredientRepository.findByIngredientId("ingredient_001").orElseThrow();
		IngredientEntity softDeletedIngredient = ingredientRepository.findByIngredientId("ingredient_003").orElseThrow();
		RecipeEntity updatedRecipe = recipeRepository.findByRecipeId("food_001").orElseThrow();
		RecipeEntity softDeletedRecipe = recipeRepository.findByRecipeId("food_041").orElseThrow();

		assertEquals("Kimchi Deluxe", updatedIngredient.getIngredientName());
		assertEquals(10, updatedIngredient.getBuyPrice());
		assertEquals("updated", updatedIngredient.getMemo());
		assertFalse(softDeletedIngredient.isActive());
		assertNotNull(softDeletedIngredient.getDeletedAt());
		assertEquals("Kimchi Fried Rice", updatedRecipe.getRecipeName());
		assertEquals(3, updatedRecipe.getDifficulty());
		assertEquals("updated", updatedRecipe.getMemo());
		assertFalse(softDeletedRecipe.isActive());
		assertNotNull(softDeletedRecipe.getDeletedAt());
		assertTrue(recipeRepository.findByRecipeIdAndActiveTrueAndDeletedAtIsNull("food_041").isEmpty());

		recipeSheetPersistenceService.synchronize(
			List.of(
				new SheetIngredient(2, "ingredient_001", "Kimchi Deluxe", 1, "Base", "Store", "Buy", "Purchase", 10, 2, "updated"),
				new SheetIngredient(3, "ingredient_002", "Rice", 0, "Base", "Store", "Buy", "Purchase", 0, 0, null),
				new SheetIngredient(4, "ingredient_003", "Seaweed", 1, "Beach", "Rock", "Gather", "Hoe", 0, 0, "revived")
			),
			List.of(
				new SheetRecipe(
					2,
					"food_001",
					"Kimchi Fried Rice",
					"Hub",
					3,
					"Pan",
					List.of(
						new SheetRecipeIngredient("ingredient_001", "Kimchi Deluxe", 1),
						new SheetRecipeIngredient("ingredient_002", "Rice", 2)
					),
					1600,
					"updated"
				),
				new SheetRecipe(
					5,
					"food_041",
					"Kimbap Deluxe",
					"Forest",
					4,
					"Board",
					List.of(
						new SheetRecipeIngredient("ingredient_003", "Seaweed", 1),
						new SheetRecipeIngredient("ingredient_002", "Rice", 1)
					),
					2100,
					"revived"
				)
			)
		);

		IngredientEntity revivedIngredient = ingredientRepository.findByIngredientIdAndActiveTrueAndDeletedAtIsNull("ingredient_003").orElseThrow();
		RecipeEntity revivedRecipe = recipeRepository.findByRecipeIdAndActiveTrueAndDeletedAtIsNull("food_041").orElseThrow();
		List<RecipeIngredientEntity> revivedRecipeIngredients = recipeIngredientRepository.findByRecipe_IdAndDeletedAtIsNullOrderBySortOrderAsc(revivedRecipe.getId());

		assertEquals("Seaweed", revivedIngredient.getIngredientName());
		assertEquals("revived", revivedIngredient.getMemo());
		assertNull(revivedIngredient.getDeletedAt());
		assertEquals(removableRecipeRowId, revivedRecipe.getId());
		assertEquals("Kimbap Deluxe", revivedRecipe.getRecipeName());
		assertTrue(revivedRecipe.isActive());
		assertNull(revivedRecipe.getDeletedAt());
		assertEquals(2, revivedRecipeIngredients.size());
		assertEquals("ingredient_003", revivedRecipeIngredients.get(0).getIngredient().getIngredientId());
		assertEquals("ingredient_002", revivedRecipeIngredients.get(1).getIngredient().getIngredientId());
	}

}
