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
	name = "recipes",
	uniqueConstraints = {
		@UniqueConstraint(name = "uk_recipes_code", columnNames = "code")
	}
)
public class RecipeEntity extends BaseTimeEntity{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 80)
	private String code;

	@Column(nullable = false, length = 120)
	private String name;

	@Column(nullable = false)
	private int difficulty;

	@Column(name = "sell_price", nullable = false)
	private int sellPrice;

	@Column(name = "reputation_reward", nullable = false)
	private int reputationReward;

	@Column(nullable = false)
	private boolean active;

	protected RecipeEntity(){
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

	public int getDifficulty(){
		return difficulty;
	}

	public void setDifficulty(int difficulty){
		this.difficulty = difficulty;
	}

	public int getSellPrice(){
		return sellPrice;
	}

	public void setSellPrice(int sellPrice){
		this.sellPrice = sellPrice;
	}

	public int getReputationReward(){
		return reputationReward;
	}

	public void setReputationReward(int reputationReward){
		this.reputationReward = reputationReward;
	}

	public boolean isActive(){
		return active;
	}

	public void setActive(boolean active){
		this.active = active;
	}

}
