package com.apolloits.util.modal.niop.srecon;

import java.io.Serializable;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XmlRootElement(name = "ReconciliationHeader")
@XmlAccessorType(XmlAccessType.FIELD)
public class ReconciliationHeader implements Serializable {
	@XmlElement(name = "SubmissionType")
	private String submissionType;
	@XmlElement(name = "SubmissionDateTime")
	private String submissionDateTime;
	@XmlElement(name = "SSIOPHubID")
	private String ssiopHubID;
	@XmlElement(name = "AwayAgencyID")
	private String awayAgencyID;
	@XmlElement(name = "HomeAgencyID")
	private String homeAgencyID;
	@XmlElement(name = "TxnDataSeqNo")
	private String txnDataSeqNo;
	@XmlElement(name = "RecordCount")
	private String recordCount;
	@Override
	public String toString() {
		return "SreconHeader [submissionType=" + submissionType + ", submissionDateTime=" + submissionDateTime
				+ ", ssiopHubID=" + ssiopHubID + ", awayAgencyID=" + awayAgencyID + ", homeAgencyID=" + homeAgencyID
				+ ", txnDataSeqNo=" + txnDataSeqNo + ", recordCount=" + recordCount + "]";
	}
	
	
}
