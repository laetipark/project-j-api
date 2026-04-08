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
	name = "recipe_ingredients",
	uniqueConstraints = {
		@UniqueConstraint(name = "uk_recipe_ingredients_recipe_ingredient", columnNames = {"recipe_id", "ingredient_id"})
	}
)
public class RecipeIngredientEntity extends BaseTimeEntity{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "recipe_id", nullable = false, foreignKey = @ForeignKey(name = "fk_recipe_ingredients_recipes"))
	private RecipeEntity recipe;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "ingredient_id", nullable = false, foreignKey = @ForeignKey(name = "fk_recipe_ingredients_ingredients"))
	private IngredientEntity ingredient;

	@Column(nullable = false)
	private int quantity;

	@Column(name = "sort_order", nullable = false)
	private int sortOrder;

	public RecipeIngredientEntity(){
	}

	public Long getId(){
		return id;
	}

	public RecipeEntity getRecipe(){
		return recipe;
	}

	public void setRecipe(RecipeEntity recipe){
		this.recipe = recipe;
	}

	public IngredientEntity getIngredient(){
		return ingredient;
	}

	public void setIngredient(IngredientEntity ingredient){
		this.ingredient = ingredient;
	}

	public int getQuantity(){
		return quantity;
	}

	public void setQuantity(int quantity){
		this.quantity = quantity;
	}

	public int getSortOrder(){
		return sortOrder;
	}

	public void setSortOrder(int sortOrder){
		this.sortOrder = sortOrder;
	}

}
