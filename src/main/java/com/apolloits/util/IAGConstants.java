package com.apolloits.util;


import org.springframework.stereotype.Component;

@Component
public class IAGConstants {

    public static final String ITAG_FILE_TYPE = "ITAG";
    public static final String ICLP_FILE_TYPE = "ICLP";
    public static final String ITGU_FILE_TYPE = "ITGU";
    public static final String IAG_VERSION_NO = "01.60.02";
    public static final String TVL_SUBMITTED_FILE_TYPE = "STVL";
    public static final String DETAIL_ROOT_ELEMENT_NAME = "TVLTagDetails";
    public static final String HEADER_ROOT_ELEMENT_NAME = "TVLHeader";
    public static final String TVL_HEADER_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    public static final String BTVL_FILE_EXTENSION = ".BTVL";
    public static final String ITAG_FILE_EXTENSION = ".ITAG";
    public static final String ICLP_FILE_EXTENSION = ".ICLP";
    public static final String ACK_FILE_EXTENSION = ".ACK";
    public static final String ICTX_FILE_EXTENSION= ".ICTX";
    public static final String ICRX_FILE_EXTENSION= ".ICRX";
    public static final String ITXC_FILE_EXTENSION = ".ITXC";
    public static final String ITGU_FILE_EXTENSION = ".ITGU";;

    public static final String BTVL_HOME_AGENCY_ID = "0041";
    public static final String XFER_STATUS_SUCCESS = "S";
    public static final String XFER_STATUS_FAILURE = "F";
    public static final String XFER_STATUS_PENDING = "P";
    public static final String TRANSLATOR_STATUS_RECEIVED = "Received";
    public static final String TRANSLATOR_STATUS_SUCCESS = "Translated Successfully";
    public static final String TRANSLATOR_STATUS_FAILURE = "Translation Failed";
    public static final String TRANSLATOR_STATUS_LOADED_SUCCESSFULLY = "Loaded Successfully";
    public static final String TRANSLATOR_STATUS_HEADER_FAILED = "Header Validation Failed";
    public static final String TRANSLATOR_STATUS_DETAIL_FAILED = "Detail Validation Failed";
    
    public static final String YYYY_MM_DD_HH_MM_SS = "yyyyMMddHHmmss";
    
    public static final String SRTA_HOME_AGENCY_ID = "0034";
    public static final String EZIOP_NIOP_ID = "9003";
    public static final String SEHUB_AWAY_AGENCY_ID = "0035";
    public static final String SRECON_FILE_TYPE = "SRECON";
    public static final String STRAN_FILE_TYPE = "STRAN";
    public static final String SCORR_FILE_TYPE = "SCORR";
    public static final String BTVL_FILE_TYPE = "BTVL";
    
    public static final String ICRX_FILE_TYPE = "ICRX";
    public static final String ICTX_FILE_TYPE = "ICTX";
    public static final String ITXC_FILE_TYPE = "ITXC";
    public static final String IRXC_FILE_TYPE = "IRXC";
    public static final String ACK_FILE_TYPE = "ACK";
    

    public static final String IAG_FILE_TYPES = "ITAG,ICLP,ITGU";
    public static final String TRANSLATOR_STATUS_LOAD_FAILED = "Load Failed";
    
    public static final String FILE_DATE_TIME_FORMAT = "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z";
    public static final String AGENCY_ID_FORMAT ="\\d{4}";
    public static final String IAG_HEADER_VERSION_FORMAT = "[0-9]{2}\\.[0-9]{2}\\.[0-9]{2}";
    public static final String TRAN_RECORD_COUNT_FORMAT = "\\d{8}";
    public static final String ICTX_FILE_NUM_FORMAT = "\\d{12}";
    public static final String ETC_TRX_SERIAL_NUM_FORMAT = "\\d{20}";
    public static final String ETC_TAG_SERIAL_NUMBER_FORMAT = "\\d{10}";
    public static final String PLAZA_FORMAT = "[A-Z 0-9]{1,15}";
    public static final String PLAZA_LANE_FORMAT = "[A-Z 0-9]{1,3}";
    public static final String LIC_STATE_FORMAT = "[A-Z0-9]{2}";
    public static final String LIC_NUMBER_FORMAT = "^[A-Z \\d-.&]{10}$";
    public static final String LIC_TYPE_FORMAT =  "[A-Z \\d*]{30}";
    public static final String ETC_DEBIT_CREDIT_FORMAT = "[+ -]";
    public static final String ETC_TOLL_AMOUNT_FORMAT = "\\d{9}";
    public static final String ITAG_DTL_TAG_AGENCY_ID = "[0-9]{4}";
    public static final String ITAG_DTL_TAG_SERIAL_NO = "[0-9]{10}";
    public static final String ITAG_DTL_TAG_STATUS = "[1-3]{1}";
    public static final String ITAG_DTL_TAG_AC_TYP_IND = "[B,F,P,V,R,*]{1}";
    public static final String ITAG_TAG_CLASS = "[0-9 ]{4}";
    public static final String ITAG_DTL_TAG_PROTOCOL = "T  |S  |6  |TS |T6 |S6 |TS6|\\*\\*\\*";
    
    public static final String ITAG_DTL_TAG_TYP = "[F,G,H,S,T,V,*]{1}";
    public static final String ITAG_DTL_TAG_MOUNT = "[I,L,R,H,V,*]{1}";
    public static final String ITAG_HEADER_COUNT_FORMAT = "\\d{10}";
    public static final String INFO_RECORD_COUNT_FORMAT = "\\d{10}";
    
    public static final String ITGU_DTL_TAG_STATUS = "[1-4]{1}";
    
    public static final String TRAN_ZIP_FILE_NAME_FORMAT = "\\d{4}_\\d{4}_\\d{14}_[A-Z]{4}\\.[A-Za-z]{3}";
    public static final String INFO_ZIP_FILE_NAME_FORMAT = "\\d{4}_\\d{14}_[A-Z]{4}.[A-Za-z]{3}";
    public static final String INFO_ACK_FILE_NAME_FORMAT = "\\d{4}_\\d{4}_\\d{14}_[A-Z]{4}.[A-Za-z]{3}";
    
    
    public static final String HEADER_RECORD_TYPE ="Header";
    public static final String DETAIL_RECORD_TYPE ="Detail";
    public static final String FILE_RECORD_TYPE ="File";
    
}
