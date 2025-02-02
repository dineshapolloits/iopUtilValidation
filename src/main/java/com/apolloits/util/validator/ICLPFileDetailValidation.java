package com.apolloits.util.validator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
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
import static com.apolloits.util.IAGConstants.DETAIL_RECORD_TYPE;
import static com.apolloits.util.IAGConstants.FILE_RECORD_TYPE;

@Slf4j
@Component
public class ICLPFileDetailValidation {

	@Autowired
	private IagAckFileMapper iagAckMapper;
	
	@Autowired
	private AgencyDataExcelReader agDataExcel;
	
	@Autowired
	@Lazy
	ValidationController controller;
	
	@Autowired
	CommonUtil commonUtil;
	
	int invalidRecordCount = 0;
	
	public boolean iclpValidation(FileValidationParam validateParam) throws IOException {
		invalidRecordCount = 0;
		File inputItagZipFile = new File(validateParam.getInputFilePath());
		 String ackFileName = null;
		 if (!inputItagZipFile.exists()) {
			 log.error("ZIP file not found");
			 controller.getErrorMsglist().add(new ErrorMsgDetail(FILE_RECORD_TYPE,"File","ZIP file not found"));
			 //validateParam.setResponseMsg("FAILED Reason::  ZIP file not found");
			 return false;
        }else {
        	if(!validateParam.getFromAgency().equals(inputItagZipFile.getName().substring(0,4))) {
       		 log.error("From Agency code not match with file Name");
       		 controller.getErrorMsglist().add(new ErrorMsgDetail(FILE_RECORD_TYPE,"From Agency","From Agency code "+validateParam.getFromAgency()+" not match with file Name ::"+inputItagZipFile.getName()));
       		 return false;
       	 }
        	// validate ZIP file name 
       	 if(commonUtil.validateZIPFileName(inputItagZipFile.getName(),validateParam)) {
       		 String fileName="";
       		ZipFile zipFile = new ZipFile(inputItagZipFile);
   		 try {
				log.info("extract file name getFileHeaders ******************* "+zipFile.getFileHeaders().get(0).getFileName());
				log.info("inputItagZipFile.getAbsolutePath() :: "+inputItagZipFile.getAbsolutePath());
				zipFile.extractAll(FilenameUtils.getFullPath(inputItagZipFile.getAbsolutePath()));
				zipFile.close();
				fileName =zipFile.getFileHeaders().get(0).getFileName();
				ackFileName = validateParam.getToAgency() + "_" + fileName.replace(".", "_") + IAGConstants.ACK_FILE_EXTENSION;
			} catch (ZipException e) {
				e.printStackTrace();
				controller.getErrorMsglist().add(new ErrorMsgDetail(FILE_RECORD_TYPE,"File","ZIP file extraction failed"));
				log.error("ZIP file extraction failed");
				//validateParam.setResponseMsg("FAILED Reason:: ZIP file extraction failed");
       		 return false;
			}
   		 //validate extract ICLP file name 
   		 if(commonUtil.validateFileName(fileName)) {
   			 
   			if(validateParam.getValidateType().equals("filename")) {
   				log.info("File name validation is sucess");
				 //validateParam.setResponseMsg("File name validation is sucess");
				 return true;
			 }
			 //Start to validate file header and detail  record
			 long noOfRecords = 0;
			try (BufferedReader br = new BufferedReader(
					new FileReader(zipFile.getFile().getParent()+File.separator+fileName))) {

				String fileRowData;
				long headerCount =0l;
				while ((fileRowData = br.readLine()) != null) {
					log.info(noOfRecords + " :: " + fileRowData);
					if(noOfRecords == 0) {
						// Validate Header record
						
						if(!validateIclpHeader(fileRowData,validateParam,fileName)) {
							//create ACK file 
							 //String ackFileName = IAGConstants.SRTA_HOME_AGENCY_ID + "_" + fileName.replace(".", "_") + IAGConstants.ACK_FILE_EXTENSION;
							iagAckMapper.mapToIagAckFile(fileName, "01", validateParam.getOutputFilePath()+File.separator+ackFileName, fileName.substring(0, 4),validateParam.getToAgency());
							//return false;
						}
						if(validateParam.getValidateType().equals("header")) {
				        	 log.info("Only file name and header validation");
				        	 return true;
				         }
						try {
							headerCount = Long.parseLong(fileRowData.substring(36,46));
						}catch (Exception e) {
							log.error("Invalid Header Count format  ::"+fileRowData);
							headerCount =0;
							controller.getErrorMsglist().add(new ErrorMsgDetail(HEADER_RECORD_TYPE,"RECORD_COUNT","Invalid record count format ::"+fileRowData));
							e.printStackTrace();
							return false;
							// TODO: handle exception
						}
						
					}else {
						if(!validateIclpDetail(fileRowData,validateParam,fileName,noOfRecords)) {
							iagAckMapper.mapToIagAckFile(fileName, "02", validateParam.getOutputFilePath()+File.separator+ackFileName, fileName.substring(0, 4),validateParam.getToAgency());
							return false;
						}
					}
					noOfRecords++;
				}
				
				//calling Delimiter validation for detail 
				if (headerCount > 0 && controller.getErrorMsglist().size() == 0) {

					if (!commonUtil.validateDelimiter(zipFile.getFile().getParent() + File.separator + fileName,
							validateParam, fileName)) {
						invalidRecordCount++;
					}
				}
				
				if(controller.getErrorMsglist().size()>0 && invalidRecordCount>0 ) {
					validateParam.setResponseMsg(" \t <b>ACK file name ::</b> \t"+ackFileName +"\t <b> Invalid record detail count ::</b> \t "+invalidRecordCount);
					iagAckMapper.mapToIagAckFile(fileName, "02", validateParam.getOutputFilePath()+File.separator+ackFileName, fileName.substring(0, 4),validateParam.getToAgency());
				}else if(controller.getErrorMsglist().size()== 0 && invalidRecordCount == 0 ) {
					log.info("Sucess ACK created");
					iagAckMapper.mapToIagAckFile(fileName, "00", validateParam.getOutputFilePath()+File.separator+ackFileName, fileName.substring(0, 4),validateParam.getToAgency());
				}
				
				if((noOfRecords-1) != headerCount ) {
					log.error("FAILED Reason :: Header count("+headerCount+") and detail count not matching ::"+(noOfRecords-1));
					controller.getErrorMsglist().add(new ErrorMsgDetail(DETAIL_RECORD_TYPE,"File","Header count("+headerCount+") and detail count not matching ::"+(noOfRecords-1)));
					//validateParam.setResponseMsg("FAILED Reason :: Header count("+headerCount+") and detail count not matching ::"+noOfRecords);
					iagAckMapper.mapToIagAckFile(fileName, "01", validateParam.getOutputFilePath()+File.separator+ackFileName, fileName.substring(0, 4),validateParam.getToAgency());
					return false;
				}
			} catch (FileNotFoundException e) {
				log.error("FileNotFoundException :: Error while reading a file." + e.getMessage());
				validateParam.setResponseMsg("\t \t File not Found. Please check file path");
				e.printStackTrace();
				return false;
			} catch (IOException e) {
				e.printStackTrace();
				// Display pop up message if exceptionn occurs
				log.error("Error while reading a file.");
				return false;
			}
	            
			 
		 }else {
			 iagAckMapper.mapToIagAckFile(fileName, "07", validateParam.getOutputFilePath()+File.separator+ackFileName, fileName.substring(0, 4),validateParam.getToAgency());
			 controller.getErrorMsglist().add(new ErrorMsgDetail(FILE_RECORD_TYPE,"File","Inside ZIP ICLP file Name validation is failed - File name :: " +fileName));
			 log.error("FAILED Reason:: Inside ZIP ICLP file Name validation is failed");
			 // validateParam.setResponseMsg("FAILED Reason:: Inside ZIP ICLP file Name validation is failed");
    		 return false;
       	 }
        }else {
        	//validateParam.setResponseMsg("\t Invalid ZIP file format");
        	log.info("FAILED Reason:: ICLP Invalid ZIP file name ::\"+inputItagZipFile.getName()");
        	log.error("ZIP File Name","ZIP file Name validation is failed");
   		 	//controller.getErrorMsglist().add(new ErrorMsgDetail(FILE_RECORD_TYPE,"ZIP File Name","ZIP file Name validation is failed"));
   		 	//ackFileName = validateParam.getToAgency() + "_" + inputItagZipFile.getName().replace(".", "_") + IAGConstants.ACK_FILE_EXTENSION;
   		 	//iagAckMapper.mapToIagAckFile(inputItagZipFile.getName(), "07", validateParam.getOutputFilePath()+File.separator+ackFileName, inputItagZipFile.getName().substring(0, 4),validateParam.getToAgency());
   		 	//validateParam.setResponseMsg("FAILED Reason:: ICLP Invalid ZIP file name ::"+inputItagZipFile.getName());
   		 return false;
        }
       	 return true;
		
        }
	}
	
	public boolean validateIclpHeader(String headervalue,FileValidationParam validateParam,String fileName) {
		log.info("ICLP headervalue :: "+headervalue);
		boolean invalidHeaderRecord = false;
		 String fileDate = fileName.substring(5, 13);
         String fileTime = fileName.substring(13, 19); //filename value
         String fromAgencyId = fileName.substring(0, 4);
         
         String headerFileType = "";
         String headerVersion = "";
         String headerFromAgencyId = "";
         String headerDate = "";
         String headerTime = "";
         try {
             headerFileType = headervalue.substring(0, 4);
             headerVersion = headervalue.substring(4, 12);
             headerFromAgencyId = headervalue.substring(12, 16);
             headerDate = headervalue.substring(16, 26);
             headerTime = headervalue.substring(27, 35);
         } catch (Exception e) {
        	 e.printStackTrace();
        	// validateParam.setResponseMsg("Header record for ICLP file is invalid - " + headervalue);
        	 controller.getErrorMsglist().add(new ErrorMsgDetail(HEADER_RECORD_TYPE,"Header","Header record for ICLP file is invalid - " + headervalue));
        	 log.error("Header record for ICLP file is invalid - " + headervalue);
        	 return false;
         }
         if (headervalue == null || headervalue.length() != 46 || headervalue.isEmpty()) {
          	controller.getErrorMsglist().add(new ErrorMsgDetail(HEADER_RECORD_TYPE,"Header Length","Invalid header length \t Header Row::"+headervalue));
          	return false;
          }
         if(!headerFileType.equals(IAGConstants.ICLP_FILE_TYPE)) {
        	 controller.getErrorMsglist().add(new ErrorMsgDetail(HEADER_RECORD_TYPE,"File Type","invalid FileType " + headerFileType));
        	 log.error("Header record file type is invalid - " + headerFileType);
        	 //validateParam.setResponseMsg("Header record file type is invalid - " + headerFileType);
        	 invalidHeaderRecord = true;
         }
         
         final Pattern pattern = Pattern.compile(IAGConstants.IAG_HEADER_VERSION_FORMAT);
         if (!pattern.matcher(headerVersion).matches() 
        		  || ValidationController.cscIdTagAgencyMap.get(fromAgencyId) == null
        				  || !headerVersion.equals(ValidationController.cscIdTagAgencyMap.get(fromAgencyId).getVersionNumber())) {
        	 log.error("FAILED Reason:: Invalid header, version format is incorrect - " + headerVersion + "\t excepted version ::"+ValidationController.cscIdTagAgencyMap.get(fromAgencyId).getVersionNumber());
        	 controller.getErrorMsglist().add(new ErrorMsgDetail(HEADER_RECORD_TYPE,"IAG Version","Invalid version " + headerVersion));
        	 //validateParam.setResponseMsg("Invalid header, version format is incorrect - " + headerVersion + "\t excepted version ::"+ValidationController.cscIdTagAgencyMap.get(fromAgencyId).getVersionNumber());
        	 invalidHeaderRecord = true;
         }
         //System.out.println("AgencyDataExcelReader.agencyCode.contains(fromAgencyId) :: " +AgencyDataExcelReader.agencyCode.contains(fromAgencyId));
         if(!headerFromAgencyId.equals(fromAgencyId) || !AgencyDataExcelReader.agencyCode.contains(fromAgencyId)) {
        	 //validateParam.setResponseMsg("Invalid header agency ID - " + fromAgencyId);
        	 controller.getErrorMsglist().add(new ErrorMsgDetail(HEADER_RECORD_TYPE,"From Agency","Invalid header agency ID - " + headerFromAgencyId));
        	 log.error("Invalid header agency ID - " + fromAgencyId);
        	 invalidHeaderRecord = true;
         }

         if (!fileDate.equals(headerDate.replace("-", "")) || !fileTime.equals(headerTime.replace(":", ""))) {
        	 controller.getErrorMsglist().add(new ErrorMsgDetail(HEADER_RECORD_TYPE,"FileDateTime","File DateTime - " + headerDate +"\t headerTime - "+headerTime));
        	 log.error("File DateTime - " + headerDate +"\t headerTime - "+headerTime);
        	 invalidHeaderRecord = true;
         }
         if (!headervalue.substring(16, 36).matches(IAGConstants.FILE_DATE_TIME_FORMAT)) {
             
        	 controller.getErrorMsglist().add(new ErrorMsgDetail(HEADER_RECORD_TYPE,"FILE_DATE_TIME"," date and time format is invalid. Format should be YYYY-MM-DDThh:mm:ssZ  \t ::"+headervalue.substring(16, 36)));
             invalidHeaderRecord = true;
         }
         
         if(invalidHeaderRecord) {
        	 return false;
         }
         if(validateParam.getValidateType().equals("header")) {
        	 log.info("Only file name and header validation");
        	 return true;
         }
         
		return true;
	}
	public boolean validateIclpDetail(String fileRowData, FileValidationParam validateParam, String fileName,long rowNo) {
		boolean invalidRecord = false;
		String licState="";
		String licNumber="";
		String licType="";
		String tagAgencyid="";
		String tagSerialNo="";
		String licEffectiveFrom="";
		String licEffectiveTo="";
		String licHomeAgency ="";
		String licAccountNo="";
		String LicVin="";
		String licGuaranteed ="";
		String licRegDate="";
		String licUpdateDate = "";
		String lineNo = "\t <b>Line No::</b> \t "+rowNo;
		if(fileRowData == null ||  fileRowData.length() != 208) {
			log.error("Detail record invalid length ::"+fileRowData);
			controller.getErrorMsglist().add(new ErrorMsgDetail(DETAIL_RECORD_TYPE,"Invalid Length","Detail record invalid length ::"+fileRowData +lineNo));
			validateParam.setResponseMsg("Detail record invalid length ::"+fileRowData);
			invalidRecord = true;
			return false;
		}
		//System.lineSeparator()
		if(fileRowData.contains(System.lineSeparator())) {
			System.out.println("@@@@@@@@@@@@@@ true");
		}else {
			System.out.println("$$$$$$$$$$$$$$$$ false");
		}
		try {
			licState = fileRowData.substring(0, 2);
			licNumber = fileRowData.substring(2, 12);;
			licType = fileRowData.substring(12, 42);
			tagAgencyid = fileRowData.substring(42, 46);
			tagSerialNo = fileRowData.substring(46, 56);
			licEffectiveFrom = fileRowData.substring(56, 76);
			licEffectiveTo = fileRowData.substring(76, 96);
			licHomeAgency = fileRowData.substring(96, 100);
			licAccountNo = fileRowData.substring(100, 150);
			LicVin = fileRowData.substring(150, 167);
			licGuaranteed = fileRowData.substring(167, 168);
			licRegDate = fileRowData.substring(168, 188);
			licUpdateDate = fileRowData.substring(188, 208);
			System.out.println("agDataExcel.getPlateStateSet() ::"+agDataExcel.getPlateStateSet());
			System.out.println("agDataExcel.getPlateStateSet().contains(licState) ::"+agDataExcel.getPlateStateSet().contains(licState));
			if (!licState.matches("[A-Z]{2}") ) {
				log.info("Invalid ICLP detail, invalid state Format- "+licState +" Row ::"+fileRowData + lineNo);
				//validateParam.setResponseMsg("Invalid ICLP detail, invalid Lic_state Format - "+licState +" Row ::"+fileRowData);
				controller.getErrorMsglist().add(new ErrorMsgDetail(DETAIL_RECORD_TYPE,"Lic State","Invalid Lic_state Format - "+licState +" Row ::"+fileRowData+ lineNo));
				invalidRecord = true;
			}
			if (!agDataExcel.getPlateStateSet().contains(licState)) {
				log.info("Invalid ICLP detail,Please check your State configuration - invalid state - "+licState +" Row ::"+fileRowData +lineNo);
				//validateParam.setResponseMsg("Invalid ICLP detail,Please check your State configuration - invalid state - "+licState +" Row ::"+fileRowData +lineNo);
				controller.getErrorMsglist().add(new ErrorMsgDetail(DETAIL_RECORD_TYPE,"Lic State","Invalid Lic_state  - "+licState +" Row ::"+fileRowData+ lineNo));
				invalidRecord = true;;
			}
			if(!licNumber.matches("^[A-Z \\d-.&]{10}$")) {
				log.info("Invalid ICLP detail, invalid Lic_Number Format - "+licNumber +" Row ::"+fileRowData);
				controller.getErrorMsglist().add(new ErrorMsgDetail(DETAIL_RECORD_TYPE,"Lic Number","Invalid Lic_Number Format - "+licNumber +" Row ::"+fileRowData+ lineNo));
				//validateParam.setResponseMsg("Invalid ICLP detail, invalid Lic_Number Format - "+licNumber +" Row ::"+fileRowData);
				invalidRecord = true;
			}
			
			if (!licType.matches("[A-Z \\d*]{30}")) {
				log.info("Invalid ICLP detail, invalid Lic_Type Format - "+licNumber +" Row ::"+fileRowData);
				//validateParam.setResponseMsg("Invalid ICLP detail, invalid Lic_Type Format - "+licNumber +" Row ::"+fileRowData);
				controller.getErrorMsglist().add(new ErrorMsgDetail(DETAIL_RECORD_TYPE,"Lic Type","Invalid Lic_Type Format - "+licNumber +" Row ::"+fileRowData+ lineNo));
				invalidRecord = true;
			}
			
			String licStateType =(licState.trim())+(licType.trim());
			//System.out.println("licStateType :::"+licStateType);
			log.info("IsPlateType :: "+ agDataExcel.getPlateStateTypeSet().contains(licStateType));
			//System.out.println("LIC_State and type ::"+agDataExcel.getPlateStateTypeSet().toString()); 
			if(!licType.matches("[\\*]{30}") &&
					!agDataExcel.getPlateStateTypeSet().contains(licStateType)) {
				log.error("Invalid ICLP detail,Please check your State and plateType Configuration - invalid Lic_Type - "+licType +" Row ::"+fileRowData+ lineNo);
				//validateParam.setResponseMsg("Invalid ICLP detail,Please check your State and plateType Configuration - invalid Lic_Type - "+licType +" Row ::"+fileRowData+ lineNo);
				controller.getErrorMsglist().add(new ErrorMsgDetail(DETAIL_RECORD_TYPE,"LicStateType","Please check your State and plateType Configuration - invalid Lic_Type - "+licType +" Row ::"+fileRowData+ lineNo));
				invalidRecord = true;
			}
			//ValidationController.cscIdTagAgencyMap.forEach((tag, agency) -> 
			//System.out.println("validateIclpDetail :: TagAgencyID: " + tag + ", Tag Start: " + agency.getTagSequenceStart() +"\t END ::"+agency.getTagSequenceEnd()));
			
			if (!tagAgencyid.matches("\\d{4}") 
					|| ValidationController.cscIdTagAgencyMap.get(tagAgencyid) == null) {
				log.info("Invalid ICLP detail, invalid TAG_AGENCY_ID - "+tagAgencyid +" Row ::"+fileRowData+ lineNo);
				//validateParam.setResponseMsg("Invalid ICLP detail, invalid TAG_AGENCY_ID - "+tagAgencyid +" Row ::"+fileRowData);
				controller.getErrorMsglist().add(new ErrorMsgDetail(DETAIL_RECORD_TYPE,"Tag Agency ID","Invalid TAG_AGENCY_ID - "+tagAgencyid +" Row ::"+fileRowData+ lineNo));
				invalidRecord = true;
			}
			
			if (!tagSerialNo.matches("\\d{10}") ) { //we can move this if condition to below if
				log.info("Invalid ICLP detail, invalid TAG_SERIAL_NUMBER Format - "+tagSerialNo +" Row ::"+fileRowData+lineNo);
				//validateParam.setResponseMsg("Invalid ICLP detail, invalid TAG_SERIAL_NUMBER Format - "+tagSerialNo +" Row ::"+fileRowData);
				controller.getErrorMsglist().add(new ErrorMsgDetail(DETAIL_RECORD_TYPE,"Tag Serial No","Invalid TAG_SERIAL_NUMBER Format - "+tagSerialNo +" Row ::"+fileRowData+ lineNo));
				invalidRecord = true;
			}
			
		/*	try {
				AgencyEntity agEntity = ValidationController.cscIdTagAgencyMap.get(tagAgencyid);
				long tagSerialNoStart = Long.valueOf(agEntity.getTagSequenceStart());
				long tagSerialNoEnd = Long.valueOf(agEntity.getTagSequenceEnd());
				long tagSerialNoLong = Long.valueOf(tagSerialNo);
				log.info("## tagSerialNoLong ::" + tagSerialNoLong + "\t tagSerialNoStart :: "
						+ tagSerialNoStart + "\t tagSerialNoEnd ::" + tagSerialNoEnd);
				if (!(tagSerialNoStart < tagSerialNoEnd && tagSerialNoStart <= tagSerialNoLong
						&& tagSerialNoEnd >= tagSerialNoLong)) {
					validateParam
							.setResponseMsg("Invalid TAG_SERIAL_NUMBER range   - " + fileRowData);
					return false;
				}
			} catch (Exception e) {
				e.printStackTrace();
				validateParam.setResponseMsg("Invalid TAG_SERIAL_NUMBER format from excel or file  - " + fileRowData);
				return false;
			}*/
			if(!isValidLicEffective(licEffectiveFrom)) {
				log.error("Invalid LIC_EFFECTIVE_FROM format   - " + licEffectiveFrom+" ROW ::" + fileRowData + lineNo);
				controller.getErrorMsglist().add(new ErrorMsgDetail(DETAIL_RECORD_TYPE,"Lic Effective From","Invalid LIC_EFFECTIVE_FROM format   - " + licEffectiveFrom+" ROW ::" + fileRowData + lineNo));
				//validateParam.setResponseMsg("Invalid TAG_SERIAL_NUMBER   - " + licEffectiveFrom+" ROW ::" + fileRowData);
				invalidRecord = true;
			}
			if(!isValidLicEffective(licEffectiveTo)) {
				log.error("Invalid LIC_EFFECTIVE_TO format from excel or file  - " + licEffectiveTo+" ROW ::" + fileRowData+lineNo);
				controller.getErrorMsglist().add(new ErrorMsgDetail(DETAIL_RECORD_TYPE,"Lic Effective To","Invalid LIC_EFFECTIVE_To format   - " + licEffectiveTo+" ROW ::" + fileRowData + lineNo));
				//validateParam.setResponseMsg("Invalid LIC_EFFECTIVE_TO format  - " + licEffectiveTo+" ROW ::" + fileRowData);
				invalidRecord = true;
			}
			if (!licHomeAgency.matches("\\d{4}") || !AgencyDataExcelReader.agencyCode.contains(licHomeAgency)) {
				log.error("Invalid LIC_HOME_AGENCY   - " + licHomeAgency+" ROW ::" + fileRowData+lineNo);
				controller.getErrorMsglist().add(new ErrorMsgDetail(DETAIL_RECORD_TYPE,"Lic Home Agency ID","Invalid LIC_HOME_AGENCY   - " + licHomeAgency+" ROW ::" + fileRowData+lineNo));
				//validateParam.setResponseMsg("Invalid LIC_HOME_AGENCY  - " + licHomeAgency+" ROW ::" + fileRowData);
				invalidRecord = true;
			}
			if (!licAccountNo.matches("[A-Z \\d*]{50}")) {
				log.error("Invalid LIC_ACCOUNT_NO   - " + licAccountNo+" ROW ::" + fileRowData+lineNo);
				controller.getErrorMsglist().add(new ErrorMsgDetail(DETAIL_RECORD_TYPE,"LIC_ACCOUNT_NO","Invalid LIC_ACCOUNT_NO   - " + licAccountNo+" ROW ::" + fileRowData+lineNo));
				//validateParam.setResponseMsg("Invalid LIC_HOME_AGENCY  - " + licAccountNo+" ROW ::" + fileRowData);
				invalidRecord = true;
			}
			if (!LicVin.matches("[A-Z \\d*]{17}")) {
				log.error("Invalid LIC_VIN   - " + LicVin+" ROW ::" + fileRowData+lineNo);
				controller.getErrorMsglist().add(new ErrorMsgDetail(DETAIL_RECORD_TYPE,"LIC_VIN","Invalid LIC_VIN   - " + LicVin+" ROW ::" + fileRowData+lineNo));
				//validateParam.setResponseMsg("Invalid LIC_VIN  - " + LicVin+" ROW ::" + fileRowData);
				invalidRecord = true;
			}
			if (!licGuaranteed.matches("[YN*]")) {
				log.error("Invalid LIC_GUARANTEED   - " + licGuaranteed+" ROW ::" + fileRowData+lineNo);
				controller.getErrorMsglist().add(new ErrorMsgDetail(DETAIL_RECORD_TYPE,"LIC_GUARANTEED","Invalid LIC_GUARANTEED   - " + licGuaranteed+" ROW ::" + fileRowData+lineNo));
				//validateParam.setResponseMsg("Invalid LIC_GUARANTEED  - " + licGuaranteed+" ROW ::" + fileRowData);
				invalidRecord = true;
			}
			if (!licRegDate.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z") && !licRegDate.matches("\\*{20}")) {
				log.error("Invalid LIC_REGISTRATION_DATE   - " +licRegDate +"\t Row ::" + fileRowData+lineNo);
				controller.getErrorMsglist().add(new ErrorMsgDetail(DETAIL_RECORD_TYPE,"LIC_REGISTRATION_DATE","Invalid LIC_REGISTRATION_DATE   - " +licRegDate +"\t Row ::" + fileRowData+lineNo));
				//validateParam.setResponseMsg("Invalid LIC_REGISTRATION_DATE  - "+licRegDate+" ROW ::" + fileRowData);
				invalidRecord = true;
			}
			if (!licUpdateDate.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z") && !licUpdateDate.matches("\\*{20}")) {
				log.error("Invalid LIC_UPDATE_DATE   - " +licUpdateDate +"\t Row ::"+ fileRowData+lineNo);
				controller.getErrorMsglist().add(new ErrorMsgDetail(DETAIL_RECORD_TYPE,"LIC_UPDATE_DATE","Invalid LIC_UPDATE_DATE   - " +licUpdateDate +"\t Row ::"+ fileRowData+lineNo));
				//validateParam.setResponseMsg("Invalid LIC_UPDATE_DATE  - "+licUpdateDate+" ROW ::" + fileRowData);
				invalidRecord = true;
			}
	            if(invalidRecord) {
	            	invalidRecordCount++;
	            }
			
		}catch (Exception e) {
			e.printStackTrace();
			//validateParam.setResponseMsg("Invalid Row detail  - "+fileRowData);
			controller.getErrorMsglist().add(new ErrorMsgDetail(DETAIL_RECORD_TYPE,"Invalid Detail","Invalid Row detail  - "+fileRowData+lineNo));
        	return false;
		}
		 
	return true;
	}
	public static void main(String ar[]) {
		String licState="";
		String licNumber="";
		String licType="";
		String tagAgencyid="";
		String tagSerialNo="";
		String licEffectiveFrom="";
		String licEffectiveTo="";
		String licHomeAgency ="";
		String licAccountNo="";
		String LicVin="";
		String licGuaranteed ="";
		String licRegDate="";
		String licUpdateDate = "";
		
		String fileRowData = "GARMJL16    ******************************000800000000062024-08-21T18:10:03Z********************000800000000000000000000000000000000000000000900000016*****************Y2023-04-17T21:27:24Z2023-04-17T21:27:24Z";
		System.out.println("fileRowData ::"+fileRowData.length());
		licState = fileRowData.substring(0, 2);
		System.out.println("licState ::"+licState);
		licNumber = fileRowData.substring(2, 12);;
		System.out.println("licNumber ::"+licNumber);
		licType = fileRowData.substring(12, 42);
		System.out.println("licType ::"+licType);
		tagAgencyid = fileRowData.substring(42, 46);
		System.out.println("tagAgencyid ::"+tagAgencyid);
		tagSerialNo = fileRowData.substring(46, 56);
		System.out.println("tagSerialNo ::"+tagSerialNo);
		licEffectiveFrom = fileRowData.substring(56, 76);
		System.out.println("licEffectiveFrom ::"+licEffectiveFrom);
		licEffectiveTo = fileRowData.substring(76, 96);
		System.out.println("licEffectiveTo ::"+licEffectiveTo);
		licHomeAgency = fileRowData.substring(96, 100);
		System.out.println("licHomeAgency ::"+licHomeAgency);
		licAccountNo = fileRowData.substring(100, 150);
		System.out.println("licAccountNo ::"+licAccountNo);
		LicVin = fileRowData.substring(150, 167);
		System.out.println("LicVin ::"+LicVin);
		licGuaranteed = fileRowData.substring(167, 168);
		System.out.println("licGuaranteed ::"+licGuaranteed);
		licRegDate = fileRowData.substring(168, 188);
		System.out.println("licRegDate ::"+licRegDate);
		licUpdateDate = fileRowData.substring(188, 208);
		System.out.println("licUpdateDate ::"+licUpdateDate);
		licUpdateDate = "********************";
		 
		String licStateType = licState.trim()+licType.trim();
		System.out.println("@@@@@ licStateType ::: "+ licStateType);
		licType = "***************************** ";
		if (licType.matches("[\\*]{30}")) {
			System.out.println("true");
		}else {
			System.out.println("false");
		}
		
		
		/*tagSerialNo = "0014641850";
		long tagSerialNoStart = Long.valueOf("0000030736");;
		long tagSerialNoEnd =Long.valueOf("0014641850");;
		long tagSerialNoLong = Long.valueOf(tagSerialNo);
		System.out.println("## tagSerialNoLong ::"+tagSerialNoLong +"\t tagSerialNoStart :: "+tagSerialNoStart +"\t tagSerialNoEnd ::"+tagSerialNoEnd);
		if(!(tagSerialNoStart<tagSerialNoEnd &&
				tagSerialNoStart <=tagSerialNoLong &&
				tagSerialNoEnd >= tagSerialNoLong)) {
			System.out.println("true");
		}else {
			System.out.println("False");
		} */
		
		 try {
			 DateTime dateTime =  new DateTime().withZone(DateTimeZone.UTC);
			 System.out.println("heasertime ::"+dateTime.toString("yyyy-MM-dd'T'HH:mm:ss'Z'"));
	        } catch (DateTimeParseException e) {
	           e.printStackTrace();
	        }
	}
	
	 private boolean isValidLicEffective(String licEffectiveFrom) {
	        // Check if the field is filled with asterisks
	        if (licEffectiveFrom.equals("********************")) {
	            return true;
	        }

	        // Validate the date/time format
	        try {
	            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
	            LocalDateTime.parse(licEffectiveFrom, formatter);
	            return true;
	        } catch (DateTimeParseException e) {
	            return false;
	        }
	    }
}
