package com.projectj.api.catalog.service;

import java.util.List;

public interface RecipeCatalogService{

	List<SheetRecipe> getRecipes();

	SheetRecipe getRecipeById(String recipeId);

}
