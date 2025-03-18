package com.apolloits.util.modal.niop;


import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
@XmlRootElement(name = "TagValidationList")
@XmlAccessorType(XmlAccessType.FIELD)
public class TagValidationList {
    @XmlElement(name = "TVLHeader")
    private TVLHeader tvlHeader;
    @XmlElement(name = "TVLDetail")
    private List<TVLDetailList> tvlDetail;

    @Override
    public String toString() {
        return "Tvl [TVLHeader=" + tvlHeader + ", TVLDetail=" + tvlDetail + "]";
    }

    public TagValidationList(TVLHeader tvlHeader, List<TVLDetailList> tvlDetail) {
        this.tvlHeader = tvlHeader;
        this.tvlDetail = tvlDetail;
    }

    public TagValidationList() {
    }

}
