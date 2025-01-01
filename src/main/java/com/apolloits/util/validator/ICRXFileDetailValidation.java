package com.apolloits.util.validator;


import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.apolloits.util.IAGConstants;
import com.apolloits.util.IagAckFileMapper;
import com.apolloits.util.controller.ValidationController;
import com.apolloits.util.modal.ErrorMsgDetail;
import com.apolloits.util.modal.FileValidationParam;
import com.apolloits.util.reader.AgencyDataExcelReader;
import com.apolloits.util.utility.CommonUtil;

import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import static com.apolloits.util.IAGConstants.HEADER_RECORD_TYPE;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import static com.apolloits.util.IAGConstants.DETAIL_RECORD_TYPE;
import static com.apolloits.util.IAGConstants.FILE_RECORD_TYPE;

@Slf4j
@Component
public class ICRXFileDetailValidation {
	
	@Autowired
	private IagAckFileMapper iagAckMapper;
	
	@Autowired
	private AgencyDataExcelReader agDataExcel;
	
	@Autowired
	@Lazy
	ValidationController controller;
	
	@Autowired
	private CommonUtil commonUtil;
	
	public boolean icrxValidation(FileValidationParam validateParam) throws IOException {

		File inputItagZipFile = new File(validateParam.getInputFilePath());
		String ackFileName = null;
		if (!inputItagZipFile.exists()) {
			log.error("ZIP file not found");
			controller.getErrorMsglist().add(new ErrorMsgDetail(FILE_RECORD_TYPE, "File", "ZIP file not found"));
			return false;
		} else {
			log.info("icrxValidation FileValidationParam vaidation from UI ");
			if (!validateParam.getFromAgency().equals(inputItagZipFile.getName().substring(0, 4))) {
				log.error("From Agency code not match with file Name");
				controller.getErrorMsglist().add(new ErrorMsgDetail(FILE_RECORD_TYPE, "From Agency", "From Agency code "
						+ validateParam.getFromAgency() + " not match with file Name ::" + inputItagZipFile.getName()));
				return false;
			}

			if (validateParam.getFromAgency().equals(validateParam.getToAgency())) {
				log.error("From Agency code and To agency code should not be same");
				controller.getErrorMsglist()
						.add(new ErrorMsgDetail(FILE_RECORD_TYPE, "From Agency",
								"From Agency code " + validateParam.getFromAgency()
										+ " and Toagency code should not be same  ::" + validateParam.getToAgency()));
				return false;
			}

			if (!AgencyDataExcelReader.agencyCode.contains(validateParam.getToAgency())) {
				log.error("To Agency code not match with file Name");
				controller.getErrorMsglist().add(new ErrorMsgDetail(FILE_RECORD_TYPE, "To Agency",
						"To Agency code " + validateParam.getToAgency() + " not match with Configuration ::"));
				return false;
			}
			// validate ZIP file name format
			if (commonUtil.validateTransactionZIPFileName(inputItagZipFile.getName(), IAGConstants.ICRX_FILE_TYPE)) {

				String fileName = "";
				// extract ZIP file
				ZipFile zipFile = new ZipFile(inputItagZipFile);
				try {
					log.info("ICRX extract file name getFileHeaders ******************* "
							+ zipFile.getFileHeaders().get(0).getFileName());
					log.info("ICRX inputItagZipFile.getAbsolutePath() :: " + inputItagZipFile.getAbsolutePath());
					zipFile.extractAll(FilenameUtils.getFullPath(inputItagZipFile.getAbsolutePath()));
					zipFile.close();
					fileName = zipFile.getFileHeaders().get(0).getFileName();
					ackFileName = validateParam.getToAgency() + "_" + fileName.replace(".", "_")
							+ IAGConstants.ACK_FILE_EXTENSION;
				} catch (ZipException e) {
					e.printStackTrace();
					controller.getErrorMsglist().add(new ErrorMsgDetail(FILE_RECORD_TYPE, "ZIP File",
							"ZIP file extraction failed \t ::" + fileName));
					// validateParam.setResponseMsg("FAILED Reason:: ZIP file extraction failed");
					return false;
				}

				if (commonUtil.isTransactionFileFormatValid(fileName, "ICRX")) {
					if (validateParam.getValidateType().equals("filename")) {
						validateParam.setResponseMsg("File name validation is sucess");
						return true;
					}

					// Start to validate file header and detail record
					long noOfRecords = 0;
					try (BufferedReader br = new BufferedReader(
							new FileReader(zipFile.getFile().getParent() + "\\" + fileName))) {

						String fileRowData;
						long headerCount = 0l;
						String ackCode = "00";
						while ((fileRowData = br.readLine()) != null) {
							log.info("noOfRecords ::" + noOfRecords + " :: " + fileRowData);
							if (noOfRecords == 0) {
								// Validate Header record
								headerCount = Long.parseLong(fileRowData.substring(40, 48));

								if (!validateIcrxHeader(fileRowData, validateParam, fileName)) {
									// create ACK file
									ackCode = "01";
								}
								iagAckMapper.mapToIagAckFile(fileName, ackCode,
										validateParam.getOutputFilePath() + "\\" + ackFileName,
										fileName.substring(0, 4), validateParam.getToAgency());
								if (validateParam.getValidateType().equals("header")) {
									log.info("Only file name and header validation");
									return true;
								}
							} else {
								validateIcrxDetail(fileRowData, validateParam, fileName, noOfRecords);
									
							}
							noOfRecords++;
						}
						if ((noOfRecords - 1) != headerCount) {
							validateParam.setResponseMsg("FAILED Reason:: Header count(" + headerCount
									+ ") and detail count not matching ::" + noOfRecords);
							iagAckMapper.mapToIagAckFile(fileName, "01",
									validateParam.getOutputFilePath() + "\\" + ackFileName, fileName.substring(0, 4),
									validateParam.getToAgency());
							return false;
						}
						String ackcode = "00";
						if (controller.getErrorMsglist().size() > 0) {
							validateParam.setResponseMsg("\t \t ACK file name ::" + ackFileName);
							ackcode="02";
						}else {
							validateParam.setResponseMsg("\t \t ACK file name ::" + ackFileName);
							
						}
						iagAckMapper.mapToIagAckFile(fileName, ackcode,
								validateParam.getOutputFilePath() + "\\" + ackFileName, fileName.substring(0, 4),
								validateParam.getToAgency());
						
					} catch (IOException e) {
						e.printStackTrace();
						// Display pop up message if exceptionn occurs
						System.out.println("Error while reading a file.");
					}

				}

			}
		}

		return true;
	}

private boolean validateIcrxDetail(String fileRowData, FileValidationParam validateParam, String fileName,
		long rowNo) {
	String lineNo = "\t <b>Row ::"+fileRowData +"\t Line No::</b>"+rowNo;
	
	if (fileRowData == null || fileRowData.length() != 59) {
		 addErrorMsg(DETAIL_RECORD_TYPE,"Detail Length","Record length is not match with ICRX length 201 ::\t "+lineNo);
	        return false;
    }
	
	// ETC_TRX_SERIAL_NUM CHAR(20) Values: 00000000000000000000 – 99999999999999999999
	if (!fileRowData.substring(0, 20).matches(IAGConstants.ETC_TRX_SERIAL_NUM_FORMAT)) {
		addErrorMsg(DETAIL_RECORD_TYPE, "ETC_TRX_SERIAL_NUM",
				"Format not matched Values: 00000000000000000000 – 99999999999999999999 ::\t "
						+ fileRowData.substring(0, 20) + "\t " + lineNo);
	}
	// ETC_POST_STATUS CHAR(4)
	if (!fileRowData.substring(20, 24).matches("POST|PPST|NPST|INSU|RJPL|OLD1|OLD2|ACCB|RINV|TAGB|RJDP|RJTA")) {
		addErrorMsg(DETAIL_RECORD_TYPE, "ETC_POST_STATUS",
				"Value should be POST|PPST|NPST|INSU|RJPL|OLD1|OLD2|ACCB|RINV|TAGB|RJDP|RJTA ::\t "
						+ fileRowData.substring(20, 24) + "\t " + lineNo);
	}
	//ETC_POST_PLAN CHAR(5)
	if (!fileRowData.substring(24, 29)
			.matches("00002|00003|00004|00005|00006|00007|00008|00009|00010|00011|00012|00013|00014|00023| {5}")) {
		addErrorMsg(DETAIL_RECORD_TYPE, "ETC_POST_PLAN",
				"Value should be 00002|00003|00004|00005|00006|00007|00008|00009|00010|00011|00012|00013|00014|00023 or blank ::\t"
						+ fileRowData.substring(24, 29) + "\t " + lineNo);
	}
	
	//ETC_DEBIT_CREDIT /CHAR(1) 
    if (!fileRowData.substring(29, 30).matches(IAGConstants.ETC_DEBIT_CREDIT_FORMAT)) {
    	addErrorMsg(DETAIL_RECORD_TYPE, "ETC_DEBIT_CREDIT",
				"Format should be [+,- or space] Invalid Format  \t ::"+ fileRowData.substring(29, 30) + "\t " + lineNo);
    }

    //ETC_OWED_AMOUNT
    //CHAR(9)
	if (!fileRowData.substring(30, 39).matches(IAGConstants.ETC_TOLL_AMOUNT_FORMAT)) {
		addErrorMsg(DETAIL_RECORD_TYPE, "ETC_TOLL_AMOUNT",
				" Values: 000000000 ($0000000.00) – 000499999 ($0004999.99). \t  Invalid Format  \t ::"
						+ fileRowData.substring(30, 39) + "\t " + lineNo);
	}
	
	//ETC_DUP_SERIAL_NUM /CHAR(20) 
    if (!fileRowData.substring(39, 59).matches("\\d{20}")) {
    	addErrorMsg(DETAIL_RECORD_TYPE, "ETC_DUP_SERIAL_NUM",
				" Values should be : 00000000000000000000 – 99999999999999999999. \t  Invalid Format  \t ::"
						+ fileRowData.substring(39, 59) + "\t " + lineNo);
    }
    
	return true;
}

private boolean validateIcrxHeader(String fileRowData, FileValidationParam validateParam, String fileName) {

	// Header Total 61
	if (fileRowData == null || fileRowData.length() != 60 || fileRowData.isEmpty()) {
		controller.getErrorMsglist().add(new ErrorMsgDetail(HEADER_RECORD_TYPE, "Header Length",
				"Invalid header lenght \t Header Row::" + fileRowData));
	}

	// FILE_TYPE
	if (!fileRowData.substring(0, 4).equals(IAGConstants.ICRX_FILE_TYPE)) {
		controller.getErrorMsglist()
				.add(new ErrorMsgDetail(HEADER_RECORD_TYPE, "FILE_TYPE", "File Type should be ICRX ::\t "
						+ fileRowData.substring(0, 4) + " \t :: Header Row::\t " + fileRowData));
	}
	// IAG Version
	if (!fileRowData.substring(4, 12).matches(IAGConstants.IAG_HEADER_VERSION_FORMAT) || !fileRowData.substring(4, 12)
			.equals(ValidationController.cscIdTagAgencyMap.get(fileRowData.substring(12, 16)).getVersionNumber())) {
		addErrorMsg(HEADER_RECORD_TYPE, "VERSION",
				"IAG Version not matched ::\t " + fileRowData.substring(0, 4) + " \t :: Header Row::\t " + fileRowData);
	}

	// FROM_AGENCY_ID //CHAR(4)
	if (!fileRowData.substring(12, 16).matches(IAGConstants.AGENCY_ID_FORMAT)
			|| !AgencyDataExcelReader.agencyCode.contains(fileRowData.substring(12, 16))) {
		addErrorMsg(HEADER_RECORD_TYPE, "FROM_AGENCY_ID",
				"From Agency ID not match with configuration. Please check Agency list \t ::"
						+ fileRowData.substring(12, 16));

	}

	// TO_AGENCY_ID //CHAR(4)

	if (!fileRowData.substring(16, 20).matches(IAGConstants.AGENCY_ID_FORMAT)
			|| !AgencyDataExcelReader.agencyCode.contains(fileRowData.substring(16, 20))) {
		addErrorMsg(HEADER_RECORD_TYPE, "TO_AGENCY_ID",
				"To Agency ID not match with configuration. Please check Agency list \t ::"
						+ fileRowData.substring(16, 20));

	}

	// FILE_DATE_TIME CHAR(20) Format: YYYY-MM-DDThh:mm:ssZ
	String headerfileDateandTime = fileRowData.substring(20, 40);
	if (!headerfileDateandTime.matches(IAGConstants.FILE_DATE_TIME_FORMAT)) {

		addErrorMsg(HEADER_RECORD_TYPE, "FILE_DATE_TIME",
				" date and time format is invalid. Format should be YYYY-MM-DDThh:mm:ssZ  \t ::"
						+ headerfileDateandTime);
	} else {
		// Check if the date and time are valid
		if (!commonUtil.isValidDateTimeInDetail(headerfileDateandTime)) {
			addErrorMsg(HEADER_RECORD_TYPE, "FILE_DATE_TIME",
					" Invalid date and time. Please check(YYYY-MM-DDThh:mm:ssZ)   \t ::" + headerfileDateandTime);
		}
	}

	// RECORD_COUNT CHAR(8) Values: 00000000 – 99999999
	if (!fileRowData.substring(40, 48).matches(IAGConstants.TRAN_RECORD_COUNT_FORMAT)) {
		addErrorMsg(HEADER_RECORD_TYPE, "RECORD_COUNT",
				" Invalid record count format. Values: 00000000 – 99999999    \t ::" + fileRowData.substring(40, 48));
	}

	// ICRX_FILE_NUM CHAR(12) Values 000000000001 – 999999999999.

	if (!fileRowData.substring(48, 60).matches(IAGConstants.ICTX_FILE_NUM_FORMAT)) {
		addErrorMsg(HEADER_RECORD_TYPE, "ICRX_FILE_NUM",
				" Invalid ICRX file number format  \t ::" + fileRowData.substring(48, 60));
	}

	return true;
}

private void addErrorMsg(String fileType,String fieldName,String errorMsg) {
	controller.getErrorMsglist().add(new ErrorMsgDetail(fileType,fieldName,errorMsg));
}
}
