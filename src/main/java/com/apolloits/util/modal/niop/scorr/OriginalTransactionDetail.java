package com.apolloits.util.modal.niop.scorr;

import com.apolloits.util.modal.niop.stran.EntryData;
import com.apolloits.util.modal.niop.stran.PlateInfo;
import com.apolloits.util.modal.niop.stran.TagInfo;

import jakarta.xml.bind.annotation.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XmlRootElement(name="OriginalTransactionDetail")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "recordType",
        "txnReferenceID",
        "exitDateTime",
        "facilityID",
        "facilityDesc",
        "exitPlaza",
        "exitPlazaDesc",
        "exitLane",
        "entryData",
        "tagInfo",
        "occupancyInd",
        "vehicleClass",
        "tollAmount",
        "discountPlanType",
        "plateInfo",
        "vehicleClassAdj",
        "systemMatchInd",
        "spare1",
        "spare2",
        "spare3",
        "spare4",
        "spare5",
        "exitDateTimeTZ",
        "entryDateTimeTZ"
})
public class OriginalTransactionDetail {

    @XmlElement(name = "RecordType", required = true)
    private String recordType;
    @XmlElement(name = "TxnReferenceID")
    private String txnReferenceID;
    @XmlElement(name = "ExitDateTime", required = true)
    @XmlSchemaType(name = "dateTime")
    private String exitDateTime;
    @XmlElement(name = "FacilityID", required = true)
    private String facilityID;
    @XmlElement(name = "FacilityDesc", required = true)
    private String facilityDesc;
    @XmlElement(name = "ExitPlaza", required = true)
    private String exitPlaza;
    @XmlElement(name = "ExitPlazaDesc", required = true)
    private String exitPlazaDesc;
    @XmlElement(name = "ExitLane", required = true)
    private String exitLane;
    @XmlElement(name = "EntryData")
    private EntryData entryData;
    @XmlElement(name = "TagInfo")
    private TagInfo tagInfo;
    @XmlElement(name = "OccupancyInd")
    private String occupancyInd;
    @XmlElement(name = "VehicleClass")
    private String vehicleClass;
    @XmlElement(name = "TollAmount")
    private String tollAmount;
    @XmlElement(name = "DiscountPlanType")
    private String discountPlanType;
    @XmlElement(name = "PlateInfo")
    private PlateInfo plateInfo;
    @XmlElement(name = "VehicleClassAdj")
    private String vehicleClassAdj;
    @XmlElement(name = "SystemMatchInd")
    private String systemMatchInd;
    @XmlElement(name = "Spare1")
    private String spare1;
    @XmlElement(name = "Spare2")
    private String spare2;
    @XmlElement(name = "Spare3")
    private String spare3;
    @XmlElement(name = "Spare4")
    private String spare4;
    @XmlElement(name = "Spare5")
    private String spare5;
    @XmlElement(name = "ExitDateTimeTZ", required = true)
    private String exitDateTimeTZ;
    @XmlElement(name = "EntryDateTimeTZ")
    private String entryDateTimeTZ;

}
