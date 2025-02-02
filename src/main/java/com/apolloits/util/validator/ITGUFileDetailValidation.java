package com.apolloits.util.validator;

import static com.apolloits.util.IAGConstants.DETAIL_RECORD_TYPE;
import static com.apolloits.util.IAGConstants.FILE_RECORD_TYPE;
import static com.apolloits.util.IAGConstants.HEADER_RECORD_TYPE;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Pattern;

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

@Slf4j
@Component
public class ITGUFileDetailValidation {

	@Autowired
	private IagAckFileMapper iagAckMapper;
	@Autowired
	@Lazy
	ValidationController controller;
	
	@Autowired
	CommonUtil commonUtil;
	
	@Autowired
	ITAGFileDetailValidation itagValidation;
	
	
	public boolean itguValidation(FileValidationParam validateParam) throws IOException {
		
		 File inputItagZipFile = new File(validateParam.getInputFilePath());
		 String ackFileName = null;
		 if (!inputItagZipFile.exists()) {
			 controller.getErrorMsglist().add(new ErrorMsgDetail(FILE_RECORD_TYPE,"File","ZIP file not found"));
			 //validateParam.setResponseMsg("FAILED Reason::  ZIP file not found");
			 log.error("FAILED Reason::  ZIP file not found");
			 return false;
        }else {
       	 if(!validateParam.getFromAgency().equals(inputItagZipFile.getName().substring(0,4))) {
       		 log.error("From Agency code not match with file Name");
       		 //validateParam.setResponseMsg("From Agency code "+validateParam.getFromAgency()+" not match with file Name ::"+inputItagZipFile.getName());
       		 controller.getErrorMsglist().add(new ErrorMsgDetail(FILE_RECORD_TYPE,"From Agency","From Agency code "+validateParam.getFromAgency()+" not match with file Name ::"+inputItagZipFile.getName()));
       		 return false;
       	 }
       	 
       	 if(validateParam.getFromAgency().equals(validateParam.getToAgency())) {
       		 log.error("From Agency code and To agency code should not be same");
       		 controller.getErrorMsglist().add(new ErrorMsgDetail(FILE_RECORD_TYPE,"From Agency","From Agency code "+validateParam.getFromAgency()+" and Toagency code should not be same  ::"+validateParam.getToAgency()));
       		 return false;
       	 }
       	 
    	 // validate ZIP file name 
    	 if(commonUtil.validateZIPFileName(inputItagZipFile.getName(),validateParam)) {
    		 String fileName="";
    		 //extract ZIP file 
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
				 validateParam.setResponseMsg("FAILED Reason:: ZIP file extraction failed");
        		 return false;
			}
    		 //validate extract ITAG file name 
    		 if(commonUtil.validateFileName(fileName)) {
    			 if(validateParam.getValidateType().equals("filename")) {
    				 validateParam.setResponseMsg("File name validation is sucess");
    				 return true;
    			 }
    			 //Start to validate file header and detail  record
    			 long noOfRecords = 0;
				try (BufferedReader br = new BufferedReader(
						new FileReader(zipFile.getFile().getParent()+File.separator+fileName))) {
					itagValidation.invalidRecordCount = 0;
					String fileRowData;
					long headerCount =0l;
					while ((fileRowData = br.readLine()) != null) {
						log.info(noOfRecords + " :: " + fileRowData);
						if(noOfRecords == 0) {
							// Validate Header record
							//headerCount = Long.parseLong(fileRowData.substring(36,46));
							if(!validateItguHeader(fileRowData,validateParam,fileName)) {
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
								headerCount = Long.parseLong(fileRowData.substring(56,66));
							}catch (Exception e) {
								log.error("Invalid Header Count format  ::"+fileRowData);
								headerCount =0;
								controller.getErrorMsglist().add(new ErrorMsgDetail(HEADER_RECORD_TYPE,"RECORD_COUNT","Invalid record count format ::"+fileRowData));
								e.printStackTrace();
								return false;
								// TODO: handle exception
							}
						}else {
							if(!itagValidation.validateItagDetail(fileRowData,validateParam,fileName,noOfRecords)) {
								validateParam.setResponseMsg(validateParam.getResponseMsg() +"\t    Line No::"+noOfRecords);
								iagAckMapper.mapToIagAckFile(fileName, "02", validateParam.getOutputFilePath()+File.separator+ackFileName, fileName.substring(0, 4),validateParam.getToAgency());
							}
						}
						noOfRecords++;
					}
					if((noOfRecords-1) != headerCount ) {
						validateParam.setResponseMsg("\t Header count("+headerCount+") and detail count not matching ::"+(noOfRecords-1));
						iagAckMapper.mapToIagAckFile(fileName, "01", validateParam.getOutputFilePath()+File.separator+ackFileName, fileName.substring(0, 4),validateParam.getToAgency());
						return false;
					}
					
					//calling Delimiter validation for detail 
					if (headerCount > 0 && controller.getErrorMsglist().size() == 0) {

						if (!commonUtil.validateDelimiter(zipFile.getFile().getParent() + File.separator + fileName,
								validateParam, fileName)) {
							itagValidation.invalidRecordCount++;
						}
					}
					
					 //validate Duplicate serial no
						validateDuplicateTagSerialNo(zipFile.getFile().getParent()+File.separator+fileName,validateParam);
					if(controller.getErrorMsglist().size()>0 && itagValidation.invalidRecordCount >0) {
						validateParam.setResponseMsg("\t \t <b>ACK file name ::</b> \t "+ackFileName +"\t <b> Invalid detail record count ::</b> \t "+itagValidation.invalidRecordCount);
						iagAckMapper.mapToIagAckFile(fileName, "02", validateParam.getOutputFilePath()+File.separator+ackFileName, fileName.substring(0, 4),validateParam.getToAgency());
					}else if(controller.getErrorMsglist().size()== 0 && itagValidation.invalidRecordCount == 0 ) {
						log.info("Sucess ACK created");
						iagAckMapper.mapToIagAckFile(fileName, "00", validateParam.getOutputFilePath()+File.separator+ackFileName, fileName.substring(0, 4),validateParam.getToAgency());
					}
				} catch (FileNotFoundException e) {
					log.error("FileNotFoundException :: Error while reading a file."+e.getMessage());
					validateParam.setResponseMsg("\t \t File not Found. Please check file path");
					e.printStackTrace();
					return false;
				} catch (IOException e) {
					e.printStackTrace();
					// Display pop up message if exception occurs
					log.error("Error while reading a file.");
					return false;
				}
    			 
    		 }else {
    			 iagAckMapper.mapToIagAckFile(fileName, "07", validateParam.getOutputFilePath()+File.separator+ackFileName, fileName.substring(0, 4),validateParam.getToAgency());
    			 //validateParam.setResponseMsg("FAILED Reason:: ITAG file Name validation is failed");
    			 log.error("ITAG file Name validation is failed");
    			 controller.getErrorMsglist().add(new ErrorMsgDetail(FILE_RECORD_TYPE,"File Name","ITAG file Name validation is failed"));
    			 return false;
    		 }
    		 
    	 }else {
    		 //validateParam.setResponseMsg("FAILED Reason:: ZIP file Name validation is failed");
    		 log.error("ZIP File Name","ZIP file Name validation is failed");
    		// controller.getErrorMsglist().add(new ErrorMsgDetail(FILE_RECORD_TYPE,"ZIP File Name","ZIP file Name validation is failed"));
    		 return false;
    	 }
		 return true;
       	 
       	 
        }
	}
	
	public boolean validateItguHeader(String headervalue,FileValidationParam validateParam,String fileName) {
		log.info("headervalue :: "+headervalue);
		boolean invalidHeaderRecord = false;
		 String fileDate = fileName.substring(5, 13);
         String fileTime = fileName.substring(13, 19); //filename value
         String fromAgencyId = fileName.substring(0, 4);
        // String fileDateTime = fileName.substring(5, fileName.lastIndexOf("."));
         
         String headerFileType = "";
         String headerVersion = "";
         String headerFromAgencyId = "";
         String headerDate = "";
         String headerTime = "";
         String preFileDateTime ="";
         try {
             headerFileType = headervalue.substring(0, 4);
             headerVersion = headervalue.substring(4, 12);
             headerFromAgencyId = headervalue.substring(12, 16);
             headerDate = headervalue.substring(16, 26);
             headerTime = headervalue.substring(27, 35);
             preFileDateTime=headervalue.substring(36,56);
         } catch (Exception e) {
        	log.error("FAILED Reason:: Header record for ITGU file is invalid format or length - " + headervalue);
        	 addErrorMsg(HEADER_RECORD_TYPE,"HeaderDate", "Header record for ITAG file is invalid format or length - " + headervalue);
        	 invalidHeaderRecord = true;
         }
         if (headervalue == null || headervalue.length() != 66 || headervalue.isEmpty()) {
           	controller.getErrorMsglist().add(new ErrorMsgDetail(HEADER_RECORD_TYPE,"Header Length","Invalid header length \t Header Row::"+headervalue));
           	return false;
           }
         if(!headerFileType.equals(IAGConstants.ITGU_FILE_TYPE)) {
        	 log.error("FAILED Reason:: Header record file type is invalid - " + headerFileType);
        	 addErrorMsg(HEADER_RECORD_TYPE,"File Type", "Header record file type is invalid - " + headerFileType);
        	 invalidHeaderRecord = true;
         }
         final Pattern pattern = Pattern.compile(IAGConstants.IAG_HEADER_VERSION_FORMAT);
         if (!pattern.matcher(headerVersion).matches() 
        		 || ValidationController.cscIdTagAgencyMap.get(fromAgencyId) == null
        		 || !headerVersion.equals(ValidationController.cscIdTagAgencyMap.get(fromAgencyId).getVersionNumber())) {
        	 log.error("FAILED Reason:: Invalid header, version format is incorrect - " + headerVersion + "\t excepted version ::"+ValidationController.cscIdTagAgencyMap.get(fromAgencyId).getVersionNumber());
        	 addErrorMsg(HEADER_RECORD_TYPE,"IAG Version", "Version format is incorrect - " + headerVersion + "\t excepted version ::"+ValidationController.cscIdTagAgencyMap.get(fromAgencyId).getVersionNumber());
        	 invalidHeaderRecord = true;
         }
         
         if(!headerFromAgencyId.equals(fromAgencyId) || !AgencyDataExcelReader.agencyCode.contains(fromAgencyId)) {
        	 log.error(" Invalid header agency ID - " + fromAgencyId);
        	 addErrorMsg(HEADER_RECORD_TYPE,"FromAgencyId", "From agency ID and headerAgency Id not matched - " + headerFromAgencyId);
        	// validateParam.setResponseMsg("FAILED Reason:: Invalid header agency ID - " + fromAgencyId);
        	 invalidHeaderRecord = true;
         }
         if (!headervalue.substring(16, 36).matches(IAGConstants.FILE_DATE_TIME_FORMAT)) {
             
        	 controller.getErrorMsglist().add(new ErrorMsgDetail(HEADER_RECORD_TYPE,"FILE_DATE_TIME"," date and time format is invalid. Format should be YYYY-MM-DDThh:mm:ssZ  \t ::"+headervalue.substring(16, 36)));
             invalidHeaderRecord = true;
         }

         if (!fileDate.equals(headerDate.replace("-", "")) || !fileTime.equals(headerTime.replace(":", ""))) {
        	 log.error("Header datetime and file date time not matched");
        	 addErrorMsg(HEADER_RECORD_TYPE,"Header DateTime","Header datetime and file date time not matched ::"+headerDate);
        	 invalidHeaderRecord = true;
         }
         
			if (!preFileDateTime.matches(IAGConstants.FILE_DATE_TIME_FORMAT)) {

				controller.getErrorMsglist()
						.add(new ErrorMsgDetail(HEADER_RECORD_TYPE, "PREV_FILE_DATE_TIME",
								" date and time format is invalid. Format should be YYYY-MM-DDThh:mm:ssZ  \t ::"
										+ preFileDateTime));
				invalidHeaderRecord = true;
			}
         if(invalidHeaderRecord) {
        	 return false;
         }
		return true;
	}
	
	private void validateDuplicateTagSerialNo(String filePath, FileValidationParam validateParam) {

		HashSet<String> tagSet=new HashSet<>();
		HashMap<String, Integer> duplicateTagMap= new HashMap<>();
		long noOfRecords = 0;
		try (BufferedReader br = new BufferedReader(
				new FileReader(filePath))) {

			String fileRowData;
			
			while ((fileRowData = br.readLine()) != null) {
				if (noOfRecords != 0) {

					String tagAgencyCodeSerial = fileRowData.substring(0, 14);
					if (!tagSet.add(tagAgencyCodeSerial)) {
						if (duplicateTagMap.containsKey(tagAgencyCodeSerial)) {
							duplicateTagMap.put(tagAgencyCodeSerial,
									duplicateTagMap.get(tagAgencyCodeSerial).intValue() + 1);
						} else {
							duplicateTagMap.put(tagAgencyCodeSerial, 1);
						}
						itagValidation.invalidRecordCount++;
					}
				}
				noOfRecords++;
			}
			log.info("Duplicate tag Serial No Map ::"+duplicateTagMap);
			log.info("set size :::"+tagSet.size());
			log.info("Invalid record count ::"+itagValidation.invalidRecordCount);
			if(duplicateTagMap.size()>0) {
			 addErrorMsg(DETAIL_RECORD_TYPE,"Duplicate Tag","<b>tag Serial No :: </b>"+duplicateTagMap);
			}
		}catch (Exception e) {
			log.error("Duplicate serial no logic error ::"+e.getMessage());
			e.printStackTrace();
		}
	}
	
	private void addErrorMsg(String fileType,String fieldName,String errorMsg) {
		controller.getErrorMsglist().add(new ErrorMsgDetail(fileType,fieldName,errorMsg));
	}
}
