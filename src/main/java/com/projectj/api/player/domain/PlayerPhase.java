package com.projectj.api.player.domain;

import com.projectj.api.common.exception.BusinessException;
import com.projectj.api.common.exception.ErrorCode;

public enum PlayerPhase{
	MORNING_EXPLORE("morning_explore"),
	AFTERNOON_SERVICE("afternoon_service"),
	SETTLEMENT("settlement");

	private final String code;

	PlayerPhase(String code){
		this.code = code;
	}

	public String getCode(){
		return code;
	}

	public static PlayerPhase fromCode(String code){
		for(PlayerPhase phase : values()){
			if(phase.code.equals(code)){
				return phase;
			}
		}
		throw new BusinessException(ErrorCode.INVALID_REQUEST, "Unknown phase code: " + code);
	}

}
