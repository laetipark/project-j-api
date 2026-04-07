package com.projectj.api.player.dto;

import jakarta.validation.constraints.Size;

public record CreatePlayerRequest(
	@Size(max = 120, message = "displayName must be 120 characters or less.")
	String displayName
){
}
