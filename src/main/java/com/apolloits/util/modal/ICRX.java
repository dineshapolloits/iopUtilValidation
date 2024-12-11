package com.apolloits.util.modal;

import lombok.Data;

@Data
public class ICRX {
	private String etcTrxSerialNum;
	private String etcPostStatus;
	private String etcPostPlan;
	private String etcDebitCredit;
	private String etcOwedAmount;
	private String etcDupSerialNum;
	
	@Override
	public String toString() {
		 StringBuilder sb = new StringBuilder();
		    sb.append(etcTrxSerialNum);
		    sb.append(etcPostStatus);
		    sb.append(etcPostPlan);
		    sb.append(etcDebitCredit);
		    sb.append(etcOwedAmount);
		    sb.append(etcDupSerialNum);
		    return sb.toString();
	}
}
