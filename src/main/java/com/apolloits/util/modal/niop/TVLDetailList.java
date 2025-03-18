package com.apolloits.util.modal.niop;

import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Data;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@XmlRootElement(name = "TVLDetail")
@XmlAccessorType(XmlAccessType.FIELD)
public class TVLDetailList {
    @XmlElement(name = "TVLTagDetails")
    private List<TVLTagDetails> tvlTagDetails;
}
