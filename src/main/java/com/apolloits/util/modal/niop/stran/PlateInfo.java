package com.apolloits.util.modal.niop.stran;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Getter;

@Getter
@XmlRootElement(name = "PlateInfo")
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
