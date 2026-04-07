package com.projectj.api.player.repository;

import com.projectj.api.player.domain.PlayerInventoryEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlayerInventoryRepository extends JpaRepository<PlayerInventoryEntity, Long>{

	@EntityGraph(attributePaths = {"resource"})
	List<PlayerInventoryEntity> findByPlayer_IdOrderByResource_CodeAsc(Long playerId);

	Optional<PlayerInventoryEntity> findByPlayer_IdAndResource_Id(Long playerId, Long resourceId);

	long countByPlayer_IdAndQuantityGreaterThan(Long playerId, int quantity);

}
