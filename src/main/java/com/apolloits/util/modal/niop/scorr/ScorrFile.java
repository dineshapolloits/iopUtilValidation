package com.apolloits.util.modal.niop.scorr;

import jakarta.xml.bind.annotation.*;
import lombok.Getter;


@XmlRootElement(name = "CorrectionData")

@Getter
public class ScorrFile {

    @XmlElement(name = "CorrectionHeader", required = true)
    private ScorrHeader scorrHeader;
    @XmlElement(name = "CorrectionDetail", required = true)
    private CorrectionDetail correctionDetail;
}