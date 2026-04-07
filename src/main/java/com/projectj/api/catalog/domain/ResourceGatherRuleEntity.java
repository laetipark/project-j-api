package com.projectj.api.catalog.domain;

import com.projectj.api.common.domain.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
	name = "resource_gather_rules",
	uniqueConstraints = {
		@UniqueConstraint(name = "uk_resource_gather_rules_region_resource", columnNames = {"region_id", "resource_id"})
	}
)
public class ResourceGatherRuleEntity extends BaseTimeEntity{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "region_id", nullable = false, foreignKey = @ForeignKey(name = "fk_resource_gather_rules_regions"))
	private RegionEntity region;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "resource_id", nullable = false, foreignKey = @ForeignKey(name = "fk_resource_gather_rules_resources"))
	private ResourceEntity resource;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "required_tool_id", foreignKey = @ForeignKey(name = "fk_resource_gather_rules_tools"))
	private ToolEntity requiredTool;

	@Column(nullable = false)
	private boolean active;

	protected ResourceGatherRuleEntity(){
	}

	public Long getId(){
		return id;
	}

	public RegionEntity getRegion(){
		return region;
	}

	public void setRegion(RegionEntity region){
		this.region = region;
	}

	public ResourceEntity getResource(){
		return resource;
	}

	public void setResource(ResourceEntity resource){
		this.resource = resource;
	}

	public ToolEntity getRequiredTool(){
		return requiredTool;
	}

	public void setRequiredTool(ToolEntity requiredTool){
		this.requiredTool = requiredTool;
	}

	public boolean isActive(){
		return active;
	}

	public void setActive(boolean active){
		this.active = active;
	}

}
