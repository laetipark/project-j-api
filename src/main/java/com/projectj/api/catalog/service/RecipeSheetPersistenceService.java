package com.projectj.api.catalog.service;

import com.projectj.api.catalog.domain.IngredientEntity;
import com.projectj.api.catalog.domain.RecipeEntity;
import com.projectj.api.catalog.domain.RecipeIngredientEntity;
import com.projectj.api.catalog.repository.IngredientRepository;
import com.projectj.api.catalog.repository.RecipeIngredientRepository;
import com.projectj.api.catalog.repository.RecipeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class RecipeSheetPersistenceService{

	private final IngredientRepository ingredientRepository;
	private final RecipeRepository recipeRepository;
	private final RecipeIngredientRepository recipeIngredientRepository;

	public RecipeSheetPersistenceService(
		IngredientRepository ingredientRepository,
		RecipeRepository recipeRepository,
		RecipeIngredientRepository recipeIngredientRepository
	){
		this.ingredientRepository = ingredientRepository;
		this.recipeRepository = recipeRepository;
		this.recipeIngredientRepository = recipeIngredientRepository;
	}

	@Transactional
	public void synchronize(List<SheetIngredient> ingredients, List<SheetRecipe> recipes){
		Map<String, IngredientEntity> ingredientEntitiesById = synchronizeIngredients(ingredients);
		Map<String, RecipeEntity> recipeEntitiesById = synchronizeRecipes(recipes);
		synchronizeRecipeIngredients(recipes, recipeEntitiesById, ingredientEntitiesById);
	}

	private Map<String, IngredientEntity> synchronizeIngredients(List<SheetIngredient> ingredients){
		Map<String, IngredientEntity> existingByIngredientId = new HashMap<>();
		for(IngredientEntity ingredient : ingredientRepository.findAll()){
			existingByIngredientId.put(ingredient.getIngredientId(), ingredient);
		}

		Set<String> incomingIngredientIds = new HashSet<>();
		for(SheetIngredient ingredient : ingredients){
			incomingIngredientIds.add(ingredient.ingredientId());
			IngredientEntity entity = existingByIngredientId.get(ingredient.ingredientId());
			if(entity == null){
				entity = new IngredientEntity();
				entity.setIngredientId(ingredient.ingredientId());
			}
			applyIngredient(entity, ingredient);
			ingredientRepository.save(entity);
			existingByIngredientId.put(entity.getIngredientId(), entity);
		}

		Instant deletedAt = Instant.now();
		for(IngredientEntity entity : existingByIngredientId.values()){
			if(incomingIngredientIds.contains(entity.getIngredientId()) || entity.isDeleted()){
				continue;
			}
			entity.setActive(false);
			entity.markDeleted(deletedAt);
			ingredientRepository.save(entity);
		}
		return existingByIngredientId;
	}

	private Map<String, RecipeEntity> synchronizeRecipes(List<SheetRecipe> recipes){
		Map<String, RecipeEntity> existingByRecipeId = new HashMap<>();
		for(RecipeEntity recipe : recipeRepository.findAll()){
			existingByRecipeId.put(recipe.getRecipeId(), recipe);
		}

		Set<String> incomingRecipeIds = new HashSet<>();
		for(SheetRecipe recipe : recipes){
			incomingRecipeIds.add(recipe.recipeId());
			RecipeEntity entity = existingByRecipeId.get(recipe.recipeId());
			if(entity == null){
				entity = new RecipeEntity();
				entity.setRecipeId(recipe.recipeId());
			}
			applyRecipe(entity, recipe);
			recipeRepository.save(entity);
			existingByRecipeId.put(entity.getRecipeId(), entity);
		}

		Instant deletedAt = Instant.now();
		for(RecipeEntity entity : existingByRecipeId.values()){
			if(incomingRecipeIds.contains(entity.getRecipeId()) || entity.isDeleted()){
				continue;
			}
			entity.setActive(false);
			entity.markDeleted(deletedAt);
			recipeRepository.save(entity);
		}
		return existingByRecipeId;
	}

	private void synchronizeRecipeIngredients(
		List<SheetRecipe> recipes,
		Map<String, RecipeEntity> recipeEntitiesById,
		Map<String, IngredientEntity> ingredientEntitiesById
	){
		Map<String, RecipeIngredientEntity> existingByPairKey = new HashMap<>();
		for(RecipeIngredientEntity recipeIngredient : recipeIngredientRepository.findAll()){
			existingByPairKey.put(
				pairKey(recipeIngredient.getRecipe().getRecipeId(), recipeIngredient.getIngredient().getIngredientId()),
				recipeIngredient
			);
		}

		Set<String> incomingPairKeys = new HashSet<>();
		for(SheetRecipe recipe : recipes){
			RecipeEntity recipeEntity = recipeEntitiesById.get(recipe.recipeId());
			for(int index = 0; index < recipe.ingredients().size(); index++){
				SheetRecipeIngredient ingredient = recipe.ingredients().get(index);
				IngredientEntity ingredientEntity = ingredientEntitiesById.get(ingredient.ingredientId());
				String pairKey = pairKey(recipe.recipeId(), ingredient.ingredientId());
				incomingPairKeys.add(pairKey);

				RecipeIngredientEntity entity = existingByPairKey.get(pairKey);
				if(entity == null){
					entity = new RecipeIngredientEntity();
					entity.setRecipe(recipeEntity);
					entity.setIngredient(ingredientEntity);
				}
				entity.restore();
				entity.setQuantity(ingredient.quantity());
				entity.setSortOrder(index);
				recipeIngredientRepository.save(entity);
				existingByPairKey.put(pairKey, entity);
			}
		}

		Instant deletedAt = Instant.now();
		for(Map.Entry<String, RecipeIngredientEntity> entry : existingByPairKey.entrySet()){
			RecipeIngredientEntity entity = entry.getValue();
			if(incomingPairKeys.contains(entry.getKey()) || entity.isDeleted()){
				continue;
			}
			entity.markDeleted(deletedAt);
			recipeIngredientRepository.save(entity);
		}
	}

	private void applyIngredient(IngredientEntity entity, SheetIngredient ingredient){
		entity.setIngredientName(ingredient.ingredientName());
		entity.setDifficulty(ingredient.difficulty());
		entity.setSupplySource(ingredient.supplySource());
		entity.setAcquisitionSource(ingredient.acquisitionSource());
		entity.setAcquisitionMethod(ingredient.acquisitionMethod());
		entity.setAcquisitionTool(ingredient.acquisitionTool());
		entity.setBuyPrice(ingredient.buyPrice());
		entity.setSellPrice(ingredient.sellPrice());
		entity.setMemo(ingredient.memo());
		entity.setActive(true);
		entity.restore();
	}

	private void applyRecipe(RecipeEntity entity, SheetRecipe recipe){
		entity.setRecipeName(recipe.recipeName());
		entity.setSupplySource(recipe.supplySource());
		entity.setDifficulty(recipe.difficulty());
		entity.setCookingMethod(recipe.cookingMethod());
		entity.setPrice(recipe.price());
		entity.setMemo(recipe.memo());
		entity.setActive(true);
		entity.restore();
	}

	private String pairKey(String recipeId, String ingredientId){
		return recipeId + "::" + ingredientId;
	}

}
