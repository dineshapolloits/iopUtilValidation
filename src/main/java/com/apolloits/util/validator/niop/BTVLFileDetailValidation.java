package com.apolloits.util.validator.niop;

import static com.apolloits.util.IAGConstants.HEADER_RECORD_TYPE;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.apolloits.util.NIOPConstants;
import com.apolloits.util.NiopAckFileMapper;
import com.apolloits.util.controller.NiopValidationController;
import com.apolloits.util.modal.ErrorMsgDetail;
import com.apolloits.util.modal.FileValidationParam;
import com.apolloits.util.modal.niop.TVLHeader;
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
	
	public boolean btvlValidation(FileValidationParam validateParam) throws IOException, JAXBException {
		log.info("Inside btvlValidation started :"+validateParam.getInputFilePath());
		 File file = new File(validateParam.getInputFilePath());
		 String ackFileName = null;
		 long start = System.currentTimeMillis();
		 if(commonUtil.validateNiopBtvlZIPFileName(file.getName(),validateParam)) {
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
				ackFileName = NiopValidationController.cscIdTagNiopAgencyMap.get(validateParam.getToAgency()).getHubId() + "_" + validateParam.getFromAgency() + "_" + fileName.split("[.]")[0] + "_" + "07"
						+ "_" + NIOPConstants.BTVL_FILE_TYPE + NIOPConstants.ACK_FILE_EXTENSION;
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
						ackFileName = NiopValidationController.cscIdTagNiopAgencyMap.get(validateParam.getToAgency()).getHubId() + "_" + validateParam.getFromAgency() + "_" + fileName.split("[.]")[0] + "_" + "07"
								+ "_" + NIOPConstants.BTVL_FILE_TYPE + NIOPConstants.ACK_FILE_EXTENSION;
						niopAckMapper.setNiopAckFile(validateParam, "STVL", commonUtil.convertFileDateToUTCDateFormat(fileName.substring(0,24)), "07", ackFileName);
						return false;
					}
    			 
    			 boolean headerValidtionFlag = headerValidation(list.getTvlHeader(),validateParam,fileName);
    			 //if validating header only
    			 if(validateParam.getValidateType().equals("header")) {
    				 log.info("Only file name and header validation");
    				 return headerValidtionFlag;
    			 }
    			 //start to validate detail records
    			 
    		 }else {
    			 log.error("BTVL File validation failed");
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
				|| NiopValidationController.cscIdTagNiopAgencyMap.get(validateParam.getFromAgency()) == null
				|| !tvlHeader.getSSIOPHubIdNumber().equals(
						NiopValidationController.cscIdTagNiopAgencyMap.get(validateParam.getFromAgency()).getHubId())) {
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
		
		if (!tvlHeader.getBulkInd().equals("B") && !tvlHeader.getBulkInd().equals("D")) {
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
		System.out.println("Detail :: cscIdTagNiopAgencyMap ::"+NiopValidationController.cscIdTagNiopAgencyMap);
		String ackFileName = NiopValidationController.cscIdTagNiopAgencyMap.get(validateParam.getFromAgency()).getHubId() + "_" + validateParam.getFromAgency() + "_" + fileName.split("[.]")[0] + "_" + "00"
				+ "_" + NIOPConstants.BTVL_FILE_TYPE + NIOPConstants.ACK_FILE_EXTENSION;
        
		if(invalidHeaderRecord) {
			ackFileName = NiopValidationController.cscIdTagNiopAgencyMap.get(validateParam.getFromAgency()).getHubId() + "_" + validateParam.getFromAgency() + "_" + fileName.split("[.]")[0] + "_" + "01"
					+ "_" + NIOPConstants.BTVL_FILE_TYPE + NIOPConstants.ACK_FILE_EXTENSION;
			niopAckMapper.setNiopAckFile(validateParam, "STVL", commonUtil.convertFileDateToUTCDateFormat(fileName.substring(10,24)), "01", ackFileName);
       	 return false;
        }
		niopAckMapper.setNiopAckFile(validateParam, "STVL", commonUtil.convertFileDateToUTCDateFormat(fileName.substring(0,24)), "00", ackFileName);
		return true;
	}
	private void addErrorMsg(String fileType,String fieldName,String errorMsg) {
		controller.getErrorMsglist().add(new ErrorMsgDetail(fileType,fieldName,errorMsg));
	}
	
}
