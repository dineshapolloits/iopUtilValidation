package com.apolloits.util.validator;

import static com.apolloits.util.IAGConstants.DETAIL_RECORD_TYPE;
import static com.apolloits.util.IAGConstants.FILE_RECORD_TYPE;
import static com.apolloits.util.IAGConstants.HEADER_RECORD_TYPE;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.apolloits.util.IAGConstants;
import com.apolloits.util.IagAckFileMapper;
import com.apolloits.util.controller.ValidationController;
import com.apolloits.util.modal.ErrorMsgDetail;
import com.apolloits.util.modal.FileValidationParam;
import com.apolloits.util.modal.ITXCTemplate;
import com.apolloits.util.reader.AgencyDataExcelReader;
import com.apolloits.util.utility.CommonUtil;
import com.apolloits.util.writer.ITXCTemplateValidationExcelWriter;

import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;

@Slf4j
@Component
public class ITXCFileDetailValidation {

	@Autowired
	private IagAckFileMapper iagAckMapper;
	
	@Autowired
	private AgencyDataExcelReader agDataExcel;
	
	@Autowired
	@Lazy
	ValidationController controller;
	
	@Autowired
	private CommonUtil commonUtil;
	
	@Autowired
	ITXCTemplateValidationExcelWriter itxctempExcel;
	
	List<ITXCTemplate> itxcTempList;
	
	@Autowired
	ICTXFileDetailValidation ictxfileValidation;
	
	
public boolean itxcValidation(FileValidationParam validateParam) throws IOException {
		
		File inputItagZipFile = new File(validateParam.getInputFilePath());
		 String ackFileName = null;
		 if (!inputItagZipFile.exists()) {
			 log.error("ZIP file not found");
			 controller.getErrorMsglist().add(new ErrorMsgDetail(FILE_RECORD_TYPE,"File","ZIP file not found"));
			 return false;
        }else {
        	log.info("ictxValidation FileValidationParam vaidation from UI ");
        	if(!commonUtil.validateFromandToAgencyByFileName(inputItagZipFile.getName(),validateParam)) {
        		return false;
        	}
        	/*if(!validateParam.getFromAgency().equals(inputItagZipFile.getName().substring(0,4))) {
          		 log.error("From Agency code not match with file Name");
          		 controller.getErrorMsglist().add(new ErrorMsgDetail(FILE_RECORD_TYPE,"From Agency","From Agency code "+validateParam.getFromAgency()+" not match with file Name ::"+inputItagZipFile.getName()));
          		 return false;
          	 }
        	
        	if(validateParam.getFromAgency().equals(validateParam.getToAgency())) {
       		 log.error("From Agency code and To agency code should not be same");
       		 controller.getErrorMsglist().add(new ErrorMsgDetail(FILE_RECORD_TYPE,"From Agency","From Agency code "+validateParam.getFromAgency()+" and Toagency code should not be same  ::"+validateParam.getToAgency()));
       		 return false;
       	 }
        	
        	if(!AgencyDataExcelReader.agencyCode.contains(validateParam.getToAgency())) {
       		 log.error("To Agency code not match with file Name");
       		 //validateParam.setResponseMsg("From Agency code "+validateParam.getFromAgency()+" not match with file Name ::"+inputItagZipFile.getName());
       		 controller.getErrorMsglist().add(new ErrorMsgDetail(FILE_RECORD_TYPE,"To Agency","To Agency code "+validateParam.getToAgency()+" not match with Configuration ::"));
       		 return false;
       	 }*/
        	
        	//validate ZIP file name format
        	if(commonUtil.validateTransactionZIPFileName(inputItagZipFile.getName(), IAGConstants.ITXC_FILE_TYPE,validateParam)) {
        		
        		String fileName="";
       		 //extract ZIP file 
       		 ZipFile zipFile = new ZipFile(inputItagZipFile);
       		 try {
					log.info("ITXC extract file name getFileHeaders ******************* "+zipFile.getFileHeaders().get(0).getFileName());
					log.info("ITXC inputItagZipFile.getAbsolutePath() :: "+inputItagZipFile.getAbsolutePath());
					zipFile.extractAll(FilenameUtils.getFullPath(inputItagZipFile.getAbsolutePath()));
					zipFile.close();
					fileName =zipFile.getFileHeaders().get(0).getFileName();
					ackFileName = validateParam.getToAgency() + "_" + fileName.replace(".", "_") + IAGConstants.ACK_FILE_EXTENSION;
				} catch (ZipException e) {
					e.printStackTrace();
					controller.getErrorMsglist().add(new ErrorMsgDetail(FILE_RECORD_TYPE,"ZIP File","ZIP file extraction failed \t ::"+fileName));
					// validateParam.setResponseMsg("FAILED Reason:: ZIP file extraction failed");
	        		 return false;
				}
       		 
       		if(commonUtil.isTransactionFileFormatValid(fileName,IAGConstants.ITXC_FILE_TYPE,validateParam)) {
   			 if(validateParam.getValidateType().equals("filename")) {
   				 validateParam.setResponseMsg("File name validation is sucess");
   				 return true;
   			 }
   			 
   			 //Start to validate file header and detail  record
   			 long noOfRecords = 0;
				try (BufferedReader br = new BufferedReader(
						new FileReader(zipFile.getFile().getParent()+File.separator+fileName))) {

					String fileRowData;
					long headerCount =0l;
					String ackCode="00";
					String itxcFileNum="";
					ictxfileValidation.invalidRecordCount = 0;
					while ((fileRowData = br.readLine()) != null) {
						log.info(noOfRecords + " :: " + fileRowData);
						if(noOfRecords == 0) {
							// Validate Header record
							
							
							if(!validateItxcHeader(fileRowData,validateParam,fileName)) {
								//create ACK file 
								 //String ackFileName = IAGConstants.SRTA_HOME_AGENCY_ID + "_" + fileName.replace(".", "_") + IAGConstants.ACK_FILE_EXTENSION;
								ackCode = "01";
								//return false;
							}
							
							iagAckMapper.mapToIagAckFile(fileName, ackCode, validateParam.getOutputFilePath()+File.separator+ackFileName, fileName.substring(0, 4),validateParam.getToAgency());
							if (fileRowData.length()> 48 && fileRowData.substring(40, 48).matches(IAGConstants.TRAN_RECORD_COUNT_FORMAT)) {
								headerCount = Long.parseLong(fileRowData.substring(40, 48));
							}else {
								log.error("Invalid count format ::"+fileRowData);
								headerCount =0;
								controller.getErrorMsglist().add(new ErrorMsgDetail(HEADER_RECORD_TYPE,"RECORD_COUNT","Invalid count format::"+fileRowData));
								return false;
							}
							if(validateParam.getValidateType().equals("header")) {
					        	 log.info("Only file name and header validation");
					        	 return true;
					         }
							itxcFileNum = fileRowData.substring(48, 60); //ITCX file sequence no for ictxTemplate excel creation
							itxcTempList = new LinkedList<ITXCTemplate>();
						}else {
							if(!validateItxcDetail(fileRowData,validateParam,noOfRecords)) {
								//validateParam.setResponseMsg(validateParam.getResponseMsg() +"\t    Line No::"+noOfRecords);
								iagAckMapper.mapToIagAckFile(fileName, "02", validateParam.getOutputFilePath()+File.separator+ackFileName, fileName.substring(0, 4),validateParam.getToAgency());
								//return false;
							}
								addITXCTemplate(fileRowData.substring(2),itxcFileNum,fileRowData.substring(0, 2)); //This method add ITXCtemplate value in list
						}
						noOfRecords++;
					}
					log.info("ictxfileValidation.invalidRecordCount :: = "+ictxfileValidation.invalidRecordCount);
					if((noOfRecords-1) != headerCount ) {
						validateParam.setResponseMsg("\t Header count("+headerCount+") and detail count not matching ::"+(noOfRecords-1));
						iagAckMapper.mapToIagAckFile(fileName, "01", validateParam.getOutputFilePath()+File.separator+ackFileName, fileName.substring(0, 4),validateParam.getToAgency());
						return false;
					}
					if(controller.getErrorMsglist().size()>0 && ictxfileValidation.invalidRecordCount >0) {
						validateParam.setResponseMsg("\t \t ACK file name ::"+ackFileName);
						iagAckMapper.mapToIagAckFile(fileName, "02", validateParam.getOutputFilePath()+File.separator+ackFileName, fileName.substring(0, 4),validateParam.getToAgency());
					} else if(controller.getErrorMsglist().size()== 0 && ictxfileValidation.invalidRecordCount == 0 ) {
						// generate ICTXTemplate format excel file.
						log.info("itxcTempList size ::" + itxcTempList.size());
						if (controller.getErrorMsglist().size() == 0) {
							String itxcTempExcelFileName =validateParam.getOutputFilePath()+File.separator+FilenameUtils.removeExtension(fileName)+"_ITXCTemplate.xlsx";
							itxctempExcel.createItxcTemplateExcel(itxcTempList, itxcTempExcelFileName, validateParam);
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
					// Display pop up message if exceptionn occurs
					log.info("Error while reading a file.");
				}
      		 }
       		
        	}
        }
		return true;
}

/**
 * @author DK
 * @param fileRowData
 * @param itxFileNum
 * This method add only good record value in itxcempList
 */
private void addITXCTemplate(String fileRowData, String ictxFileNum,String corrReason) {
	if (controller.getErrorMsglist().size() == 0) {
		ITXCTemplate itxcTem = new ITXCTemplate();
		itxcTem.setCorrReason(corrReason);
		itxcTem.setItxcFileNum(ictxFileNum);
		itxcTem.setEtcTrxSerialNo(fileRowData.substring(0, 20));
		itxcTem.setEtcRevenueDate(fileRowData.substring(20, 28));
		itxcTem.setEtcTagAgency(fileRowData.substring(76, 80));
		itxcTem.setEtcTagSerialNumber(fileRowData.substring(80, 90));
		itxcTem.setEtcValidationStatus(fileRowData.substring(96, 97));
		itxcTem.setEtcLicState(fileRowData.substring(97, 99));
		itxcTem.setEtcLicNumber(fileRowData.substring(99, 109));
		itxcTem.setEtcClassCharged(fileRowData.substring(139, 142));
		itxcTem.setEtcExitDateTime(fileRowData.substring(148, 173));
		itxcTem.setEtcExitPlaza(fileRowData.substring(173, 188));
		itxcTem.setEtcExitLane(fileRowData.substring(188, 191));
		itxcTem.setEtcTrxType(fileRowData.substring(32, 33));
		itxcTem.setEtcEntryDateTime(fileRowData.substring(33, 58));
		itxcTem.setEtcEntryPlaza(fileRowData.substring(58, 73));
		itxcTem.setEtcEntryLane(fileRowData.substring(73, 76));
		itxcTem.setEtcReadPerformance(fileRowData.substring(90, 92));
		itxcTem.setEtcWritePerf(fileRowData.substring(92, 94));
		itxcTem.setEtcTagPgmStatus(fileRowData.substring(94, 95));
		itxcTem.setEtcLaneMode(fileRowData.substring(95, 96));
		itxcTem.setEtcOverSpeed(fileRowData.substring(147, 148));
		itxcTem.setEtcDebitCredit(fileRowData.substring(191, 192));
		itxcTem.setEtcTollAmount(fileRowData.substring(192, 201));
		itxcTempList.add(itxcTem);
	}
}


private boolean validateItxcDetail(String fileRowData, FileValidationParam validateParam,
		long rowNo) {
	String lineNo = "\t <b> Row ::</b>"+fileRowData +"\t <b>Line No::<b> \t"+rowNo;
	
	// Record count 204
    if (fileRowData == null || fileRowData.length() != 203) {
    	 addErrorMsg(DETAIL_RECORD_TYPE,"Detail Length","Record length is not match with ITXC length 203 ::\t "+lineNo);
         return false;
    }
  //CORR_REASON CHAR(2
    if (!fileRowData.substring(0, 2).matches("01|02|03|04|05|06|07|08")) {
    	addErrorMsg(DETAIL_RECORD_TYPE, "CORR_REASON",
				"Correction code should be 01|02|03|04|05|06|07|08 ::\t "
						+ fileRowData.substring(0, 2) + "\t " + lineNo);
    	ictxfileValidation.invalidRecordCount ++;
    }
    	ictxfileValidation.validateIctxDetail(fileRowData.substring(2),validateParam,rowNo);
    	
	return true;
}


private boolean validateItxcHeader(String fileRowData, FileValidationParam validateParam, String fileName) {
	boolean invalidHeaderRecord = false;
	// Header Total 61
    if (fileRowData == null || fileRowData.length() != 60 || fileRowData.isEmpty()) {
    	controller.getErrorMsglist().add(new ErrorMsgDetail(HEADER_RECORD_TYPE,"Header Length","Invalid header length \t Header Row::"+fileRowData));
    	return false;
    }
    
    // FILE_TYPE
    if (!fileRowData.substring(0, 4).equals(IAGConstants.ITXC_FILE_TYPE)) {
    	invalidHeaderRecord = true;
        controller.getErrorMsglist().add(new ErrorMsgDetail(HEADER_RECORD_TYPE,"FILE_TYPE","File Type should be ITXC ::\t "+fileRowData.substring(0, 4)+" \t :: Header Row::\t "+fileRowData));
    }
    //IAG Version
    if (!fileRowData.substring(4, 12).matches(IAGConstants.IAG_HEADER_VERSION_FORMAT) || ( ValidationController.cscIdTagAgencyMap.get(fileRowData.substring(12, 16)) != null &&
    		!fileRowData.substring(4, 12).equals(ValidationController.cscIdTagAgencyMap.get(fileRowData.substring(12, 16)).getVersionNumber()))) {
    	invalidHeaderRecord = true;
    	addErrorMsg(HEADER_RECORD_TYPE,"VERSION","IAG Version not matched ::\t "+fileRowData.substring(0, 4)+" \t :: Header Row::\t "+fileRowData);
    }
	
    // FROM_AGENCY_ID  //CHAR(4)
	if (!fileRowData.substring(12, 16).matches(IAGConstants.AGENCY_ID_FORMAT)
			|| !AgencyDataExcelReader.agencyCode.contains(fileRowData.substring(12, 16)) || !fileRowData.substring(12, 16).equals(validateParam.getFromAgency())) {
		addErrorMsg(HEADER_RECORD_TYPE,"FROM_AGENCY_ID","From Agency ID not match with configuration. Please check Agency list \t ::"+fileRowData.substring(12, 16));
		invalidHeaderRecord = true;
	}

    // TO_AGENCY_ID //CHAR(4)

	if (!fileRowData.substring(16, 20).matches(IAGConstants.AGENCY_ID_FORMAT)
			|| !AgencyDataExcelReader.agencyCode.contains(fileRowData.substring(16, 20)) || !fileRowData.substring(16, 20).equals(validateParam.getToAgency())) {
		invalidHeaderRecord = true;
		addErrorMsg(HEADER_RECORD_TYPE,"TO_AGENCY_ID","To Agency ID not match with configuration. Please check Agency list \t ::"+fileRowData.substring(16, 20));
		
	}
	
	// FILE_DATE_TIME CHAR(20) Format: YYYY-MM-DDThh:mm:ssZ
	String headerfileDateandTime = fileRowData.substring(20, 40);
    if (!headerfileDateandTime.matches(IAGConstants.FILE_DATE_TIME_FORMAT)) {
    	invalidHeaderRecord = true;
        addErrorMsg(HEADER_RECORD_TYPE,"FILE_DATE_TIME"," date and time format is invalid. Format should be YYYY-MM-DDThh:mm:ssZ  \t ::"+headerfileDateandTime);
    }else {
    	//Check if the date and time are valid
    	if (!commonUtil.isValidDateTimeInDetail(headerfileDateandTime)) {
    		invalidHeaderRecord = true;
    		addErrorMsg(HEADER_RECORD_TYPE,"FILE_DATE_TIME"," Invalid date and time. Please check(YYYY-MM-DDThh:mm:ssZ)   \t ::"+headerfileDateandTime);
        }
    }
    
	// RECORD_COUNT CHAR(8) Values: 00000000 – 99999999
	if (!fileRowData.substring(40, 48).matches(IAGConstants.TRAN_RECORD_COUNT_FORMAT)) {
		invalidHeaderRecord = true;
		addErrorMsg(HEADER_RECORD_TYPE,"RECORD_COUNT"," Invalid record count format. Values: 00000000 – 99999999    \t ::"+fileRowData.substring(40, 48));
	}

	// ICTX_FILE_NUM CHAR(12) Values 000000000001 – 999999999999.

	if (!fileRowData.substring(48, 60).matches(IAGConstants.ICTX_FILE_NUM_FORMAT)) {
		invalidHeaderRecord = true;
		addErrorMsg(HEADER_RECORD_TYPE,"ICTX_FILE_NUM"," Invalid ICTX file number format  \t ::"+fileRowData.substring(48, 60));
	}
	if (invalidHeaderRecord) {
		return false;
	}
	return true;
}

private void addErrorMsg(String fileType, String fieldName, String errorMsg) {
	controller.getErrorMsglist().add(new ErrorMsgDetail(fileType, fieldName, errorMsg));
}
}
