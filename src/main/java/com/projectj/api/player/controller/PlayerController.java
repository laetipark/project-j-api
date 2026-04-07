package com.projectj.api.player.controller;

import com.projectj.api.common.api.ApiResponse;
import com.projectj.api.player.dto.CreatePlayerRequest;
import com.projectj.api.player.dto.CreatePlayerResponse;
import com.projectj.api.player.dto.PlayerSnapshotResponse;
import com.projectj.api.player.service.PlayerLifecycleService;
import com.projectj.api.player.service.PlayerSnapshotService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/players")
public class PlayerController{

	private final PlayerLifecycleService playerLifecycleService;
	private final PlayerSnapshotService playerSnapshotService;

	public PlayerController(PlayerLifecycleService playerLifecycleService, PlayerSnapshotService playerSnapshotService){
		this.playerLifecycleService = playerLifecycleService;
		this.playerSnapshotService = playerSnapshotService;
	}

	@PostMapping
	public ApiResponse<CreatePlayerResponse> createPlayer(@Valid @RequestBody(required = false) CreatePlayerRequest request){
		CreatePlayerRequest resolvedRequest = request == null ? new CreatePlayerRequest(null) : request;
		return ApiResponse.success(playerLifecycleService.createPlayer(resolvedRequest));
	}

	@GetMapping("/{playerId}/snapshot")
	public ApiResponse<PlayerSnapshotResponse> getSnapshot(@PathVariable String playerId){
		return ApiResponse.success(playerSnapshotService.getSnapshot(playerId));
	}

}
