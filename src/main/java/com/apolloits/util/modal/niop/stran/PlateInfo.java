package com.apolloits.util.modal.niop.stran;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XmlRootElement(name = "PlateInfo")
@XmlAccessorType(XmlAccessType.FIELD)
public class PlateInfo {

	@XmlElement(name = "PlateCountry")
	private String plateCountry;
	@XmlElement(name = "PlateState")
	private String plateState;
	@XmlElement(name = "PlateNumber")
	private String plateNumber;
	@XmlElement(name = "PlateType")
	private String plateType;
	@Override
	public String toString() {
		return "PlateInfo [plateCountry=" + plateCountry + ", plateState=" + plateState + ", plateNumber=" + plateNumber
				+ ", plateType=" + plateType + "]";
	}
	
	
}
