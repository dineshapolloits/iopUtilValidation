package com.apolloits.util.modal.niop.stran;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XmlRootElement(name = "TransactionRecord")
@XmlAccessorType(XmlAccessType.FIELD)
public class TransactionRecord {

	@XmlElement(name = "RecordType")
	private String recordType;
	@XmlElement(name = "TxnReferenceID")
	private String txnReferenceID;
	@XmlElement(name = "ExitDateTime")
	private String exitDateTime;
	@XmlElement(name = "FacilityID")
	private String facilityID;
	@XmlElement(name = "FacilityDesc")
	private String facilityDesc;
	@XmlElement(name = "ExitPlaza")
	private String exitPlaza;
	@XmlElement(name = "ExitPlazaDesc")
	private String exitPlazaDesc;
	/*@XmlElement(name = "EntryDateTime")
	private String entryDateTime;
	@XmlElement(name = "EntryLane")
	private String entryLane;
	@XmlElement(name = "EntryPlaza")
	private String entryPlaza;
	@XmlElement(name = "EntryPlazaDescription")
	private String entryPlazaDesc;*/
	@XmlElement(name = "ExitLane")
	private String exitLane;
	@XmlElement(name = "EntryData")
	private EntryData EntryData; 
	@XmlElement(name = "TagInfo")
	private TagInfo tagInfo;
	@XmlElement(name = "OccupancyInd")
	private String occupancyInd;
	@XmlElement(name = "VehicleClass")
	private String vehicleClass;
	@XmlElement(name = "TollAmount")
	private String tollAmount;
	@XmlElement(name = "DiscountPlanType")
	private String  discountPlanType;
	@XmlElement(name = "PlateInfo")
	private PlateInfo plateInfo;
	@XmlElement(name = "VehicleClassAdj")
	private String  vehicleClassAdj;
	@XmlElement(name = "SystemMatchInd")
	private String  systemMatchInd;
	@XmlElement(name = "Spare1")
	private String  spare1;
	@XmlElement(name = "Spare2")
	private String  spare2;
	@XmlElement(name = "Spare3")
	private String  spare3;
	@XmlElement(name = "Spare4")
	private String  spare4;
	@XmlElement(name = "Spare5")
	private String  spare5;
	@XmlElement(name = "ExitDateTimeTZ")
	private String exitDateTimeTZ;
	@XmlElement(name = "EntryDateTimeTZ")
	private String entryDateTimeTZ;
	
	private String postingDisposition;
	private String txnDataSeqNo;
	
	public void setTxnDataSeqNo(String txnDataSeqNo) {
		this.txnDataSeqNo = txnDataSeqNo;
	}

	public void setPostingDisposition(String postingDisposition) {
		this.postingDisposition = postingDisposition;
	}

	@Override
	public String toString() {
		return "TransactionRecord [recordType=" + recordType + ", txnReferenceID=" + txnReferenceID + ", exitDateTime="
				+ exitDateTime + ", facilityID=" + facilityID + ", facilityDesc=" + facilityDesc + ", exitPlaza="
				+ exitPlaza + ", exitPlazaDesc=" + exitPlazaDesc + ", exitLane=" + exitLane + ", EntryData=" + EntryData
				+ ", tagInfo=" + tagInfo + ", occupancyInd=" + occupancyInd + ", vehicleClass=" + vehicleClass
				+ ", tollAmount=" + tollAmount + ", discountPlanType=" + discountPlanType + ", plateInfo=" + plateInfo
				+ ", vehicleClassAdj=" + vehicleClassAdj + ", systemMatchInd=" + systemMatchInd + ", spare1=" + spare1
				+ ", spare2=" + spare2 + ", spare3=" + spare3 + ", spare4=" + spare4 + ", spare5=" + spare5
				+ ", exitDateTimeTZ=" + exitDateTimeTZ + ", entryDateTimeTZ=" + entryDateTimeTZ
				+ ", postingDisposition=" + postingDisposition + ", txnDataSeqNo=" + txnDataSeqNo + "]";
	}

}
