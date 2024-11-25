package com.apolloits.util.modal;

import lombok.Data;

@Data
public class ErrorMsgDetail {

	public ErrorMsgDetail(String fileType,String fieldName, String errorMsg) {
		this.fileType = fileType;
		this.fieldName = fieldName;
		this.errorMsg = errorMsg;
	}
	private String fileType;
	private String fieldName;
	private String errorMsg;
}
