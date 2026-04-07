package com.projectj.api.catalog.repository;

import com.projectj.api.catalog.domain.GameSettingEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GameSettingRepository extends JpaRepository<GameSettingEntity, Long>{

	@EntityGraph(attributePaths = {"startRegion"})
	Optional<GameSettingEntity> findTopByOrderByIdAsc();

}
