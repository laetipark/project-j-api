package com.projectj.api.catalog.repository;

import com.projectj.api.catalog.domain.IngredientEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IngredientRepository extends JpaRepository<IngredientEntity, Long>{

	List<IngredientEntity> findAllByActiveTrueAndDeletedAtIsNullOrderByIdAsc();

	Optional<IngredientEntity> findByIngredientIdAndActiveTrueAndDeletedAtIsNull(String ingredientId);

	Optional<IngredientEntity> findByIngredientId(String ingredientId);

	Optional<IngredientEntity> findByIngredientNameAndActiveTrueAndDeletedAtIsNull(String ingredientName);

}
