package com.projectj.api.common.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import java.time.Instant;

@MappedSuperclass
public abstract class BaseTimeEntity{

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Column(name = "deleted_at")
	private Instant deletedAt;

	@PrePersist
	protected void onCreate(){
		Instant now = Instant.now();
		this.createdAt = now;
		this.updatedAt = now;
	}

	@PreUpdate
	protected void onUpdate(){
		this.updatedAt = Instant.now();
	}

	public Instant getCreatedAt(){
		return createdAt;
	}

	public Instant getUpdatedAt(){
		return updatedAt;
	}

	public Instant getDeletedAt(){
		return deletedAt;
	}

	protected void setCreatedAt(Instant createdAt){
		this.createdAt = createdAt;
	}

	protected void setUpdatedAt(Instant updatedAt){
		this.updatedAt = updatedAt;
	}

	public void setDeletedAt(Instant deletedAt){
		this.deletedAt = deletedAt;
	}

	public void restore(){
		this.deletedAt = null;
	}

	public void markDeleted(Instant deletedAt){
		this.deletedAt = deletedAt;
	}

	public boolean isDeleted(){
		return deletedAt != null;
	}

}
