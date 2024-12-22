package com.apolloits.util.validator;

import static com.apolloits.util.IAGConstants.DETAIL_RECORD_TYPE;
import static com.apolloits.util.IAGConstants.FILE_RECORD_TYPE;
import static com.apolloits.util.IAGConstants.HEADER_RECORD_TYPE;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
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
import com.apolloits.util.modal.ICTXTemplate;
import com.apolloits.util.reader.AgencyDataExcelReader;
import com.apolloits.util.utility.CommonUtil;
import com.apolloits.util.writer.ICTXTemplateValidationExcelWriter;

import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;

@Slf4j
@Component
public class ICTXFileDetailValidation {


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
	ICTXTemplateValidationExcelWriter ictxtempExcel;
	
	List<ICTXTemplate> ictxTempList;
	
public boolean ictxValidation(FileValidationParam validateParam) throws IOException {
		
		File inputItagZipFile = new File(validateParam.getInputFilePath());
		 String ackFileName = null;
		 if (!inputItagZipFile.exists()) {
			 log.error("ZIP file not found");
			 controller.getErrorMsglist().add(new ErrorMsgDetail(FILE_RECORD_TYPE,"File","ZIP file not found"));
			 return false;
        }else {
        	log.info("ictxValidation FileValidationParam vaidation from UI ");
        	if(!validateParam.getFromAgency().equals(inputItagZipFile.getName().substring(0,4))) {
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
       	 }
        	//validate ZIP file name format
        	if(validateZIPFileName(inputItagZipFile.getName())) {
        		
        		String fileName="";
       		 //extract ZIP file 
       		 ZipFile zipFile = new ZipFile(inputItagZipFile);
       		 try {
					log.info("ICTX extract file name getFileHeaders ******************* "+zipFile.getFileHeaders().get(0).getFileName());
					log.info("ICTX inputItagZipFile.getAbsolutePath() :: "+inputItagZipFile.getAbsolutePath());
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
       		 
       		 if(commonUtil.isTransactionFileFormatValid(fileName,"ICTX")) {
    			 if(validateParam.getValidateType().equals("filename")) {
    				 validateParam.setResponseMsg("File name validation is sucess");
    				 return true;
    			 }
    			 
    			 //Start to validate file header and detail  record
    			 long noOfRecords = 0;
				try (BufferedReader br = new BufferedReader(
						new FileReader(zipFile.getFile().getParent()+"\\"+fileName))) {

					String fileRowData;
					long headerCount =0l;
					String ackCode="00";
					String ictxFileNum="";
					while ((fileRowData = br.readLine()) != null) {
						log.info(noOfRecords + " :: " + fileRowData);
						if(noOfRecords == 0) {
							// Validate Header record
							headerCount = Long.parseLong(fileRowData.substring(40, 48));
							
							if(!validateIctxHeader(fileRowData,validateParam,fileName)) {
								//create ACK file 
								 //String ackFileName = IAGConstants.SRTA_HOME_AGENCY_ID + "_" + fileName.replace(".", "_") + IAGConstants.ACK_FILE_EXTENSION;
								ackCode = "01";
								//return false;
							}
							
							iagAckMapper.mapToIagAckFile(fileName, ackCode, validateParam.getOutputFilePath()+"\\"+ackFileName, fileName.substring(0, 4),validateParam.getToAgency());
							if(validateParam.getValidateType().equals("header")) {
					        	 log.info("Only file name and header validation");
					        	 return true;
					         }
							ictxFileNum = fileRowData.substring(48, 60); //ICTX file sequence no for ictxTemplate excel creation
							ictxTempList = new LinkedList<ICTXTemplate>();
						}else {
							if(!validateIctxDetail(fileRowData,validateParam,noOfRecords)) {
								//validateParam.setResponseMsg(validateParam.getResponseMsg() +"\t    Line No::"+noOfRecords);
								iagAckMapper.mapToIagAckFile(fileName, "02", validateParam.getOutputFilePath()+"\\"+ackFileName, fileName.substring(0, 4),validateParam.getToAgency());
								return false;
							}
							addICTXTemplate(fileRowData,ictxFileNum); //This method add ICTXtemplate value in list
						}
						noOfRecords++;
					}
					if((noOfRecords-1) != headerCount ) {
						validateParam.setResponseMsg("FAILED Reason:: Header count("+headerCount+") and detail count not matching ::"+noOfRecords);
						iagAckMapper.mapToIagAckFile(fileName, "01", validateParam.getOutputFilePath()+"\\"+ackFileName, fileName.substring(0, 4),validateParam.getToAgency());
						return false;
					}
					if(controller.getErrorMsglist().size()>0) {
						validateParam.setResponseMsg("\t \t ACK file name ::"+ackFileName);
						iagAckMapper.mapToIagAckFile(fileName, "02", validateParam.getOutputFilePath()+"\\"+ackFileName, fileName.substring(0, 4),validateParam.getToAgency());
					} else {
						// generate ICTXTemplate format excel file.
						log.info("ictxTempList size ::" + ictxTempList.size());
						if (controller.getErrorMsglist().size() == 0) {
							String ictxTempExcelFileName =validateParam.getOutputFilePath()+File.separator+FilenameUtils.removeExtension(fileName)+"_ICTXTemplate.xlsx";
							ictxtempExcel.createIctxTemplateExcel(ictxTempList, ictxTempExcelFileName, validateParam);
						}
					}
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

/**
 * @author DK
 * @param fileRowData
 * @param ictxFileNum
 * This method add only good record value in ictxTempList
 */
private void addICTXTemplate(String fileRowData, String ictxFileNum) {
	if (controller.getErrorMsglist().size() == 0) {
		ICTXTemplate ictxTem = new ICTXTemplate();
		ictxTem.setIctxFileNum(ictxFileNum);
		ictxTem.setEtcTrxSerialNo(fileRowData.substring(0, 20));
		ictxTem.setEtcRevenueDate(fileRowData.substring(20, 28));
		ictxTem.setEtcTagAgency(fileRowData.substring(76, 80));
		ictxTem.setEtcTagSerialNumber(fileRowData.substring(80, 90));
		ictxTem.setEtcValidationStatus(fileRowData.substring(96, 97));
		ictxTem.setEtcLicState(fileRowData.substring(97, 99));
		ictxTem.setEtcLicNumber(fileRowData.substring(99, 109));
		ictxTem.setEtcClassCharged(fileRowData.substring(139, 142));
		ictxTem.setEtcExitDateTime(fileRowData.substring(148, 173));
		ictxTem.setEtcExitPlaza(fileRowData.substring(173, 188));
		ictxTem.setEtcExitLane(fileRowData.substring(188, 191));
		ictxTem.setEtcTrxType(fileRowData.substring(32, 33));
		ictxTem.setEtcEntryDateTime(fileRowData.substring(33, 58));
		ictxTem.setEtcEntryPlaza(fileRowData.substring(58, 73));
		ictxTem.setEtcEntryLane(fileRowData.substring(73, 76));
		ictxTem.setEtcReadPerformance(fileRowData.substring(90, 92));
		ictxTem.setEtcWritePerf(fileRowData.substring(92, 94));
		ictxTem.setEtcTagPgmStatus(fileRowData.substring(94, 95));
		ictxTem.setEtcLaneMode(fileRowData.substring(95, 96));
		ictxTem.setEtcOverSpeed(fileRowData.substring(147, 148));
		ictxTem.setEtcDebitCredit(fileRowData.substring(191, 192));
		ictxTem.setEtcTollAmount(fileRowData.substring(192, 201));
		ictxTempList.add(ictxTem);
	}
}

public boolean validateIctxDetail(String fileRowData, FileValidationParam validateParam, long rowNo) {
	
	String lineNo = "\t Row ::"+fileRowData +"\t Line No::"+rowNo;
	// If detail record length is not matched. not validating other fields 
    if (fileRowData == null || fileRowData.length() != 201) {
        addErrorMsg(DETAIL_RECORD_TYPE,"Detail Length","Record length is not match with ICTX length 201 ::\t "+lineNo);
        return false;
    }
    // ETC_TRX_SERIAL_NUM CHAR(20) Values: 00000000000000000000 – 99999999999999999999
    if (!fileRowData.substring(0, 20).matches(IAGConstants.ETC_TRX_SERIAL_NUM_FORMAT)) {
        addErrorMsg(DETAIL_RECORD_TYPE,"ETC_TRX_SERIAL_NUM","Format not matched Values: 00000000000000000000 – 99999999999999999999 ::\t "+fileRowData.substring(0, 20)+"\t "+lineNo);
    }
	
	// ETC_REVENUE_DATE CHAR(8) Format: YYYYMMDD
	if (!fileRowData.substring(20, 28).matches("\\d{8}")) {
		 addErrorMsg(DETAIL_RECORD_TYPE,"ETC_REVENUE_DATE","Date format is invalid. Format should be Format: YYYYMMDD::\t "+fileRowData.substring(20, 28)+"\t "+lineNo);
	}
	
	// ETC_FAC_AGENCY CHAR(4)
    if (!fileRowData.substring(28, 32).matches(IAGConstants.AGENCY_ID_FORMAT ) || 
    		 !AgencyDataExcelReader.agencyCode.contains(fileRowData.substring(28, 32))) {
        addErrorMsg(DETAIL_RECORD_TYPE,"ETC_FAC_AGENCY","ETC FAC Agency code not configured::\t "+fileRowData.substring(28, 32)+"\t "+lineNo);
    }
    
 // ETC_TRX_TYPE CHAR(1) 
    //Values:
    //B – Barrier
    //C – Ticketed Complete
    //X – Ticketed Unmatched Exit
    if (!fileRowData.substring(32, 33).matches("[BCX]")) {
        addErrorMsg(DETAIL_RECORD_TYPE,"ETC_TRX_TYPE"," It will sopport only [B,C,X] \t "+fileRowData.substring(32, 33)+"\t "+lineNo);
    }
    String entryDateTime = fileRowData.substring(33, 58);
    System.out.println("entryDateTime ::"+entryDateTime);
    // ETC_ENTRY_DATE_TIME CHAR(25)  Format: YYYY-MM-DDThh:mm:ss±HH:MM
	if (!entryDateTime.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}[-+]\\d{2}:\\d{2}")
			&& !entryDateTime.equals("*************************")) {
		addErrorMsg(DETAIL_RECORD_TYPE, "ETC_ENTRY_DATE_TIME",
				" Invalid entry dateTime format \t " + entryDateTime + "\t " + lineNo);
	}/* else {
		// Check if the date and time are valid
		if (!entryDateTime.equals("*************************")) {
			if (!commonUtil.isValidDateTimeInDetail(entryDateTime))
				addErrorMsg(DETAIL_RECORD_TYPE, "ETC_ENTRY_DATE_TIME",
						" Invalid date and time. Please check(YYYY-MM-DDThh:mm:ssZ)   \t ::" + entryDateTime);
		}

	}*/
	
    // ETC_ENTRY_PLAZA CHAR(15)
    if (!commonUtil.isValidEtcPlaza(fileRowData.substring(58, 73),"entry")) {
    	addErrorMsg(DETAIL_RECORD_TYPE, "ETC_ENTRY_PLAZA",
				" Invalid entry plaza format \t " + fileRowData.substring(58, 73) + "\t " + lineNo);
    }
    
    // ETC_ENTRY_LANE CHAR(3)
    if ( !commonUtil.isValidEtcLane (fileRowData.substring(73, 76),"entry")) {
    	addErrorMsg(DETAIL_RECORD_TYPE, "ETC_ENTRY_LANE",
				" Invalid entry lane format \t " + fileRowData.substring(73, 76) + "\t " + lineNo);
    }
    
    // ETC_TAG_AGENCY CHAR(4) Values: 0000 – 9999
    String etctagAgencyId = fileRowData.substring(76, 80);
    if (!etctagAgencyId.matches(IAGConstants.AGENCY_ID_FORMAT) ||
    		!AgencyDataExcelReader.agencyCode.contains(etctagAgencyId)) {
        addErrorMsg(DETAIL_RECORD_TYPE, "ETC_TAG_AGENCY",
				" Invalid tag agency code"+ etctagAgencyId + "\t " + lineNo);
    }
    
    // ETC_TAG_SERIAL_NUMBER CHAR(10) Values: 0000000001 – 9999999999

    if (!fileRowData.substring(80, 90).matches(IAGConstants.ETC_TAG_SERIAL_NUMBER_FORMAT)) {
    	addErrorMsg(DETAIL_RECORD_TYPE, "ETC_TAG_SERIAL_NUMBER",
				" Invalid format"+ fileRowData.substring(80, 90) + "\t " + lineNo);
    }else {
    	//need to check format range from agency table start and end tag column
    }
    
    // ETC_READ_PERFORMANCE CHAR(2) Values: 00 – 99
    String etcreadPerformance = fileRowData.substring(90, 92);
    if (!etcreadPerformance.matches("\\d{2}") &&  !etcreadPerformance.equals("**")) {
    	addErrorMsg(DETAIL_RECORD_TYPE, "ETC_READ_PERFORMANCE",
				" Invalid format"+ etcreadPerformance + "\t " + lineNo);
    }
    
    // ETC_WRITE_PERF CHAR(2) Values: 00 – 99 
    String etcWritePerf = fileRowData.substring(92, 94);
    if (!etcWritePerf.matches("\\d{2}") && !etcWritePerf.equals("**")) {
    	addErrorMsg(DETAIL_RECORD_TYPE, "ETC_WRITE_PERF",
				" Invalid format"+ etcWritePerf + "\t " + lineNo);
    }
    
    // ETC_TAG_PGM_STATUS CHAR(1)
    if (!fileRowData.substring(94, 95).matches("[SUF*]")) {
    	addErrorMsg(DETAIL_RECORD_TYPE, "ETC_TAG_PGM_STATUS",
				" Should be [S,U,F,*]. Invalid Data"+ fileRowData.substring(94, 95) + "\t " + lineNo);
    }

    // ETC_LANE_MODE CHAR(1) 
    if (!fileRowData.substring(95, 96).matches("[EAMCO]")) {
        addErrorMsg(DETAIL_RECORD_TYPE, "ETC_TAG_PGM_STATUS",
				" Should be [E,A,M,C,O]. Invalid Data"+ fileRowData.substring(95, 96) + "\t " + lineNo);
    }
    
    
 // ETC_VALIDATION_STATUS CHAR(1) 
    //Values: 1 – Good
    //2 – Low Balance
    //3 – Zero/Negative Balance (only if agreed upon between the agencies) 

    if (!fileRowData.substring(96, 97).matches("[123*]")) {
        addErrorMsg(DETAIL_RECORD_TYPE, "ETC_VALIDATION_STATUS",
				" Should be [1,2,3,*]. Invalid Data"+ fileRowData.substring(96, 97) + "\t " + lineNo);
    }

    // ETC_LIC_STATE CHAR(2) 
    String licState = fileRowData.substring(97, 99);
    if (!licState.matches(IAGConstants.LIC_STATE_FORMAT) && !licState.equals("**")) {
        addErrorMsg(DETAIL_RECORD_TYPE, "ETC_LIC_STATE",
				" Invalid format \t ::"+ licState + "\t " + lineNo);
    }
    
    // ETC_LIC_NUMBER CHAR(10) 
    String etcLicNo = fileRowData.substring(99, 109);
    if (!etcLicNo.matches(IAGConstants.LIC_NUMBER_FORMAT) && !etcLicNo.equals("**********")) {
  
        addErrorMsg(DETAIL_RECORD_TYPE, "ETC_LIC_NUMBER",
				" Invalid format \t ::"+ etcLicNo + "\t " + lineNo);
    }

    // ETC_LIC_TYPE CHAR(30)
    String etcLicType = fileRowData.substring(109, 139);
    if (!etcLicType.matches(IAGConstants.LIC_TYPE_FORMAT) && !etcLicType.equals("******************************")) {
        addErrorMsg(DETAIL_RECORD_TYPE, "ETC_LIC_TYPE",
				" Invalid format \t ::"+ etcLicType + "\t " + lineNo);
    }
    
    // ETC_CLASS_CHARGED CHAR(3) .
    if (!fileRowData.substring(139, 142).matches("[A-Z 0-9]{1,3}")) {
    	addErrorMsg(DETAIL_RECORD_TYPE, "ETC_CLASS_CHARGED",
				" Invalid format \t ::"+ fileRowData.substring(139, 142) + "\t " + lineNo);
    }
    
    // ETC_ACTUAL_AXLES CHAR(2) //Values: 00 – 99 
    if (!fileRowData.substring(142, 144).matches("\\d{2}")) {
        addErrorMsg(DETAIL_RECORD_TYPE, "ETC_ACTUAL_AXLES",
				" Invalid data  \t ::"+ fileRowData.substring(142, 144) + "\t " + lineNo);
    }
    
    // ETC_EXIT_SPEED CHAR(3) 
    //Values: 000 – 999
    if (!fileRowData.substring(144, 147).matches("\\d{3}")) {
        addErrorMsg(DETAIL_RECORD_TYPE, "ETC_EXIT_SPEED",
				" Invalid data  \t ::"+ fileRowData.substring(144, 147) + "\t " + lineNo);
    }

    // ETC_OVER_SPEED CHAR(1)  //Values: Y – Speed is over threshold //N – Speed is not over threshold
    if (!fileRowData.substring(147, 148).matches("[YN]")) {
        addErrorMsg(DETAIL_RECORD_TYPE, "ETC_OVER_SPEED",
				" Should be [ Y or N]. Invalid data  \t ::"+ fileRowData.substring(147, 148) + "\t " + lineNo);
    }
    
    // ETC_EXIT_DATE_TIME CHAR(25) //Format: YYYY-MM-DDThh:mm:ss±HH:MM
    //need to check valid date
    if (!fileRowData.substring(148, 173).matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}[-+]\\d{2}:\\d{2}")) {
        addErrorMsg(DETAIL_RECORD_TYPE, "ETC_EXIT_DATE_TIME",
				" Format should be : YYYY-MM-DDThh:mm:ss±HH:MM . \t Invalid data  \t ::"+ fileRowData.substring(148, 173) + "\t " + lineNo);
    }
    
    // ETC_EXIT_PLAZA CHAR(15) 
    if (!commonUtil.isValidEtcPlaza(fileRowData.substring(173, 188),"exit")) {
    	addErrorMsg(DETAIL_RECORD_TYPE, "ETC_EXIT_PLAZA",
				" Invalid Format  \t ::"+ fileRowData.substring(173, 188) + "\t " + lineNo);
    }
    
    // ETC_EXIT_LANE CHAR(3)

    if (!commonUtil.isValidEtcLane(fileRowData.substring(188, 191),"exit")) {
    	addErrorMsg(DETAIL_RECORD_TYPE, "ETC_EXIT_LANE",
				" Invalid Format  \t ::"+ fileRowData.substring(188, 191) + "\t " + lineNo);
    }
    
    // ETC_DEBIT_CREDIT /CHAR(1) 
    //Space ( ) – Debit from customer account
    //Minus (-) – Credit to customer account
    if (!fileRowData.substring(191, 192).matches(IAGConstants.ETC_DEBIT_CREDIT_FORMAT)) {
    	addErrorMsg(DETAIL_RECORD_TYPE, "ETC_DEBIT_CREDIT",
				"Format should be [+,- or space] Invalid Format  \t ::"+ fileRowData.substring(191, 192) + "\t " + lineNo);
    }

    // ETC_TOLL_AMOUNT
    //CHAR(9)
    //The toll due (in US cents) as calculated by the Away Agency.
    //Values: 000000000 ($0000000.00) – 000499999 ($0004999.99) 
    if (!fileRowData.substring(192, 201).matches(IAGConstants.ETC_TOLL_AMOUNT_FORMAT)) {
        addErrorMsg(DETAIL_RECORD_TYPE, "ETC_TOLL_AMOUNT",
				" Values: 000000000 ($0000000.00) – 000499999 ($0004999.99). \t  Invalid Format  \t ::"+ fileRowData.substring(192, 201) + "\t " + lineNo);
    }
    
    
	return true;
}

private boolean validateIctxHeader(String fileRowData, FileValidationParam validateParam, String fileName) {
	
	// Header Total 61
    if (fileRowData == null || fileRowData.length() != 60 || fileRowData.isEmpty()) {
    	controller.getErrorMsglist().add(new ErrorMsgDetail(HEADER_RECORD_TYPE,"Header Length","Invalid header lenght \t Header Row::"+fileRowData));
    }
    
    // FILE_TYPE
    if (!fileRowData.substring(0, 4).equals(IAGConstants.ICTX_FILE_TYPE)) {
        controller.getErrorMsglist().add(new ErrorMsgDetail(HEADER_RECORD_TYPE,"FILE_TYPE","File Type should be ICTX ::\t "+fileRowData.substring(0, 4)+" \t :: Header Row::\t "+fileRowData));
    }
    //IAG Version
    if (!fileRowData.substring(4, 12).matches(IAGConstants.IAG_HEADER_VERSION_FORMAT) || 
    		!fileRowData.substring(4, 12).equals(ValidationController.cscIdTagAgencyMap.get(fileRowData.substring(12, 16)).getVersionNumber())) {
    	addErrorMsg(HEADER_RECORD_TYPE,"VERSION","IAG Version not matched ::\t "+fileRowData.substring(0, 4)+" \t :: Header Row::\t "+fileRowData);
    }
	
    // FROM_AGENCY_ID  //CHAR(4)
	if (!fileRowData.substring(12, 16).matches(IAGConstants.AGENCY_ID_FORMAT)
			|| !AgencyDataExcelReader.agencyCode.contains(fileRowData.substring(12, 16))) {
		addErrorMsg(HEADER_RECORD_TYPE,"FROM_AGENCY_ID","From Agency ID not match with configuration. Please check Agency list \t ::"+fileRowData.substring(12, 16));
		
	}

    // TO_AGENCY_ID //CHAR(4)

	if (!fileRowData.substring(16, 20).matches(IAGConstants.AGENCY_ID_FORMAT)
			|| !AgencyDataExcelReader.agencyCode.contains(fileRowData.substring(16, 20))) {
		addErrorMsg(HEADER_RECORD_TYPE,"TO_AGENCY_ID","To Agency ID not match with configuration. Please check Agency list \t ::"+fileRowData.substring(16, 20));
		
	}
	
	// FILE_DATE_TIME CHAR(20) Format: YYYY-MM-DDThh:mm:ssZ
	String headerfileDateandTime = fileRowData.substring(20, 40);
    if (!headerfileDateandTime.matches(IAGConstants.FILE_DATE_TIME_FORMAT)) {
        
        addErrorMsg(HEADER_RECORD_TYPE,"FILE_DATE_TIME"," date and time format is invalid. Format should be YYYY-MM-DDThh:mm:ssZ  \t ::"+headerfileDateandTime);
    }else {
    	//Check if the date and time are valid
    	if (!commonUtil.isValidDateTimeInDetail(headerfileDateandTime)) {
    		addErrorMsg(HEADER_RECORD_TYPE,"FILE_DATE_TIME"," Invalid date and time. Please check(YYYY-MM-DDThh:mm:ssZ)   \t ::"+headerfileDateandTime);
        }
    }
    
	// RECORD_COUNT CHAR(8) Values: 00000000 – 99999999
	if (!fileRowData.substring(40, 48).matches(IAGConstants.TRAN_RECORD_COUNT_FORMAT)) {
		addErrorMsg(HEADER_RECORD_TYPE,"RECORD_COUNT"," Invalid record count format. Values: 00000000 – 99999999    \t ::"+fileRowData.substring(40, 48));
	}

	// ICTX_FILE_NUM CHAR(12) Values 000000000001 – 999999999999.

	if (!fileRowData.substring(48, 60).matches(IAGConstants.ICTX_FILE_NUM_FORMAT)) {
		addErrorMsg(HEADER_RECORD_TYPE,"ICTX_FILE_NUM"," Invalid ICTX file number format  \t ::"+fileRowData.substring(48, 60));
	}

	return true;
}

private boolean validateZIPFileName(String zipFileName) {
	
	if (zipFileName != null && zipFileName.length() == 33) {
		String[] fileParams = zipFileName.split("[_.]");
		System.out.println("zipFileName ::"+Arrays.toString(fileParams));
		if ((fileParams.length == 5 && fileParams[3].equals(IAGConstants.ICTX_FILE_TYPE)
				&& "zip".equalsIgnoreCase(fileParams[4])) && AgencyDataExcelReader.agencyCode.contains(fileParams[0])
						&& AgencyDataExcelReader.agencyCode.contains(fileParams[1])
				) {

			SimpleDateFormat dateFormat = new SimpleDateFormat(IAGConstants.YYYY_MM_DD_HH_MM_SS);
			dateFormat.setLenient(false);
			try {
				dateFormat.parse(fileParams[2].trim());
				return true;
			} catch (ParseException pe) {
				controller.getErrorMsglist().add(new ErrorMsgDetail(FILE_RECORD_TYPE,"File","Zip file Name Date and time invalid :: YYYYMMDDHHMMSS \t ::"+zipFileName));
				return false;
				
			}

		}
		controller.getErrorMsglist().add(new ErrorMsgDetail(FILE_RECORD_TYPE,"File","Zip file Name Validation Failed :: {FROM_AGENCY_ID}_{TO_AGENCY_ID}_YYYYMMDDHHMMSS.ICTX \t ::"+zipFileName));
	}else {
	controller.getErrorMsglist().add(new ErrorMsgDetail(FILE_RECORD_TYPE,"File","Zip file Name invalid length :: file lenght should be 33 \t ::"+zipFileName));
	}
		return false;
}

private void addErrorMsg(String fileType,String fieldName,String errorMsg) {
	controller.getErrorMsglist().add(new ErrorMsgDetail(fileType,fieldName,errorMsg));
}
}
