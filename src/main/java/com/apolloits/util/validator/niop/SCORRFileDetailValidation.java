package com.apolloits.util.validator.niop;

import static com.apolloits.util.IAGConstants.DETAIL_RECORD_TYPE;
import static com.apolloits.util.IAGConstants.FILE_RECORD_TYPE;
import static com.apolloits.util.IAGConstants.HEADER_RECORD_TYPE;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
import com.apolloits.util.modal.NiopAgencyEntity;
import com.apolloits.util.modal.niop.scorr.CorrectionRecord;
import com.apolloits.util.modal.niop.scorr.OriginalTransactionDetail;
import com.apolloits.util.modal.niop.scorr.ScorrFile;
import com.apolloits.util.modal.niop.scorr.ScorrHeader;
import com.apolloits.util.modal.niop.stran.EntryData;
import com.apolloits.util.modal.niop.stran.PlateInfo;
import com.apolloits.util.modal.niop.stran.TagInfo;
import com.apolloits.util.utility.CommonUtil;
import com.apolloits.util.writer.niop.SCORRTemplateValidationExcelWriter;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;

@Slf4j
@Component
public class SCORRFileDetailValidation {

	@Autowired
	CommonUtil commonUtil;
	
	@Autowired
	private NiopAckFileMapper niopAckMapper;
	
	@Autowired
	@Lazy
	NiopValidationController controller;
	
	int invalidRecordCount = 0;
	
	List<CorrectionRecord> scorrTempList;
	
	@Autowired
	SCORRTemplateValidationExcelWriter scorrtempExcel;
	
	public boolean scorrValidation(FileValidationParam validateParam) throws IOException, JAXBException {
		invalidRecordCount = 0;
		String ackFileName = null;
		long start = System.currentTimeMillis();
		File file = new File(validateParam.getInputFilePath());
		scorrTempList = new ArrayList<>();
		 
			if (commonUtil.validateNiopTranZIPFileName(file.getName(), validateParam)) {

				log.info("Zip file name validaton true");
				
				ScorrFile	scorrFile = null;
				 //start unzip file
				 String fileName="";
	    		 //extract ZIP file 
	    		 ZipFile zipFile = new ZipFile(file);
	    		 try {
					log.info("SCOR extract file name getFileHeaders ******************* "+zipFile.getFileHeaders().get(0).getFileName());
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
	    			 log.error("SCORR File validation failed ::"+fileName);
	    			 return false;
	    		 }
	    		 
					try {
						File xmlfile = new File(zipFile.getFile().getParent() + File.separator + fileName);
						JAXBContext jaxbContext = JAXBContext.newInstance(ScorrFile.class);
						Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
						scorrFile = (ScorrFile) unmarshaller.unmarshal(xmlfile);
						System.out.println("SCORR List getScorrHeader:: " + scorrFile.getScorrHeader());
						System.out.println("SCORR List getCorrectionDetail:: "
								+ scorrFile.getCorrectionDetail().getCorrectionRecordList().get(0));
					} catch (Exception e) {
						e.printStackTrace();
						validateParam.setResponseMsg("Invalid XML file. Please check XML format");
						log.error("Invalid XML file. Please check XML format");
						ackFileName = NiopValidationController.allCscIdNiopAgencyMap.get(validateParam.getToAgency())
								.getHubId() + "_" + validateParam.getToAgency() + "_" + fileName.split("[.]")[0] + "_"
								+ "07" + "_" + validateParam.getFileType() + NIOPConstants.ACK_FILE_EXTENSION;
						addErrorMsg(FILE_RECORD_TYPE, "Format", " Invalid XML format   ");
						niopAckMapper.setNiopAckFile(validateParam, validateParam.getFileType(),
								commonUtil.convertFileDateToUTCDateFormat(fileName.substring(15, 29)), "07",
								ackFileName);

						return false;
					}
					
					 boolean headerValidtionFlag = headerValidation(scorrFile.getScorrHeader(),validateParam,fileName);
		    		 
		    		 //if validating header only
					 if(validateParam.getValidateType().equals("header")) {
						 log.info("Only file name and header validation");
						 if(headerValidtionFlag) {
							 ackFileName = NiopValidationController.allCscIdNiopAgencyMap.get(validateParam.getToAgency()).getHubId() + "_" + validateParam.getToAgency() + "_" + fileName.split("[.]")[0] + "_" + "00"
										+ "_" + validateParam.getFileType() + NIOPConstants.ACK_FILE_EXTENSION;
							 niopAckMapper.setNiopAckFile(validateParam, validateParam.getFileType(), scorrFile.getScorrHeader().getSubmissionDateTime(), "00", ackFileName);
						 }
						 return headerValidtionFlag;
					 }
					 
					//start to validate detail records
					 boolean detailValidtionFlag = detailValidation(scorrFile,validateParam,fileName);
					 
					 if(controller.getErrorMsglist().size()>0 && invalidRecordCount >0) {
						 ackFileName = NiopValidationController.allCscIdNiopAgencyMap.get(validateParam.getToAgency()).getHubId() + "_" + validateParam.getToAgency() + "_" + fileName.split("[.]")[0] + "_" + "02"
									+ "_" + validateParam.getFileType() + NIOPConstants.ACK_FILE_EXTENSION;
							niopAckMapper.setNiopAckFile(validateParam, validateParam.getFileType(), scorrFile.getScorrHeader().getSubmissionDateTime(), "02", ackFileName);	
					 }else if(controller.getErrorMsglist().size() == 0 && invalidRecordCount == 0) {
						 ackFileName = NiopValidationController.allCscIdNiopAgencyMap.get(validateParam.getToAgency()).getHubId() + "_" + validateParam.getToAgency() + "_" + fileName.split("[.]")[0] + "_" + "00"
									+ "_" + validateParam.getFileType() + NIOPConstants.ACK_FILE_EXTENSION;
						 niopAckMapper.setNiopAckFile(validateParam, validateParam.getFileType(), scorrFile.getScorrHeader().getSubmissionDateTime(), "00", ackFileName);
						 //validateParam.setResponseMsg("\t <b>ACK file Name :</b>"+ackFileName);
					 }
					//below condition for creating STRAN excel template
					 if(scorrTempList.size() >0) {
						 String scorrTempExcelFileName =validateParam.getOutputFilePath()+File.separator+FilenameUtils.removeExtension(fileName)+"_SCORRTemplate.xlsx";
						 scorrtempExcel.createScorrTemplateExcel(scorrTempList, scorrTempExcelFileName, validateParam);
					 }
	    		 
	    		 
			} else {
				log.error("ZIP file validation failed");
				return false;
			}

			
			long end = System.currentTimeMillis();
			log.info("File processing time :: " + (end - start) / 1000f + " seconds");
		
		 return true;
	}
	
	private boolean detailValidation(ScorrFile scorrFile, FileValidationParam validateParam,
			String fileName) {
		log.info("Detail Validation started :: "+fileName);
		if(!scorrFile.getScorrHeader().getRecordCount().equals(String.valueOf(scorrFile.getCorrectionDetail().getCorrectionRecordList().size()))) {
			addErrorMsg(HEADER_RECORD_TYPE,"RecordCount"," Invalid Header RecordCount   \t ::"+scorrFile.getScorrHeader().getRecordCount()+"\t Detail Count :: \t"+scorrFile.getCorrectionDetail().getCorrectionRecordList().size());
			String ackFileName = NiopValidationController.allCscIdNiopAgencyMap.get(validateParam.getToAgency()).getHubId() + "_" + validateParam.getToAgency() + "_" + fileName.substring(0,29) + "_" + "01"
					+ "_" + validateParam.getFileType() + NIOPConstants.ACK_FILE_EXTENSION;
			niopAckMapper.setNiopAckFile(validateParam, validateParam.getFileType(), scorrFile.getScorrHeader().getSubmissionDateTime(), "01", ackFileName);
       	 	log.error("Header record and detail record count are not matched");
			return false;
		}
		HashSet<String> trxSerialIdSet = new HashSet<>();
		HashMap<String, Integer> duplicateTrxSerialIdMap = new HashMap<>();
		
		List<CorrectionRecord> corrList = scorrFile.getCorrectionDetail().getCorrectionRecordList();
		log.info("getCorrectionRecordList size ::"+scorrFile.getCorrectionDetail().getCorrectionRecordList().size());
		
		for(int count=0; count<corrList.size();count++) {
			validateRecord(corrList.get(count),validateParam,fileName);
			//validateDuplicateTrxSerialNo(corrList.get(count).getTxnReferenceID(),trxSerialIdSet,duplicateTrxSerialIdMap);// Find duplicate Transaction serial number
			corrList.get(count).setTxnDataSeqNo(scorrFile.getScorrHeader().getTxnDataSeqNo());
			scorrTempList.add(corrList.get(count));
			System.out.println("count:: \t"+count +"tranRecord ::"+ corrList.get(count));
		}
		
		return false;
	}
	
	private boolean validateRecord(CorrectionRecord correctionRecord, FileValidationParam validateParam, String fileName) {
		String lineNo = "\t <b> Original record missing ::</b>";
		if (correctionRecord.getOriginalTransactionDetail() != null) {
			lineNo = "\t <b>OriginalTxnReferenceID ::</b>"
					+ correctionRecord.getOriginalTransactionDetail().getTxnReferenceID();
		}
		boolean invalidRecord = false;
		
		// Record Type
		String recordType = correctionRecord.getRecordType();
		Pattern pattern = Pattern.compile(NIOPConstants.CORR_RECORD_TYPE_FORMAT);
		if (recordType == null || !pattern.matcher(recordType).matches()) {
			log.error("Invalid SCORR detail, Record Type - " + recordType + lineNo);
			addErrorMsg(DETAIL_RECORD_TYPE, "Record Type", "Invalid Record type - " + recordType + lineNo);
			invalidRecord = true;
		}
		//Correction Date/Time
		pattern = Pattern.compile(NIOPConstants.UTC_DATE_REGEX);
		if(correctionRecord.getCorrectionDateTime() ==null || !pattern.matcher(correctionRecord.getCorrectionDateTime()).matches()) {
			log.error("Invalid STRAN detail, Correction Date/Time - " + correctionRecord.getCorrectionDateTime() + lineNo);
			addErrorMsg(DETAIL_RECORD_TYPE, "Correction Date/Time",
					"Invalid Exit Date/Time - " + correctionRecord.getCorrectionDateTime() + lineNo);
			invalidRecord = true;
		}
		//Correction Reason
		pattern = Pattern.compile(NIOPConstants.CORR_REASON);
		if (correctionRecord.getCorrectionReason() != null && !pattern.matcher(correctionRecord.getCorrectionReason()).matches()) {
			log.error("Invalid SCORR detail, Correction Reason - " + correctionRecord.getCorrectionReason() + lineNo);
			addErrorMsg(DETAIL_RECORD_TYPE, "Correction Reason", "Invalid Correction Reason  - " + correctionRecord.getCorrectionReason() + lineNo);
			invalidRecord = true;
		}
		//Resubmit Reason
		pattern = Pattern.compile(NIOPConstants.CORR_RESUBMIT_REASON);
		if (correctionRecord.getResubmitReason() != null && !pattern.matcher(correctionRecord.getResubmitReason()).matches()) {
			log.error("Invalid SCORR detail, Resubmit Reason - " + correctionRecord.getResubmitReason() + lineNo);
			addErrorMsg(DETAIL_RECORD_TYPE, "Resubmit Reason", "Invalid Resubmit Reason  - " + correctionRecord.getResubmitReason() + lineNo);
			invalidRecord = true;
		}
		//Other Correction Description
		pattern = Pattern.compile(NIOPConstants.CORR_OTHER_REASON);
		if (correctionRecord.getCorrectionReason() != null && !pattern.matcher(correctionRecord.getCorrectionReason()).matches()) {
			log.error("Invalid SCORR detail, Other Correction Description - " + correctionRecord.getCorrectionReason() + lineNo);
			addErrorMsg(DETAIL_RECORD_TYPE, "Other Correction Desc", "Invalid Other Correction Description  - " + correctionRecord.getCorrectionReason() + lineNo);
			invalidRecord = true;
		}
		//Correction Count
		pattern = Pattern.compile(NIOPConstants.CORR_RESUBMIT_COUNT_FORMAT);
		if (correctionRecord.getCorrectionSeqNo() != null && !pattern.matcher(correctionRecord.getCorrectionSeqNo()).matches()) {
			log.error("Invalid SCORR detail, Correction Count - " + correctionRecord.getCorrectionSeqNo() + lineNo);
			addErrorMsg(DETAIL_RECORD_TYPE, "Correction Count", "Invalid Correction Count  - " + correctionRecord.getCorrectionSeqNo() + lineNo);
			invalidRecord = true;
		}
		// Resubmit Count
		pattern = Pattern.compile(NIOPConstants.CORR_RESUBMIT_COUNT_FORMAT);
		if (correctionRecord.getResubmitCount() != null
				&& !pattern.matcher(correctionRecord.getResubmitCount()).matches()) {
			log.error("Invalid SCORR detail, Resubmit Count - " + correctionRecord.getResubmitCount() + lineNo);
			addErrorMsg(DETAIL_RECORD_TYPE, "Resubmit Count",
					"Invalid Resubmit Count  - " + correctionRecord.getResubmitCount() + lineNo);
			invalidRecord = true;
		}
		//Home Agency Reference ID
		pattern = Pattern.compile(NIOPConstants.TXN_REFERENCE_ID_FORMAT);
		if(correctionRecord.getHomeAgencyTxnRefID() !=null && !pattern.matcher(correctionRecord.getHomeAgencyTxnRefID()).matches()) {
			log.error("Invalid STRAN detail, Home Agency Reference ID - " + correctionRecord.getHomeAgencyTxnRefID() + lineNo);
			addErrorMsg(DETAIL_RECORD_TYPE, "Home Agency Reference ID",
					"Invalid Home Agency Reference ID - " + correctionRecord.getHomeAgencyTxnRefID() + lineNo);
			invalidRecord = true;
		}
		
		//validate original Transaction Details. All other fields from the Transaction Data Detail 
		
		if(validateOrginalRecord(correctionRecord.getOriginalTransactionDetail(),validateParam)) {
			invalidRecord = true;
		}
		
		if(invalidRecord) {
			correctionRecord.setPostingDisposition("T");
			invalidRecordCount++;
		}
        
        return invalidRecord;
	}

	private boolean validateOrginalRecord(OriginalTransactionDetail transactionRecord,
			FileValidationParam validateParam) {
		//Validate same like STRAN

		String lineNo = "\t <b>TxnReferenceID ::</b>"+transactionRecord.getTxnReferenceID() ;
		boolean invalidRecord = false;
		
		//Record Type
		String recordType = transactionRecord.getRecordType();
		Pattern pattern = Pattern.compile(NIOPConstants.TXN_RECORD_TYPE_FORMAT);
		if(recordType ==null || !pattern.matcher(recordType).matches()) {
			log.error("Invalid SCORR detail, Record Type - " + recordType + lineNo);
			addErrorMsg(DETAIL_RECORD_TYPE, "Record Type",
					"Invalid Record type - " + recordType + lineNo);
			invalidRecord = true;
		}
		//Txn Reference ID
		pattern = Pattern.compile(NIOPConstants.TXN_REFERENCE_ID_FORMAT);
		if(transactionRecord.getTxnReferenceID() ==null || !pattern.matcher(transactionRecord.getTxnReferenceID()).matches()) {
			log.error("Invalid SCORR detail, Txn Reference ID - " + transactionRecord.getTxnReferenceID() + lineNo);
			addErrorMsg(DETAIL_RECORD_TYPE, "Txn Reference ID",
					"Invalid Record type - " + transactionRecord.getTxnReferenceID() + lineNo);
			invalidRecord = true;
		}
		//Exit Date/Time
		pattern = Pattern.compile(NIOPConstants.UTC_DATE_REGEX);
		if(transactionRecord.getExitDateTime() ==null || !pattern.matcher(transactionRecord.getExitDateTime()).matches()) {
			log.error("Invalid SCORR detail, Exit Date/Time - " + transactionRecord.getExitDateTime() + lineNo);
			addErrorMsg(DETAIL_RECORD_TYPE, "Exit Date/Time",
					"Invalid Exit Date/Time - " + transactionRecord.getExitDateTime() + lineNo);
			invalidRecord = true;
		}
		
		//Facility ID
		pattern = Pattern.compile(NIOPConstants.TXN_FACILITY_ID_FORMAT);
		if(transactionRecord.getFacilityID() ==null || !pattern.matcher(transactionRecord.getFacilityID()).matches()) {
			log.error("Invalid SCORR detail, Facility ID - " + transactionRecord.getFacilityID() + lineNo);
			addErrorMsg(DETAIL_RECORD_TYPE, "Facility ID",
					"Invalid Facility ID - " + transactionRecord.getFacilityID() + lineNo);
			invalidRecord = true;
		}
		//Facility Description
		pattern = Pattern.compile(NIOPConstants.TXN_FACILITY_ID_DESC_FORMAT);
		if(transactionRecord.getFacilityDesc() ==null || !pattern.matcher(transactionRecord.getFacilityDesc()).matches()) {
			log.error("Invalid SCORR detail, Facility Description - " + transactionRecord.getFacilityDesc() + lineNo);
			addErrorMsg(DETAIL_RECORD_TYPE, "Facility Description",
					"Invalid Facility Description - " + transactionRecord.getFacilityDesc() + lineNo);
			invalidRecord = true;
		}
		//Exit Plaza
		pattern = Pattern.compile(NIOPConstants.TXN_PLAZA_FORMAT);
		if(transactionRecord.getExitPlaza() ==null || !pattern.matcher(transactionRecord.getExitPlaza()).matches()) {
			log.error("Invalid SCORR detail, Exit Plaza - " + transactionRecord.getExitPlaza() + lineNo);
			addErrorMsg(DETAIL_RECORD_TYPE, "Exit Plaza",
					"Invalid Exit Plaza - " + transactionRecord.getExitPlaza() + lineNo);
			invalidRecord = true;
		}
		//Exit Plaza Description 
		pattern = Pattern.compile(NIOPConstants.TXN_FACILITY_ID_DESC_FORMAT);
		if(transactionRecord.getExitPlazaDesc() ==null || !pattern.matcher(transactionRecord.getExitPlazaDesc()).matches()) {
			log.error("Invalid SCORR detail, Exit Plaza Description  - " + transactionRecord.getExitPlazaDesc() + lineNo);
			addErrorMsg(DETAIL_RECORD_TYPE, "Exit Plaza Description",
					"Invalid Exit Plaza Description  - " + transactionRecord.getExitPlazaDesc() + lineNo);
			invalidRecord = true;
		}
		
		// Exit Lane
		pattern = Pattern.compile(NIOPConstants.TXN_LANE_FORMAT);
		if (transactionRecord.getExitLane() == null
				|| !pattern.matcher(transactionRecord.getExitLane()).matches()) {
			log.error(
					"Invalid SCORR detail, Exit Lane  - " + transactionRecord.getExitLane() + lineNo);
			addErrorMsg(DETAIL_RECORD_TYPE, "Exit Lane",
					"Invalid Exit Lane  - " + transactionRecord.getExitLane() + lineNo);
			invalidRecord = true;
		}
		//below entry validation - Required for TC and VC transactions
		EntryData entryData = transactionRecord.getEntryData();
		pattern = Pattern.compile(NIOPConstants.TXN_ENTRY_TYPE);
		if(recordType != null  && pattern.matcher(recordType).matches()) {
			
			if(entryData!= null) {
				
				//Entry Date/Time
				pattern = Pattern.compile(NIOPConstants.UTC_DATE_REGEX);
				if(entryData.getEntryDateTime() ==null || !pattern.matcher(entryData.getEntryDateTime()).matches()) {
					log.error("Invalid SCORR detail, Entry Date/Time - " + entryData.getEntryDateTime() + lineNo);
					addErrorMsg(DETAIL_RECORD_TYPE, "Entry Date/Time",
							"Invalid Entry Date/Time - " + entryData.getEntryDateTime() + lineNo);
					invalidRecord = true;
				}
				
				//Entry Plaza
				pattern = Pattern.compile(NIOPConstants.TXN_PLAZA_FORMAT);
				if (entryData.getEntryPlaza() == null || !pattern.matcher(entryData.getEntryPlaza()).matches()) {
					log.error("Invalid SCORR detail, Entry Plaza - " + entryData.getEntryPlaza() + lineNo);
					addErrorMsg(DETAIL_RECORD_TYPE, "Entry Plaza",
							"Invalid Entry Plaza - " + entryData.getEntryPlaza() + lineNo);
					invalidRecord = true;
				}
				//Entry Plaza Description 
				pattern = Pattern.compile(NIOPConstants.TXN_FACILITY_ID_DESC_FORMAT);
				if(entryData.getEntryPlazaDesc() ==null || !pattern.matcher(entryData.getEntryPlazaDesc()).matches()) {
					log.error("Invalid SCORR detail, Entry Plaza Description  - " + entryData.getEntryPlazaDesc() + lineNo);
					addErrorMsg(DETAIL_RECORD_TYPE, "Entry Plaza Description",
							"Invalid Entry Plaza Description  - " + entryData.getEntryPlazaDesc() + lineNo);
					invalidRecord = true;
				}
				
				// Entry Lane
				pattern = Pattern.compile(NIOPConstants.TXN_LANE_FORMAT);
				if (entryData.getEntryLane() == null
						|| !pattern.matcher(entryData.getEntryLane()).matches()) {
					log.error(
							"Invalid SCORR detail, Entry Lane  - " + entryData.getEntryLane() + lineNo);
					addErrorMsg(DETAIL_RECORD_TYPE, "Entry Lane",
							"Invalid Entry Lane  - " + entryData.getEntryLane() + lineNo);
					invalidRecord = true;
				}
				//Entry Date/Time w/TZ
				pattern = Pattern.compile(NIOPConstants.TXN_UTC_TIME_ZONE_FORMAT);
				if(transactionRecord.getEntryDateTimeTZ() ==null || !pattern.matcher(transactionRecord.getEntryDateTimeTZ()).matches()) {
					log.error("Invalid SCORR detail, Entry Date/Time w/TZ - " + transactionRecord.getEntryDateTimeTZ() + lineNo);
					addErrorMsg(DETAIL_RECORD_TYPE, "Entry Date/Time w/TZ",
							"Invalid Entry Date/Time w/TZ - " + transactionRecord.getEntryDateTimeTZ() + lineNo);
					invalidRecord = true;
				}
				
			} else {
				log.error("Invalid SCORR detail, Invalid Entry Data  - " + recordType + lineNo);
				addErrorMsg(DETAIL_RECORD_TYPE, "Entry ", "Invalid Entry Data  - " + recordType + lineNo);
				invalidRecord = true;
			}
		}
		//Validate tag details.
		TagInfo tagInfo = transactionRecord.getTagInfo();
		if (tagInfo != null) {
			log.info("NiopValidationController.cscIdTagNiopAgencyMap ::"+NiopValidationController.cscIdTagNiopAgencyMap);
			//Tag Agency ID
			pattern = Pattern.compile(NIOPConstants.AGENCY_ID_FORMAT);
			if (tagInfo.getTagAgencyID() == null || !pattern.matcher(tagInfo.getTagAgencyID()).matches()
					|| NiopValidationController.cscIdTagNiopAgencyMap.get(tagInfo.getTagAgencyID()) == null) {
				log.error("Invalid SCORR detail, Invalid Tag Agency ID  - " + tagInfo.getTagAgencyID() + lineNo);
				addErrorMsg(DETAIL_RECORD_TYPE, "Tag Agency ID", "Invalid Tag Agency ID  - " + tagInfo.getTagAgencyID() + lineNo);
				invalidRecord = true;
			}
			
			pattern = Pattern.compile(NIOPConstants.BTVL_DTL_TAG_SERIAL_NO);
			if (tagInfo.getTagSerialNo() == null || !pattern.matcher(tagInfo.getTagSerialNo()).matches()) {
				log.error("Invalid SCORR detail, Invalid Tag Serial No - " + tagInfo.getTagSerialNo() + lineNo);
				addErrorMsg(DETAIL_RECORD_TYPE, "Tag Serial Number", "Invalid Tag Serial No  - " + tagInfo.getTagSerialNo() + lineNo);
				invalidRecord = true;
			}else {

	        	try {
	        		NiopAgencyEntity agEntity = NiopValidationController.cscIdTagNiopAgencyMap.get(tagInfo.getTagAgencyID());
	        		if(agEntity != null) {
	    			long tagSerialNoStart = Long.valueOf(agEntity.getTagSequenceStart());
	    			long tagSerialNoEnd = Long.valueOf(agEntity.getTagSequenceEnd());
	    			long tagSerialNoLong = Long.valueOf(tagInfo.getTagSerialNo());
	    			log.info("## tagSerialNoLong ::" + tagSerialNoLong + "\t tagSerialNoStart :: " + tagSerialNoStart
	    					+ "\t tagSerialNoEnd ::" + tagSerialNoEnd);
	    			
	    			if (!(tagSerialNoStart < tagSerialNoEnd && tagSerialNoStart <= tagSerialNoLong
	    					&& tagSerialNoEnd >= tagSerialNoLong)) {
	    				log.error("## tagSerialNoLong ::" + tagSerialNoLong + "\t tagSerialNoStart :: " + tagSerialNoStart
	    						+ "\t tagSerialNoEnd ::" + tagSerialNoEnd);
	    				log.error("Invalid Tag Serial Number range   - " + tagSerialNoLong);
	    				addErrorMsg(DETAIL_RECORD_TYPE,"Tag Serial Number", "Invalid TAG_SERIAL_NUMBER range   - " + tagInfo.getTagSerialNo());
	    				invalidRecord=true;
	    			}
	        		}else {
	        			log.error("Invalid SCORR detail, invalid tag serial number range - "+tagInfo.getTagSerialNo());
	                	addErrorMsg(DETAIL_RECORD_TYPE,"Tag Serial Number","Invalid Tag serial number Range or Tag Agency ID \t"+tagInfo.getTagSerialNo());
	        		}
				} catch (Exception e) {
					e.printStackTrace();
					addErrorMsg(DETAIL_RECORD_TYPE,"Tag Serial Number","Invalid Tag Agency ID for TAG_SERIAL_NUMBER range from excel or file  - " + lineNo);
					//validateParam.setResponseMsg("Invalid TAG_SERIAL_NUMBER format from excel or file  - " + fileRowData);
					invalidRecord=true;
				}
	        	
	        
			}
			
			pattern = Pattern.compile(NIOPConstants.BTVL_DTL_TAG_STATUS);
			if (tagInfo.getTagStatus() == null || !pattern.matcher(tagInfo.getTagStatus()).matches()) {
				log.error("Invalid SCORR detail, Invalid Tag Status - " + tagInfo.getTagStatus() + lineNo);
				addErrorMsg(DETAIL_RECORD_TYPE, "Tag Status", "Invalid Tag Status  - " + tagInfo.getTagStatus() + lineNo);
				invalidRecord = true;
			}
		}
		
		//Occupancy Indicator 
		pattern = Pattern.compile(NIOPConstants.TXN_OCCUPANCY_IND_VALUE);
		if (transactionRecord.getOccupancyInd() != null && !pattern.matcher(transactionRecord.getOccupancyInd()).matches()) {
			log.error("Invalid SCORR detail, Invalid Occupancy Indicator - " + transactionRecord.getOccupancyInd() + lineNo);
			addErrorMsg(DETAIL_RECORD_TYPE, "Occupancy Indicator", "Invalid Occupancy Indicator  - " + transactionRecord.getOccupancyInd() + lineNo);
			invalidRecord = true;
		}
		
		//Vehicle Classification 
		pattern = Pattern.compile(NIOPConstants.TXN_LANE_FORMAT);
		if (transactionRecord.getVehicleClass() != null && !pattern.matcher(transactionRecord.getVehicleClass()).matches()) {
			log.error("Invalid SCORR detail, Invalid Vehicle Class - " + transactionRecord.getVehicleClass() + lineNo);
			addErrorMsg(DETAIL_RECORD_TYPE, "Vehicle Class", "Invalid Vehicle Class  - " + transactionRecord.getVehicleClass() + lineNo);
			invalidRecord = true;
		}
		
		//TollAmount
		pattern = Pattern.compile(NIOPConstants.TXN_TOLL_AMOUNT);
		if (transactionRecord.getTollAmount() == null || !pattern.matcher(transactionRecord.getTollAmount()).matches()) {
			log.error("Invalid SCORR detail, Invalid TollAmount - " + transactionRecord.getTollAmount() + lineNo);
			addErrorMsg(DETAIL_RECORD_TYPE, "TollAmount", "Invalid TollAmount  - " + transactionRecord.getTollAmount() + lineNo);
			invalidRecord = true;
		}
		
		//Discount Plan
		pattern = Pattern.compile(NIOPConstants.BTVL_DTL_DISCOUNT_PLAN_FORMAT);
		if (transactionRecord.getDiscountPlanType() != null && !pattern.matcher(transactionRecord.getDiscountPlanType()).matches()) {
			log.error("Invalid SCORR detail, Invalid Discount Plan - " + transactionRecord.getDiscountPlanType() + lineNo);
			addErrorMsg(DETAIL_RECORD_TYPE, "Discount Plan", "Invalid Discount Plan  - " + transactionRecord.getDiscountPlanType() + lineNo);
			invalidRecord = true;
		}
		//PlateInfo
		PlateInfo plateInfo = transactionRecord.getPlateInfo();
		if(plateInfo != null) {
			pattern = Pattern.compile(NIOPConstants.TXN_PLATE_TYPE);
			if(transactionRecord.getRecordType() != null && (recordType != null && pattern.matcher(recordType).matches())) {
				//License Plate Country
				pattern = Pattern.compile(NIOPConstants.BTVL_DTL_PLATE_COUNTRY);
				if (plateInfo.getPlateCountry() == null || !pattern.matcher(plateInfo.getPlateCountry()).matches()) {
					log.error("Invalid SCORR detail, Invalid License Plate Country - " + plateInfo.getPlateCountry() + lineNo);
					addErrorMsg(DETAIL_RECORD_TYPE, "License Plate Country", "Invalid License Plate Country  - " + plateInfo.getPlateCountry() + lineNo);
					invalidRecord = true;
				}
				//License Plate State
				pattern = Pattern.compile(NIOPConstants.BTVL_DTL_PLATE_STATE_FORMAT);
				if (plateInfo.getPlateState() == null || !pattern.matcher(plateInfo.getPlateState()).matches()) {
					log.error("Invalid SCORR detail, Invalid License Plate State - " + plateInfo.getPlateState() + lineNo);
					addErrorMsg(DETAIL_RECORD_TYPE, "License Plate State", "Invalid License Plate State  - " + plateInfo.getPlateState() + lineNo);
					invalidRecord = true;
				}
				//License Plate Number
				pattern = Pattern.compile(NIOPConstants.BTVL_DTL_PLATE_NUMBER_FORMAT);
				if (plateInfo.getPlateNumber() == null || !pattern.matcher(plateInfo.getPlateNumber()).matches()) {
					log.error("Invalid SCORR detail, Invalid License Plate Number - " + plateInfo.getPlateNumber() + lineNo);
					addErrorMsg(DETAIL_RECORD_TYPE, "License Plate Number", "Invalid License Plate Number  - " + plateInfo.getPlateNumber() + lineNo);
					invalidRecord = true;
				}
				//License Plate Type 
				pattern = Pattern.compile(NIOPConstants.BTVL_DTL_PLATE_TYPE_FORMAT);
				if (plateInfo.getPlateType() == null || !pattern.matcher(plateInfo.getPlateType()).matches()) {
					log.error("Invalid SCORR detail, Invalid License Plate Type - " + plateInfo.getPlateType() + lineNo);
					addErrorMsg(DETAIL_RECORD_TYPE, "License Plate Type", "Invalid License Plate Type  - " + plateInfo.getPlateType() + lineNo);
					invalidRecord = true;
				}
				
			}else {
				log.error("Invalid SCORR detail, Invalid Plate Record Type  - " + recordType + lineNo);
				addErrorMsg(DETAIL_RECORD_TYPE, "Plate Record Type", "Invalid Plate Record Type  - " + recordType + lineNo);
				invalidRecord = true;
			}
			
		}
		//Vehicle Classification Adjustment Flag
		//pattern = Pattern.compile(NIOPConstants.BTVL_DTL_DISCOUNT_PLAN_FORMAT);
		if (transactionRecord.getVehicleClassAdj() != null && !"V".equals(transactionRecord.getVehicleClassAdj())) {
			log.error("Invalid SCORR detail, Invalid Vehicle Classification Adjustment Flag - " + transactionRecord.getVehicleClassAdj() + lineNo);
			addErrorMsg(DETAIL_RECORD_TYPE, "Vehicle Classification Adjustment Flag", "Invalid Vehicle Classification Adjustment Flag  - " + transactionRecord.getVehicleClassAdj() + lineNo);
			invalidRecord = true;
		}
		//System Matched Flag
		pattern = Pattern.compile(NIOPConstants.TXN_SYSTEM_MATCH_FLAG_VALUE);
		if (transactionRecord.getSystemMatchInd() != null && !pattern.matcher(transactionRecord.getSystemMatchInd()).matches()) {
			log.error("Invalid SCORR detail, Invalid System Matched Flag - " + transactionRecord.getSystemMatchInd() + lineNo);
			addErrorMsg(DETAIL_RECORD_TYPE, "System Matched Flag", "Invalid System Matched Flag  - " + transactionRecord.getSystemMatchInd() + lineNo);
			invalidRecord = true;
		}
		//Spare 1
		pattern = Pattern.compile(NIOPConstants.TXN_SPARE1_FORMAT);
		if (transactionRecord.getSpare1() != null && !pattern.matcher(transactionRecord.getSpare1()).matches()) {
			log.error("Invalid SCORR detail, Invalid Spare 1 - " + transactionRecord.getSpare1() + lineNo);
			addErrorMsg(DETAIL_RECORD_TYPE, "Spare 1", "Invalid Spare 1  - " + transactionRecord.getSpare1() + lineNo);
			invalidRecord = true;
		}
		
		//Exit Date/Time w/TZ
		pattern = Pattern.compile(NIOPConstants.TXN_UTC_TIME_ZONE_FORMAT);
		if(transactionRecord.getExitDateTimeTZ() ==null || !pattern.matcher(transactionRecord.getExitDateTimeTZ()).matches()) {
			log.error("Invalid SCORR detail, Exit Date/Time w/TZ - " + transactionRecord.getExitDateTimeTZ() + lineNo);
			addErrorMsg(DETAIL_RECORD_TYPE, "Exit Date/Time w/TZ",
					"Invalid Exit Date/Time w/TZ - " + transactionRecord.getExitDateTimeTZ() + lineNo);
			invalidRecord = true;
		}
		
		//Entry Date/Time w/TZ
		pattern = Pattern.compile(NIOPConstants.TXN_UTC_TIME_ZONE_FORMAT);
		if(transactionRecord.getEntryDateTimeTZ() !=null && !pattern.matcher(transactionRecord.getEntryDateTimeTZ()).matches()) {
			log.error("Invalid SCORR detail, Entry Date/Time w/TZ - " + transactionRecord.getEntryDateTimeTZ() + lineNo);
			addErrorMsg(DETAIL_RECORD_TYPE, "Entry Date/Time w/TZ",
					"Invalid Entry Date/Time w/TZ - " + transactionRecord.getEntryDateTimeTZ() + lineNo);
			invalidRecord = true;
		}
		
        return invalidRecord;
		
	}

	private boolean headerValidation(ScorrHeader scorrHeader, FileValidationParam validateParam, String fileName) {
		boolean invalidHeaderRecord = false;
		log.info("SCORR Header validation started :: "+scorrHeader);
		if (!scorrHeader.getSubmissionType().equals(validateParam.getFileType())) {
        	addErrorMsg(HEADER_RECORD_TYPE,"SubmissionType"," Invalid SubmissionType   \t ::"+scorrHeader.getSubmissionType());
        	log.error("Invalid SubmissionType   \t ::"+scorrHeader.getSubmissionType());
        	invalidHeaderRecord = true;
        }
		
		if (!(scorrHeader.getSubmissionDateTime().length() == 20) ||
                !(scorrHeader.getSubmissionDateTime().matches(NIOPConstants.UTC_DATE_YEAR_REGEX))) {
        	addErrorMsg(HEADER_RECORD_TYPE,"SubmissionDateTime"," Invalid SubmissionDateTime   \t ::"+scorrHeader.getSubmissionDateTime());
        	log.error("Invalid SubmissionDateTime   \t ::"+scorrHeader.getSubmissionDateTime());
        	invalidHeaderRecord = true;
        }
		
		if (!scorrHeader.getSsiopHubID().matches(NIOPConstants.AGENCY_ID_FORMAT)
				|| NiopValidationController.allCscIdNiopAgencyMap.get(validateParam.getFromAgency()) == null
				|| !scorrHeader.getSsiopHubID().equals(
						String.valueOf(NiopValidationController.allCscIdNiopAgencyMap.get(validateParam.getFromAgency()).getHubId()))) {
			addErrorMsg(HEADER_RECORD_TYPE, "SSIOPHubID",
					" Invalid SSIOPHUBID   \t ::" + scorrHeader.getSsiopHubID());
			log.error("Header validation failed, Invalid SSIOPHUBID  :: " + scorrHeader.getSsiopHubID());
			invalidHeaderRecord = true;
		}
		
		if(!scorrHeader.getAwayAgencyID().matches(NIOPConstants.AGENCY_ID_FORMAT) || !scorrHeader.getAwayAgencyID().equals(validateParam.getFromAgency())){
			addErrorMsg(HEADER_RECORD_TYPE,"AwayAgencyID"," Invalid AwayAgencyID   \t ::"+scorrHeader.getAwayAgencyID());
            log.error("Header validation failed, Invalid AwayAgencyID  :: " +scorrHeader.getAwayAgencyID());
            invalidHeaderRecord = true;
        }
		
		if(!scorrHeader.getHomeAgencyID().matches(NIOPConstants.AGENCY_ID_FORMAT) || !scorrHeader.getHomeAgencyID().equals(validateParam.getToAgency())){
			addErrorMsg(HEADER_RECORD_TYPE,"HomeAgencyID"," Invalid HomeAgencyID   \t ::"+scorrHeader.getHomeAgencyID());
            log.error("Header validation failed, Invalid HomeAgencyID  :: " +scorrHeader.getHomeAgencyID());
            invalidHeaderRecord = true;
        }
		
		if(!scorrHeader.getTxnDataSeqNo().matches(NIOPConstants.TXN_DATA_SEQ_NO_FORMAT)){
			addErrorMsg(HEADER_RECORD_TYPE,"TxnDataSeqNo"," Invalid TxnDataSeqNo   \t ::"+scorrHeader.getTxnDataSeqNo());
            log.error("Header validation failed, Invalid TxnDataSeqNo  :: " +scorrHeader.getTxnDataSeqNo());
            invalidHeaderRecord = true;
        }
		
		if(!scorrHeader.getRecordCount().matches(NIOPConstants.TXN_RECORD_COUNT_FORMAT)){
			addErrorMsg(HEADER_RECORD_TYPE,"Record Count"," Invalid Record Count   \t ::"+scorrHeader.getRecordCount());
            log.error("Header validation failed, Invalid RecordCount  :: " +scorrHeader.getRecordCount());
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
