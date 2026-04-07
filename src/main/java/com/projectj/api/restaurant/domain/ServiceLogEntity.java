package com.projectj.api.restaurant.domain;

import com.projectj.api.catalog.domain.RecipeEntity;
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
@Table(name = "service_logs")
public class ServiceLogEntity extends BaseTimeEntity{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "player_id", nullable = false, foreignKey = @ForeignKey(name = "fk_service_logs_players"))
	private PlayerEntity player;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "day_run_id", nullable = false, foreignKey = @ForeignKey(name = "fk_service_logs_day_runs"))
	private DayRunEntity dayRun;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "recipe_id", foreignKey = @ForeignKey(name = "fk_service_logs_recipes"))
	private RecipeEntity recipe;

	@Column(name = "requested_capacity", nullable = false)
	private int requestedCapacity;

	@Column(name = "cookable_count", nullable = false)
	private int cookableCount;

	@Column(name = "sold_count", nullable = false)
	private int soldCount;

	@Column(name = "earned_gold", nullable = false)
	private int earnedGold;

	@Column(name = "earned_reputation", nullable = false)
	private int earnedReputation;

	@Column(nullable = false)
	private boolean skipped;

	public ServiceLogEntity(){
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

	public RecipeEntity getRecipe(){
		return recipe;
	}

	public void setRecipe(RecipeEntity recipe){
		this.recipe = recipe;
	}

	public int getRequestedCapacity(){
		return requestedCapacity;
	}

	public void setRequestedCapacity(int requestedCapacity){
		this.requestedCapacity = requestedCapacity;
	}

	public int getCookableCount(){
		return cookableCount;
	}

	public void setCookableCount(int cookableCount){
		this.cookableCount = cookableCount;
	}

	public int getSoldCount(){
		return soldCount;
	}

	public void setSoldCount(int soldCount){
		this.soldCount = soldCount;
	}

	public int getEarnedGold(){
		return earnedGold;
	}

	public void setEarnedGold(int earnedGold){
		this.earnedGold = earnedGold;
	}

	public int getEarnedReputation(){
		return earnedReputation;
	}

	public void setEarnedReputation(int earnedReputation){
		this.earnedReputation = earnedReputation;
	}

	public boolean isSkipped(){
		return skipped;
	}

	public void setSkipped(boolean skipped){
		this.skipped = skipped;
	}

}
