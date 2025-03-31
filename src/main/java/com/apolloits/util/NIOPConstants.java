package com.apolloits.util;

public class NIOPConstants {

	 public static final String BTVL_FILE_TYPE = "BTVL";
	 public static final String DTVL_FILE_TYPE = "DTVL";
	 public static final String STRAN_FILE_TYPE = "STRAN";
	 public static final String SRECON_FILE_TYPE = "SRECON";
	 public static final String SCORR_FILE_TYPE = "SCORR";
	 public static final String ACK_FILE_TYPE = "ACK";
	 
	 public static final String BTVL_FILE_EXTENSION = ".BTVL";
	 public static final String DTVL_FILE_EXTENSION = ".DTVL";
	 public static final String ACK_FILE_EXTENSION = ".ACK";
	 public static final String STRAN_FILE_EXTENSION = ".STRAN";
	 public static final String SRECON_FILE_EXTENSION = ".SRECON";
	 
	 public static final String AGENCY_ID_FORMAT ="\\d{4}";
	 public static final String UTC_DATE_TIME_FORMAT = "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z";
	 public static final String UTC_DATE_REGEX = 
	            "^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])T(0[0-9]|1\\d|2[0-3]):([0-5]\\d):([0-5]\\d)Z$";
	 
	 public static final String UTC_DATE_YEAR_REGEX = 
	            "^(20[01][0-9]|20[2-9][0-9]|21[0-9]{2})-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])T(0[0-9]|1\\d|2[0-3]):([0-5]\\d):([0-5]\\d)Z$"; // Matches years from 2000 to 2199.
	 
	 public static final String BTVL_ZIP_FILE_NAME_FORMAT = "\\d{4}_\\d{4}_\\d{14}\\_BTVL\\.(?i)zip";
	 public static final String BTVL_FILE_NAME_FORMAT = "\\d{4}_\\d{4}_\\d{14}\\.(?i)BTVL";
	 
	 public static final String BTVL_DTL_TAG_SERIAL_NO = "\\d{1,10}";
	 public static final String BTVL_DTL_TAG_STATUS = "[V,Z,I]{1}";
	 public static final String BTVL_DTL_TAG_TYPE = "[S,L,F,G,T]{1}";
	 public static final String BTVL_DTL_PLATE_COUNTRY = "^(US|CA|MX|-)$";
	 public static final String BTVL_DTL_PLATE_STATE_FORMAT = "[A-Z]{1,2}";
	 public static final String BTVL_DTL_PLATE_NUMBER_FORMAT = "[A-Z 0-9]{1,15}";
	 public static final String BTVL_DTL_PLATE_TYPE_FORMAT = "[A-Z 0-9]{1,30}";
	 public static final String BTVL_DTL_FLEET_INDICATOR_FORMAT = "[Y,N]{1}";
	 public static final String BTVL_DTL_ACCOUNT_NUMBER_FORMAT = "[A-Z 0-9]{1,50}";
	 public static final String BTVL_DTL_DISCOUNT_PLAN_FORMAT = "[A-Z 0-9]{1,12}";
	 
	 public static final String TXN_DATA_SEQ_NO_FORMAT = "\\d{1,12}";
	 public static final String TXN_RECORD_COUNT_FORMAT = "\\d{1,10}";
	 public static final String TXN_RECORD_TYPE_FORMAT = "^(TB01|TC01|TC02|VB01|VC01|VC02)$";
	 public static final String TXN_REFERENCE_ID_FORMAT = "\\d{1,20}";
	 public static final String TXN_FACILITY_ID_FORMAT = "[a-zA-Z 0-9]{1,10}";
	 public static final String TXN_FACILITY_ID_DESC_FORMAT = "[a-zA-Z 0-9]{1,30}";
	 public static final String TXN_PLAZA_FORMAT = "[A-Z0-9]{1,15}";
	 public static final String TXN_LANE_FORMAT = "[A-Z0-9]{1,4}";
	 public static final String TXN_ENTRY_TYPE = "^(TC01|TC02|VC01|VC02)$";
	 public static final String TXN_OCCUPANCY_IND_VALUE = "[1,2,3]{1}"; //Occupancy Indicator 
	 public static final String TXN_TOLL_AMOUNT = "\\d{1,9}";
	 public static final String TXN_PLATE_TYPE = "^(VB01|VC01|VC02)$";
	 public static final String TXN_SYSTEM_MATCH_FLAG_VALUE = "[1,0]{1}"; //System Matched Flag
	 public static final String TXN_SPARE1_FORMAT = "[0-1 ]{1}"; //Spare 1
	 public static final String TXN_UTC_TIME_ZONE_FORMAT = 
	            "^(\\d{4})-(\\d{2})-(\\d{2})T(\\d{2}):(\\d{2}):(\\d{2})([+-])(\\d{2}):(\\d{2})$";
	 
	 public static final String RECON_ADJ_RESUBMIT_COUNT_FORMAT = "\\d{1,3}"; //Adjustment Count && Resubmit Count
	 public static final String RECON_POSTING_DISPOSITION_VALUE = "[P,D,I,N,S,T,C,O]{1}"; //Posting Disposition
	 public static final String RECON_FLAT_PERCENTAGE_FEE_FORMAT = "\\d{1,9}"; // Transaction Flat Fee && Transaction Percentage Fee
	 public static final String RECON_SPARE_FORMAT = "[A-Z 0-9]{1,10}"; //Spare 1-5
	 public static final String RECON_TOLL_AMOUNT =  "^-\\d{1,9}$|^\\d{1,9}$";
	 
	 public static final String CORR_RECORD_TYPE_FORMAT = "^(TB01A|TC01A|TC02A|VB01A|VC01A|VC02A)$";
	 public static final String CORR_REASON = "[C,I,L,T,O]{1}";
	 public static final String CORR_RESUBMIT_REASON = "[R,S]{1}";
	 public static final String CORR_OTHER_REASON = "[A-Z 0-9]{1,255}";
	 public static final String CORR_RESUBMIT_COUNT_FORMAT = "\\d{1,3}";
	 
	 public static final String ACK_CODES = "^(00|01|03|04|05|07|10|11|12|13)$";
}
