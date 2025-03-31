package com.apolloits.util.modal.niop.srecon;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XmlRootElement(name = "ReconciliationData")
@XmlAccessorType(XmlAccessType.FIELD)
public class ReconciliationData {

	
	@XmlElement(name = "ReconciliationHeader")
	private ReconciliationHeader reconciliationHeader;
	@XmlElement(name = "ReconciliationDetail")
	private ReconciliationDetail reconciliationDetail; //list

}
