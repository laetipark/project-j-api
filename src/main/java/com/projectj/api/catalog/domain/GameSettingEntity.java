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

@Entity
@Table(name = "game_settings")
public class GameSettingEntity extends BaseTimeEntity{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "start_day", nullable = false)
	private int startDay;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "start_region_id", nullable = false, foreignKey = @ForeignKey(name = "fk_game_settings_regions"))
	private RegionEntity startRegion;

	@Column(name = "start_gold", nullable = false)
	private int startGold;

	@Column(name = "start_reputation", nullable = false)
	private int startReputation;

	@Column(name = "default_service_capacity", nullable = false)
	private int defaultServiceCapacity;

	@Column(name = "default_inventory_slot_limit", nullable = false)
	private int defaultInventorySlotLimit;

	protected GameSettingEntity(){
	}

	public Long getId(){
		return id;
	}

	public int getStartDay(){
		return startDay;
	}

	public void setStartDay(int startDay){
		this.startDay = startDay;
	}

	public RegionEntity getStartRegion(){
		return startRegion;
	}

	public void setStartRegion(RegionEntity startRegion){
		this.startRegion = startRegion;
	}

	public int getStartGold(){
		return startGold;
	}

	public void setStartGold(int startGold){
		this.startGold = startGold;
	}

	public int getStartReputation(){
		return startReputation;
	}

	public void setStartReputation(int startReputation){
		this.startReputation = startReputation;
	}

	public int getDefaultServiceCapacity(){
		return defaultServiceCapacity;
	}

	public void setDefaultServiceCapacity(int defaultServiceCapacity){
		this.defaultServiceCapacity = defaultServiceCapacity;
	}

	public int getDefaultInventorySlotLimit(){
		return defaultInventorySlotLimit;
	}

	public void setDefaultInventorySlotLimit(int defaultInventorySlotLimit){
		this.defaultInventorySlotLimit = defaultInventorySlotLimit;
	}

}
