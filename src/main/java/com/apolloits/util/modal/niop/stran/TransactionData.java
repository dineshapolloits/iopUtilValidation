package com.apolloits.util.modal.niop.stran;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XmlRootElement(name="TransactionData")
@XmlAccessorType(XmlAccessType.FIELD)
public class TransactionData {
	@XmlElement(name = "TransactionHeader")
	public TransactionHeader transactionHeader;
	@XmlElement(name = "TransactionDetail")
	public TransactionDetail transactionDetail;

}
