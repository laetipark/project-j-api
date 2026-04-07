package com.projectj.api.restaurant.controller;

import com.projectj.api.common.api.ApiResponse;
import com.projectj.api.player.dto.PlayerSnapshotResponse;
import com.projectj.api.restaurant.dto.SelectRecipeRequest;
import com.projectj.api.restaurant.dto.ServiceRunResponse;
import com.projectj.api.restaurant.service.RestaurantService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/players/{playerId}")
public class RestaurantController{

	private final RestaurantService restaurantService;

	public RestaurantController(RestaurantService restaurantService){
		this.restaurantService = restaurantService;
	}

	@PostMapping("/recipes/select")
	public ApiResponse<PlayerSnapshotResponse> selectRecipe(
		@PathVariable String playerId,
		@Valid @RequestBody SelectRecipeRequest request
	){
		return ApiResponse.success(restaurantService.selectRecipe(playerId, request));
	}

	@PostMapping("/service/run")
	public ApiResponse<ServiceRunResponse> runService(@PathVariable String playerId){
		return ApiResponse.success(restaurantService.runService(playerId));
	}

	@PostMapping("/service/skip")
	public ApiResponse<ServiceRunResponse> skipService(@PathVariable String playerId){
		return ApiResponse.success(restaurantService.skipService(playerId));
	}

}
