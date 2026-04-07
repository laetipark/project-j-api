package com.projectj.api.exploration.repository;

import com.projectj.api.exploration.domain.GatherLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GatherLogRepository extends JpaRepository<GatherLogEntity, Long>{
}
