package com.projectj.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.google-sheets")
public class GoogleSheetsProperties{

	private String credentialsPath;
	private String spreadsheetId;
	private Long ingredientSheetGid;
	private String ingredientSheetName = "재료";
	private Long recipeSheetGid;
	private String recipeSheetName = "레시피";
	private String refreshCron = "2 6 * * * *";

	public String getCredentialsPath(){
		return credentialsPath;
	}

	public void setCredentialsPath(String credentialsPath){
		this.credentialsPath = credentialsPath;
	}

	public String getSpreadsheetId(){
		return spreadsheetId;
	}

	public void setSpreadsheetId(String spreadsheetId){
		this.spreadsheetId = spreadsheetId;
	}

	public Long getRecipeSheetGid(){
		return recipeSheetGid;
	}

	public Long getIngredientSheetGid(){
		return ingredientSheetGid;
	}

	public void setIngredientSheetGid(Long ingredientSheetGid){
		this.ingredientSheetGid = ingredientSheetGid;
	}

	public String getIngredientSheetName(){
		return ingredientSheetName;
	}

	public void setIngredientSheetName(String ingredientSheetName){
		this.ingredientSheetName = ingredientSheetName;
	}

	public void setRecipeSheetGid(Long recipeSheetGid){
		this.recipeSheetGid = recipeSheetGid;
	}

	public String getRecipeSheetName(){
		return recipeSheetName;
	}

	public void setRecipeSheetName(String recipeSheetName){
		this.recipeSheetName = recipeSheetName;
	}

	public String getRefreshCron(){
		return refreshCron;
	}

	public void setRefreshCron(String refreshCron){
		this.refreshCron = refreshCron;
	}

	public boolean isConfigured(){
		return hasText(credentialsPath) && hasText(spreadsheetId);
	}

	public boolean hasRecipeTarget(){
		return recipeSheetGid != null || hasText(recipeSheetName);
	}

	public boolean hasIngredientTarget(){
		return ingredientSheetGid != null || hasText(ingredientSheetName);
	}

	private boolean hasText(String value){
		return value != null && !value.isBlank();
	}

}
