package com.apolloits.util.modal;

import lombok.Data;

@Data
public class FileValidationParam {
	private String fileType;
	private String validateType;
	private String fromAgency;
	private String toAgency;
	private String inputFilePath;
	private String outputFilePath;
	
	private long recordCount;
	
	private String responseMsg;
	
	
}
