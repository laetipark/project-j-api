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
	name = "regions",
	uniqueConstraints = {
		@UniqueConstraint(name = "uk_regions_code", columnNames = "code")
	}
)
public class RegionEntity extends BaseTimeEntity{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 80)
	private String code;

	@Column(nullable = false, length = 120)
	private String name;

	@Column(nullable = false)
	private boolean active;

	protected RegionEntity(){
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

	public boolean isActive(){
		return active;
	}

	public void setActive(boolean active){
		this.active = active;
	}

}
