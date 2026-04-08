package com.projectj.api.catalog.repository;

import com.projectj.api.catalog.domain.RecipeIngredientEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecipeIngredientRepository extends JpaRepository<RecipeIngredientEntity, Long>{

	@EntityGraph(attributePaths = {"recipe", "ingredient"})
	List<RecipeIngredientEntity> findAllByDeletedAtIsNullOrderByIdAsc();

	@EntityGraph(attributePaths = {"ingredient"})
	List<RecipeIngredientEntity> findByRecipe_IdAndDeletedAtIsNullOrderBySortOrderAsc(Long recipeId);

}
