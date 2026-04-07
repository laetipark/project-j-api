package com.projectj.api.catalog.controller;

import com.projectj.api.catalog.dto.BootstrapResponse;
import com.projectj.api.catalog.service.BootstrapService;
import com.projectj.api.common.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class BootstrapController{

	private final BootstrapService bootstrapService;

	public BootstrapController(BootstrapService bootstrapService){
		this.bootstrapService = bootstrapService;
	}

	@GetMapping("/bootstrap")
	public ApiResponse<BootstrapResponse> getBootstrap(){
		return ApiResponse.success(bootstrapService.getBootstrap());
	}

}
