package com.apolloits.util.modal.niop;

import jakarta.xml.bind.annotation.XmlType;
import lombok.Data;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Getter;

@Getter
@XmlRootElement(name = "TVLAccountDetails")
@XmlType(propOrder = {"accountNumber", "fleetIndicator"})
public class TVLAccountDetails {
    @XmlElement(name = "AccountNumber")
    private String accountNumber;
    @XmlElement(name = "FleetIndicator")
    private String fleetIndicator;
}
