package com.apolloits.util;

public class NIOPConstants {

	 public static final String BTVL_FILE_TYPE = "BTVL";
	 public static final String DTVL_FILE_TYPE = "DTVL";
	 public static final String STRAN_FILE_TYPE = "STRAN";
	 
	 public static final String BTVL_FILE_EXTENSION = ".BTVL";
	 public static final String ACK_FILE_EXTENSION = ".ACK";
	 
	 public static final String AGENCY_ID_FORMAT ="\\d{4}";
	 public static final String UTC_DATE_TIME_FORMAT = "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z";
	 public static final String UTC_DATE_REGEX = 
	            "^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])T(0[0-9]|1\\d|2[0-3]):([0-5]\\d):([0-5]\\d)Z$";
	 
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
	 public static final String TXN_FACILITY_ID_FORMAT = "[A-Z 0-9]{1,10}";
	 public static final String TXN_FACILITY_ID_DESC_FORMAT = "[A-Z 0-9]{1,30}";
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
}
