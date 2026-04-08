package com.projectj.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class JongguRestaurantApiApplication{

	public static void main(String[] args){
		SpringApplication.run(JongguRestaurantApiApplication.class, args);
	}

}
