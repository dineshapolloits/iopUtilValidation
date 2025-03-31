package com.apolloits.util.modal.niop.scorr;

import jakarta.xml.bind.annotation.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XmlRootElement(name = "CorrectionRecord")
@XmlAccessorType(XmlAccessType.FIELD)
public class CorrectionRecord {

    @XmlElement(name = "RecordType", required = true)
    private String recordType;
    @XmlElement(name = "CorrectionDateTime")
    private String correctionDateTime;
    @XmlElement(name = "CorrectionReason")
    private String correctionReason;
    @XmlElement(name = "ResubmitReason")
    private String resubmitReason;
    @XmlElement(name = "CorrectionOtherDesc")
    private String correctionOtherDesc;
    @XmlElement(name = "CorrectionSeqNo")
    private String correctionSeqNo;
    @XmlElement(name = "ResubmitCount")
    private String resubmitCount;
    @XmlElement(name = "HomeAgencyTxnRefID")
    private String homeAgencyTxnRefID;
    @XmlElement(name = "OriginalTransactionDetail", required = true)
    private OriginalTransactionDetail originalTransactionDetail;
    
    private String postingDisposition;
	private String txnDataSeqNo;
	@Override
	public String toString() {
		return "CorrectionRecord [recordType=" + recordType + ", correctionDateTime=" + correctionDateTime
				+ ", correctionReason=" + correctionReason + ", resubmitReason=" + resubmitReason
				+ ", correctionOtherDesc=" + correctionOtherDesc + ", correctionSeqNo=" + correctionSeqNo
				+ ", resubmitCount=" + resubmitCount + ", homeAgencyTxnRefID=" + homeAgencyTxnRefID
				+ ", originalTransactionDetail=" + originalTransactionDetail + "]";
	}
    
    
}
