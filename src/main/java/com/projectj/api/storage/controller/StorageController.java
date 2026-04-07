package com.projectj.api.storage.controller;

import com.projectj.api.common.api.ApiResponse;
import com.projectj.api.player.dto.PlayerSnapshotResponse;
import com.projectj.api.storage.dto.StorageTransferRequest;
import com.projectj.api.storage.service.StorageService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/players/{playerId}/storage")
public class StorageController{

	private final StorageService storageService;

	public StorageController(StorageService storageService){
		this.storageService = storageService;
	}

	@PostMapping("/deposit")
	public ApiResponse<PlayerSnapshotResponse> deposit(
		@PathVariable String playerId,
		@Valid @RequestBody StorageTransferRequest request
	){
		return ApiResponse.success(storageService.deposit(playerId, request));
	}

	@PostMapping("/withdraw")
	public ApiResponse<PlayerSnapshotResponse> withdraw(
		@PathVariable String playerId,
		@Valid @RequestBody StorageTransferRequest request
	){
		return ApiResponse.success(storageService.withdraw(playerId, request));
	}

}
