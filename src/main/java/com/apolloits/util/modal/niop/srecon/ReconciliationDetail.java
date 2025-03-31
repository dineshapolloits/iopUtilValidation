package com.apolloits.util.modal.niop.srecon;

import java.io.Serializable;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XmlRootElement(name = "ReconciliationDetail")
@XmlAccessorType(XmlAccessType.FIELD)
public class ReconciliationDetail implements Serializable {

	 	@XmlElement(name = "ReconciliationRecord")
	    private List<ReconciliationRecord> reconRecordList;
}
