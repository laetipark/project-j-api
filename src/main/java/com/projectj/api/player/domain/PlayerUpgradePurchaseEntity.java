package com.projectj.api.player.domain;

import com.projectj.api.catalog.domain.UpgradeEntity;
import com.projectj.api.common.domain.BaseTimeEntity;
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
	name = "player_upgrade_purchases",
	uniqueConstraints = {
		@UniqueConstraint(name = "uk_player_upgrade_purchases_player_upgrade", columnNames = {"player_id", "upgrade_id"})
	}
)
public class PlayerUpgradePurchaseEntity extends BaseTimeEntity{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "player_id", nullable = false, foreignKey = @ForeignKey(name = "fk_player_upgrade_purchases_players"))
	private PlayerEntity player;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "upgrade_id", nullable = false, foreignKey = @ForeignKey(name = "fk_player_upgrade_purchases_upgrades"))
	private UpgradeEntity upgrade;

	public PlayerUpgradePurchaseEntity(){
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

	public UpgradeEntity getUpgrade(){
		return upgrade;
	}

	public void setUpgrade(UpgradeEntity upgrade){
		this.upgrade = upgrade;
	}

}
