package com.apolloits.util.modal.niop;

import lombok.Data;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@XmlRootElement(name = "TVLTagDetails")
@XmlType(propOrder = {"homeAgencyId", "tagAgencyId", "tagSerialNumber", "tagStatus", "tagType", "tagClass", "tvlPlateDetails", "tvlAccountDetails"})
public class TVLTagDetails {
    @XmlElement(name = "HomeAgencyID")
    private String homeAgencyId;
    @XmlElement(name = "TagAgencyID")
    private String tagAgencyId;
    @XmlElement(name = "TagSerialNumber")
    private String tagSerialNumber;
    @XmlElement(name = "TagStatus")
    private String tagStatus;
    @XmlElement(name = "TagType")
    private String tagType;
    @XmlElement(name = "TagClass")
    private Integer tagClass;
    @XmlElement(name = "TVLPlateDetails")
    private List<TVLPlateDetails> tvlPlateDetails;
    @XmlElement(name = "TVLAccountDetails")
    private TVLAccountDetails tvlAccountDetails;

}
