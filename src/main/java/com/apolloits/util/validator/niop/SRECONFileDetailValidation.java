package com.apolloits.util.validator.niop;

import static com.apolloits.util.IAGConstants.DETAIL_RECORD_TYPE;
import static com.apolloits.util.IAGConstants.FILE_RECORD_TYPE;
import static com.apolloits.util.IAGConstants.HEADER_RECORD_TYPE;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
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
import com.apolloits.util.modal.niop.srecon.ReconciliationData;
import com.apolloits.util.modal.niop.srecon.ReconciliationHeader;
import com.apolloits.util.modal.niop.srecon.ReconciliationRecord;
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
public class SRECONFileDetailValidation {

	@Autowired
	CommonUtil commonUtil;
	
	@Autowired
	private NiopAckFileMapper niopAckMapper;
	
	@Autowired
	@Lazy
	NiopValidationController controller;
	
	
	int invalidRecordCount = 0;
	
	public boolean sreconValidation(FileValidationParam validateParam) throws IOException, JAXBException {
		log.info("Inside STRANValidation started :" + validateParam.getInputFilePath());
		invalidRecordCount = 0;
		String ackFileName = null;
		long start = System.currentTimeMillis();
		File file = new File(validateParam.getInputFilePath());
		ReconciliationData reconData;
	/*	try {
			//POC for SRECON reading 
			//File xmlfile = new File(zipFile.getFile().getParent()+File.separator+fileName);
			JAXBContext jaxbContext = JAXBContext.newInstance(ReconciliationData.class);
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			reconData = (ReconciliationData) unmarshaller.unmarshal(xmlfile);
			System.out.println("SRECON List getReconciliationHeader:: " + reconData.getReconciliationHeader());
			System.out.println("SRECON List getReconRecordList:: " + reconData.getReconciliationDetail().getReconRecordList().get(0)); 
			
		} catch (Exception e) {
			e.printStackTrace();
		}*/
		if(commonUtil.validateNiopTranZIPFileName(file.getName(),validateParam)) {
			 log.info("Zip file name validaton true");
			 
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
    		 //inside ZIP file name validation
    		 if(commonUtil.validateNiopTranFileName(fileName,validateParam)) {
    			 if(validateParam.getValidateType().equals("filename")) {
    				 validateParam.setResponseMsg("File name validation is sucess");
    				 return true;
    			 }
    		 }else {
    			 log.error("SRECON File validation failed ::"+fileName);
    			 return false;
    		 }
    		 
    		 try {
 				File xmlfile = new File(zipFile.getFile().getParent()+File.separator+fileName);
 				JAXBContext jaxbContext = JAXBContext.newInstance(ReconciliationData.class);
 				Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
 				reconData = (ReconciliationData) unmarshaller.unmarshal(xmlfile);
 				System.out.println("SRECON List getReconciliationHeader:: " + reconData.getReconciliationHeader());
 				System.out.println("SRECON List getReconRecordList:: " + reconData.getReconciliationDetail().getReconRecordList().get(0)); 
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
    		 
			boolean headerValidtionFlag = headerValidation(reconData.getReconciliationHeader(), validateParam, fileName);

			// if validating header only
			if (validateParam.getValidateType().equals("header")) {
				log.info("Only file name and header validation");
				if (headerValidtionFlag) {
					ackFileName = NiopValidationController.allCscIdNiopAgencyMap.get(validateParam.getToAgency())
							.getHubId() + "_" + validateParam.getToAgency() + "_" + fileName.split("[.]")[0] + "_"
							+ "00" + "_" + validateParam.getFileType() + NIOPConstants.ACK_FILE_EXTENSION;
					niopAckMapper.setNiopAckFile(validateParam, validateParam.getFileType(),
							reconData.getReconciliationHeader().getSubmissionDateTime(), "00", ackFileName);
				}
				return headerValidtionFlag;
			}
			
			//start to validate detail records
			 boolean detailValidtionFlag = detailValidation(reconData,validateParam,fileName);
			 if(controller.getErrorMsglist().size()>0 && invalidRecordCount >0) {
				 ackFileName = NiopValidationController.allCscIdNiopAgencyMap.get(validateParam.getToAgency()).getHubId() + "_" + validateParam.getToAgency() + "_" + fileName.split("[.]")[0] + "_" + "02"
							+ "_" + validateParam.getFileType() + NIOPConstants.ACK_FILE_EXTENSION;
					niopAckMapper.setNiopAckFile(validateParam, validateParam.getFileType(), reconData.getReconciliationHeader().getSubmissionDateTime(), "02", ackFileName);	
			 }else if(controller.getErrorMsglist().size() == 0 && invalidRecordCount == 0) {
				 ackFileName = NiopValidationController.allCscIdNiopAgencyMap.get(validateParam.getToAgency()).getHubId() + "_" + validateParam.getToAgency() + "_" + fileName.split("[.]")[0] + "_" + "00"
							+ "_" + validateParam.getFileType() + NIOPConstants.ACK_FILE_EXTENSION;
				 niopAckMapper.setNiopAckFile(validateParam, validateParam.getFileType(), reconData.getReconciliationHeader().getSubmissionDateTime(), "00", ackFileName);
				 //validateParam.setResponseMsg("\t <b>ACK file Name :</b>"+ackFileName);
			 }
			 
    		 
		}else {
			 log.error("ZIP file validation failed");
			 return false;
		 }
		

		long end = System.currentTimeMillis();
		log.info("File processed time :: " + (end - start) / 1000f + " seconds");
		return true;
	}
	
	private boolean detailValidation(ReconciliationData reconData, FileValidationParam validateParam,
			String fileName) {
		log.info("Detail Validation started :: "+fileName);
		if(!reconData.getReconciliationHeader().getRecordCount().equals(String.valueOf(reconData.getReconciliationDetail().getReconRecordList().size()))) {
			addErrorMsg(HEADER_RECORD_TYPE,"RecordCount"," Invalid Header RecordCount   \t ::"+reconData.getReconciliationHeader().getRecordCount()+"\t Detail Count :: \t"+reconData.getReconciliationDetail().getReconRecordList().size());
			String ackFileName = NiopValidationController.allCscIdNiopAgencyMap.get(validateParam.getToAgency()).getHubId() + "_" + validateParam.getToAgency() + "_" + fileName.substring(0,29) + "_" + "01"
					+ "_" + validateParam.getFileType() + NIOPConstants.ACK_FILE_EXTENSION;
			niopAckMapper.setNiopAckFile(validateParam, validateParam.getFileType(), reconData.getReconciliationHeader().getSubmissionDateTime(), "01", ackFileName);
       	 	log.error("Header record and detail record count are not matched");
			return false;
		}
		HashSet<String> trxSerialIdSet = new HashSet<>();
		HashMap<String, Integer> duplicateTrxSerialIdMap = new HashMap<>();
		List<ReconciliationRecord> reconRecord = reconData.getReconciliationDetail().getReconRecordList();
		log.info("getTransactionDetail size ::"+reconData.getReconciliationDetail().getReconRecordList().size());
		
		for(int count=0; count<reconRecord.size();count++) {
			validateRecord(reconRecord.get(count),validateParam,fileName);
			validateDuplicateTrxSerialNo(reconRecord.get(count).getTxnReferenceID()+"-"+reconRecord.get(count).getAdjustmentCount(),trxSerialIdSet,duplicateTrxSerialIdMap);// Find duplicate Transaction serial number
		}
		
		log.info("duplicateTrxSerialIdMap ::"+ duplicateTrxSerialIdMap);
		if (duplicateTrxSerialIdMap.size() > 0) {
			addErrorMsg(DETAIL_RECORD_TYPE, "Txn Reference ID", " Duplicate <b> Txn Reference ID :: </b>" + duplicateTrxSerialIdMap);
		}
		return false;
	}
	
	private void validateDuplicateTrxSerialNo(String txnReferenceID, HashSet<String> tagSet,
			HashMap<String, Integer> duplicateTagMap) {

		log.info("txnReferenceID ::"+txnReferenceID);

		if (!tagSet.add(txnReferenceID)) {
			if (duplicateTagMap.containsKey(txnReferenceID)) {
				duplicateTagMap.put(txnReferenceID, duplicateTagMap.get(txnReferenceID).intValue() + 1);
			} else {
				duplicateTagMap.put(txnReferenceID, 1);
			}
			invalidRecordCount++;
		}
		log.info("Duplicate TxnReferenceID Map ::" + duplicateTagMap);
		log.info("set size :::" + tagSet.size());
		log.info("Invalid record count ::" + invalidRecordCount);

	}
	
	private boolean validateRecord(ReconciliationRecord reconRecord, FileValidationParam validateParam,
			String fileName) {
		String lineNo = "\t <b>TxnReferenceID ::</b>"+reconRecord.getTxnReferenceID() ;
		boolean invalidRecord = false;
		
		//Txn Reference ID
		Pattern pattern = Pattern.compile(NIOPConstants.TXN_REFERENCE_ID_FORMAT);
		if (reconRecord.getTxnReferenceID() == null || !pattern.matcher(reconRecord.getTxnReferenceID()).matches()) {
			log.error("Invalid SRECON detail, Txn Reference ID - " + reconRecord.getTxnReferenceID() + lineNo);
			addErrorMsg(DETAIL_RECORD_TYPE, "Txn Reference ID",
					"Invalid Txn Reference ID - " + reconRecord.getTxnReferenceID() + lineNo);
			invalidRecord = true;
		}
		//Adjustment Count
		pattern = Pattern.compile(NIOPConstants.RECON_ADJ_RESUBMIT_COUNT_FORMAT);
		if (reconRecord.getAdjustmentCount() == null || !pattern.matcher(reconRecord.getAdjustmentCount()).matches()) {
			log.error("Invalid SRECON detail, Adjustment Count - " + reconRecord.getAdjustmentCount() + lineNo);
			addErrorMsg(DETAIL_RECORD_TYPE, "Adjustment Count",
					"Invalid Adjustment Count - " + reconRecord.getAdjustmentCount() + lineNo);
			invalidRecord = true;
		}
		//Resubmit Count
		if (reconRecord.getResubmitCount() == null || !pattern.matcher(reconRecord.getResubmitCount()).matches()) {
			log.error("Invalid SRECON detail, Resubmit Count - " + reconRecord.getResubmitCount() + lineNo);
			addErrorMsg(DETAIL_RECORD_TYPE, "Resubmit Count",
					"Invalid Resubmit Count - " + reconRecord.getResubmitCount() + lineNo);
			invalidRecord = true;
		}
		//Reconciliation Home Agency ID
		
		if (reconRecord.getReconHomeAgencyID() == null || !reconRecord.getReconHomeAgencyID().matches(NIOPConstants.AGENCY_ID_FORMAT)
				|| NiopValidationController.allCscIdNiopAgencyMap.get(validateParam.getFromAgency()) == null
				|| !reconRecord.getReconHomeAgencyID().equals(validateParam.getFromAgency())) {
			addErrorMsg(DETAIL_RECORD_TYPE, "Recon Home Agency ID",
					" Invalid Recon Home Agency ID   \t ::" + reconRecord.getReconHomeAgencyID()+ lineNo);
			log.error("Header validation failed, Invalid Recon Home Agency ID  :: " + reconRecord.getReconHomeAgencyID()+ lineNo);
			invalidRecord = true;
		}
		
		//Home Agency Reference ID
		pattern = Pattern.compile(NIOPConstants.TXN_REFERENCE_ID_FORMAT);
		if(reconRecord.getHomeAgencyTxnRefID() != null && !pattern.matcher(reconRecord.getHomeAgencyTxnRefID()).matches()) {
			log.error("Invalid SRECON detail, Home Agency Reference ID - " + reconRecord.getHomeAgencyTxnRefID() + lineNo);
			addErrorMsg(DETAIL_RECORD_TYPE, "Home Agency Reference ID",
					"Invalid Home Agency Reference ID - " + reconRecord.getHomeAgencyTxnRefID() + lineNo);
			invalidRecord = true;
		}
		//Posting Disposition
		pattern = Pattern.compile(NIOPConstants.RECON_POSTING_DISPOSITION_VALUE);
		if(reconRecord.getPostingDisposition() == null || !pattern.matcher(reconRecord.getPostingDisposition()).matches()) {
			log.error("Invalid SRECON detail, Posting Disposition - " + reconRecord.getPostingDisposition() + lineNo);
			addErrorMsg(DETAIL_RECORD_TYPE, "Posting Disposition",
					"Invalid Posting Disposition - " + reconRecord.getPostingDisposition() + lineNo);
			invalidRecord = true;
		}
		//Posted Discount Plan
		pattern = Pattern.compile(NIOPConstants.BTVL_DTL_DISCOUNT_PLAN_FORMAT);
		if (reconRecord.getDiscountPlanType() != null && !pattern.matcher(reconRecord.getDiscountPlanType()).matches()) {
			log.error("Invalid SRECON detail, Invalid Posted Discount Plan - " + reconRecord.getDiscountPlanType() + lineNo);
			addErrorMsg(DETAIL_RECORD_TYPE, "Posted Discount Plan", "Invalid Discount Plan  - " + reconRecord.getDiscountPlanType() + lineNo);
			invalidRecord = true;
		}
		//Posted Amount
		pattern = Pattern.compile(NIOPConstants.RECON_TOLL_AMOUNT);
		if (reconRecord.getPostedAmount() == null || !pattern.matcher(reconRecord.getPostedAmount()).matches()) {
			log.error("Invalid SREON detail, Invalid Posted Amount - " + reconRecord.getPostedAmount() + lineNo);
			addErrorMsg(DETAIL_RECORD_TYPE, "TollAmount", "Invalid Posted Amount  - " + reconRecord.getPostedAmount() + lineNo);
			invalidRecord = true;
		}
		//Posted Date/Time
		pattern = Pattern.compile(NIOPConstants.UTC_DATE_YEAR_REGEX);
		if(reconRecord.getPostedDateTime() ==null || !pattern.matcher(reconRecord.getPostedDateTime()).matches()) {
			log.error("Invalid STRAN detail, Posted Date/Time - " + reconRecord.getPostedDateTime() + lineNo);
			addErrorMsg(DETAIL_RECORD_TYPE, "Posted Date/Time",
					"Invalid Posted Date/Time - " + reconRecord.getPostedDateTime() + lineNo);
			invalidRecord = true;
		}
		//Transaction Flat Fee
		pattern = Pattern.compile(NIOPConstants.RECON_FLAT_PERCENTAGE_FEE_FORMAT);
		if(reconRecord.getTransFlatFee() ==null || !pattern.matcher(reconRecord.getTransFlatFee()).matches()) {
			log.error("Invalid SRECON detail, Transaction Flat Fee - " + reconRecord.getTransFlatFee() + lineNo);
			addErrorMsg(DETAIL_RECORD_TYPE, "Transaction Flat Fee",
					"Invalid Transaction Flat Fee - " + reconRecord.getTransFlatFee() + lineNo);
			invalidRecord = true;
		}
		
		// Transaction Percentage Fee
		pattern = Pattern.compile(NIOPConstants.RECON_FLAT_PERCENTAGE_FEE_FORMAT);
		if (reconRecord.getTransPercentFee() == null || !pattern.matcher(reconRecord.getTransPercentFee()).matches()) {
			log.error("Invalid SRECON detail, Transaction Percentage Fee - " + reconRecord.getTransPercentFee() + lineNo);
			addErrorMsg(DETAIL_RECORD_TYPE, "Transaction Percentage Fee",
					"Invalid Transaction Percentage Fee - " + reconRecord.getTransPercentFee() + lineNo);
			invalidRecord = true;
		}
		
		// Spare 1
		pattern = Pattern.compile(NIOPConstants.RECON_SPARE_FORMAT);
		if (reconRecord.getSpare1() != null && !pattern.matcher(reconRecord.getSpare1()).matches()) {
			log.error("Invalid STRAN detail, Invalid Spare 1 - " + reconRecord.getSpare1() + lineNo);
			addErrorMsg(DETAIL_RECORD_TYPE, "Spare 1", "Invalid Spare 1  - " + reconRecord.getSpare1() + lineNo);
			invalidRecord = true;
		}
		
		//Spare2
		if (reconRecord.getSpare2() != null && !pattern.matcher(reconRecord.getSpare2()).matches()) {
			log.error("Invalid SRECON detail, Invalid Spare 2 - " + reconRecord.getSpare2() + lineNo);
			addErrorMsg(DETAIL_RECORD_TYPE, "Spare 2", "Invalid Spare 2  - " + reconRecord.getSpare2() + lineNo);
			invalidRecord = true;
		}
		
		//Spare3
		if (reconRecord.getSpare3() != null && !pattern.matcher(reconRecord.getSpare3()).matches()) {
			log.error("Invalid SRECON detail, Invalid Spare 3 - " + reconRecord.getSpare3() + lineNo);
			addErrorMsg(DETAIL_RECORD_TYPE, "Spare 3", "Invalid Spare 3  - " + reconRecord.getSpare3() + lineNo);
			invalidRecord = true;
		}
		
		//Spare4
		if (reconRecord.getSpare4() != null && !pattern.matcher(reconRecord.getSpare4()).matches()) {
			log.error("Invalid SRECON detail, Invalid Spare 4 - " + reconRecord.getSpare4() + lineNo);
			addErrorMsg(DETAIL_RECORD_TYPE, "Spare 4", "Invalid Spare 4  - " + reconRecord.getSpare4() + lineNo);
			invalidRecord = true;
		}
		
		//Spare5
		if (reconRecord.getSpare5() != null && !pattern.matcher(reconRecord.getSpare5()).matches()) {
			log.error("Invalid SRECON detail, Invalid Spare 5 - " + reconRecord.getSpare5() + lineNo);
			addErrorMsg(DETAIL_RECORD_TYPE, "Spare 5", "Invalid Spare 5  - " + reconRecord.getSpare5() + lineNo);
			invalidRecord = true;
		}
		
		if(invalidRecord) {
			invalidRecordCount++;
		}
		   return invalidRecord;
	}
	
	private boolean headerValidation(ReconciliationHeader headerReconData, FileValidationParam validateParam, String fileName) {
		boolean invalidHeaderRecord = false;
		log.info("SRECON Header validation started :: "+headerReconData);
		if (!headerReconData.getSubmissionType().equals(validateParam.getFileType())) {
        	addErrorMsg(HEADER_RECORD_TYPE,"SubmissionType"," Invalid SubmissionType   \t ::"+headerReconData.getSubmissionType());
        	log.error("Invalid SubmissionType   \t ::"+headerReconData.getSubmissionType());
        	invalidHeaderRecord = true;
        }
		
		if (!(headerReconData.getSubmissionDateTime().length() == 20) ||
                !(headerReconData.getSubmissionDateTime().matches(NIOPConstants.UTC_DATE_YEAR_REGEX))) {
        	addErrorMsg(HEADER_RECORD_TYPE,"SubmissionDateTime"," Invalid SubmissionDateTime   \t ::"+headerReconData.getSubmissionDateTime());
        	log.error("Invalid SubmissionDateTime   \t ::"+headerReconData.getSubmissionDateTime());
        	invalidHeaderRecord = true;
        }
		
		if (!headerReconData.getSsiopHubID().matches(NIOPConstants.AGENCY_ID_FORMAT)
				|| NiopValidationController.allCscIdNiopAgencyMap.get(validateParam.getFromAgency()) == null
				|| !headerReconData.getSsiopHubID().equals(
						String.valueOf(NiopValidationController.allCscIdNiopAgencyMap.get(validateParam.getFromAgency()).getHubId()))) {
			addErrorMsg(HEADER_RECORD_TYPE, "SSIOPHubID",
					" Invalid SSIOPHUBID   \t ::" + headerReconData.getSsiopHubID());
			log.error("Header validation failed, Invalid SSIOPHUBID  :: " + headerReconData.getSsiopHubID());
			invalidHeaderRecord = true;
		}
		
		if(!headerReconData.getAwayAgencyID().matches(NIOPConstants.AGENCY_ID_FORMAT) || !headerReconData.getAwayAgencyID().equals(validateParam.getFromAgency())){
			addErrorMsg(HEADER_RECORD_TYPE,"AwayAgencyID"," Invalid AwayAgencyID   \t ::"+headerReconData.getAwayAgencyID());
            log.error("Header validation failed, Invalid AwayAgencyID  :: " +headerReconData.getAwayAgencyID());
            invalidHeaderRecord = true;
        }
		
		if(!headerReconData.getHomeAgencyID().matches(NIOPConstants.AGENCY_ID_FORMAT) || !headerReconData.getHomeAgencyID().equals(validateParam.getToAgency())){
			addErrorMsg(HEADER_RECORD_TYPE,"HomeAgencyID"," Invalid HomeAgencyID   \t ::"+headerReconData.getHomeAgencyID());
            log.error("Header validation failed, Invalid HomeAgencyID  :: " +headerReconData.getHomeAgencyID());
            invalidHeaderRecord = true;
        }
		
		if(!headerReconData.getTxnDataSeqNo().matches(NIOPConstants.TXN_DATA_SEQ_NO_FORMAT)){
			addErrorMsg(HEADER_RECORD_TYPE,"TxnDataSeqNo"," Invalid TxnDataSeqNo   \t ::"+headerReconData.getTxnDataSeqNo());
            log.error("Header validation failed, Invalid TxnDataSeqNo  :: " +headerReconData.getTxnDataSeqNo());
            invalidHeaderRecord = true;
        }
		
		if(!headerReconData.getRecordCount().matches(NIOPConstants.TXN_RECORD_COUNT_FORMAT)){
			addErrorMsg(HEADER_RECORD_TYPE,"Record Count"," Invalid Record Count   \t ::"+headerReconData.getRecordCount());
            log.error("Header validation failed, Invalid RecordCount  :: " +headerReconData.getRecordCount());
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
