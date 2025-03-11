package com.apolloits.util.modal.niop.stran;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Getter;

@Getter
@XmlRootElement(name="TransactionData")
public class TransactionData {
	@XmlElement(name = "TransactionHeader")
	public TransactionHeader transactionHeader;
	@XmlElement(name = "TransactionDetail")
	public TransactionDetail transactionDetail;

}
