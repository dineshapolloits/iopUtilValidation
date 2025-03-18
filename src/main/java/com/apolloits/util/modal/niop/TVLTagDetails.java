package com.apolloits.util.modal.niop;

import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XmlRootElement(name = "TVLTagDetails")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"homeAgencyId", "tagAgencyId", "tagSerialNumber", "tagStatus","discountPlans",
        "DiscountPlanStartDate","DiscountPlanEndDate",
        "tagType", "tagClass", "tvlPlateDetails", "tvlAccountDetails"})
public class TVLTagDetails {
    @XmlElement(name = "HomeAgencyID")
    private String homeAgencyId;
    @XmlElement(name = "TagAgencyID")
    private String tagAgencyId;
    @XmlElement(name = "TagSerialNumber")
    private String tagSerialNumber;
    @XmlElement(name = "TagStatus")
    private String tagStatus;
    @XmlElement(name="Discount Plans")
    private String discountPlans;
    @XmlElement(name="Discount Plan Start Date")
    private String DiscountPlanStartDate;
    @XmlElement(name="Discount Plan End Date")
    private String DiscountPlanEndDate;
    @XmlElement(name = "TagType")
    private String tagType;
    @XmlElement(name = "TagClass")
    private Integer tagClass;
    @XmlElement(name = "TVLPlateDetails")
    private List<TVLPlateDetails> tvlPlateDetails;
    @XmlElement(name = "TVLAccountDetails")
    private TVLAccountDetails tvlAccountDetails;

}
