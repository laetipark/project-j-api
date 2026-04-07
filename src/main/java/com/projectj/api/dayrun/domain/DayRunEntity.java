package com.projectj.api.dayrun.domain;

import com.projectj.api.catalog.domain.RecipeEntity;
import com.projectj.api.common.domain.BaseTimeEntity;
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
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
	name = "day_runs",
	uniqueConstraints = {
		@UniqueConstraint(name = "uk_day_runs_player_day", columnNames = {"player_id", "day_number"})
	}
)
public class DayRunEntity extends BaseTimeEntity{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "player_id", nullable = false, foreignKey = @ForeignKey(name = "fk_day_runs_players"))
	private PlayerEntity player;

	@Column(name = "day_number", nullable = false)
	private int dayNumber;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "selected_recipe_id", foreignKey = @ForeignKey(name = "fk_day_runs_selected_recipes"))
	private RecipeEntity selectedRecipe;

	@Column(name = "gather_success_count", nullable = false)
	private int gatherSuccessCount;

	@Column(name = "gather_failure_count", nullable = false)
	private int gatherFailureCount;

	@Column(name = "total_gathered_quantity", nullable = false)
	private int totalGatheredQuantity;

	@Column(name = "service_skipped", nullable = false)
	private boolean serviceSkipped;

	@Column(name = "sold_count", nullable = false)
	private int soldCount;

	@Column(name = "earned_gold", nullable = false)
	private int earnedGold;

	@Column(name = "earned_reputation", nullable = false)
	private int earnedReputation;

	public DayRunEntity(){
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

	public int getDayNumber(){
		return dayNumber;
	}

	public void setDayNumber(int dayNumber){
		this.dayNumber = dayNumber;
	}

	public RecipeEntity getSelectedRecipe(){
		return selectedRecipe;
	}

	public void setSelectedRecipe(RecipeEntity selectedRecipe){
		this.selectedRecipe = selectedRecipe;
	}

	public int getGatherSuccessCount(){
		return gatherSuccessCount;
	}

	public void setGatherSuccessCount(int gatherSuccessCount){
		this.gatherSuccessCount = gatherSuccessCount;
	}

	public int getGatherFailureCount(){
		return gatherFailureCount;
	}

	public void setGatherFailureCount(int gatherFailureCount){
		this.gatherFailureCount = gatherFailureCount;
	}

	public int getTotalGatheredQuantity(){
		return totalGatheredQuantity;
	}

	public void setTotalGatheredQuantity(int totalGatheredQuantity){
		this.totalGatheredQuantity = totalGatheredQuantity;
	}

	public boolean isServiceSkipped(){
		return serviceSkipped;
	}

	public void setServiceSkipped(boolean serviceSkipped){
		this.serviceSkipped = serviceSkipped;
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

}
