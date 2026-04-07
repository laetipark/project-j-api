package com.projectj.api.catalog.dto;

public record ToolDefinitionResponse(
	String code,
	String name,
	boolean defaultUnlocked
){
}
