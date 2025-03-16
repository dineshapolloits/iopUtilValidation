package com.apolloits.util.modal.niop.srecon;

import java.io.Serializable;
import java.util.List;


import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Getter;

@Getter
@XmlRootElement(name = "ReconciliationDetail")
public class ReconciliationDetail implements Serializable {

	 	@XmlElement(name = "ReconciliationRecord")
	    private List<ReconciliationRecord> reconRecordList;
}
