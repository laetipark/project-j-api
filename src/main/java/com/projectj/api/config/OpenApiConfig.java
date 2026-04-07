package com.projectj.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig{

	@Bean
	public OpenAPI jongguRestaurantOpenApi(){
		return new OpenAPI().info(
			new Info()
				.title("Jonggu Restaurant API")
				.description("Unity prototype backend for player progression, economy, inventory, exploration, and restaurant flow.")
				.version("v1")
		);
	}

}
