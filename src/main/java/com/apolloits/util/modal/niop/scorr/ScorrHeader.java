package com.apolloits.util.modal.niop.scorr;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;
import lombok.Getter;

import java.io.Serializable;

@XmlRootElement(name = "CorrectionHeader")

@Getter
public class ScorrHeader implements Serializable {

    @XmlElement(name = "SubmissionType", required = true)
    private String submissionType;
    @XmlElement(name = "SubmissionDateTime", required = true)
    @XmlSchemaType(name = "dateTime")
    private String submissionDateTime;
    @XmlElement(name = "SSIOPHubID", required = true)
    private String ssiopHubID;
    @XmlElement(name = "AwayAgencyID", required = true)
    private String awayAgencyID;
    @XmlElement(name = "HomeAgencyID", required = true)
    private String homeAgencyID;
    @XmlElement(name = "TxnDataSeqNo")
    private String txnDataSeqNo;
    @XmlElement(name = "RecordCount")
    private String recordCount;
}
