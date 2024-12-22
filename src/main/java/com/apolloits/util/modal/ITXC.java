package com.apolloits.util.modal;

import lombok.Data;

@Data
public class ITXC {
	
	private String corrReason;
	private String etcTrxSerialNum;
	private String etcRevenueDate;
	private String etcFacAgency;
	private String etcTrxType;
	private String etcEntryDateTime;
	private String etcEntryPlaza;
	private String etcEntryLane;
	private String etcTagAgency;
	private String etcTagSerialNumber;
	private String etcReadPerformance;
	private String etcWritePerf;
	private String etcTagPgmStatus;
	private String etcLaneMode;
	private String etcValidationStatus;
	private String etcLicState;
	private String etcLicNumber;
	private String etcLicType;
	private String etcClassCharged;
	private String etcActualAxles;
	private String etcExitSpeed;
	private String etcOverSpeed;
	private String etcExitDateTime;
	private String etcExitPlaza;
	private String etcExitLane;
	private String etcDebitCredit;
	private String etcTollAmount;
	
	@Override
	public String toString() {
		 StringBuilder sb = new StringBuilder();
		 	sb.append(corrReason);
		    sb.append(etcTrxSerialNum);
		    sb.append(etcRevenueDate);
		    sb.append(etcFacAgency);
		    sb.append(etcTrxType);
		    sb.append(etcEntryDateTime);
		    sb.append(etcEntryPlaza);
		    sb.append(etcEntryLane);
		    sb.append(etcTagAgency);
		    sb.append(etcTagSerialNumber);
		    sb.append(etcReadPerformance);
		    sb.append(etcWritePerf);
		    sb.append(etcTagPgmStatus);
		    sb.append(etcLaneMode);
		    sb.append(etcValidationStatus);
		    sb.append(etcLicState);
		    sb.append(etcLicNumber);
		    sb.append(etcLicType);
		    sb.append(etcClassCharged);
		    sb.append(etcActualAxles);
		    sb.append(etcExitSpeed);
		    sb.append(etcOverSpeed);
		    sb.append(etcExitDateTime);
		    sb.append(etcExitPlaza);
		    sb.append(etcExitLane);
		    sb.append(etcDebitCredit);
		    sb.append(etcTollAmount);
		    return sb.toString();
	}
}
