package com.apolloits.util.modal.niop.stran;

import java.util.List;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Getter;

@Getter
@XmlRootElement(name="TransactionDetail")
public class TransactionDetail {
	@XmlElement(name = "TransactionRecord")
	public List<TransactionRecord> TransactionRecord;


}
