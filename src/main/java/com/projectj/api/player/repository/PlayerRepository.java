package com.projectj.api.player.repository;

import com.projectj.api.player.domain.PlayerEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PlayerRepository extends JpaRepository<PlayerEntity, Long>{

	Optional<PlayerEntity> findByPublicId(String publicId);

	@EntityGraph(attributePaths = {"currentRegion", "selectedRecipe"})
	@Query("select player from PlayerEntity player where player.publicId = :publicId")
	Optional<PlayerEntity> findDetailedByPublicId(@Param("publicId") String publicId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@EntityGraph(attributePaths = {"currentRegion", "selectedRecipe"})
	@Query("select player from PlayerEntity player where player.publicId = :publicId")
	Optional<PlayerEntity> findLockedByPublicId(@Param("publicId") String publicId);

}
