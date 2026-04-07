package com.projectj.api.catalog.domain;

import com.projectj.api.common.domain.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
	name = "tools",
	uniqueConstraints = {
		@UniqueConstraint(name = "uk_tools_code", columnNames = "code")
	}
)
public class ToolEntity extends BaseTimeEntity{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 80)
	private String code;

	@Column(nullable = false, length = 120)
	private String name;

	@Column(name = "default_unlocked", nullable = false)
	private boolean defaultUnlocked;

	@Column(nullable = false)
	private boolean active;

	protected ToolEntity(){
	}

	public Long getId(){
		return id;
	}

	public String getCode(){
		return code;
	}

	public void setCode(String code){
		this.code = code;
	}

	public String getName(){
		return name;
	}

	public void setName(String name){
		this.name = name;
	}

	public boolean isDefaultUnlocked(){
		return defaultUnlocked;
	}

	public void setDefaultUnlocked(boolean defaultUnlocked){
		this.defaultUnlocked = defaultUnlocked;
	}

	public boolean isActive(){
		return active;
	}

	public void setActive(boolean active){
		this.active = active;
	}

}
