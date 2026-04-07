package com.projectj.api.exploration.domain;

import com.projectj.api.catalog.domain.RegionEntity;
import com.projectj.api.catalog.domain.ResourceEntity;
import com.projectj.api.common.domain.BaseTimeEntity;
import com.projectj.api.dayrun.domain.DayRunEntity;
import com.projectj.api.player.domain.PlayerEntity;
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
@Table(name = "gather_logs")
public class GatherLogEntity extends BaseTimeEntity{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "player_id", nullable = false, foreignKey = @ForeignKey(name = "fk_gather_logs_players"))
	private PlayerEntity player;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "day_run_id", nullable = false, foreignKey = @ForeignKey(name = "fk_gather_logs_day_runs"))
	private DayRunEntity dayRun;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "region_id", nullable = false, foreignKey = @ForeignKey(name = "fk_gather_logs_regions"))
	private RegionEntity region;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "resource_id", nullable = false, foreignKey = @ForeignKey(name = "fk_gather_logs_resources"))
	private ResourceEntity resource;

	@Column(name = "quantity_requested", nullable = false)
	private int quantityRequested;

	@Column(name = "quantity_granted", nullable = false)
	private int quantityGranted;

	@Column(nullable = false)
	private boolean success;

	@Column(name = "failure_reason", length = 200)
	private String failureReason;

	public GatherLogEntity(){
	}

	public Long getId(){
		return id;
	}

	public PlayerEntity getPlayer(){
		return player;
	}

	public void setPlayer(PlayerEntity player){
		this.player = player;
	}

	public DayRunEntity getDayRun(){
		return dayRun;
	}

	public void setDayRun(DayRunEntity dayRun){
		this.dayRun = dayRun;
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

	public int getQuantityRequested(){
		return quantityRequested;
	}

	public void setQuantityRequested(int quantityRequested){
		this.quantityRequested = quantityRequested;
	}

	public int getQuantityGranted(){
		return quantityGranted;
	}

	public void setQuantityGranted(int quantityGranted){
		this.quantityGranted = quantityGranted;
	}

	public boolean isSuccess(){
		return success;
	}

	public void setSuccess(boolean success){
		this.success = success;
	}

	public String getFailureReason(){
		return failureReason;
	}

	public void setFailureReason(String failureReason){
		this.failureReason = failureReason;
	}

}
