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
		@UniqueConstraint(name = "uk_recipes_recipe_id", columnNames = "recipe_id")
	}
)
public class RecipeEntity extends BaseTimeEntity{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "recipe_id", nullable = false, length = 120)
	private String recipeId;

	@Column(name = "recipe_name", nullable = false, length = 120)
	private String recipeName;

	@Column(name = "supply_source", length = 120)
	private String supplySource;

	@Column(nullable = false)
	private int difficulty;

	@Column(name = "cooking_method", length = 120)
	private String cookingMethod;

	@Column(nullable = false)
	private int price;

	@Column(length = 500)
	private String memo;

	@Column(nullable = false)
	private boolean active;

	public RecipeEntity(){
	}

	public Long getId(){
		return id;
	}

	public String getRecipeId(){
		return recipeId;
	}

	public void setRecipeId(String recipeId){
		this.recipeId = recipeId;
	}

	public String getRecipeName(){
		return recipeName;
	}

	public void setRecipeName(String recipeName){
		this.recipeName = recipeName;
	}

	public String getSupplySource(){
		return supplySource;
	}

	public void setSupplySource(String supplySource){
		this.supplySource = supplySource;
	}

	public int getDifficulty(){
		return difficulty;
	}

	public void setDifficulty(int difficulty){
		this.difficulty = difficulty;
	}

	public String getCookingMethod(){
		return cookingMethod;
	}

	public void setCookingMethod(String cookingMethod){
		this.cookingMethod = cookingMethod;
	}

	public int getPrice(){
		return price;
	}

	public void setPrice(int price){
		this.price = price;
	}

	public String getMemo(){
		return memo;
	}

	public void setMemo(String memo){
		this.memo = memo;
	}

	public boolean isActive(){
		return active;
	}

	public void setActive(boolean active){
		this.active = active;
	}

}
