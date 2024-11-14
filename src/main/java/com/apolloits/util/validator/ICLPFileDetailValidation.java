package com.apolloits.util.validator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.apolloits.util.CommonUtil;
import com.apolloits.util.IAGConstants;
import com.apolloits.util.IagAckFileMapper;
import com.apolloits.util.controller.ValidationController;
import com.apolloits.util.modal.FileValidationParam;
import com.apolloits.util.reader.AgencyDataExcelReader;

import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;

@Slf4j
@Component
public class ICLPFileDetailValidation {

	@Autowired
	private IagAckFileMapper iagAckMapper;
	
	@Autowired
	private AgencyDataExcelReader agDataExcel;
	
	public boolean iclpValidation(FileValidationParam validateParam) throws IOException {
		
		File inputItagZipFile = new File(validateParam.getInputFilePath());
		 String ackFileName = null;
		 if (!inputItagZipFile.exists()) {
			 validateParam.setResponseMsg("FAILED Reason::  ZIP file not found");
			 return false;
        }else {
        	if(!validateParam.getFromAgency().equals(inputItagZipFile.getName().substring(0,4))) {
       		 log.error("From Agency code not match with file Name");
       		 validateParam.setResponseMsg("From Agency code "+validateParam.getFromAgency()+" not match with file Name ::"+inputItagZipFile.getName());
       		 return false;
       	 }
        	// validate ZIP file name 
       	 if(CommonUtil.validateZIPFileName(inputItagZipFile.getName())) {
       		 String fileName="";
       		ZipFile zipFile = new ZipFile(inputItagZipFile);
   		 try {
				log.info("extract file name getFileHeaders ******************* "+zipFile.getFileHeaders().get(0).getFileName());
				log.info("inputItagZipFile.getAbsolutePath() :: "+inputItagZipFile.getAbsolutePath());
				zipFile.extractAll(FilenameUtils.getFullPath(inputItagZipFile.getAbsolutePath()));
				zipFile.close();
				fileName =zipFile.getFileHeaders().get(0).getFileName();
				ackFileName = IAGConstants.SRTA_HOME_AGENCY_ID + "_" + fileName.replace(".", "_") + IAGConstants.ACK_FILE_EXTENSION;
			} catch (ZipException e) {
				e.printStackTrace();
				 validateParam.setResponseMsg("FAILED Reason:: ZIP file extraction failed");
       		 return false;
			}
   		 //validate extract ICLP file name 
   		 if(CommonUtil.validateFileName(fileName)) {
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
						if(!validateIclpHeader(fileRowData,validateParam,fileName)) {
							//create ACK file 
							 //String ackFileName = IAGConstants.SRTA_HOME_AGENCY_ID + "_" + fileName.replace(".", "_") + IAGConstants.ACK_FILE_EXTENSION;
							iagAckMapper.mapToIagAckFile(fileName, "01", validateParam.getOutputFilePath()+"\\"+ackFileName, fileName.substring(0, 4));
							return false;
						}
					}else {
						if(!validateIclpDetail(fileRowData,validateParam,fileName)) {
							iagAckMapper.mapToIagAckFile(fileName, "02", validateParam.getOutputFilePath()+"\\"+ackFileName, fileName.substring(0, 4));
							return false;
						}
					}
					noOfRecords++;
				}
				if((noOfRecords-1) != headerCount ) {
					validateParam.setResponseMsg("FAILED Reason :: Header count("+headerCount+") and detail count not matching ::"+noOfRecords);
					iagAckMapper.mapToIagAckFile(fileName, "01", validateParam.getOutputFilePath()+"\\"+ackFileName, fileName.substring(0, 4));
					return false;
				}
			} catch (IOException e) {
				e.printStackTrace();
				// Display pop up message if exceptionn occurs
				System.out.println("Error while reading a file.");
			}
	            
			 
		 }else {
			 iagAckMapper.mapToIagAckFile(fileName, "07", validateParam.getOutputFilePath()+"\\"+ackFileName, fileName.substring(0, 4));
			 validateParam.setResponseMsg("FAILED Reason:: Inside ZIP ICLP file Name validation is failed");
    		 return false;
       	 }
        }else {
        	log.info("FAILED Reason:: ICLP Invalid ZIP file name ::\"+inputItagZipFile.getName()");
        	validateParam.setResponseMsg("FAILED Reason:: ICLP Invalid ZIP file name ::"+inputItagZipFile.getName());
        }
       	 return true;
		
        }
	}
	
	public boolean validateIclpHeader(String headervalue,FileValidationParam validateParam,String fileName) {
		log.info("ICLP headervalue :: "+headervalue);
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
        	 validateParam.setResponseMsg("Header record for ICLP file is invalid - " + headervalue);
        	 return false;
         }
         if(!headerFileType.equals(IAGConstants.ICLP_FILE_TYPE)) {
        	 validateParam.setResponseMsg("Header record file type is invalid - " + headerFileType);
        	 return false;
         }
         final Pattern pattern = Pattern.compile(IAGConstants.ITAG_HEADER_VERSION);
         if (!pattern.matcher(headerVersion).matches() ||
        		 !headerVersion.equals(ValidationController.cscIdTagAgencyMap.get(fromAgencyId).getVersionNumber())) {
        	 log.error("FAILED Reason:: Invalid header, version format is incorrect - " + headerVersion + "\t excepted version ::"+ValidationController.cscIdTagAgencyMap.get(fromAgencyId).getVersionNumber());
        	 validateParam.setResponseMsg("Invalid header, version format is incorrect - " + headerVersion + "\t excepted version ::"+ValidationController.cscIdTagAgencyMap.get(fromAgencyId).getVersionNumber());
        	 return false;
         }
         System.out.println("AgencyDataExcelReader.agencyCode.contains(fromAgencyId) :: " +AgencyDataExcelReader.agencyCode.contains(fromAgencyId));
         if(!headerFromAgencyId.equals(fromAgencyId) || !AgencyDataExcelReader.agencyCode.contains(fromAgencyId)) {
        	 validateParam.setResponseMsg("Invalid header agency ID - " + fromAgencyId);
        	 return false;
         }

         if (!fileDate.equals(headerDate.replace("-", "")) || !fileTime.equals(headerTime.replace(":", ""))) {
        	 return false;
         }
		return true;
	}
	public boolean validateIclpDetail(String fileRowData, FileValidationParam validateParam, String fileName) {

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
		if(fileRowData == null ||  fileRowData.length() != 208) {
			log.info("Detail record invalid length ::"+fileRowData);
			validateParam.setResponseMsg("Detail record invalid length ::"+fileRowData);
			return false;
		}
		try {
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
			System.out.println("agDataExcel.getPlateStateSet() ::"+agDataExcel.getPlateStateSet());
			System.out.println("agDataExcel.getPlateStateSet().contains(licState) ::"+agDataExcel.getPlateStateSet().contains(licState));
			if (!licState.matches("[A-Z]{2}") ) {
				log.info("Invalid ICLP detail, invalid state Format- "+licState +" Row ::"+fileRowData);
				validateParam.setResponseMsg("Invalid ICLP detail, invalid Lic_state Format - "+licState +" Row ::"+fileRowData);
				return false;
			}
			if (!agDataExcel.getPlateStateSet().contains(licState)) {
				log.info("Invalid ICLP detail,Please check your State configuration - invalid state - "+licState +" Row ::"+fileRowData);
				validateParam.setResponseMsg("Invalid ICLP detail,Please check your State configuration - invalid state - "+licState +" Row ::"+fileRowData);
				return false;
			}
			if(!licNumber.matches("^[A-Z \\d-.&]{10}$")) {
				log.info("Invalid ICLP detail, invalid Lic_Number Format - "+licNumber +" Row ::"+fileRowData);
				validateParam.setResponseMsg("Invalid ICLP detail, invalid Lic_Number Format - "+licNumber +" Row ::"+fileRowData);
				return false;
			}
			
			if (!licType.matches("[A-Z \\d*]{30}")) {
				log.info("Invalid ICLP detail, invalid Lic_Type Format - "+licNumber +" Row ::"+fileRowData);
				validateParam.setResponseMsg("Invalid ICLP detail, invalid Lic_Type Format - "+licNumber +" Row ::"+fileRowData);
				return false;
			}
			
			String licStateType =(licState.trim())+(licType.trim());
			//System.out.println("licStateType :::"+licStateType);
			log.info("IsPlateType :: "+ agDataExcel.getPlateStateTypeSet().contains(licStateType));
			//System.out.println("LIC_State and type ::"+agDataExcel.getPlateStateTypeSet().toString()); 
			if(!licType.matches("[\\*]{30}") &&
					!agDataExcel.getPlateStateTypeSet().contains(licStateType)) {
				validateParam.setResponseMsg("Invalid ICLP detail,Please check your State and plateType Configuration - invalid Lic_Type - "+licType +" Row ::"+fileRowData);
				return false;
			}
			//ValidationController.cscIdTagAgencyMap.forEach((tag, agency) -> 
			//System.out.println("validateIclpDetail :: TagAgencyID: " + tag + ", Tag Start: " + agency.getTagSequenceStart() +"\t END ::"+agency.getTagSequenceEnd()));
			
			if (!tagAgencyid.matches("\\d{4}") 
					|| ValidationController.cscIdTagAgencyMap.get(tagAgencyid) == null) {
				log.info("Invalid ICLP detail, invalid TAG_AGENCY_ID - "+tagAgencyid +" Row ::"+fileRowData);
				validateParam.setResponseMsg("Invalid ICLP detail, invalid TAG_AGENCY_ID - "+tagAgencyid +" Row ::"+fileRowData);
				return false;
			}
			
			if (!tagSerialNo.matches("\\d{10}") ) { //we can move this if condition to below if
				log.info("Invalid ICLP detail, invalid TAG_SERIAL_NUMBER Format - "+tagSerialNo +" Row ::"+fileRowData);
				validateParam.setResponseMsg("Invalid ICLP detail, invalid TAG_SERIAL_NUMBER Format - "+tagSerialNo +" Row ::"+fileRowData);
				return false;
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
				log.info("Invalid LIC_EFFECTIVE_FROM format from excel or file  - " + licEffectiveFrom+" ROW ::" + fileRowData);
				validateParam.setResponseMsg("Invalid TAG_SERIAL_NUMBER   - " + licEffectiveFrom+" ROW ::" + fileRowData);
				return false;
			}
			if(!isValidLicEffective(licEffectiveTo)) {
				log.info("Invalid LIC_EFFECTIVE_TO format from excel or file  - " + licEffectiveTo+" ROW ::" + fileRowData);
				validateParam.setResponseMsg("Invalid LIC_EFFECTIVE_TO format  - " + licEffectiveTo+" ROW ::" + fileRowData);
				return false;
			}
			if (!licHomeAgency.matches("\\d{4}") || !AgencyDataExcelReader.agencyCode.contains(licHomeAgency)) {
				log.info("Invalid LIC_HOME_AGENCY   - " + licHomeAgency+" ROW ::" + fileRowData);
				validateParam.setResponseMsg("Invalid LIC_HOME_AGENCY  - " + licHomeAgency+" ROW ::" + fileRowData);
				return false;
			}
			if (!licAccountNo.matches("[A-Z \\d*]{50}")) {
				log.error("Invalid LIC_ACCOUNT_NO   - " + licAccountNo+" ROW ::" + fileRowData);
				validateParam.setResponseMsg("Invalid LIC_HOME_AGENCY  - " + licAccountNo+" ROW ::" + fileRowData);
				return false;
			}
			if (!LicVin.matches("[A-Z \\d*]{17}")) {
				log.error("Invalid LIC_VIN   - " + LicVin+" ROW ::" + fileRowData);
				validateParam.setResponseMsg("Invalid LIC_VIN  - " + LicVin+" ROW ::" + fileRowData);
				return false;
			}
			if (!licGuaranteed.matches("[YN*]")) {
				log.error("Invalid LIC_GUARANTEED   - " + licGuaranteed+" ROW ::" + fileRowData);
				validateParam.setResponseMsg("Invalid LIC_GUARANTEED  - " + licGuaranteed+" ROW ::" + fileRowData);
				return false;
			}
			if (!licRegDate.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z") && !licRegDate.matches("\\*{20}")) {
				log.error("Invalid LIC_REGISTRATION_DATE   - " + fileRowData);
				validateParam.setResponseMsg("Invalid LIC_REGISTRATION_DATE  - "+licRegDate+" ROW ::" + fileRowData);
				return false;
			}
			if (!licUpdateDate.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z") && !licUpdateDate.matches("\\*{20}")) {
				log.error("Invalid LIC_UPDATE_DATE   - " + fileRowData);
				validateParam.setResponseMsg("Invalid LIC_UPDATE_DATE  - "+licUpdateDate+" ROW ::" + fileRowData);
				return false;
			}
	            
			
		}catch (Exception e) {
			e.printStackTrace();
			validateParam.setResponseMsg("Invalid Row detail  - "+fileRowData);
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
