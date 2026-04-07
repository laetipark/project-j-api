package com.projectj.api.player.repository;

import com.projectj.api.player.domain.PlayerToolEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlayerToolRepository extends JpaRepository<PlayerToolEntity, Long>{

	@EntityGraph(attributePaths = {"tool"})
	List<PlayerToolEntity> findByPlayer_IdOrderByTool_CodeAsc(Long playerId);

	boolean existsByPlayer_IdAndTool_Id(Long playerId, Long toolId);

}
