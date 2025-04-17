package com.apolloits.util.modal;

import lombok.Data;

import java.io.Serializable;

@Data
public class IagAckFile{
	
	private String fileType;
    private String fileVersion;
    private String fileCreationDate;
    private String fromAgencyId;
    private String toAgencyId;
    private String origFileName;
    private String returnCode;

    //CTOC
    private String recordType;
    private String ackDate;
}
