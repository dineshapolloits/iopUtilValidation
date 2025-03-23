package com.apolloits.util.modal.niop.scorr;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import lombok.Getter;

import java.util.List;

@XmlRootElement(name = "CorrectionDetail")

@Getter
public class CorrectionDetail {
	@XmlElement(name = "CorrectionRecord")
    private List<CorrectionRecord> correctionRecordList;
}
