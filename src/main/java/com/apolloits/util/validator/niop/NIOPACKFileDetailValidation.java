package com.apolloits.util.validator.niop;

import static com.apolloits.util.IAGConstants.DETAIL_RECORD_TYPE;
import static com.apolloits.util.IAGConstants.HEADER_RECORD_TYPE;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.apolloits.util.NIOPConstants;
import com.apolloits.util.controller.NiopValidationController;
import com.apolloits.util.modal.ErrorMsgDetail;
import com.apolloits.util.modal.FileValidationParam;
import com.apolloits.util.modal.niop.NiopAckFile;
import com.apolloits.util.utility.CommonUtil;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class NIOPACKFileDetailValidation {

	@Autowired
	CommonUtil commonUtil;
	
	
	@Autowired
	@Lazy
	NiopValidationController controller;
	
	public boolean ackValidation(FileValidationParam validateParam) throws IOException {
		File file = new File(validateParam.getInputFilePath());
		NiopAckFile ackFile;
		boolean validationFlag = false;
		 if(validateNiopACKFileName(file.getName(),validateParam)) {
			 try {
					//POC for SRECON reading 
					JAXBContext jaxbContext = JAXBContext.newInstance(NiopAckFile.class);
					Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
					ackFile = (NiopAckFile) unmarshaller.unmarshal(file);
					log.info("ACK  details:: " + ackFile.toString()); 
					
				} catch (Exception e) {
					log.error("Invalid XML file. Please check XML format");
					e.printStackTrace();
					validateParam.setResponseMsg("Invalid XML file. Please check XML format");
					return validationFlag;
				}
			 validationFlag =  validateACKDetails(file.getName(),validateParam,ackFile);
			 
		 }else {
			 log.error("ACK File validation failed");
			 return validationFlag;
		 }
		return  validationFlag;
	}

	private boolean validateACKDetails(String fileName, FileValidationParam validateParam,NiopAckFile ackFile) {
		boolean invalidRecord = true;
		//Submission Type
		if(ackFile.getSubmissionType() == null || !ackFile.getSubmissionType().equals(NIOPConstants.ACK_FILE_TYPE)) {
			addErrorMsg(DETAIL_RECORD_TYPE,"SubmissionType"," Invalid SubmissionType   \t ::"+ackFile.getSubmissionType());
        	log.error("Invalid SubmissionType   \t ::"+ackFile.getSubmissionType());
        	invalidRecord = false;
		}
		
		//Original submission Type
		String[] filenameSplit = fileName.split("[_.]");
		String fileType ="";
		if(fileName.length() == 46) {
			fileType = "STVL";
		}else {
			fileType = filenameSplit[7];
		}
		String originalSubmissionType =ackFile.getOriginalSubmissionType();
		if(originalSubmissionType == null || !originalSubmissionType.equals(fileType)) {
			addErrorMsg(DETAIL_RECORD_TYPE,"Original submission Type"," Invalid Original submission Type   \t ::"+originalSubmissionType);
        	log.error("Invalid Original SubmissionType   \t ::"+originalSubmissionType);
        	invalidRecord = false;
		}
		
		//Original Submission Date/Time
		if(ackFile.getOriginalSubmissionDateTime() == null  || !ackFile.getOriginalSubmissionDateTime().matches(NIOPConstants.UTC_DATE_YEAR_REGEX)) {
			addErrorMsg(DETAIL_RECORD_TYPE,"Original Submission Date/Time"," Invalid Original Submission Date/Time   \t ::"+ackFile.getOriginalSubmissionDateTime());
        	log.error("Invalid Original Submission Date/Time   \t ::"+ackFile.getOriginalSubmissionDateTime());
        	invalidRecord = false;
		}
		
		//NIOP Hub ID 
		if(ackFile.getNiopHubID() == null  || !ackFile.getNiopHubID().equals(String.valueOf(NiopValidationController.allCscIdNiopAgencyMap.get(validateParam.getFromAgency()).getHubId()))) {
			addErrorMsg(DETAIL_RECORD_TYPE,"Original Submission Date/Time"," Invalid Original Submission Date/Time   \t ::"+ackFile.getNiopHubID());
        	log.error("Invalid Original Submission Date/Time   \t ::"+ackFile.getNiopHubID());
        	invalidRecord = false;
		}
		
		//From Agency ID 
		if(!ackFile.getFromAgencyID().matches(NIOPConstants.AGENCY_ID_FORMAT) || !ackFile.getFromAgencyID().equals(validateParam.getFromAgency())){
			addErrorMsg(DETAIL_RECORD_TYPE,"From Agency ID"," Invalid From Agency ID   \t ::"+ackFile.getFromAgencyID());
            log.error("Detail validation failed, Invalid From Agency ID  :: " +ackFile.getFromAgencyID());
            invalidRecord = false;
        }
		//To Agency ID 
		if(!ackFile.getToAgencyID().matches(NIOPConstants.AGENCY_ID_FORMAT) || !ackFile.getToAgencyID().equals(validateParam.getToAgency())){
			addErrorMsg(DETAIL_RECORD_TYPE,"To Agency ID "," Invalid To Agency ID    \t ::"+ackFile.getToAgencyID());
            log.error("Detail validation failed, Invalid To Agency ID   :: " +ackFile.getToAgencyID());
            invalidRecord = false;
        }
		//ACK Date/Time
		if(ackFile.getAckDateTime() == null  || !ackFile.getAckDateTime().matches(NIOPConstants.UTC_DATE_YEAR_REGEX)) {
			addErrorMsg(DETAIL_RECORD_TYPE,"ACK Date/Time"," Invalid ACK Date/Time   \t ::"+ackFile.getAckDateTime());
        	log.error("Invalid ACK Date/Time   \t ::"+ackFile.getAckDateTime());
        	invalidRecord = false;
		}
		//Return Code
		if(ackFile.getAckReturnCode() == null  || !ackFile.getAckReturnCode().matches(NIOPConstants.ACK_CODES)) {
			addErrorMsg(DETAIL_RECORD_TYPE,"Return Code"," Invalid Return Code   \t ::"+ackFile.getAckReturnCode());
        	log.error("Invalid Return Code   \t ::"+ackFile.getAckReturnCode());
        	invalidRecord = false;
		}
		
		return invalidRecord;
	}

	private boolean validateNiopACKFileName(String fileName, FileValidationParam validateParam) {
		
		if (fileName.contains(NIOPConstants.BTVL_FILE_TYPE) || fileName.contains(NIOPConstants.DTVL_FILE_TYPE) ) {
			return validateTVLFileName(fileName, validateParam);
		} else {
			return validateTranFileName(fileName,validateParam);
		}
		
	}

	private boolean validateTranFileName(String fileName, FileValidationParam validateParam) {
		boolean fileNameValidationFlag = false;
		String[] fileParams = fileName.split("[_.]");
		System.out.println("validateTranFileName() ::  fileParams ::"+Arrays.toString(fileParams) +"\t length fileParams ::"+fileParams.length);
		if(fileName.matches("\\d{4}_\\d{4}_\\d{4}_\\d{4}_\\d{4}_\\d{14}_\\d{2}_(STRAN|SCORR|SRECON)\\.ACK")) {
			if(fileParams[1].equals(validateParam.getFromAgency()) && 
					fileParams[0].equals(String.valueOf(NiopValidationController.allCscIdNiopAgencyMap.get(validateParam.getFromAgency()).getHubId())) &&
					fileParams[3].equals(validateParam.getToAgency()) &&
					fileParams[2].equals(String.valueOf(NiopValidationController.allCscIdNiopAgencyMap.get(validateParam.getToAgency()).getHubId())) &&
					fileParams[4].equals(validateParam.getFromAgency()) &&
					fileParams[6].matches(NIOPConstants.ACK_CODES) &&
					CommonUtil.isValidDateTime(fileParams[5])) {
				
				fileNameValidationFlag = true;
			}
		}
		validateParam.setResponseMsg("File name validation failed");
		return fileNameValidationFlag;
	}

	private boolean validateTVLFileName(String fileName, FileValidationParam validateParam) {
		boolean fileNameValidationFlag = false;
		String[] fileParams = fileName.split("[_.]");
		System.out.println("length fileParams ::"+fileParams.length);
		System.out.println("validateTVLFileName() ::  fileParams ::"+Arrays.toString(fileParams));
		if(fileName.matches("\\d{4}_\\d{4}_\\d{4}_\\d{4}_\\d{14}_\\d{2}_(BTVL|DTVL)\\.ACK") && fileParams.length == 8) {
			if(fileParams[1].equals(validateParam.getFromAgency()) && 
					fileParams[0].equals(String.valueOf(NiopValidationController.allCscIdNiopAgencyMap.get(validateParam.getFromAgency()).getHubId())) &&
					fileParams[3].equals(validateParam.getToAgency()) &&
					fileParams[2].equals(String.valueOf(NiopValidationController.allCscIdNiopAgencyMap.get(validateParam.getToAgency()).getHubId())) &&
					fileParams[5].matches(NIOPConstants.ACK_CODES) &&
					CommonUtil.isValidDateTime(fileParams[4])) {
				
				fileNameValidationFlag = true;
			}
			
		}
		validateParam.setResponseMsg("File name validation failed");
		return fileNameValidationFlag;
	}
	private void addErrorMsg(String fileType,String fieldName,String errorMsg) {
		controller.getErrorMsglist().add(new ErrorMsgDetail(fileType,fieldName,errorMsg));
	}
}
