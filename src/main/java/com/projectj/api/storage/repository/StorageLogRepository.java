package com.projectj.api.storage.repository;

import com.projectj.api.storage.domain.StorageLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StorageLogRepository extends JpaRepository<StorageLogEntity, Long>{
}
