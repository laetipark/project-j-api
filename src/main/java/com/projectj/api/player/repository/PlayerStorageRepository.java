package com.projectj.api.player.repository;

import com.projectj.api.player.domain.PlayerStorageEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlayerStorageRepository extends JpaRepository<PlayerStorageEntity, Long>{

	@EntityGraph(attributePaths = {"resource"})
	List<PlayerStorageEntity> findByPlayer_IdOrderByResource_CodeAsc(Long playerId);

	Optional<PlayerStorageEntity> findByPlayer_IdAndResource_Id(Long playerId, Long resourceId);

}
