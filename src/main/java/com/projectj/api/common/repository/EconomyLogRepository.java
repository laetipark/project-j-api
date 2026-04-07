package com.projectj.api.common.repository;

import com.projectj.api.common.domain.EconomyLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EconomyLogRepository extends JpaRepository<EconomyLogEntity, Long>{
}
