package com.apolloits.util.validator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
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
import com.apolloits.util.modal.AgencyEntity;
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
public class ITAGFileDetailValidation {

	@Autowired
	private IagAckFileMapper iagAckMapper;
	@Autowired
	@Lazy
	ValidationController controller;
	
	@Autowired
	CommonUtil commonUtil;
	
	int invalidRecordCount = 0;
	
	public boolean itagValidation(FileValidationParam validateParam) throws IOException {
		//iagAckMapper = new IagAckFileMapper();
		invalidRecordCount = 0;
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
        		 ackFileName = validateParam.getToAgency() + "_" + inputItagZipFile.getName().replace(".ZIP", "") + IAGConstants.ACK_FILE_EXTENSION;
        		 iagAckMapper.mapToIagAckFile(inputItagZipFile.getName(), "07", validateParam.getOutputFilePath()+File.separator+ackFileName, validateParam.getFromAgency(),validateParam.getToAgency(),validateParam.getVersion());
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
        		 controller.getErrorMsglist().add(new ErrorMsgDetail(FILE_RECORD_TYPE,"To Agency","To Agency code "+validateParam.getToAgency()+" not match with Configuration Tabel. Please check Agency Configuration"));
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

						String fileRowData;
						long headerCount =0l;
						String ackCode="00";
						while ((fileRowData = br.readLine()) != null) {
							log.info(noOfRecords + " :: " + fileRowData);
							if(noOfRecords == 0) {
								// Validate Header record
								//headerCount = Long.parseLong(fileRowData.substring(36,46));
								if(!validateItagHeader(fileRowData,validateParam,fileName)) {
									//create ACK file 
									ackCode="01";
								}
								iagAckMapper.mapToIagAckFile(fileName, ackCode, validateParam.getOutputFilePath()+File.separator+ackFileName, fileName.substring(0, 4),validateParam.getToAgency(),validateParam.getVersion());
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
								if(!validateItagDetail(fileRowData,validateParam,fileName,noOfRecords)) {
									validateParam.setResponseMsg(validateParam.getResponseMsg() +"\t    Line No::"+noOfRecords);
									iagAckMapper.mapToIagAckFile(fileName, "02", validateParam.getOutputFilePath()+File.separator+ackFileName, fileName.substring(0, 4),validateParam.getToAgency(),validateParam.getVersion());
									//return false;
								}
							}
							noOfRecords++;
						}
						if((noOfRecords-1) != headerCount ) {
							validateParam.setResponseMsg("\t Header count("+headerCount+") and detail count not matching ::"+(noOfRecords-1));
							iagAckMapper.mapToIagAckFile(fileName, "01", validateParam.getOutputFilePath()+File.separator+ackFileName, fileName.substring(0, 4),validateParam.getToAgency(),validateParam.getVersion());
							return false;
						}
						
						//calling Delimiter validation for detail 
						if (headerCount > 0 && controller.getErrorMsglist().size() == 0) {

							if (!commonUtil.validateDelimiter(zipFile.getFile().getParent() + File.separator + fileName,
									validateParam, fileName)) {
								invalidRecordCount++;
							}
						}
						
						 //validate Duplicate serial no
							validateDuplicateTagSerialNo(zipFile.getFile().getParent()+File.separator+fileName,validateParam);
						if(controller.getErrorMsglist().size()>0 && invalidRecordCount >0) {
							validateParam.setResponseMsg("\t \t <b>ACK file name ::</b> \t "+ackFileName +"\t <b> Invalid detail record count ::</b> \t "+invalidRecordCount);
							iagAckMapper.mapToIagAckFile(fileName, "02", validateParam.getOutputFilePath()+File.separator+ackFileName, fileName.substring(0, 4),validateParam.getToAgency(),validateParam.getVersion());
						}else if(controller.getErrorMsglist().size()== 0 && invalidRecordCount == 0 ) {
							log.info("Sucess ACK created");
							iagAckMapper.mapToIagAckFile(fileName, "00", validateParam.getOutputFilePath()+File.separator+ackFileName, fileName.substring(0, 4),validateParam.getToAgency(),validateParam.getVersion());
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
        			 iagAckMapper.mapToIagAckFile(fileName, "07", validateParam.getOutputFilePath()+File.separator+ackFileName, fileName.substring(0, 4),validateParam.getToAgency(),validateParam.getVersion());
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
						invalidRecordCount++;
					}
				}
				noOfRecords++;
			}
			log.info("Duplicate tag Serial No Map ::"+duplicateTagMap);
			log.info("set size :::"+tagSet.size());
			log.info("Invalid record count ::"+invalidRecordCount);
			if(duplicateTagMap.size()>0) {
			 addErrorMsg(DETAIL_RECORD_TYPE,"Duplicate Tag","<b>tag Serial No :: </b>"+duplicateTagMap);
			}
		}catch (Exception e) {
			log.error("Duplicate serial no logic error ::"+e.getMessage());
			e.printStackTrace();
		}
	}

	public boolean validateItagHeader(String headervalue,FileValidationParam validateParam,String fileName) {
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
        	 invalidHeaderRecord = true;
         }
         if (headervalue == null || headervalue.length() != 46 || headervalue.isEmpty()) {
           	controller.getErrorMsglist().add(new ErrorMsgDetail(HEADER_RECORD_TYPE,"Header Length","Invalid header length \t Header Row::"+headervalue));
           	return false;
           }
         if(!headerFileType.equals(IAGConstants.ITAG_FILE_TYPE)) {
        	 log.error("FAILED Reason:: Header record file type is invalid - " + headerFileType);
        	 //validateParam.setResponseMsg("FAILED Reason:: Header record file type is invalid - " + headerFileType);
        	 addErrorMsg(HEADER_RECORD_TYPE,"File Type", "Header record file type is invalid - " + headerFileType);
        	 invalidHeaderRecord = true;
         }
         final Pattern pattern = Pattern.compile(IAGConstants.IAG_HEADER_VERSION_FORMAT);
         if (!pattern.matcher(headerVersion).matches() 
        		// || ValidationController.cscIdTagAgencyMap.get(fromAgencyId) == null
        		// || !headerVersion.equals(ValidationController.cscIdTagAgencyMap.get(fromAgencyId).getVersionNumber())
        		 || !headerVersion.equals(validateParam.getVersion())
        		 ) {
        	 log.error("FAILED Reason:: Invalid header, version format is incorrect - " + headerVersion + "\t excepted version ::"+validateParam.getVersion());
        	 addErrorMsg(HEADER_RECORD_TYPE,"IAG Version", "Version format is incorrect - " + headerVersion + "\t excepted version ::"+validateParam.getVersion());
        	// validateParam.setResponseMsg("FAILED Reason:: Invalid header, version format is incorrect - " + headerVersion + "\t excepted version ::"+ValidationController.cscIdTagAgencyMap.get(fromAgencyId).getVersionNumber());
        	 invalidHeaderRecord = true;
         }
         
         if(!headerFromAgencyId.equals(fromAgencyId) || !AgencyDataExcelReader.agencyCode.contains(fromAgencyId)) {
        	 log.error("FAILED Reason:: Invalid header agency ID - " + fromAgencyId);
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
         if(invalidHeaderRecord) {
        	 return false;
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
		String lineNo = "\t <b>Row ::</b>"+fileRowData +"\t <b>Line No::</b>"+rowNo;
		boolean invalidRecord = false;
		if(fileRowData == null ||  fileRowData.length() != 85) {
			log.info("Detail record invalid length ::"+fileRowData);
			//validateParam.setResponseMsg("Detail record invalid length ::"+fileRowData);
			addErrorMsg(DETAIL_RECORD_TYPE,"DetailRecord","Invalid length ::"+lineNo);
			//return false;
			invalidRecord=true;
		}else {
		if(fileRowData.endsWith("\r\n")) {
			System.out.println("@@@@@@@@@@@@@@ fasle");
		}
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
			addErrorMsg(DETAIL_RECORD_TYPE,"DetailRecord","Invalid Row detail ::"+lineNo);
			//validateParam.setResponseMsg("Invalid Row detail  - "+fileRowData);
        	return false;
		}
		
		Pattern pattern = Pattern.compile(IAGConstants.ITAG_DTL_TAG_AGENCY_ID);
        if (!pattern.matcher(tagAgencyId).matches() || !(AgencyDataExcelReader.tagAgencyCode.contains(tagAgencyId))  ) {
        	//validateParam.setResponseMsg("Invalid ITAG detail, invalid tag agency ID - "+tagAgencyId +" Row ::"+fileRowData);
        	log.error("Invalid ITAG detail, invalid tag agency ID - "+tagAgencyId +" Row ::"+fileRowData);
        	addErrorMsg(DETAIL_RECORD_TYPE,"Tag agency ID","Invalid tag agency ID - "+tagAgencyId +lineNo);
        	invalidRecord=true;
        }
         pattern = Pattern.compile(IAGConstants.ITAG_DTL_TAG_SERIAL_NO);
        if (!pattern.matcher(tagSerialNo).matches()) { //need to check start and end tag range from DB
        	validateParam.setResponseMsg("Invalid  detail, invalid tag serial number - "+tagSerialNo +lineNo);
        	log.error("Invalid ITAG detail, invalid tag serial number - "+tagSerialNo +" Row ::"+lineNo);
        	addErrorMsg(DETAIL_RECORD_TYPE,"Tag serial number","Invalid Tag serial number format-"+tagSerialNo +lineNo);
        	invalidRecord=true;
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
				log.error("Invalid TAG_SERIAL_NUMBER range   - " + tagSerialNo +" Row ::"+lineNo);
				addErrorMsg(DETAIL_RECORD_TYPE,"TAG_SERIAL_NUMBER", "Invalid TAG_SERIAL_NUMBER range   - " + tagSerialNo +lineNo);
				invalidRecord=true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			addErrorMsg(DETAIL_RECORD_TYPE,"TAG_SERIAL_NUMBER","Invalid Tag Agency ID for TAG_SERIAL_NUMBER  from excel or file  - " + lineNo);
			//validateParam.setResponseMsg("Invalid TAG_SERIAL_NUMBER format from excel or file  - " + fileRowData);
			invalidRecord=true;
		}
		if(validateParam.getFileType().equals(IAGConstants.ITGU_FILE_TYPE) ){
			 pattern = Pattern.compile(IAGConstants.ITGU_DTL_TAG_STATUS);
		}else {
			 pattern = Pattern.compile(IAGConstants.ITAG_DTL_TAG_STATUS);
		}
        // pattern = Pattern.compile(IAGConstants.ITAG_DTL_TAG_STATUS);
        if (!pattern.matcher(tagStatus).matches()) {
        	log.error("Invalid ITAG detail, invalid tag status - "+tagStatus +lineNo);
        	//validateParam.setResponseMsg("Invalid ITAG detail, invalid tag status - "+tagStatus +" Row ::"+fileRowData);
        	addErrorMsg(DETAIL_RECORD_TYPE,"tag status", "Invalid  status - "+ tagStatus +lineNo);
        	invalidRecord=true;
        }
        //TAG_ACCT_INFO
        pattern = Pattern.compile("[0-9A-F]{6}");
        if(!pattern.matcher(tagAcctInfo).matches()) {
        	log.error("Invalid ITAG detail, TAG_ACCT_INFO invalid Tag account info  - "+tagAcctInfo+lineNo);
        	addErrorMsg(DETAIL_RECORD_TYPE,"TAG_ACCT_INFO", "Invalid Tag account info - "+tagAcctInfo+lineNo);
        	invalidRecord=true;
        }
        //TAG_HOME_AGENCY
        if(ValidationController.cscIdTagAgencyMap.get(tagHomeAgency) == null ) {
        	log.error("Invalid ITAG detail, TAG_HOME_AGENCY invalid Tag home agency  - "+tagHomeAgency+lineNo);
        	addErrorMsg(DETAIL_RECORD_TYPE,"TAG_HOME_AGENCY", "Invalid tag home agency - "+tagHomeAgency+lineNo);
        	invalidRecord=true;
        }
         pattern = Pattern.compile(IAGConstants.ITAG_DTL_TAG_AC_TYP_IND);
        if (!pattern.matcher(tagAcTypeInd).matches()) {
        	log.error("Invalid ITAG detail, TAG_AC_TYPE_IND invalid tag type - "+tagAcTypeInd+lineNo);
        	addErrorMsg(DETAIL_RECORD_TYPE,"TAG_AC_TYPE_IND", "Invalid tag AC type - "+tagAcTypeInd+lineNo);
        	//validateParam.setResponseMsg("Invalid ITAG detail, invalid tag type - "+tagAcTypeInd+" Row ::"+fileRowData);
        	invalidRecord=true;
        }
        //TAG_ACCOUNT_NO
        pattern = Pattern.compile("[0-9 A-Z*]{50}");
        if (!pattern.matcher(tagAccountNo).matches()) {
        	log.error("Invalid ITAG detail, TAG_ACCOUNT_NO invalid TAG_ACCOUNT_NO - "+tagAccountNo+lineNo);
        	addErrorMsg(DETAIL_RECORD_TYPE,"TAG_ACCOUNT_NO", "Invalid TAG_ACCOUNT_NO - "+tagAccountNo+lineNo);
        	invalidRecord=true;
        }
        
        //TAG_PROTOCOL
        pattern = Pattern.compile(IAGConstants.ITAG_DTL_TAG_PROTOCOL);
        if (!pattern.matcher(tagProtocol).matches()) {
        	log.error("Invalid ITAG detail, TAG_PROTOCOL invalid tag type - "+tagProtocol+lineNo);
        	addErrorMsg(DETAIL_RECORD_TYPE,"TAG_PROTOCOL", "Invalid TAG_PROTOCOL - "+tagProtocol+lineNo);
        	invalidRecord=true;
        }
        //TAG_TYPE
        pattern = Pattern.compile(IAGConstants.ITAG_DTL_TAG_TYP);
        if (!pattern.matcher(tagType).matches()) {
        	log.error("Invalid ITAG detail, TAG_TYPE invalid tag type - "+tagType+lineNo);
        	addErrorMsg(DETAIL_RECORD_TYPE,"TAG_TYPE", "Invalid tag  type - "+tagType+lineNo);
        	invalidRecord=true;
        }
         pattern = Pattern.compile(IAGConstants.ITAG_DTL_TAG_MOUNT);
        if (!pattern.matcher(tagMount).matches()) {
        	log.error("Invalid ITAG detail, invalid tag mount - "+tagMount +lineNo);
        	addErrorMsg(DETAIL_RECORD_TYPE,"Tag Mount", "Invalid tag mount - "+tagMount +lineNo);
        	//validateParam.setResponseMsg("Invalid ITAG detail, invalid tag mount - "+tagMount +" Row ::"+fileRowData);
        	invalidRecord=true;
        }
        pattern = Pattern.compile(IAGConstants.ITAG_TAG_CLASS);
        if(!pattern.matcher(tagClass).matches() && !tagClass.equals("****")) {
        	log.error("Invalid ITAG detail, invalid TAG_CLASSt - "+tagClass +lineNo);
        	addErrorMsg(DETAIL_RECORD_TYPE,"TAG_CLASS", "Invalid tag mount - "+tagClass +lineNo);
        	invalidRecord=true;
        }
	}
		if(invalidRecord) {
			invalidRecordCount++;
		}
		return true;
	}
	
	private void addErrorMsg(String fileType,String fieldName,String errorMsg) {
		controller.getErrorMsglist().add(new ErrorMsgDetail(fileType,fieldName,errorMsg));
	}
}
