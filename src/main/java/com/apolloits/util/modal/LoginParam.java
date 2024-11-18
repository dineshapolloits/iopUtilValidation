package com.apolloits.util.modal;

import lombok.Data;

@Data
public class LoginParam {

	private String username;
	private String password;
	private String errorMsg;
}
