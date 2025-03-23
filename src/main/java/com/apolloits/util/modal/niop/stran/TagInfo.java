package com.apolloits.util.modal.niop.stran;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XmlRootElement(name = "TagInfo")
@XmlAccessorType(XmlAccessType.FIELD)
public class TagInfo {
	
	@XmlElement(name = "TagAgencyID")
	private String tagAgencyID;
	@XmlElement(name = "TagSerialNo")
	private String tagSerialNo;
	@XmlElement(name = "TagStatus")
	private String tagStatus;
	@Override
	public String toString() {
		return "TagInfo [tagAgencyID=" + tagAgencyID + ", tagSerialNo=" + tagSerialNo + ", tagStatus=" + tagStatus
				+ "]";
	}
	
	
}
