package com.apolloits.util.modal.niop.srecon;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Getter;

@Getter
@XmlRootElement(name = "ReconciliationData")
public class ReconciliationData {

	
	@XmlElement(name = "ReconciliationHeader")
	private ReconciliationHeader reconciliationHeader;
	@XmlElement(name = "ReconciliationDetail")
	private ReconciliationDetail reconciliationDetail; //list

}
