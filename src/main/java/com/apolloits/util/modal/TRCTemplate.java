package com.apolloits.util.modal;

import lombok.Data;

@Data
public class TRCTemplate {


	private String sequence;
	private String tagID;
	private String tran;
	private String tranAmount;
	private String entryTranDate;
	private String entryPlaza;
	private String entryLane;
	private String exitTranDate;
	private String exitPlaza;
	private String exitLane;
	private String axleCount;
	private String occupancy;
	private String protocolType;
	private String vehicleType;
	private String wrTranFee;
	private String wrFeeType;
	private String guarantee;
	private String postAmt;
	private String responseCode;
	private String niopFee;
	private String originalFilename;

}
