package com.apolloits.util.modal.niop;

import java.io.Serializable;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XmlRootElement(name = "TVLHeader")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"submittedFileType", "submittedDateTime", "SSIOPHubIdNumber", "homeAgencyIdNumber", "bulkInd", "bulkIdentifierValue", "totalRecordCount"})
public class TVLHeader implements Serializable {
    @XmlElement(name = "SubmissionType")
    private String submittedFileType;
    @XmlElement(name = "SubmissionDateTime")
    private String submittedDateTime;
    @XmlElement(name = "SSIOPHubID")
    private String SSIOPHubIdNumber;
    @XmlElement(name = "HomeAgencyID")
    private String homeAgencyIdNumber;
    @XmlElement(name = "BulkIndicator")
    private String bulkInd;
    @XmlElement(name = "BulkIdentifier")
    private String bulkIdentifierValue;
    @XmlElement(name = "RecordCount")
    private String totalRecordCount;

}
