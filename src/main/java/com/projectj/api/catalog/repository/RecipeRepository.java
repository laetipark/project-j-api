package com.projectj.api.catalog.repository;

import com.projectj.api.catalog.domain.RecipeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RecipeRepository extends JpaRepository<RecipeEntity, Long>{

	List<RecipeEntity> findAllByActiveTrueOrderByIdAsc();

	Optional<RecipeEntity> findByCode(String code);

}
