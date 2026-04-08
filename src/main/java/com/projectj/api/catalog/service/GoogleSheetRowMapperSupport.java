package com.projectj.api.catalog.service;

import com.projectj.api.common.exception.BusinessException;
import com.projectj.api.common.exception.ErrorCode;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class GoogleSheetRowMapperSupport{

	private static final Pattern NUMBER_PATTERN = Pattern.compile("(\\d+)");

	private GoogleSheetRowMapperSupport(){
	}

	static int requiredHeaderIndex(List<String> headerRow, String headerName, String sheetType){
		for(int index = 0; index < headerRow.size(); index++){
			String value = headerRow.get(index);
			if(value != null && headerName.equals(value.trim())){
				return index;
			}
		}
		throw new BusinessException(
			ErrorCode.GOOGLE_SHEETS_INVALID_FORMAT,
			"Google Sheets %s header is missing: %s".formatted(sheetType, headerName)
		);
	}

	static String requiredCell(List<String> row, int index, String headerName, int rowNumber, String sheetType){
		String value = cell(row, index);
		if(value == null){
			throw new BusinessException(
				ErrorCode.GOOGLE_SHEETS_INVALID_FORMAT,
				"Google Sheets %s value is required. header=%s row=%d".formatted(sheetType, headerName, rowNumber)
			);
		}
		return value;
	}

	static String cell(List<String> row, int index){
		if(index >= row.size()){
			return null;
		}
		String value = row.get(index);
		if(value == null){
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	static int parseRequiredNumber(String value, String headerName, int rowNumber, String sheetType){
		if(value == null){
			return 0;
		}
		Matcher matcher = NUMBER_PATTERN.matcher(value.replace(",", ""));
		if(!matcher.find()){
			throw new BusinessException(
				ErrorCode.GOOGLE_SHEETS_INVALID_FORMAT,
				"Google Sheets %s value is invalid. header=%s row=%d value=%s".formatted(
					sheetType,
					headerName,
					rowNumber,
					value
				)
			);
		}
		return Integer.parseInt(matcher.group(1));
	}

}
