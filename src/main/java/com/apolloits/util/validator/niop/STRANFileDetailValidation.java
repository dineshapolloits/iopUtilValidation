package com.apolloits.util.validator.niop;

import static com.apolloits.util.IAGConstants.FILE_RECORD_TYPE;
import static com.apolloits.util.IAGConstants.HEADER_RECORD_TYPE;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.apolloits.util.NIOPConstants;
import com.apolloits.util.NiopAckFileMapper;
import com.apolloits.util.controller.NiopValidationController;
import com.apolloits.util.modal.ErrorMsgDetail;
import com.apolloits.util.modal.FileValidationParam;
import com.apolloits.util.modal.niop.stran.TransactionData;
import com.apolloits.util.modal.niop.stran.TransactionHeader;
import com.apolloits.util.modal.niop.stran.TransactionRecord;
import com.apolloits.util.utility.CommonUtil;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;

@Slf4j
@Component
public class STRANFileDetailValidation {
	
	@Autowired
	CommonUtil commonUtil;
	
	@Autowired
	private NiopAckFileMapper niopAckMapper;
	
	@Autowired
	@Lazy
	NiopValidationController controller;
	
	int invalidRecordCount = 0;
	
	public boolean starnValidation(FileValidationParam validateParam) throws IOException, JAXBException {
		log.info("Inside STRANValidation started :"+validateParam.getInputFilePath());
		invalidRecordCount = 0;
		 String ackFileName = null;
		 long start = System.currentTimeMillis();
		 File file = new File(validateParam.getInputFilePath());
		 if(commonUtil.validateNiopTranZIPFileName(file.getName(),validateParam)) {
			 
			 log.info("Zip file name validaton true");
			 TransactionData tranData = null;
			 //start unzip file
			 String fileName="";
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
				ackFileName = NiopValidationController.allCscIdNiopAgencyMap.get(validateParam.getToAgency()).getHubId() + "_" + validateParam.getToAgency() + "_" + fileName.split("[.]")[0] + "_" + "07"
						+ "_" + validateParam.getFileType() + NIOPConstants.ACK_FILE_EXTENSION;
				log.info("ACK File Name ::" + ackFileName);
				niopAckMapper.setNiopAckFile(validateParam, validateParam.getFileType(), commonUtil.convertFileDateToUTCDateFormat(fileName.substring(15,29)), "07", ackFileName);
				 validateParam.setResponseMsg("FAILED Reason:: ZIP file extraction failed");
        		 return false;
			}
    		 if(commonUtil.validateNiopTranFileName(fileName,validateParam)) {
    			 if(validateParam.getValidateType().equals("filename")) {
    				 validateParam.setResponseMsg("File name validation is sucess");
    				 return true;
    			 }
    		 }else {
    			 log.error("STRAN File validation failed ::"+fileName);
    			 return false;
    		 }
    		 
    		 try {
				File xmlfile = new File(zipFile.getFile().getParent()+File.separator+fileName);
				JAXBContext jaxbContext = JAXBContext.newInstance(TransactionData.class);
				Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
				tranData = (TransactionData) unmarshaller.unmarshal(xmlfile);
				System.out.println("TVL List getTvlHeader:: " + tranData.getTransactionHeader());
				System.out.println("TVL List getTvlDetail:: " + tranData.getTransactionDetail().getTransactionRecord().get(0)); 
			} catch (Exception e) {
				e.printStackTrace();
				validateParam.setResponseMsg("Invalid XML file. Please check XML format");
				log.error("Invalid XML file. Please check XML format");
				ackFileName = NiopValidationController.allCscIdNiopAgencyMap.get(validateParam.getToAgency()).getHubId() + "_" + validateParam.getToAgency() + "_" + fileName.split("[.]")[0] + "_" + "07"
						+ "_" + validateParam.getFileType() + NIOPConstants.ACK_FILE_EXTENSION;
				addErrorMsg(FILE_RECORD_TYPE,"Format"," Invalid XML format   ");						
				niopAckMapper.setNiopAckFile(validateParam, validateParam.getFileType(), commonUtil.convertFileDateToUTCDateFormat(fileName.substring(15,29)), "07", ackFileName);
				
				return false;
			}
    		 boolean headerValidtionFlag = headerValidation(tranData.getTransactionHeader(),validateParam,fileName);
    		 
    		 //if validating header only
			 if(validateParam.getValidateType().equals("header")) {
				 log.info("Only file name and header validation");
				 if(headerValidtionFlag) {
					 ackFileName = NiopValidationController.allCscIdNiopAgencyMap.get(validateParam.getToAgency()).getHubId() + "_" + validateParam.getToAgency() + "_" + fileName.split("[.]")[0] + "_" + "00"
								+ "_" + validateParam.getFileType() + NIOPConstants.ACK_FILE_EXTENSION;
					 niopAckMapper.setNiopAckFile(validateParam, validateParam.getFileType(), tranData.getTransactionHeader().getSubmissionDateTime(), "00", ackFileName);
				 }
				 return headerValidtionFlag;
			 }
			 //start to validate detail records
			 boolean detailValidtionFlag = detailValidation(tranData,validateParam,fileName);
			 
		 }else {
			 log.error("ZIP file validation failed");
			 return false;
		 }
      
        long end = System.currentTimeMillis();
		log.info("File processing time :: "+ (end - start) / 1000f + " seconds");
		 return true;
	}
	
	private boolean detailValidation(TransactionData tranData, FileValidationParam validateParam,
			String fileName) {
		log.info("Detail Validation started :: "+fileName);
		if(!tranData.getTransactionHeader().getRecordCount().equals(String.valueOf(tranData.getTransactionDetail().getTransactionRecord().size()))) {
			addErrorMsg(HEADER_RECORD_TYPE,"RecordCount"," Invalid Header RecordCount   \t ::"+tranData.getTransactionHeader().getRecordCount()+"\t Detail Count :: \t"+tranData.getTransactionDetail().getTransactionRecord().size());
			String ackFileName = NiopValidationController.allCscIdNiopAgencyMap.get(validateParam.getToAgency()).getHubId() + "_" + validateParam.getToAgency() + "_" + fileName.substring(0,24) + "_" + "01"
					+ "_" + validateParam.getFileType() + NIOPConstants.ACK_FILE_EXTENSION;
			niopAckMapper.setNiopAckFile(validateParam, validateParam.getFileType(), tranData.getTransactionHeader().getSubmissionDateTime(), "01", ackFileName);
       	 	log.error("Header record and detail record count are not matched");
			return false;
		}
		
		List<TransactionRecord> tranRecord = tranData.getTransactionDetail().getTransactionRecord();
		log.info("getTransactionDetail size ::"+tranData.getTransactionDetail().getTransactionRecord().size());
		for(int count=0; count<tranRecord.size();count++) {
			validateRecord(tranRecord.get(count),validateParam,fileName);
		}
		
		return false;
	}

	private void validateRecord(TransactionRecord transactionRecord, FileValidationParam validateParam,
			String fileName) {
		String lineNo = "\t <b>TxnReferenceID ::</b>"+transactionRecord.getTxnReferenceID() ;
		boolean invalidRecord = false;
		
		
	}

	private boolean headerValidation(TransactionHeader headerTranData, FileValidationParam validateParam, String fileName) {
		boolean invalidHeaderRecord = false;
		log.info("STRAN Header validation started :: "+headerTranData);
		if (!headerTranData.getSubmissionType().equals(validateParam.getFileType())) {
        	addErrorMsg(HEADER_RECORD_TYPE,"SubmissionType"," Invalid SubmissionType   \t ::"+headerTranData.getSubmissionType());
        	log.error("Invalid SubmissionType   \t ::"+headerTranData.getSubmissionType());
        	invalidHeaderRecord = true;
        }
		
		if (!(headerTranData.getSubmissionDateTime().length() == 20) ||
                !(headerTranData.getSubmissionDateTime().matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z"))) {
        	addErrorMsg(HEADER_RECORD_TYPE,"SubmissionDateTime"," Invalid SubmissionDateTime   \t ::"+headerTranData.getSubmissionDateTime());
        	log.error("Invalid SubmissionDateTime   \t ::"+headerTranData.getSubmissionDateTime());
        	invalidHeaderRecord = true;
        }
		
		if (!headerTranData.getSsiopHubID().matches(NIOPConstants.AGENCY_ID_FORMAT)
				|| NiopValidationController.allCscIdNiopAgencyMap.get(validateParam.getFromAgency()) == null
				|| !headerTranData.getSsiopHubID().equals(
						String.valueOf(NiopValidationController.allCscIdNiopAgencyMap.get(validateParam.getFromAgency()).getHubId()))) {
			addErrorMsg(HEADER_RECORD_TYPE, "SSIOPHubID",
					" Invalid SSIOPHUBID   \t ::" + headerTranData.getSsiopHubID());
			log.error("Header validation failed, Invalid SSIOPHUBID  :: " + headerTranData.getSsiopHubID());
			invalidHeaderRecord = true;
		}
		
		if(!headerTranData.getAwayAgencyID().matches(NIOPConstants.AGENCY_ID_FORMAT) || !headerTranData.getAwayAgencyID().equals(validateParam.getFromAgency())){
			addErrorMsg(HEADER_RECORD_TYPE,"AwayAgencyID"," Invalid AwayAgencyID   \t ::"+headerTranData.getAwayAgencyID());
            log.error("Header validation failed, Invalid AwayAgencyID  :: " +headerTranData.getAwayAgencyID());
            invalidHeaderRecord = true;
        }
		
		if(!headerTranData.getHomeAgencyID().matches(NIOPConstants.AGENCY_ID_FORMAT) || !headerTranData.getHomeAgencyID().equals(validateParam.getToAgency())){
			addErrorMsg(HEADER_RECORD_TYPE,"HomeAgencyID"," Invalid HomeAgencyID   \t ::"+headerTranData.getHomeAgencyID());
            log.error("Header validation failed, Invalid HomeAgencyID  :: " +headerTranData.getHomeAgencyID());
            invalidHeaderRecord = true;
        }
		
		if(!headerTranData.getTxnDataSeqNo().matches(NIOPConstants.TXN_DATA_SEQ_NO_FORMAT)){
			addErrorMsg(HEADER_RECORD_TYPE,"TxnDataSeqNo"," Invalid TxnDataSeqNo   \t ::"+headerTranData.getTxnDataSeqNo());
            log.error("Header validation failed, Invalid TxnDataSeqNo  :: " +headerTranData.getTxnDataSeqNo());
            invalidHeaderRecord = true;
        }
		
		if(!headerTranData.getRecordCount().matches(NIOPConstants.TXN_RECORD_COUNT_FORMAT)){
			addErrorMsg(HEADER_RECORD_TYPE,"Record Count"," Invalid Record Count   \t ::"+headerTranData.getRecordCount());
            log.error("Header validation failed, Invalid RecordCount  :: " +headerTranData.getRecordCount());
            invalidHeaderRecord = true;
        }
		if(invalidHeaderRecord) {
			String ackFileName = NiopValidationController.allCscIdNiopAgencyMap.get(validateParam.getToAgency()).getHubId() + "_" + validateParam.getToAgency() + "_" + fileName.split("[.]")[0] + "_" + "07"
					+ "_" + validateParam.getFileType() + NIOPConstants.ACK_FILE_EXTENSION;
			niopAckMapper.setNiopAckFile(validateParam, validateParam.getFileType(), commonUtil.convertFileDateToUTCDateFormat(fileName.substring(15,29)), "07", ackFileName);
       	 return false;
        }
		
		return true;
	}

	private void addErrorMsg(String fileType,String fieldName,String errorMsg) {
		controller.getErrorMsglist().add(new ErrorMsgDetail(fileType,fieldName,errorMsg));
	}

}
