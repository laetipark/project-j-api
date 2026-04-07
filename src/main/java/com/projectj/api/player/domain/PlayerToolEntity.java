package com.projectj.api.player.domain;

import com.projectj.api.catalog.domain.ToolEntity;
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
	name = "player_tools",
	uniqueConstraints = {
		@UniqueConstraint(name = "uk_player_tools_player_tool", columnNames = {"player_id", "tool_id"})
	}
)
public class PlayerToolEntity extends BaseTimeEntity{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "player_id", nullable = false, foreignKey = @ForeignKey(name = "fk_player_tools_players"))
	private PlayerEntity player;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "tool_id", nullable = false, foreignKey = @ForeignKey(name = "fk_player_tools_tools"))
	private ToolEntity tool;

	public PlayerToolEntity(){
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

	public ToolEntity getTool(){
		return tool;
	}

	public void setTool(ToolEntity tool){
		this.tool = tool;
	}

}
