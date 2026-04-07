package com.projectj.api.dayrun.controller;

import com.projectj.api.common.api.ApiResponse;
import com.projectj.api.dayrun.service.DayAdvanceService;
import com.projectj.api.player.dto.PlayerSnapshotResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/players/{playerId}/day")
public class DayRunController{

	private final DayAdvanceService dayAdvanceService;

	public DayRunController(DayAdvanceService dayAdvanceService){
		this.dayAdvanceService = dayAdvanceService;
	}

	@PostMapping("/next")
	public ApiResponse<PlayerSnapshotResponse> nextDay(@PathVariable String playerId){
		return ApiResponse.success(dayAdvanceService.nextDay(playerId));
	}

}
