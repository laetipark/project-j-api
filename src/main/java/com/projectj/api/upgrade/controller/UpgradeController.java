package com.projectj.api.upgrade.controller;

import com.projectj.api.common.api.ApiResponse;
import com.projectj.api.upgrade.dto.UpgradePurchaseResponse;
import com.projectj.api.upgrade.service.UpgradePurchaseService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/players/{playerId}/upgrades")
public class UpgradeController{

	private final UpgradePurchaseService upgradePurchaseService;

	public UpgradeController(UpgradePurchaseService upgradePurchaseService){
		this.upgradePurchaseService = upgradePurchaseService;
	}

	@PostMapping("/{upgradeCode}/purchase")
	public ApiResponse<UpgradePurchaseResponse> purchase(
		@PathVariable String playerId,
		@PathVariable String upgradeCode
	){
		return ApiResponse.success(upgradePurchaseService.purchase(playerId, upgradeCode));
	}

}
