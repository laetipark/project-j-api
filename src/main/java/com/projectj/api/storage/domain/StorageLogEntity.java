package com.projectj.api.storage.domain;

import com.projectj.api.catalog.domain.ResourceEntity;
import com.projectj.api.common.domain.BaseTimeEntity;
import com.projectj.api.player.domain.PlayerEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "storage_logs")
public class StorageLogEntity extends BaseTimeEntity{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "player_id", nullable = false, foreignKey = @ForeignKey(name = "fk_storage_logs_players"))
	private PlayerEntity player;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "resource_id", nullable = false, foreignKey = @ForeignKey(name = "fk_storage_logs_resources"))
	private ResourceEntity resource;

	@Enumerated(EnumType.STRING)
	@Column(name = "action_type", nullable = false, length = 40)
	private StorageActionType actionType;

	@Column(nullable = false)
	private int quantity;

	public StorageLogEntity(){
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

	public StorageActionType getActionType(){
		return actionType;
	}

	public void setActionType(StorageActionType actionType){
		this.actionType = actionType;
	}

	public int getQuantity(){
		return quantity;
	}

	public void setQuantity(int quantity){
		this.quantity = quantity;
	}

}
