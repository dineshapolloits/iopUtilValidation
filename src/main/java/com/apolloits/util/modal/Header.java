package com.apolloits.util.modal;

import lombok.Data;

@Data
public class Header {

	private String fileType;
	private String version;
	private String fromAgencyId;
	private String toAgencyId;
	private String fileDateTime;
	private String recordCount;
	private String ictxfileNum;
	
	
	@Override
	public String toString() {
		 StringBuilder sb = new StringBuilder();
		 sb.append(fileType);
		 sb.append(version);
		 sb.append(fromAgencyId);
		 sb.append(toAgencyId);
		 sb.append(fileDateTime);
		 sb.append(recordCount);
		 sb.append(ictxfileNum);
		 return sb.toString();
	}
	
	
}
