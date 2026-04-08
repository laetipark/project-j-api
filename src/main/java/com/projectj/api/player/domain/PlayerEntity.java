package com.projectj.api.player.domain;

import com.projectj.api.catalog.domain.RegionEntity;
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
import jakarta.persistence.Version;

@Entity
@Table(
	name = "players",
	uniqueConstraints = {
		@UniqueConstraint(name = "uk_players_public_id", columnNames = "public_id")
	}
)
public class PlayerEntity extends BaseTimeEntity{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "public_id", nullable = false, length = 36)
	private String publicId;

	@Column(name = "display_name", length = 120)
	private String displayName;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "current_region_id", nullable = false, foreignKey = @ForeignKey(name = "fk_players_current_regions"))
	private RegionEntity currentRegion;

	@Column(nullable = false)
	private int gold;

	@Column(nullable = false)
	private int reputation;

	@Column(name = "service_capacity", nullable = false)
	private int serviceCapacity;

	@Column(name = "inventory_slot_limit", nullable = false)
	private int inventorySlotLimit;

	@Column(name = "selected_recipe_id", length = 120)
	private String selectedRecipeId;

	@Version
	@Column(nullable = false)
	private long version;

	public PlayerEntity(){
	}

	public Long getId(){
		return id;
	}

	public String getPublicId(){
		return publicId;
	}

	public void setPublicId(String publicId){
		this.publicId = publicId;
	}

	public String getDisplayName(){
		return displayName;
	}

	public void setDisplayName(String displayName){
		this.displayName = displayName;
	}

	public RegionEntity getCurrentRegion(){
		return currentRegion;
	}

	public void setCurrentRegion(RegionEntity currentRegion){
		this.currentRegion = currentRegion;
	}

	public int getGold(){
		return gold;
	}

	public void setGold(int gold){
		this.gold = gold;
	}

	public int getReputation(){
		return reputation;
	}

	public void setReputation(int reputation){
		this.reputation = reputation;
	}

	public int getServiceCapacity(){
		return serviceCapacity;
	}

	public void setServiceCapacity(int serviceCapacity){
		this.serviceCapacity = serviceCapacity;
	}

	public int getInventorySlotLimit(){
		return inventorySlotLimit;
	}

	public void setInventorySlotLimit(int inventorySlotLimit){
		this.inventorySlotLimit = inventorySlotLimit;
	}

	public String getSelectedRecipeId(){
		return selectedRecipeId;
	}

	public void setSelectedRecipeId(String selectedRecipeId){
		this.selectedRecipeId = selectedRecipeId;
	}

	public long getVersion(){
		return version;
	}

}
