package com.projectj.api.catalog.domain;

import com.projectj.api.common.domain.BaseTimeEntity;
import com.projectj.api.upgrade.domain.UpgradeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
	name = "upgrades",
	uniqueConstraints = {
		@UniqueConstraint(name = "uk_upgrades_code", columnNames = "code")
	}
)
public class UpgradeEntity extends BaseTimeEntity{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 80)
	private String code;

	@Column(nullable = false, length = 120)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(name = "upgrade_type", nullable = false, length = 40)
	private UpgradeType upgradeType;

	@Column(name = "target_value")
	private Integer targetValue;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "tool_id", foreignKey = @ForeignKey(name = "fk_upgrades_tools"))
	private ToolEntity tool;

	@Column(name = "gold_cost", nullable = false)
	private int goldCost;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "prerequisite_upgrade_id", foreignKey = @ForeignKey(name = "fk_upgrades_prerequisite"))
	private UpgradeEntity prerequisiteUpgrade;

	@Column(nullable = false)
	private boolean active;

	protected UpgradeEntity(){
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

	public UpgradeType getUpgradeType(){
		return upgradeType;
	}

	public void setUpgradeType(UpgradeType upgradeType){
		this.upgradeType = upgradeType;
	}

	public Integer getTargetValue(){
		return targetValue;
	}

	public void setTargetValue(Integer targetValue){
		this.targetValue = targetValue;
	}

	public ToolEntity getTool(){
		return tool;
	}

	public void setTool(ToolEntity tool){
		this.tool = tool;
	}

	public int getGoldCost(){
		return goldCost;
	}

	public void setGoldCost(int goldCost){
		this.goldCost = goldCost;
	}

	public UpgradeEntity getPrerequisiteUpgrade(){
		return prerequisiteUpgrade;
	}

	public void setPrerequisiteUpgrade(UpgradeEntity prerequisiteUpgrade){
		this.prerequisiteUpgrade = prerequisiteUpgrade;
	}

	public boolean isActive(){
		return active;
	}

	public void setActive(boolean active){
		this.active = active;
	}

}
