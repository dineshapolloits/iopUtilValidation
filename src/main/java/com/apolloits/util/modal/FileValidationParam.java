package com.apolloits.util.modal;

import java.util.Date;

import lombok.Data;

@Data
public class FileValidationParam {
	private String fileType;
	private String validateType;
	private String fromAgency;
	private String toAgency;
	private String inputFilePath;
	private String outputFilePath;
	private String fileDate; //11-26-2024 DD-MM-YYYY
	
	private long recordCount;
	
	private String responseMsg;
	
	
}
