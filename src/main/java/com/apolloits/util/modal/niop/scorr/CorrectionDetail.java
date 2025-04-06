package com.apolloits.util.modal.niop.scorr;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@XmlRootElement(name = "CorrectionDetail")
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@Setter
public class CorrectionDetail {
	@XmlElement(name = "CorrectionRecord")
    private List<CorrectionRecord> correctionRecordList;
}
