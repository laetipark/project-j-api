package com.projectj.api.catalog.controller;

import com.projectj.api.catalog.dto.GoogleSheetRecipeCatalogResponse;
import com.projectj.api.catalog.service.GoogleSheetsRecipeCatalogService;
import com.projectj.api.common.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/catalog/google-sheets")
public class GoogleSheetsCatalogController{

	private final GoogleSheetsRecipeCatalogService googleSheetsRecipeCatalogService;

	public GoogleSheetsCatalogController(GoogleSheetsRecipeCatalogService googleSheetsRecipeCatalogService){
		this.googleSheetsRecipeCatalogService = googleSheetsRecipeCatalogService;
	}

	@GetMapping("/recipes")
	public ApiResponse<GoogleSheetRecipeCatalogResponse> getRecipes(){
		return ApiResponse.success(googleSheetsRecipeCatalogService.getRecipeCatalogResponse());
	}

	@PostMapping("/recipes/refresh")
	public ApiResponse<GoogleSheetRecipeCatalogResponse> refreshRecipes(){
		return ApiResponse.success(googleSheetsRecipeCatalogService.refreshRecipes());
	}

}
