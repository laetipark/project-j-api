package com.projectj.api.catalog.repository;

import com.projectj.api.catalog.domain.RegionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RegionRepository extends JpaRepository<RegionEntity, Long>{

	List<RegionEntity> findAllByActiveTrueOrderByIdAsc();

	Optional<RegionEntity> findByCode(String code);

}
