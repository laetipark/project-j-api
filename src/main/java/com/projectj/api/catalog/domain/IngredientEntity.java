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
	name = "ingredients",
	uniqueConstraints = {
		@UniqueConstraint(name = "uk_ingredients_ingredient_id", columnNames = "ingredient_id")
	}
)
public class IngredientEntity extends BaseTimeEntity{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "ingredient_id", nullable = false, length = 120)
	private String ingredientId;

	@Column(name = "ingredient_name", nullable = false, length = 120)
	private String ingredientName;

	@Column(nullable = false)
	private int difficulty;

	@Column(name = "supply_source", length = 120)
	private String supplySource;

	@Column(name = "acquisition_source", length = 120)
	private String acquisitionSource;

	@Column(name = "acquisition_method", length = 120)
	private String acquisitionMethod;

	@Column(name = "acquisition_tool", length = 120)
	private String acquisitionTool;

	@Column(name = "buy_price", nullable = false)
	private int buyPrice;

	@Column(name = "sell_price", nullable = false)
	private int sellPrice;

	@Column(length = 500)
	private String memo;

	@Column(nullable = false)
	private boolean active;

	public IngredientEntity(){
	}

	public Long getId(){
		return id;
	}

	public String getIngredientId(){
		return ingredientId;
	}

	public void setIngredientId(String ingredientId){
		this.ingredientId = ingredientId;
	}

	public String getIngredientName(){
		return ingredientName;
	}

	public void setIngredientName(String ingredientName){
		this.ingredientName = ingredientName;
	}

	public int getDifficulty(){
		return difficulty;
	}

	public void setDifficulty(int difficulty){
		this.difficulty = difficulty;
	}

	public String getSupplySource(){
		return supplySource;
	}

	public void setSupplySource(String supplySource){
		this.supplySource = supplySource;
	}

	public String getAcquisitionSource(){
		return acquisitionSource;
	}

	public void setAcquisitionSource(String acquisitionSource){
		this.acquisitionSource = acquisitionSource;
	}

	public String getAcquisitionMethod(){
		return acquisitionMethod;
	}

	public void setAcquisitionMethod(String acquisitionMethod){
		this.acquisitionMethod = acquisitionMethod;
	}

	public String getAcquisitionTool(){
		return acquisitionTool;
	}

	public void setAcquisitionTool(String acquisitionTool){
		this.acquisitionTool = acquisitionTool;
	}

	public int getBuyPrice(){
		return buyPrice;
	}

	public void setBuyPrice(int buyPrice){
		this.buyPrice = buyPrice;
	}

	public int getSellPrice(){
		return sellPrice;
	}

	public void setSellPrice(int sellPrice){
		this.sellPrice = sellPrice;
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
