package com.apolloits.util.modal.niop.stran;

import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XmlRootElement(name="TransactionDetail")
@XmlAccessorType(XmlAccessType.FIELD)
public class TransactionDetail {
	@XmlElement(name = "TransactionRecord")
	public List<TransactionRecord> TransactionRecord;


}
