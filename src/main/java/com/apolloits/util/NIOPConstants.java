package com.apolloits.util;

public class NIOPConstants {

	 public static final String BTVL_FILE_TYPE = "BTVL";
	 
	 public static final String BTVL_FILE_EXTENSION = ".BTVL";
	 public static final String ACK_FILE_EXTENSION = ".ACK";
	 
	 public static final String AGENCY_ID_FORMAT ="\\d{4}";
	 public static final String UTC_DATE_TIME_FORMAT = "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z";
	 
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
}
