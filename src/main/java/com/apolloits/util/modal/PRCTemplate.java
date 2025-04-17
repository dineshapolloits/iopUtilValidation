package com.apolloits.util.modal;

import lombok.Data;

@Data
public class PRCTemplate {


	private String sequence;
	private String licensePlate;
	private String tran;
	private String state;
	//private String tagID;
	private String tranAmount;
	private String entryTranDate;
	private String entryPlaza;
	private String entryLane;
	private String exitTranDate;
	private String exitPlaza;
	private String exitLane;
	private String axleCount;
	//private String vehicleType;
	private String lpType;
	
	private String wrTranFee;
	private String wrFeeType;
	//private String guarantee;
	private String postAmt;
	private String responseCode;
	private String niopFee;
	private String originalFilename;
}
