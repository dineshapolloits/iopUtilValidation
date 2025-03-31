package com.apolloits.util.modal.niop;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
@XmlRootElement(name="Acknowledgement")
@XmlAccessorType(XmlAccessType.FIELD)
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
    @XmlElement(name = "AckReturnCode")
    private String AckReturnCode;

	@Override
	public String toString() {
		return "NiopAckFile [submissionType=" + submissionType + ", originalSubmissionType=" + originalSubmissionType
				+ ", originalSubmissionDateTime=" + originalSubmissionDateTime + ", niopHubID=" + niopHubID
				+ ", fromAgencyID=" + fromAgencyID + ", toAgencyID=" + toAgencyID + ", ackDateTime=" + ackDateTime
				+ ", AckReturnCode=" + AckReturnCode + "]";
	}
    

}
