package com.projectj.api.restaurant.repository;

import com.projectj.api.restaurant.domain.ServiceLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceLogRepository extends JpaRepository<ServiceLogEntity, Long>{
}
