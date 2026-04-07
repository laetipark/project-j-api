package com.projectj.api.catalog.repository;

import com.projectj.api.catalog.domain.ResourceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ResourceRepository extends JpaRepository<ResourceEntity, Long>{

	List<ResourceEntity> findAllByActiveTrueOrderByIdAsc();

	Optional<ResourceEntity> findByCode(String code);

}
