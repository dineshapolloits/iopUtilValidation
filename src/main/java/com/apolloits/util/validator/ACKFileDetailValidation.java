package com.apolloits.util.validator;

import static com.apolloits.util.IAGConstants.DETAIL_RECORD_TYPE;
import static com.apolloits.util.IAGConstants.FILE_RECORD_TYPE;
import static com.apolloits.util.IAGConstants.HEADER_RECORD_TYPE;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.apolloits.util.IAGConstants;
import com.apolloits.util.controller.ValidationController;
import com.apolloits.util.modal.ErrorMsgDetail;
import com.apolloits.util.modal.FileValidationParam;
import com.apolloits.util.reader.AgencyDataExcelReader;
import com.apolloits.util.utility.CommonUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ACKFileDetailValidation {

	
	@Autowired
	@Lazy
	ValidationController controller;
	
	@Autowired
	private CommonUtil commonUtil;
	
	String fileName = "";
	
	public boolean ackValidation(FileValidationParam validateParam) throws IOException {

		File inputAckFile = new File(validateParam.getInputFilePath());
		if (!inputAckFile.exists()) {
			log.error("ACK file not found");
			controller.getErrorMsglist().add(new ErrorMsgDetail(FILE_RECORD_TYPE, "File", "ACK file not found"));
			return false;
		} else {
			log.info("ACKValidation FileValidationParam vaidation from UI ");
			fileName = inputAckFile.getName();
			
			/*if (!validateParam.getFromAgency().equals(inputAckFile.getName().substring(0, 4))) {
				log.error("From Agency code not match with file Name");
				controller.getErrorMsglist().add(new ErrorMsgDetail(FILE_RECORD_TYPE, "From Agency", "From Agency code "
						+ validateParam.getFromAgency() + " not match with file Name ::" + inputAckFile.getName()));
				return false;
			}*/

			if (validateParam.getFromAgency().equals(validateParam.getToAgency())) {
				log.error("From Agency code and To agency code should not be same");
				controller.getErrorMsglist()
						.add(new ErrorMsgDetail(FILE_RECORD_TYPE, "From Agency",
								"From Agency code " + validateParam.getFromAgency()
										+ " and Toagency code should not be same  ::" + validateParam.getToAgency()));
				return false;
			}

			/*if (!AgencyDataExcelReader.agencyCode.contains(validateParam.getToAgency())) {
				log.error("To Agency code not match with file Name");
				controller.getErrorMsglist().add(new ErrorMsgDetail(FILE_RECORD_TYPE, "To Agency",
						"To Agency code " + validateParam.getToAgency() + " not match with Configuration ::"));
				return false;
			}*/
			
			//validating ITAG and ICLP ack filename
			if(fileName.contains(IAGConstants.ITAG_FILE_TYPE) || fileName.contains(IAGConstants.ICLP_FILE_TYPE)) {
				log.info("Information file ACK validation");
				if(!validateITAGandICLPAckname(fileName,validateParam)) {
					controller.getErrorMsglist().add(new ErrorMsgDetail(FILE_RECORD_TYPE, "File Name",
							"Invalid file name format :: \t {FROM_AGENCY_ID}_{FILE_NAME}_{FILE_TYPE}.ACK \t ::" + fileName));
					return false;
				}
			}else if(!isValidTransactionFileType(fileName)) {
				log.info("Transaction file ACK validation");
				controller.getErrorMsglist().add(new ErrorMsgDetail(FILE_RECORD_TYPE, "File Name",
						"Invalid file name format :: \t {FROM_AGENCY_ID}_{FILE_NAME}_{FILE_TYPE}.ACK \t ::" + fileName));
				return false;
			}
			
			if (validateParam.getValidateType().equals("filename")) {
				validateParam.setResponseMsg("File name validation is sucess");
				return true;
			}
			
			//validate detail
			validateACKDetail(fileName,validateParam);
		}
		return true;
		
	}
	
	private boolean isValidTransactionFileType(String fileName) {
		
		if (!fileName.matches("\\d{4}_\\d{4}_\\d{4}_\\d{14}_(ICTX|ICRX|ITXC|IRXC)\\.ACK")) {
			return false;
        }
		return true;
    }

	private void validateACKDetail(String ackFileName, FileValidationParam validateParam) throws FileNotFoundException {
		
		long noOfRecords = 0;
		try (BufferedReader br = new BufferedReader(
				new FileReader(validateParam.getInputFilePath()))) {

			String fileRowData = br.readLine();
			if(fileRowData== null ||  fileRowData.length() != 92 ) {
				controller.getErrorMsglist().add(new ErrorMsgDetail(HEADER_RECORD_TYPE, "Header Length",
						"Invalid header length \t <b>Header Row::</b>" + fileRowData));
				return;
			}
			
			// FILE_TYPE
			if (!fileRowData.substring(0, 4).trim().equals(IAGConstants.ACK_FILE_TYPE)) {
				controller.getErrorMsglist()
						.add(new ErrorMsgDetail(HEADER_RECORD_TYPE, "FILE_TYPE", "File Type should be ACK ::\t "
								+ fileRowData.substring(0, 4) + " \t ::<b> Header Row::</b>\t " + fileRowData));
			}
			
			// IAG Version
			if (!fileRowData.substring(4, 12).matches(IAGConstants.IAG_HEADER_VERSION_FORMAT)
					|| ValidationController.cscIdTagAgencyMap.get(fileRowData.substring(12, 16)) == null
					|| !fileRowData.substring(4, 12)
					.equals(ValidationController.cscIdTagAgencyMap.get(fileRowData.substring(12, 16)).getVersionNumber())) {
				addErrorMsg(HEADER_RECORD_TYPE, "VERSION",
						"IAG Version not matched ::\t " + fileRowData.substring(12, 16) + " \t :: Header Row::\t " + fileRowData);
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
			
			//ORIG_FILE_NAME_TYPE //CHAR 50
			String origFileName=ackFileName.substring(ackFileName.indexOf("_")+1, ackFileName.lastIndexOf("_"))+"."+ackFileName.substring(ackFileName.lastIndexOf("_")+1).replace(".ACK","");
			log.info("origFileName ::\t"+origFileName);
			if (!fileRowData.substring(20, 70).trim().equals(origFileName)) {
				addErrorMsg(HEADER_RECORD_TYPE, "ORIG_FILE_NAME_TYPE",
						"ack file name not match with deatil \t ::"
								+ fileRowData.substring(20, 70).trim());
			}
			// FILE_DATE_TIME CHAR(20) Format: YYYY-MM-DDThh:mm:ssZ
			String headerfileDateandTime = fileRowData.substring(70, 90);
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
			
			// RETURN_CODE CHAR(2)
			if (!fileRowData.substring(90, 92).matches("00|01|02|03|04|05|06|07")) {
				addErrorMsg(HEADER_RECORD_TYPE, "RETURN_CODE",
						"Value should be 00|01|02|03|04|05|06|07 ::\t "
								+ fileRowData.substring(90, 92));
			}
			
			
		}catch (IOException e) {
			e.printStackTrace();
			validateParam.setResponseMsg("ACK file detail record reading exception ::\t"+ackFileName);
			System.out.println("Error while reading a file.");
		}
	}
	
	private void addErrorMsg(String fileType,String fieldName,String errorMsg) {
		controller.getErrorMsglist().add(new ErrorMsgDetail(fileType,fieldName,errorMsg));
	}

	private boolean validateITAGandICLPAckname(String ackFileName, FileValidationParam validateParam) {
		

		if (ackFileName != null && ackFileName.length() == 33) {
			String[] fileParams = fileName.split("[_.]");
			
			if ( IAGConstants.IAG_FILE_TYPES.contains(fileParams[3]) &&
					AgencyDataExcelReader.agencyCode.contains(fileParams[0]) 
					&&  fileParams[4].equals(IAGConstants.ACK_FILE_TYPE)) {
				
				SimpleDateFormat dateFormat = new SimpleDateFormat(IAGConstants.YYYY_MM_DD_HH_MM_SS);
				dateFormat.setLenient(false);
				try {
					dateFormat.parse(fileParams[2].trim());
					return true;
				} catch (ParseException pe) {
					return false;
				}
				
			}
		}
		return false;
 
	}
}
