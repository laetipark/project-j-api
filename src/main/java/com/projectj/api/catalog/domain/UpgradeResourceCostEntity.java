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
	name = "upgrade_resource_costs",
	uniqueConstraints = {
		@UniqueConstraint(name = "uk_upgrade_resource_costs_upgrade_resource", columnNames = {"upgrade_id", "resource_id"})
	}
)
public class UpgradeResourceCostEntity extends BaseTimeEntity{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "upgrade_id", nullable = false, foreignKey = @ForeignKey(name = "fk_upgrade_resource_costs_upgrades"))
	private UpgradeEntity upgrade;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "resource_id", nullable = false, foreignKey = @ForeignKey(name = "fk_upgrade_resource_costs_resources"))
	private ResourceEntity resource;

	@Column(nullable = false)
	private int quantity;

	protected UpgradeResourceCostEntity(){
	}

	public Long getId(){
		return id;
	}

	public UpgradeEntity getUpgrade(){
		return upgrade;
	}

	public void setUpgrade(UpgradeEntity upgrade){
		this.upgrade = upgrade;
	}

	public ResourceEntity getResource(){
		return resource;
	}

	public void setResource(ResourceEntity resource){
		this.resource = resource;
	}

	public int getQuantity(){
		return quantity;
	}

	public void setQuantity(int quantity){
		this.quantity = quantity;
	}

}
