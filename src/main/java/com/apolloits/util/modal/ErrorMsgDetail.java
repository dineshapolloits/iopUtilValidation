package com.apolloits.util.modal;

import lombok.Data;

@Data
public class ErrorMsgDetail {

	public ErrorMsgDetail(String fieldName, String errorMsg) {
		this.fieldName = fieldName;
		this.errorMsg = errorMsg;
	}
	private String fieldName;
	private String errorMsg;
}
