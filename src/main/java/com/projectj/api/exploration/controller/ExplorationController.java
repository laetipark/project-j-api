package com.projectj.api.exploration.controller;

import com.projectj.api.common.api.ApiResponse;
import com.projectj.api.exploration.dto.GatherRequest;
import com.projectj.api.exploration.dto.GatherResponse;
import com.projectj.api.exploration.dto.TravelRequest;
import com.projectj.api.exploration.service.ExplorationService;
import com.projectj.api.player.dto.PlayerSnapshotResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/players/{playerId}")
public class ExplorationController{

	private final ExplorationService explorationService;

	public ExplorationController(ExplorationService explorationService){
		this.explorationService = explorationService;
	}

	@PostMapping("/travel")
	public ApiResponse<PlayerSnapshotResponse> travel(
		@PathVariable String playerId,
		@Valid @RequestBody TravelRequest request
	){
		return ApiResponse.success(explorationService.travel(playerId, request));
	}

	@PostMapping("/gathers")
	public ApiResponse<GatherResponse> gather(
		@PathVariable String playerId,
		@Valid @RequestBody GatherRequest request
	){
		return ApiResponse.success(explorationService.gather(playerId, request));
	}

	@PostMapping("/exploration/skip")
	public ApiResponse<PlayerSnapshotResponse> skipExploration(@PathVariable String playerId){
		return ApiResponse.success(explorationService.skipExploration(playerId));
	}

}
