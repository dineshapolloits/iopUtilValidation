package com.apolloits.util.modal;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "NiopAgency")
@Data
public class NiopAgencyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int AgencyId;
    private Integer HomeAgency;
    private Integer AwayAgency;
    private String CSCID;
    private String CSCName;
    private String CSCAgencyShortName;
    private String VersionNumber;
    private String  HomeAgencyID;
    private String TagAgencyID;
    private String TagSequenceStart;
    private String TagSequenceEnd;
    private Integer BTVL;
    private String HubName;
    private Integer HubId;
}
