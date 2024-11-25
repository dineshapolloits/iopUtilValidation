package com.apolloits.util.validator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.apolloits.util.CommonUtil;
import com.apolloits.util.IAGConstants;
import com.apolloits.util.IagAckFileMapper;
import com.apolloits.util.controller.ValidationController;
import com.apolloits.util.modal.AgencyEntity;
import com.apolloits.util.modal.ErrorMsgDetail;
import com.apolloits.util.modal.FileValidationParam;
import com.apolloits.util.reader.AgencyDataExcelReader;

import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import static com.apolloits.util.IAGConstants.HEADER_RECORD_TYPE;
import static com.apolloits.util.IAGConstants.DETAIL_RECORD_TYPE;
import static com.apolloits.util.IAGConstants.FILE_RECORD_TYPE;


@Slf4j
@Component
public class ITAGFileDetailValidation {

	@Autowired
	private IagAckFileMapper iagAckMapper;
	@Autowired
	@Lazy
	ValidationController controller;
	
	public boolean itagValidation(FileValidationParam validateParam) throws IOException {
		//iagAckMapper = new IagAckFileMapper();
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
        	 if(!AgencyDataExcelReader.agencyCode.contains(validateParam.getToAgency())) {
        		 log.error("To Agency code not match with file Name");
        		 //validateParam.setResponseMsg("From Agency code "+validateParam.getFromAgency()+" not match with file Name ::"+inputItagZipFile.getName());
        		 controller.getErrorMsglist().add(new ErrorMsgDetail(FILE_RECORD_TYPE,"To Agency","To Agency code "+validateParam.getToAgency()+" not match with Configuration ::"));
        		 return false;
        	 }
        	 
        	 // validate ZIP file name 
        	 if(CommonUtil.validateZIPFileName(inputItagZipFile.getName())) {
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
        		 if(CommonUtil.validateFileName(fileName)) {
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
						while ((fileRowData = br.readLine()) != null) {
							log.info(noOfRecords + " :: " + fileRowData);
							if(noOfRecords == 0) {
								// Validate Header record
								headerCount = Long.parseLong(fileRowData.substring(36,46));
								if(!validateItagHeader(fileRowData,validateParam,fileName)) {
									//create ACK file 
									 //String ackFileName = IAGConstants.SRTA_HOME_AGENCY_ID + "_" + fileName.replace(".", "_") + IAGConstants.ACK_FILE_EXTENSION;
									iagAckMapper.mapToIagAckFile(fileName, "01", validateParam.getOutputFilePath()+"\\"+ackFileName, fileName.substring(0, 4),validateParam.getToAgency());
									return false;
								}
								if(validateParam.getValidateType().equals("header")) {
						        	 log.info("Only file name and header validation");
						        	 return true;
						         }
							}else {
								if(!validateItagDetail(fileRowData,validateParam,fileName,noOfRecords)) {
									validateParam.setResponseMsg(validateParam.getResponseMsg() +"\t    Line No::"+noOfRecords);
									iagAckMapper.mapToIagAckFile(fileName, "02", validateParam.getOutputFilePath()+"\\"+ackFileName, fileName.substring(0, 4),validateParam.getToAgency());
									return false;
								}
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
						}
					} catch (IOException e) {
						e.printStackTrace();
						// Display pop up message if exceptionn occurs
						System.out.println("Error while reading a file.");
					}
        	            
        			 
        		 }else {
        			 iagAckMapper.mapToIagAckFile(fileName, "07", validateParam.getOutputFilePath()+"\\"+ackFileName, fileName.substring(0, 4),validateParam.getToAgency());
        			 //validateParam.setResponseMsg("FAILED Reason:: ITAG file Name validation is failed");
        			 log.error("ITAG file Name validation is failed");
        			 controller.getErrorMsglist().add(new ErrorMsgDetail(FILE_RECORD_TYPE,"File Name","ITAG file Name validation is failed"));
        			 return false;
        		 }
        		 
        	 }else {
        		 //validateParam.setResponseMsg("FAILED Reason:: ZIP file Name validation is failed");
        		 log.error("ZIP File Name","ZIP file Name validation is failed");
        		 controller.getErrorMsglist().add(new ErrorMsgDetail(FILE_RECORD_TYPE,"ZIP File Name","ZIP file Name validation is failed"));
        		 return false;
        	 }
			 return true;
         }
	}
	
	public boolean validateItagHeader(String headervalue,FileValidationParam validateParam,String fileName) {
		log.info("headervalue :: "+headervalue);
		 String fileDate = fileName.substring(5, 13);
         String fileTime = fileName.substring(13, 19); //filename value
         String fromAgencyId = fileName.substring(0, 4);
        // String fileDateTime = fileName.substring(5, fileName.lastIndexOf("."));
         
         String headerFileType = "";
         String headerVersion = "";
         String headerFromAgencyId = "";
         String headerDate = "";
         String headerTime = "";
         //String headerFileDateTime ="";
         try {
             headerFileType = headervalue.substring(0, 4);
             headerVersion = headervalue.substring(4, 12);
             headerFromAgencyId = headervalue.substring(12, 16);
             headerDate = headervalue.substring(16, 26);
             headerTime = headervalue.substring(27, 35);
             //headerFileDateTime = headervalue.substring(16, 36).replaceAll("[-T:Z]", "");
         } catch (Exception e) {
        	// validateParam.setResponseMsg("FAILED Reason:: Header record for ITAG file is invalid format or length - " + headervalue);
        	log.error("FAILED Reason:: Header record for ITAG file is invalid format or length - " + headervalue);
        	 addErrorMsg(HEADER_RECORD_TYPE,"HeaderDate", "Header record for ITAG file is invalid format or length - " + headervalue);
        	 //return false;
         }
         if(!headerFileType.equals(IAGConstants.ITAG_FILE_TYPE)) {
        	 log.error("FAILED Reason:: Header record file type is invalid - " + headerFileType);
        	 //validateParam.setResponseMsg("FAILED Reason:: Header record file type is invalid - " + headerFileType);
        	 addErrorMsg(HEADER_RECORD_TYPE,"File Type", "Header record file type is invalid - " + headerFileType);
        	 //return false;
         }
         final Pattern pattern = Pattern.compile(IAGConstants.ITAG_HEADER_VERSION);
         if (!pattern.matcher(headerVersion).matches() || 
        		 !headerVersion.equals(ValidationController.cscIdTagAgencyMap.get(fromAgencyId).getVersionNumber())) {
        	 log.error("FAILED Reason:: Invalid header, version format is incorrect - " + headerVersion + "\t excepted version ::"+ValidationController.cscIdTagAgencyMap.get(fromAgencyId).getVersionNumber());
        	 addErrorMsg(HEADER_RECORD_TYPE,"IAG Version", "Version format is incorrect - " + headerVersion + "\t excepted version ::"+ValidationController.cscIdTagAgencyMap.get(fromAgencyId).getVersionNumber());
        	// validateParam.setResponseMsg("FAILED Reason:: Invalid header, version format is incorrect - " + headerVersion + "\t excepted version ::"+ValidationController.cscIdTagAgencyMap.get(fromAgencyId).getVersionNumber());
        	// return false;
         }
         
         if(!headerFromAgencyId.equals(fromAgencyId) || !AgencyDataExcelReader.agencyCode.contains(fromAgencyId)) {
        	 log.error("FAILED Reason:: Invalid header agency ID - " + fromAgencyId);
        	 addErrorMsg(HEADER_RECORD_TYPE,"FromAgencyId", "From agency ID and headerAgency Id not matched - " + fromAgencyId);
        	// validateParam.setResponseMsg("FAILED Reason:: Invalid header agency ID - " + fromAgencyId);
        	 //return false;
         }

         if (!fileDate.equals(headerDate.replace("-", "")) || !fileTime.equals(headerTime.replace(":", ""))) {
        	 log.error("Header datetime and file date time not matched");
        	 addErrorMsg(HEADER_RECORD_TYPE,"Header DateTime","Header datetime and file date time not matched ::"+headerDate);
        	 //return false;
         }
		return true;
	}
	
	public boolean validateItagDetail(String fileRowData, FileValidationParam validateParam, String fileName,long rowNo) {
		
		
		 ValidationController.cscIdTagAgencyMap.forEach((tag, agency) -> 
         System.out.println("validateItagDetail :: TagAgencyID: " + tag + ", Tag Start: " + agency.getTagSequenceStart() +"\t END ::"+agency.getTagSequenceEnd()));
 
		String tagAgencyId="";
		String tagSerialNo="";
		String tagStatus="";
		String tagAcctInfo="";
		String tagHomeAgency="";
		String tagAcTypeInd="";
		String tagAccountNo="";
		String tagProtocol ="";
		String tagType="";
		String tagMount="";
		String tagClass ="";
		String lineNo = "\t Line No::"+rowNo;
		
		if(fileRowData == null ||  fileRowData.length() != 85) {
			log.info("Detail record invalid length ::"+fileRowData);
			//validateParam.setResponseMsg("Detail record invalid length ::"+fileRowData);
			addErrorMsg(DETAIL_RECORD_TYPE,"DetailRecord","Invalid length ::"+fileRowData +lineNo);
			//return false;
		}else {
		
		try {
			tagAgencyId = fileRowData.substring(0,4);
			//System.out.println("tagAgencyId ::"+tagAgencyId);
			tagSerialNo = fileRowData.substring(4,14);
			//System.out.println("tagSerialNo ::"+tagSerialNo);
			tagStatus = fileRowData.substring(14,15);
			//System.out.println("tagStatus ::"+tagStatus);
			tagAcctInfo = fileRowData.substring(15,21);
			//System.out.println("tagAcctInfo ::"+tagAcctInfo);
			tagHomeAgency = fileRowData.substring(21,25);
			//System.out.println("tagHomeAgency ::"+tagHomeAgency);
			tagAcTypeInd = fileRowData.substring(25,26);
			//System.out.println("tagAcTypeInd ::"+tagAcTypeInd);
			tagAccountNo = fileRowData.substring(26,76);
			//System.out.println("tagAccountNo ::"+tagAccountNo);
			tagProtocol = fileRowData.substring(76,79);
			//System.out.println("tagProtocol ::"+tagProtocol);
			tagType = fileRowData.substring(79,80);
			//System.out.println("tagType ::"+tagType);
			tagMount = fileRowData.substring(80,81);
			//System.out.println("tagMount ::"+tagMount);
			tagClass = fileRowData.substring(81,85);
			//System.out.println("tagClass ::"+tagClass);
		} catch (Exception e) {
			e.printStackTrace();
			addErrorMsg(DETAIL_RECORD_TYPE,"DetailRecord","Invalid Row detail ::"+fileRowData +lineNo);
			//validateParam.setResponseMsg("Invalid Row detail  - "+fileRowData);
        	return false;
		}
		
		Pattern pattern = Pattern.compile(IAGConstants.ITAG_DTL_TAG_AGENCY_ID);
        if (!pattern.matcher(tagAgencyId).matches() || !(AgencyDataExcelReader.tagAgencyCode.contains(tagAgencyId))  ) {
        	//validateParam.setResponseMsg("Invalid ITAG detail, invalid tag agency ID - "+tagAgencyId +" Row ::"+fileRowData);
        	log.error("Invalid ITAG detail, invalid tag agency ID - "+tagAgencyId +" Row ::"+fileRowData);
        	addErrorMsg(DETAIL_RECORD_TYPE,"Tag agency ID","Invalid tag agency ID - "+tagAgencyId +" Row ::"+fileRowData +lineNo);
        	//return false;
        }
         pattern = Pattern.compile(IAGConstants.ITAG_DTL_TAG_SERIAL_NO);
        if (!pattern.matcher(tagSerialNo).matches()) { //need to check start and end tag range from DB
        	validateParam.setResponseMsg("Invalid ITAG detail, invalid tag serial number - "+tagSerialNo +" Row ::"+fileRowData);
        	log.error("Invalid ITAG detail, invalid tag serial number - "+tagSerialNo +" Row ::"+fileRowData);
        	addErrorMsg(DETAIL_RECORD_TYPE,"Tag serial number","Invalid Tag serial number format-"+tagSerialNo +" Row ::"+fileRowData +lineNo);
        	//return false;
        }
        
		try {
			AgencyEntity agEntity = ValidationController.cscIdTagAgencyMap.get(tagAgencyId);
			long tagSerialNoStart = Long.valueOf(agEntity.getTagSequenceStart());
			long tagSerialNoEnd = Long.valueOf(agEntity.getTagSequenceEnd());
			long tagSerialNoLong = Long.valueOf(tagSerialNo);
			log.info("## tagSerialNoLong ::" + tagSerialNoLong + "\t tagSerialNoStart :: " + tagSerialNoStart
					+ "\t tagSerialNoEnd ::" + tagSerialNoEnd);
			if (!(tagSerialNoStart < tagSerialNoEnd && tagSerialNoStart <= tagSerialNoLong
					&& tagSerialNoEnd >= tagSerialNoLong)) {
				log.error("## tagSerialNoLong ::" + tagSerialNoLong + "\t tagSerialNoStart :: " + tagSerialNoStart
						+ "\t tagSerialNoEnd ::" + tagSerialNoEnd);
			//	validateParam.setResponseMsg("Invalid TAG_SERIAL_NUMBER range   - " + tagSerialNo +" Row ::"+fileRowData);
				log.error("Invalid TAG_SERIAL_NUMBER range   - " + tagSerialNo +" Row ::"+fileRowData);
				addErrorMsg(DETAIL_RECORD_TYPE,"TAG_SERIAL_NUMBER", "Invalid TAG_SERIAL_NUMBER range   - " + tagSerialNo +" Row ::"+fileRowData+lineNo);
				//return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			addErrorMsg(DETAIL_RECORD_TYPE,"TAG_SERIAL_NUMBER","Invalid Tag Agency ID for TAG_SERIAL_NUMBER  from excel or file  - " + fileRowData +"\t Line No::"+rowNo);
			//validateParam.setResponseMsg("Invalid TAG_SERIAL_NUMBER format from excel or file  - " + fileRowData);
			//return false;
		}

         pattern = Pattern.compile(IAGConstants.ITAG_DTL_TAG_STATUS);
        if (!pattern.matcher(tagStatus).matches()) {
        	log.error("Invalid ITAG detail, invalid tag status - "+tagStatus +" Row ::"+fileRowData);
        	//validateParam.setResponseMsg("Invalid ITAG detail, invalid tag status - "+tagStatus +" Row ::"+fileRowData);
        	addErrorMsg(DETAIL_RECORD_TYPE,"tag status", "Invalid  status - "+ tagStatus +" Row ::"+fileRowData+lineNo);
        	//return false;
        }

         pattern = Pattern.compile(IAGConstants.ITAG_DTL_TAG_TYP);
        if (!pattern.matcher(tagAcTypeInd).matches()) {
        	log.error("Invalid ITAG detail, invalid tag type - "+tagAcTypeInd+" Row ::"+fileRowData);
        	addErrorMsg(DETAIL_RECORD_TYPE,"Tag Type", "invalid tag type - "+tagAcTypeInd+" Row ::"+fileRowData+lineNo);
        	//validateParam.setResponseMsg("Invalid ITAG detail, invalid tag type - "+tagAcTypeInd+" Row ::"+fileRowData);
        	//return false;
        }

         pattern = Pattern.compile(IAGConstants.ITAG_DTL_TAG_MOUNT);
        if (!pattern.matcher(tagMount).matches()) {
        	log.error("Invalid ITAG detail, invalid tag mount - "+tagMount +" Row ::"+fileRowData);
        	addErrorMsg(DETAIL_RECORD_TYPE,"Tag Mount", "Invalid tag mount - "+tagMount +" Row ::"+fileRowData+lineNo);
        	//validateParam.setResponseMsg("Invalid ITAG detail, invalid tag mount - "+tagMount +" Row ::"+fileRowData);
            //return false;
        }
	}
		return true;
	}
	
	private void addErrorMsg(String fileType,String fieldName,String errorMsg) {
		controller.getErrorMsglist().add(new ErrorMsgDetail(fileType,fieldName,errorMsg));
	}
}
