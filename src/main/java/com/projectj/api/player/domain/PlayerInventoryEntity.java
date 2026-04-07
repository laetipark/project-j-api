package com.projectj.api.player.domain;

import com.projectj.api.catalog.domain.ResourceEntity;
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
	name = "player_inventory",
	uniqueConstraints = {
		@UniqueConstraint(name = "uk_player_inventory_player_resource", columnNames = {"player_id", "resource_id"})
	}
)
public class PlayerInventoryEntity extends BaseTimeEntity{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "player_id", nullable = false, foreignKey = @ForeignKey(name = "fk_player_inventory_players"))
	private PlayerEntity player;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "resource_id", nullable = false, foreignKey = @ForeignKey(name = "fk_player_inventory_resources"))
	private ResourceEntity resource;

	@Column(nullable = false)
	private int quantity;

	public PlayerInventoryEntity(){
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
