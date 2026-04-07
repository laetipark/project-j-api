package com.projectj.api.player.service;

import com.projectj.api.common.exception.BusinessException;
import com.projectj.api.common.exception.ErrorCode;
import com.projectj.api.player.domain.PlayerEntity;
import com.projectj.api.player.domain.PlayerPhase;
import com.projectj.api.player.repository.PlayerRepository;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class PlayerSupportService{

	public static final String HUB_REGION_CODE = "Hub";

	private final PlayerRepository playerRepository;

	public PlayerSupportService(PlayerRepository playerRepository){
		this.playerRepository = playerRepository;
	}

	public PlayerEntity getPlayer(String playerId){
		return playerRepository.findDetailedByPublicId(playerId)
			.orElseThrow(() -> new BusinessException(ErrorCode.PLAYER_NOT_FOUND, "Player was not found: " + playerId));
	}

	public PlayerEntity getPlayerForUpdate(String playerId){
		return playerRepository.findLockedByPublicId(playerId)
			.orElseThrow(() -> new BusinessException(ErrorCode.PLAYER_NOT_FOUND, "Player was not found: " + playerId));
	}

	public void requirePhase(PlayerEntity player, String message, PlayerPhase... allowedPhases){
		boolean matches = Arrays.stream(allowedPhases).anyMatch(phase -> phase == player.getCurrentPhase());
		if(!matches){
			throw new BusinessException(ErrorCode.INVALID_PHASE, message);
		}
	}

	public void requireInHub(PlayerEntity player, String message){
		if(!isInHub(player)){
			throw new BusinessException(ErrorCode.INVALID_REGION, message);
		}
	}

	public boolean isInHub(PlayerEntity player){
		return HUB_REGION_CODE.equals(player.getCurrentRegion().getCode());
	}

}
