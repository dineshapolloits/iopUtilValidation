package com.apolloits.util.validator.niop;

import static com.apolloits.util.IAGConstants.DETAIL_RECORD_TYPE;
import static com.apolloits.util.IAGConstants.HEADER_RECORD_TYPE;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.apolloits.util.NIOPConstants;
import com.apolloits.util.NiopAckFileMapper;
import com.apolloits.util.controller.NiopValidationController;
import com.apolloits.util.modal.ErrorMsgDetail;
import com.apolloits.util.modal.FileValidationParam;
import com.apolloits.util.modal.NiopAgencyEntity;
import com.apolloits.util.modal.niop.TVLAccountDetails;
import com.apolloits.util.modal.niop.TVLHeader;
import com.apolloits.util.modal.niop.TVLPlateDetails;
import com.apolloits.util.modal.niop.TVLTagDetails;
import com.apolloits.util.modal.niop.TagValidationList;
import com.apolloits.util.utility.CommonUtil;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;

@Slf4j
@Component
public class BTVLFileDetailValidation {

	@Autowired
	CommonUtil commonUtil;
	
	@Autowired
	private NiopAckFileMapper niopAckMapper;
	
	@Autowired
	@Lazy
	NiopValidationController controller;
	
	int invalidRecordCount = 0;
	
	public boolean btvlValidation(FileValidationParam validateParam,String tvlType) throws IOException, JAXBException {
		log.info("Inside btvlValidation started :"+validateParam.getInputFilePath());
		invalidRecordCount = 0;
		 File file = new File(validateParam.getInputFilePath());
		 String ackFileName = null;
		 long start = System.currentTimeMillis();
		 if(commonUtil.validateNiopTvlZIPFileName(file.getName(),validateParam)) {
			 log.info("Zip file name validaton true");
			 //start unzip file
			 String fileName="";
			 TagValidationList list = null;
    		 //extract ZIP file 
    		 ZipFile zipFile = new ZipFile(file);
    		 try {
				log.info("extract file name getFileHeaders ******************* "+zipFile.getFileHeaders().get(0).getFileName());
				log.info("inputItagZipFile.getAbsolutePath() :: "+file.getAbsolutePath());
				zipFile.extractAll(FilenameUtils.getFullPath(file.getAbsolutePath()));
				zipFile.close();
				fileName =zipFile.getFileHeaders().get(0).getFileName();
				
			} catch (ZipException e) {
				e.printStackTrace();
				ackFileName = NiopValidationController.allCscIdNiopAgencyMap.get(validateParam.getToAgency()).getHubId() + "_" + validateParam.getFromAgency() + "_" + fileName.split("[.]")[0] + "_" + "07"
						+ "_" + tvlType + NIOPConstants.ACK_FILE_EXTENSION;
				log.info("ACK File Name ::" + ackFileName);
				niopAckMapper.setNiopAckFile(validateParam, "STVL", commonUtil.convertFileDateToUTCDateFormat(fileName.substring(0,24)), "07", ackFileName);
				 validateParam.setResponseMsg("FAILED Reason:: ZIP file extraction failed");
        		 return false;
			}
    		 //validate unzip file name
    		 if(commonUtil.validateNiopBtvlFileName(fileName,validateParam)) {
    			 if(validateParam.getValidateType().equals("filename")) {
    				 validateParam.setResponseMsg("File name validation is sucess");
    				 return true;
    			 }
					try {
						 File xmlfile = new File(zipFile.getFile().getParent()+File.separator+fileName);
						JAXBContext jaxbContext = JAXBContext.newInstance(TagValidationList.class);
						Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
						list = (TagValidationList) unmarshaller.unmarshal(xmlfile);
						System.out.println("TVL List getTvlHeader:: " + list.getTvlHeader().toString());
						System.out.println("TVL List getTvlHeader:: " + list.getTvlDetail());
					} catch (Exception e) {
						e.printStackTrace();
						validateParam.setResponseMsg("Invalid XML file. Please check XML format");
						log.error("Invalid XML file. Please check XML format");
						ackFileName = NiopValidationController.allCscIdNiopAgencyMap.get(validateParam.getToAgency()).getHubId() + "_" + validateParam.getFromAgency() + "_" + fileName.split("[.]")[0] + "_" + "07"
								+ "_" + tvlType + NIOPConstants.ACK_FILE_EXTENSION;
						niopAckMapper.setNiopAckFile(validateParam, "STVL", commonUtil.convertFileDateToUTCDateFormat(fileName.substring(0,24)), "07", ackFileName);
						return false;
					}
    			 
    			 boolean headerValidtionFlag = headerValidation(list.getTvlHeader(),validateParam,fileName);
    			 //if validating header only
    			 if(validateParam.getValidateType().equals("header")) {
    				 log.info("Only file name and header validation");
    				 if(headerValidtionFlag) {
    					 ackFileName = NiopValidationController.allCscIdNiopAgencyMap.get(validateParam.getToAgency()).getHubId() + "_" + validateParam.getFromAgency() + "_" + fileName.split("[.]")[0] + "_" + "00"
    								+ "_" + tvlType + NIOPConstants.ACK_FILE_EXTENSION;
    					 niopAckMapper.setNiopAckFile(validateParam, "STVL", list.getTvlHeader().getSubmittedDateTime(), "00", ackFileName);
    				 }
    				 return headerValidtionFlag;
    			 }
    			 //start to validate detail records
    			 boolean detailValidtionFlag = detailValidation(list,validateParam,fileName);
    			 if(controller.getErrorMsglist().size()>0 && invalidRecordCount >0) {
    				 ackFileName = NiopValidationController.allCscIdNiopAgencyMap.get(validateParam.getToAgency()).getHubId() + "_" + validateParam.getFromAgency() + "_" + fileName.split("[.]")[0] + "_" + "02"
								+ "_" + tvlType + NIOPConstants.ACK_FILE_EXTENSION;
						validateParam.setResponseMsg("\t \t <b>ACK file name ::</b> \t "+ackFileName +"\t <b> Invalid detail record count ::</b> \t "+invalidRecordCount);
						niopAckMapper.setNiopAckFile(validateParam, "STVL", list.getTvlHeader().getSubmittedDateTime(), "02", ackFileName);	
    			 }else if(controller.getErrorMsglist().size() == 0 && invalidRecordCount == 0) {
    				 ackFileName = NiopValidationController.allCscIdNiopAgencyMap.get(validateParam.getToAgency()).getHubId() + "_" + validateParam.getFromAgency() + "_" + fileName.split("[.]")[0] + "_" + "00"
								+ "_" + tvlType + NIOPConstants.ACK_FILE_EXTENSION;
					 niopAckMapper.setNiopAckFile(validateParam, "STVL", list.getTvlHeader().getSubmittedDateTime(), "00", ackFileName);
					 //validateParam.setResponseMsg("\t <b>ACK file Name :</b>"+ackFileName);
    			 }
    		 }else {
    			 log.error("TVL File validation failed ::"+tvlType);
    			 return false;
    		 }
		 }else {
			 log.error("ZIP file validation failed");
			 return false;
		 }
       
         long end = System.currentTimeMillis();
 		System.out.println((end - start) / 1000f + " seconds");
		return true;
	}

	private boolean detailValidation(TagValidationList list, FileValidationParam validateParam,String fileName) {
		//String lineNo = "\t <b>Row ::</b>"+fileRowData +"\t <b>Line No::</b>"+rowNo;
		//start to validate count of record
		if(!list.getTvlHeader().getTotalRecordCount().equals(String.valueOf(list.getTvlDetail().get(0).getTvlTagDetails().size()))) {
			addErrorMsg(HEADER_RECORD_TYPE,"RecordCount"," Invalid Header RecordCount   \t ::"+list.getTvlHeader().getTotalRecordCount()+"\t Detail Count :: \t"+list.getTvlDetail().get(0).getTvlTagDetails().size());
			String ackFileName = NiopValidationController.allCscIdNiopAgencyMap.get(validateParam.getToAgency()).getHubId() + "_" + validateParam.getFromAgency() + "_" + fileName.substring(0,24) + "_" + "01"
					+ "_" + NIOPConstants.BTVL_FILE_TYPE + NIOPConstants.ACK_FILE_EXTENSION;
			niopAckMapper.setNiopAckFile(validateParam, "STVL", list.getTvlHeader().getSubmittedDateTime(), "01", ackFileName);
       	 log.error("Header record and detail record count are not matched");
			return false;
		}
		List<TVLTagDetails> tvlTagDetails= list.getTvlDetail().get(0).getTvlTagDetails();
		log.info("tvlDetailList size ::"+ tvlTagDetails.size());
		for(int count=0; count<tvlTagDetails.size();count++) {
			validateRecord(tvlTagDetails.get(count),validateParam,fileName);
		}
		return false;
	}

	private boolean validateRecord(TVLTagDetails tvlTagDetails, FileValidationParam validateParam, String fileName) {
		String lineNo = "\t <b>TagSerialNumber ::</b>"+tvlTagDetails.getTagSerialNumber() ;
		boolean invalidRecord = false;
		Pattern pattern = Pattern.compile(NIOPConstants.AGENCY_ID_FORMAT);
		
		if (!pattern.matcher(tvlTagDetails.getHomeAgencyId()).matches()
				|| !(tvlTagDetails.getHomeAgencyId().equals(validateParam.getFromAgency()))) {
			log.error("Invalid BTVL detail, Home Agency ID - " + tvlTagDetails.getHomeAgencyId() + " TagSerialNumber ::"
					+ tvlTagDetails.getTagSerialNumber());
			addErrorMsg(DETAIL_RECORD_TYPE, "Home Agency ID",
					"Invalid home agency ID format - " + tvlTagDetails.getHomeAgencyId() + lineNo);
			invalidRecord = true;
		}
		
        if (!pattern.matcher(tvlTagDetails.getTagAgencyId()).matches() || NiopValidationController.cscIdTagNiopAgencyMap.get(tvlTagDetails.getTagAgencyId()) == null  
        		) {
        	//validateParam.setResponseMsg("Invalid ITAG detail, invalid tag agency ID - "+tagAgencyId +" Row ::"+fileRowData);
        	log.error("Invalid BTVL detail, TagAgencyID - "+tvlTagDetails.getHomeAgencyId() +" TagSerialNumber ::"+tvlTagDetails.getTagSerialNumber());
        	addErrorMsg(DETAIL_RECORD_TYPE,"Tag Agency ID","Invalid Tag agency ID format or value \t"+tvlTagDetails.getTagAgencyId() +lineNo);
        	invalidRecord=true;
        }
        
        pattern = Pattern.compile(NIOPConstants.BTVL_DTL_TAG_SERIAL_NO);
        if (!pattern.matcher(tvlTagDetails.getTagSerialNumber()).matches()) { //need to check start and end tag range from DB
        	log.error("Invalid BTVL detail, invalid tag serial number - "+tvlTagDetails.getTagSerialNumber());
        	addErrorMsg(DETAIL_RECORD_TYPE,"Tag Serial Number","Invalid Tag serial number format \t"+tvlTagDetails.getTagSerialNumber());
        	invalidRecord=true;
        }else {
        	try {
        		NiopAgencyEntity agEntity = NiopValidationController.cscIdTagNiopAgencyMap.get(tvlTagDetails.getTagAgencyId());
        		if(agEntity != null) {
    			long tagSerialNoStart = Long.valueOf(agEntity.getTagSequenceStart());
    			long tagSerialNoEnd = Long.valueOf(agEntity.getTagSequenceEnd());
    			long tagSerialNoLong = Long.valueOf(tvlTagDetails.getTagSerialNumber());
    			log.info("## tagSerialNoLong ::" + tagSerialNoLong + "\t tagSerialNoStart :: " + tagSerialNoStart
    					+ "\t tagSerialNoEnd ::" + tagSerialNoEnd);
    			
    			if (!(tagSerialNoStart < tagSerialNoEnd && tagSerialNoStart <= tagSerialNoLong
    					&& tagSerialNoEnd >= tagSerialNoLong)) {
    				log.error("## tagSerialNoLong ::" + tagSerialNoLong + "\t tagSerialNoStart :: " + tagSerialNoStart
    						+ "\t tagSerialNoEnd ::" + tagSerialNoEnd);
    				log.error("Invalid Tag Serial Number range   - " + tagSerialNoLong);
    				addErrorMsg(DETAIL_RECORD_TYPE,"Tag Serial Number", "Invalid TAG_SERIAL_NUMBER range   - " + tvlTagDetails.getTagSerialNumber());
    				invalidRecord=true;
    			}
        		}else {
        			log.error("Invalid BTVL detail, invalid tag serial number range - "+tvlTagDetails.getTagSerialNumber());
                	addErrorMsg(DETAIL_RECORD_TYPE,"Tag Serial Number","Invalid Tag serial number Range or Tag Agency ID \t"+tvlTagDetails.getTagSerialNumber());
        		}
			} catch (Exception e) {
				e.printStackTrace();
				addErrorMsg(DETAIL_RECORD_TYPE,"Tag Serial Number","Invalid Tag Agency ID for TAG_SERIAL_NUMBER range from excel or file  - " + lineNo);
				//validateParam.setResponseMsg("Invalid TAG_SERIAL_NUMBER format from excel or file  - " + fileRowData);
				invalidRecord=true;
			}
        	
        }
        
        pattern = Pattern.compile(NIOPConstants.BTVL_DTL_TAG_STATUS);
        if (!pattern.matcher(tvlTagDetails.getTagStatus()).matches()) { 
        	log.error("Invalid BTVL detail, Tag Status - "+tvlTagDetails.getTagStatus()+lineNo);
        	addErrorMsg(DETAIL_RECORD_TYPE,"Tag Status","Invalid Tag Status-"+tvlTagDetails.getTagStatus()+lineNo);
        	invalidRecord=true;
        }
        
        //3 discount fields Plans,start date and end date -
        
        pattern = Pattern.compile(NIOPConstants.BTVL_DTL_DISCOUNT_PLAN_FORMAT);
        if (tvlTagDetails.getDiscountPlans()!= null &&  !pattern.matcher(tvlTagDetails.getDiscountPlans()).matches()) { 
        	log.error("Invalid BTVL detail, Discount Plans - "+tvlTagDetails.getDiscountPlans()+lineNo);
        	addErrorMsg(DETAIL_RECORD_TYPE,"Discount Plans","Discount Plans-"+tvlTagDetails.getDiscountPlans()+lineNo);
        	invalidRecord=true;
        } 
        //Discount plan start date
        pattern = Pattern.compile(NIOPConstants.UTC_DATE_TIME_FORMAT);
		if (tvlTagDetails.getDiscountPlanStartDate() != null
				&& !pattern.matcher(tvlTagDetails.getDiscountPlanStartDate()).matches()) {
			log.error("Invalid BTVL detail, Discount Plan Start Date format - "
					+ tvlTagDetails.getDiscountPlanStartDate() + lineNo);
			addErrorMsg(DETAIL_RECORD_TYPE, "Discount Plan Start Date",
					"Invalid Discount Plan Start Date format-" + tvlTagDetails.getDiscountPlanStartDate() + lineNo);
			invalidRecord = true;
		}else if (tvlTagDetails.getDiscountPlanStartDate() != null && !commonUtil.isValidDateTimeInDetail(tvlTagDetails.getDiscountPlanStartDate())) {
			log.error("Invalid BTVL detail, Discount Plan Start Date - "
					+ tvlTagDetails.getDiscountPlanStartDate() + lineNo);
			addErrorMsg(DETAIL_RECORD_TYPE, "Discount Plan Start Date",
					"Invalid Discount Plan Start Date-" + tvlTagDetails.getDiscountPlanStartDate() + lineNo);
			invalidRecord = true;
		}
		
		if(tvlTagDetails.getDiscountPlanEndDate() != null && !pattern.matcher(tvlTagDetails.getDiscountPlanEndDate()).matches()) {
			log.error("Invalid BTVL detail, Discount Plan End Date format- "+tvlTagDetails.getDiscountPlanEndDate()+lineNo);
        	addErrorMsg(DETAIL_RECORD_TYPE,"Discount Plan End Date","Invalid Discount Plan End Date format-"+tvlTagDetails.getDiscountPlanEndDate()+lineNo);
        	invalidRecord=true;
		}else if (tvlTagDetails.getDiscountPlanEndDate() != null && !commonUtil.isValidDateTimeInDetail(tvlTagDetails.getDiscountPlanEndDate())) {
			log.error("Invalid BTVL detail, Discount Plan End Date - "
					+ tvlTagDetails.getDiscountPlanEndDate() + lineNo);
			addErrorMsg(DETAIL_RECORD_TYPE, "Discount Plan End Date",
					"Invalid Discount Plan End Date-" + tvlTagDetails.getDiscountPlanEndDate() + lineNo);
			invalidRecord = true;
		}
		
        
        pattern = Pattern.compile(NIOPConstants.BTVL_DTL_TAG_TYPE);
        if (tvlTagDetails.getTagType() != null && !pattern.matcher(tvlTagDetails.getTagType()).matches()) { 
        	log.error("Invalid BTVL detail, Tag Type- "+tvlTagDetails.getTagType()+lineNo);
        	addErrorMsg(DETAIL_RECORD_TYPE,"Tag Type","Invalid Tag Type-"+tvlTagDetails.getTagType()+lineNo);
        	invalidRecord=true;
        }
        
        int tagClass = tvlTagDetails.getTagClass();
        if (!(tagClass>=2 && tagClass <=15)) { 
        	log.error("Invalid BTVL detail, Tag Class - "+tvlTagDetails.getTagClass()+lineNo);
        	addErrorMsg(DETAIL_RECORD_TYPE,"Tag Class","Invalid Tag Class-"+tvlTagDetails.getTagClass()+lineNo);
        	invalidRecord=true;
        }
        
        if(tvlTagDetails.getTvlPlateDetails() != null) {
        	List<TVLPlateDetails> tvlPlateDetails = tvlTagDetails.getTvlPlateDetails();
        	for (TVLPlateDetails tvlPlateDet : tvlPlateDetails) {
        		pattern = Pattern.compile(NIOPConstants.BTVL_DTL_PLATE_COUNTRY);
        		if(tvlPlateDet.getPlateCountry()== null || !pattern.matcher(tvlPlateDet.getPlateCountry()).matches()) {
        			log.error("Invalid BTVL detail, License Plate Country - "+tvlPlateDet.getPlateCountry()+lineNo);
                	addErrorMsg(DETAIL_RECORD_TYPE,"License Plate Country","Invalid License Plate Country-"+tvlPlateDet.getPlateCountry()+lineNo);
                	invalidRecord=true;
        		}
        		pattern = Pattern.compile(NIOPConstants.BTVL_DTL_PLATE_STATE_FORMAT);
        		if(tvlPlateDet.getPlateState() == null || !pattern.matcher(tvlPlateDet.getPlateState()).matches()) {
        			log.error("Invalid BTVL detail, License Plate State - "+tvlPlateDet.getPlateState()+lineNo);
                	addErrorMsg(DETAIL_RECORD_TYPE,"License Plate State","Invalid License Plate state-"+tvlPlateDet.getPlateState()+lineNo);
                	invalidRecord=true;
        		}
        		pattern = Pattern.compile(NIOPConstants.BTVL_DTL_PLATE_NUMBER_FORMAT);
        		if(tvlPlateDet.getPlateNumber() == null || !pattern.matcher(tvlPlateDet.getPlateNumber()).matches()) {
        			log.error("Invalid BTVL detail, License Plate Number - "+tvlPlateDet.getPlateNumber()+lineNo);
                	addErrorMsg(DETAIL_RECORD_TYPE,"License Plate Number","Invalid License Plate Number-"+tvlPlateDet.getPlateNumber()+lineNo);
                	invalidRecord=true;
        		}
        		pattern = Pattern.compile(NIOPConstants.BTVL_DTL_PLATE_TYPE_FORMAT);
        		if(tvlPlateDet.getPlateType() != null && !pattern.matcher(tvlPlateDet.getPlateType()).matches()) {
        			log.error("Invalid BTVL detail, License Plate Type - "+tvlPlateDet.getPlateType()+lineNo);
                	addErrorMsg(DETAIL_RECORD_TYPE,"License Plate Type","Invalid License Plate Type-"+tvlPlateDet.getPlateType()+lineNo);
                	invalidRecord=true;
        		}
        		pattern = Pattern.compile(NIOPConstants.UTC_DATE_TIME_FORMAT);
				if (tvlPlateDet.getPlateEffectiveFrom() != null
						&& !pattern.matcher(tvlPlateDet.getPlateEffectiveFrom()).matches()) {
					log.error("Invalid BTVL detail, License Plate Effective From format - "
							+ tvlPlateDet.getPlateEffectiveFrom() + lineNo);
					addErrorMsg(DETAIL_RECORD_TYPE, "License Plate Effective From format",
							"Invalid License Plate Effective From-" + tvlPlateDet.getPlateEffectiveFrom() + lineNo);
					invalidRecord = true;
				}else if (!commonUtil.isValidDateTimeInDetail(tvlPlateDet.getPlateEffectiveFrom())) {
					log.error("Invalid BTVL detail, License Plate Effective From - "
							+ tvlPlateDet.getPlateEffectiveFrom() + lineNo);
					addErrorMsg(DETAIL_RECORD_TYPE, "License Plate Effective From",
							"Invalid License Plate Effective From-" + tvlPlateDet.getPlateEffectiveFrom() + lineNo);
					invalidRecord = true;
				}
        		
        		pattern = Pattern.compile(NIOPConstants.UTC_DATE_TIME_FORMAT);
        		if(tvlPlateDet.getPlateEffectiveTo() != null && !pattern.matcher(tvlPlateDet.getPlateEffectiveTo()).matches()) {
        			log.error("Invalid BTVL detail, License Plate Effective To format- "+tvlPlateDet.getPlateEffectiveTo()+lineNo);
                	addErrorMsg(DETAIL_RECORD_TYPE,"License Plate Effective To","Invalid License Plate Effective To format-"+tvlPlateDet.getPlateEffectiveTo()+lineNo);
                	invalidRecord=true;
        		}else if (!commonUtil.isValidDateTimeInDetail(tvlPlateDet.getPlateEffectiveTo())) {
					log.error("Invalid BTVL detail, License Plate Effective To - "
							+ tvlPlateDet.getPlateEffectiveTo() + lineNo);
					addErrorMsg(DETAIL_RECORD_TYPE, "License Plate Effective To",
							"Invalid License Plate Effective To-" + tvlPlateDet.getPlateEffectiveTo() + lineNo);
					invalidRecord = true;
				}
        		
			}
        	
        }
        
        TVLAccountDetails tvlAccountDetails = tvlTagDetails.getTvlAccountDetails();
    	
    	if(tvlAccountDetails != null) {
    		
    		pattern = Pattern.compile(NIOPConstants.BTVL_DTL_ACCOUNT_NUMBER_FORMAT);
    		if(tvlAccountDetails.getAccountNumber()!=null && !pattern.matcher(tvlAccountDetails.getAccountNumber()).matches()) {
    			log.error("Invalid BTVL detail, Account Number - "+tvlAccountDetails.getAccountNumber()+lineNo);
            	addErrorMsg(DETAIL_RECORD_TYPE,"Account Number","Invalid Account Number -"+tvlAccountDetails.getAccountNumber()+lineNo);
            	invalidRecord=true;
    		}
    		
    		pattern = Pattern.compile(NIOPConstants.BTVL_DTL_FLEET_INDICATOR_FORMAT);
    		if(tvlAccountDetails.getFleetIndicator()!=null && !pattern.matcher(tvlAccountDetails.getFleetIndicator()).matches()) {
    			log.error("Invalid BTVL detail, Fleet Indicator - "+tvlAccountDetails.getFleetIndicator()+lineNo);
            	addErrorMsg(DETAIL_RECORD_TYPE,"Fleet Indicator","Invalid Fleet Indicator-"+tvlAccountDetails.getFleetIndicator()+lineNo);
            	invalidRecord=true;
    		}
    	}
        if(invalidRecord) {
			invalidRecordCount++;
		}
        
        return invalidRecord;
	}

	private boolean headerValidation(TVLHeader tvlHeader, FileValidationParam validateParam,String fileName) {
		
		boolean invalidHeaderRecord = false;
		
		if (!tvlHeader.getSubmittedFileType().equals("STVL")) {
        	addErrorMsg(HEADER_RECORD_TYPE,"SubmissionType"," Invalid SubmissionType   \t ::"+tvlHeader.getSubmittedFileType());
        	log.error("Invalid SubmissionType   \t ::"+tvlHeader.getSubmittedFileType());
        	invalidHeaderRecord = true;
        }
		
		if (!(tvlHeader.getSubmittedDateTime().length() == 20) &&
                !(tvlHeader.getSubmittedDateTime().matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z"))) {
        	addErrorMsg(HEADER_RECORD_TYPE,"SubmissionDateTime"," Invalid SubmissionDateTime   \t ::"+tvlHeader.getSubmittedDateTime());
        	log.error("Invalid SubmissionDateTime   \t ::"+tvlHeader.getSubmittedDateTime());
        	invalidHeaderRecord = true;
        }
		
		if (!tvlHeader.getSSIOPHubIdNumber().matches("\\d{4}")
				|| NiopValidationController.allCscIdNiopAgencyMap.get(validateParam.getFromAgency()) == null
				|| !tvlHeader.getSSIOPHubIdNumber().equals(
						String.valueOf(NiopValidationController.allCscIdNiopAgencyMap.get(validateParam.getFromAgency()).getHubId()))) {
			addErrorMsg(HEADER_RECORD_TYPE, "SSIOPHubID",
					" Invalid SSIOPHUBID   \t ::" + tvlHeader.getSSIOPHubIdNumber());
			log.error("Header validation failed, Invalid SSIOPHUBID  :: " + tvlHeader.getSSIOPHubIdNumber());
			invalidHeaderRecord = true;
		}
		
		if(!tvlHeader.getHomeAgencyIdNumber().matches("\\d{4}") || !tvlHeader.getHomeAgencyIdNumber().equals(validateParam.getFromAgency())){
			addErrorMsg(HEADER_RECORD_TYPE,"HomeAgencyID"," Invalid HomeAgencyID   \t ::"+tvlHeader.getHomeAgencyIdNumber());
            log.error("Header validation failed, Invalid HomeAgencyID  :: " +tvlHeader.getHomeAgencyIdNumber());
            invalidHeaderRecord = true;
        }
		//below condition for BTVL file
		if (!tvlHeader.getBulkInd().equals("B") && validateParam.getFileType().equals(NIOPConstants.BTVL_FILE_TYPE)) {
			addErrorMsg(HEADER_RECORD_TYPE,"BulkIndicator"," Invalid BulkIndicator   \t ::"+tvlHeader.getBulkInd());
            log.error("Header validation failed, Invalid BulkIndicator  :: " +tvlHeader.getBulkInd());
            invalidHeaderRecord = true;

		}
		//below condition for DTVL file
		if (!tvlHeader.getBulkInd().equals("D") && validateParam.getFileType().equals(NIOPConstants.DTVL_FILE_TYPE)) {
			addErrorMsg(HEADER_RECORD_TYPE,"BulkIndicator"," Invalid BulkIndicator   \t ::"+tvlHeader.getBulkInd());
            log.error("Header validation failed, Invalid BulkIndicator  :: " +tvlHeader.getBulkInd());
            invalidHeaderRecord = true;

		}
		
		if(!tvlHeader.getBulkIdentifierValue().matches("\\d{1,9}")){
			addErrorMsg(HEADER_RECORD_TYPE,"BulkIdentifier"," Invalid BulkIdentifier   \t ::"+tvlHeader.getBulkIdentifierValue());
            log.error("Header validation failed, Invalid BulkIdentifier  :: " +tvlHeader.getBulkIdentifierValue());
            invalidHeaderRecord = true;
        }
		
		if(!tvlHeader.getTotalRecordCount().matches("\\d{1,10}")){
			addErrorMsg(HEADER_RECORD_TYPE,"RecordCount"," Invalid RecordCount   \t ::"+tvlHeader.getTotalRecordCount());
            log.error("Header validation failed, Invalid RecordCount  :: " +tvlHeader.getTotalRecordCount());
            invalidHeaderRecord = true;
        }
        
		if(invalidHeaderRecord) {
			String ackFileName = NiopValidationController.allCscIdNiopAgencyMap.get(validateParam.getToAgency()).getHubId() + "_" + validateParam.getFromAgency() + "_" + fileName.split("[.]")[0] + "_" + "07"
					+ "_" + NIOPConstants.BTVL_FILE_TYPE + NIOPConstants.ACK_FILE_EXTENSION;
			niopAckMapper.setNiopAckFile(validateParam, "STVL", commonUtil.convertFileDateToUTCDateFormat(fileName.substring(10,24)), "07", ackFileName);
       	 return false;
        }
		return true;
	}
	private void addErrorMsg(String fileType,String fieldName,String errorMsg) {
		controller.getErrorMsglist().add(new ErrorMsgDetail(fileType,fieldName,errorMsg));
	}
	
}
