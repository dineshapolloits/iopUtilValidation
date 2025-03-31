package com.apolloits.util.modal.niop.srecon;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XmlRootElement(name = "ReconciliationRecord")
@XmlAccessorType(XmlAccessType.FIELD)
public class ReconciliationRecord {

	 	@XmlElement(name = "TxnReferenceID")
	    private String txnReferenceID;
	    @XmlElement(name = "AdjustmentCount")
	    private String adjustmentCount;
	    @XmlElement(name = "ResubmitCount")
	    private String resubmitCount;
	    @XmlElement(name = "ReconHomeAgencyID")
	    private String reconHomeAgencyID;
	    @XmlElement(name = "HomeAgencyTxnRefID")
	    private String homeAgencyTxnRefID;
	    @XmlElement(name = "PostingDisposition")
	    private String postingDisposition;
	    @XmlElement(name = "DiscountPlanType")
	    private String discountPlanType;
	    @XmlElement(name = "PostedAmount")
	    private String postedAmount;
	    @XmlElement(name = "PostedDateTime")
	    private String postedDateTime;
	    @XmlElement(name = "TransFlatFee")
	    private String transFlatFee;
	    @XmlElement(name = "TransPercentFee")
	    private String transPercentFee;
	    @XmlElement(name = "Spare1")
	    private String spare1;
	    @XmlElement(name = "Spare2")
	    private String spare2;
	    @XmlElement(name = "Spare3")
		private String spare3;
	    @XmlElement(name = "Spare4")
	    private String spare4;
	    @XmlElement(name = "Spare5")
		private String spare5;
	    
		@Override
		public String toString() {
			return "ReconciliationRecord [txnReferenceID=" + txnReferenceID + ", adjustmentCount=" + adjustmentCount
					+ ", resubmitCount=" + resubmitCount + ", reconHomeAgencyID=" + reconHomeAgencyID
					+ ", homeAgencyTxnRefID=" + homeAgencyTxnRefID + ", postingDisposition=" + postingDisposition
					+ ", discountPlanType=" + discountPlanType + ", postedAmount=" + postedAmount + ", postedDateTime="
					+ postedDateTime + ", transFlatFee=" + transFlatFee + ", transPercentFee=" + transPercentFee
					+ ", spare1=" + spare1 + ", spare2=" + spare2 + ", spare3=" + spare3 + ", spare4=" + spare4
					+ ", spare5=" + spare5 + "]";
		}
	    
}
