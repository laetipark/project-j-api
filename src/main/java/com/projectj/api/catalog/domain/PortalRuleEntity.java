package com.projectj.api.catalog.domain;

import com.projectj.api.common.domain.BaseTimeEntity;
import com.projectj.api.player.domain.PlayerPhase;
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
	name = "portal_rules",
	uniqueConstraints = {
		@UniqueConstraint(name = "uk_portal_rules_code", columnNames = "code")
	}
)
public class PortalRuleEntity extends BaseTimeEntity{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 80)
	private String code;

	@Column(nullable = false, length = 120)
	private String name;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "from_region_id", nullable = false, foreignKey = @ForeignKey(name = "fk_portal_rules_from_regions"))
	private RegionEntity fromRegion;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "to_region_id", nullable = false, foreignKey = @ForeignKey(name = "fk_portal_rules_to_regions"))
	private RegionEntity toRegion;

	@Column(name = "required_phase", nullable = false, length = 40)
	private PlayerPhase requiredPhase;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "required_tool_id", foreignKey = @ForeignKey(name = "fk_portal_rules_tools"))
	private ToolEntity requiredTool;

	@Column(name = "required_reputation", nullable = false)
	private int requiredReputation;

	@Column(nullable = false)
	private boolean active;

	protected PortalRuleEntity(){
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

	public RegionEntity getFromRegion(){
		return fromRegion;
	}

	public void setFromRegion(RegionEntity fromRegion){
		this.fromRegion = fromRegion;
	}

	public RegionEntity getToRegion(){
		return toRegion;
	}

	public void setToRegion(RegionEntity toRegion){
		this.toRegion = toRegion;
	}

	public PlayerPhase getRequiredPhase(){
		return requiredPhase;
	}

	public void setRequiredPhase(PlayerPhase requiredPhase){
		this.requiredPhase = requiredPhase;
	}

	public ToolEntity getRequiredTool(){
		return requiredTool;
	}

	public void setRequiredTool(ToolEntity requiredTool){
		this.requiredTool = requiredTool;
	}

	public int getRequiredReputation(){
		return requiredReputation;
	}

	public void setRequiredReputation(int requiredReputation){
		this.requiredReputation = requiredReputation;
	}

	public boolean isActive(){
		return active;
	}

	public void setActive(boolean active){
		this.active = active;
	}

}
