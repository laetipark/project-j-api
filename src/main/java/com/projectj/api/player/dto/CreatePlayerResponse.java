package com.projectj.api.player.dto;

public record CreatePlayerResponse(
	String playerId,
	PlayerSnapshotResponse snapshot
){
}
