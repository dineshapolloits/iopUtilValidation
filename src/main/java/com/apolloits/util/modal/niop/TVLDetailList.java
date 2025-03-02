package com.apolloits.util.modal.niop;

import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Data;

import jakarta.xml.bind.annotation.XmlElement;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@XmlRootElement(name = "TVLDetail")
public class TVLDetailList {
    @XmlElement(name = "TVLTagDetails")
    private List<TVLTagDetails> tvlTagDetails;
}
