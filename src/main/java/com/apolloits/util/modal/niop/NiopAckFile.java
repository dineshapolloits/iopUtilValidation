package com.apolloits.util.modal.niop;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Setter;

import java.io.Serializable;

@Setter
@XmlRootElement(name="Acknowledgement")
public class NiopAckFile implements Serializable {

    @XmlElement(name="SubmissionType")
    private String submissionType;
    @XmlElement(name="OrigSubmissionType")
    private String originalSubmissionType;
    @XmlElement(name="OrigSubmissionDateTime")
    private String originalSubmissionDateTime;
    @XmlElement(name="NIOPHubID")
    private String niopHubID;
    @XmlElement(name="FromAgencyID")
    private String fromAgencyID;
    @XmlElement(name="ToAgencyID")
    private String toAgencyID;
    @XmlElement(name="AckDateTime")
    private String ackDateTime;
    private String AckReturnCode;

    @XmlElement(name = "AckReturnCode", required = true)
    public String getAckReturnCode() {
        return AckReturnCode;
    }


}
