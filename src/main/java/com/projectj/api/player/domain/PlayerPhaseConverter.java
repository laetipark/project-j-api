package com.projectj.api.player.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PlayerPhaseConverter implements AttributeConverter<PlayerPhase, String>{

	@Override
	public String convertToDatabaseColumn(PlayerPhase attribute){
		if(attribute == null){
			return null;
		}
		return attribute.getCode();
	}

	@Override
	public PlayerPhase convertToEntityAttribute(String dbData){
		if(dbData == null){
			return null;
		}
		return PlayerPhase.fromCode(dbData);
	}

}
