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
		@UniqueConstraint(name = "uk_recipe_ingredients_recipe_resource", columnNames = {"recipe_id", "resource_id"})
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
	@JoinColumn(name = "resource_id", nullable = false, foreignKey = @ForeignKey(name = "fk_recipe_ingredients_resources"))
	private ResourceEntity resource;

	@Column(nullable = false)
	private int quantity;

	protected RecipeIngredientEntity(){
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
