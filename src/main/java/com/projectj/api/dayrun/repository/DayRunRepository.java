package com.projectj.api.dayrun.repository;

import com.projectj.api.dayrun.domain.DayRunEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DayRunRepository extends JpaRepository<DayRunEntity, Long>{

	@EntityGraph(attributePaths = {"selectedRecipe"})
	Optional<DayRunEntity> findByPlayer_IdAndDayNumber(Long playerId, int dayNumber);

	@EntityGraph(attributePaths = {"selectedRecipe"})
	Optional<DayRunEntity> findTopByPlayer_IdAndDayNumberLessThanOrderByDayNumberDesc(Long playerId, int dayNumber);

}
