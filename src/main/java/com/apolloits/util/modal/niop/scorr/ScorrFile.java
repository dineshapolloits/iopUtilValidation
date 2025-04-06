package com.apolloits.util.modal.niop.scorr;

import jakarta.xml.bind.annotation.*;
import lombok.Getter;
import lombok.Setter;


@XmlRootElement(name = "CorrectionData")
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@Setter
public class ScorrFile {

    @XmlElement(name = "CorrectionHeader", required = true)
    private ScorrHeader scorrHeader;
    @XmlElement(name = "CorrectionDetail", required = true)
    private CorrectionDetail correctionDetail;
}