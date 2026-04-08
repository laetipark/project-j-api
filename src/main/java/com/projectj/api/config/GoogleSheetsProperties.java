package com.projectj.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.google-sheets")
public class GoogleSheetsProperties{

	private String credentialsPath;
	private String spreadsheetId;
	private Long recipeSheetGid;
	private String recipeSheetName;
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

	private boolean hasText(String value){
		return value != null && !value.isBlank();
	}

}
