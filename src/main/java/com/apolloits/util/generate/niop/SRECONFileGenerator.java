package com.apolloits.util.generate.niop;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.apolloits.util.IopTranslatorException;
import com.apolloits.util.NIOPConstants;
import com.apolloits.util.controller.NiopValidationController;
import com.apolloits.util.modal.FileValidationParam;
import com.apolloits.util.modal.NiopAgencyEntity;
import com.apolloits.util.modal.niop.srecon.ReconciliationData;
import com.apolloits.util.modal.niop.srecon.ReconciliationDetail;
import com.apolloits.util.modal.niop.srecon.ReconciliationHeader;
import com.apolloits.util.modal.niop.srecon.ReconciliationRecord;
import com.apolloits.util.utility.CommonUtil;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SRECONFileGenerator {

	private String filename = "";
	private String fileCreateDateandTime = "";
	private String txnDataSeqNo="0";
	NiopAgencyEntity agency;
	List<ReconciliationRecord> reconRecordList;
	
	
	@Autowired
	CommonUtil commonUtil;
	
	public boolean sreconGen(FileValidationParam validateParam) {

		
		long start = System.currentTimeMillis();
		reconRecordList = getSRECONTemplateExcel(validateParam);
		ReconciliationHeader reconHeader = getSreconHeader(validateParam,reconRecordList);
		
		//Set value to object to write file
		ReconciliationData  reconData = new ReconciliationData();
		reconData.setReconciliationHeader(reconHeader);
		
		ReconciliationDetail reconDetail = new ReconciliationDetail();
		reconDetail.setReconRecordList(reconRecordList);
		
		reconData.setReconciliationDetail(reconDetail);
		writeFile(reconData,validateParam);
		long end = System.currentTimeMillis();
		log.info("File Creation time ::"+(end - start) / 1000f + " seconds");
		return true;
		
	}
	
	
	private boolean writeFile(ReconciliationData reconData, FileValidationParam validateParam) {

		try {
			String filePath = validateParam.getOutputFilePath() + File.separator + filename;
			// Step 2: Create JAXB context and instantiate marshaller
			JAXBContext context = JAXBContext.newInstance(ReconciliationData.class);
			Marshaller marshaller = context.createMarshaller();

			// Optional: Set the marshaller property to format the XML output
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

			// Step 3: Create an SRECON object and marshal it to XML
			marshaller.marshal(reconData, new File(filePath));

			String zipFilename = commonUtil.moveToZipFile(filePath, validateParam);
			validateParam.setResponseMsg(" File created ::\t " + zipFilename);

		} catch (JAXBException e) {
			e.printStackTrace();
			validateParam.setResponseMsg("SRECON file creation issue. Please check logs");
			return false;
		}
		return true;
	}

	private ReconciliationHeader getSreconHeader(FileValidationParam validateParam,
			List<ReconciliationRecord> reconRecordList) {
		ReconciliationHeader reconHeader = null;
		
		fileCreateDateandTime = commonUtil.getUTCDateandTime();
		log.info("fileCreateDateandTime ::" + fileCreateDateandTime);
		fileCreateDateandTime = validateParam.getFileDate()
				+ fileCreateDateandTime.substring(fileCreateDateandTime.indexOf("T"), fileCreateDateandTime.length());
		log.info("After append fileCreateDateandTime ::" + fileCreateDateandTime);
		NiopAgencyEntity agEntity = NiopValidationController.allCscIdNiopAgencyMap.get(validateParam.getFromAgency());
		if (agEntity == null) {
			validateParam.setResponseMsg("Please check agency configuration");
			return reconHeader;
		}
		// Set file name to class variable
		filename = agEntity.getHubId() + "_" + validateParam.getFromAgency() + "_" + validateParam.getToAgency() + "_"
				+ fileCreateDateandTime.replaceAll("[-T:Z]", "") + NIOPConstants.SRECON_FILE_EXTENSION;
		
		log.info("SRECON file Name ::"+filename);
		
		reconHeader = new ReconciliationHeader();
		
		reconHeader.setSubmissionType(NIOPConstants.SRECON_FILE_TYPE);
		reconHeader.setSubmissionDateTime(fileCreateDateandTime);
		reconHeader.setSsiopHubID(agEntity.getHubId().toString());
		reconHeader.setAwayAgencyID(validateParam.getToAgency());
		reconHeader.setHomeAgencyID(validateParam.getFromAgency());
		reconHeader.setTxnDataSeqNo(txnDataSeqNo);
		reconHeader.setRecordCount(String.valueOf(reconRecordList.size()));
		
		return reconHeader;
	}

	private List<ReconciliationRecord> getSRECONTemplateExcel(FileValidationParam validateParam) {
		String STRAN_SHEET = "SRECON";
		
		try {
			log.info("Excel data path localtion form getInputFilePath ::"+validateParam.getInputFilePath());
			FileInputStream is = new FileInputStream(validateParam.getInputFilePath());
			Workbook workbook = new XSSFWorkbook(is);
			log.info("Number of sheets : " + workbook.getNumberOfSheets());

			reconRecordList = excelToSRECONList(workbook.getSheet(STRAN_SHEET),validateParam);

		} catch (Exception e) {
			validateParam.setResponseMsg("File not generated Please check input excel data");
			e.printStackTrace();
			return reconRecordList = new ArrayList<>();
		}
		return reconRecordList;
	}

	private List<ReconciliationRecord> excelToSRECONList(Sheet sheet, FileValidationParam validateParam) {

		log.info("Inside ****************** excelToSRECONList()");
        try {
       	String fileType = "STRAN";
          Iterator<Row> rows = sheet.iterator();
          reconRecordList = new ArrayList<>();
          int rowNumber = 0;
          while (rows.hasNext()) {
            Row currentRow = rows.next();
            // skip header
            if (rowNumber == 0) {
            	String fieldValue = commonUtil.getStringFormatCell(currentRow.getCell(2,MissingCellPolicy.CREATE_NULL_AS_BLANK));
            	log.info("fieldValue ::"+fieldValue);
            	if(fieldValue.equals("Correction Date/Time")) {
            		fileType = "SCORR";
            	}
            	log.info("fileType ::"+fileType);
              rowNumber++;
              continue;
            }
            ReconciliationRecord sreconRecord = new ReconciliationRecord();
            if(fileType.equals("STRAN")) {
            	setSTRANreconValue(sreconRecord,currentRow,validateParam);
            }else {
            	setSCORRreconValue(sreconRecord,currentRow,validateParam);
            }
            reconRecordList.add(sreconRecord);
            System.out.println("tranRecord :: "+sreconRecord.toString());
          }
         
          if(reconRecordList != null && reconRecordList.size()>0) {
          log.info("@@@@ SRECON input data  loaded sucessfully:: ******************** ::"+reconRecordList.size());
          }else {
       	   throw new IopTranslatorException("SRECON input data not loaded");
          }
          
        }catch (NullPointerException e) {
        	validateParam.setResponseMsg("Excel SRECON  sheet not found. Please check sheet");
	       	log.error("NullPointerException:: ******************** SRECON Sheet");
				e.printStackTrace();
			}catch (Exception e) {
       	log.error("Exception:: ******************** STRAN Sheet");
			e.printStackTrace();
		}
      
		return reconRecordList;
	
	}

	private void setSCORRreconValue(ReconciliationRecord sreconRecord, Row currentRow,
			FileValidationParam validateParam) {


		// sreconRecord.setTxnDataSeqNo(commonUtil.getStringFormatCell(currentRow.getCell(0,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
		txnDataSeqNo = commonUtil.getStringFormatCell(currentRow.getCell(0, MissingCellPolicy.CREATE_NULL_AS_BLANK));
		sreconRecord.setTxnReferenceID(
				commonUtil.getStringFormatCell(currentRow.getCell(10, MissingCellPolicy.CREATE_NULL_AS_BLANK)));

		String adjCount = commonUtil
				.getStringFormatCell(currentRow.getCell(41, MissingCellPolicy.CREATE_NULL_AS_BLANK));
		if (adjCount != null && !adjCount.isEmpty()) {
			sreconRecord.setAdjustmentCount(adjCount);
		} else {
			sreconRecord.setAdjustmentCount("0");
		}

		String resubmitCount = commonUtil
				.getStringFormatCell(currentRow.getCell(42, MissingCellPolicy.CREATE_NULL_AS_BLANK));
		if (resubmitCount != null && !resubmitCount.isEmpty()) {
			sreconRecord.setResubmitCount(resubmitCount);
		} else {
			sreconRecord.setResubmitCount("0");
		}

		String reconHomeAgencyID = commonUtil
				.getStringFormatCell(currentRow.getCell(43, MissingCellPolicy.CREATE_NULL_AS_BLANK));
		if (reconHomeAgencyID != null && !reconHomeAgencyID.isEmpty()) {
			sreconRecord.setReconHomeAgencyID(reconHomeAgencyID);
		} else {
			sreconRecord.setReconHomeAgencyID(validateParam.getFromAgency());
		}

		String homeAgencyTxnRefID = commonUtil
				.getStringFormatCell(currentRow.getCell(44, MissingCellPolicy.CREATE_NULL_AS_BLANK));
		if (homeAgencyTxnRefID != null && !homeAgencyTxnRefID.isEmpty()) {
			sreconRecord.setHomeAgencyTxnRefID(homeAgencyTxnRefID);
		}

		sreconRecord.setPostingDisposition(
				commonUtil.getStringFormatCell(currentRow.getCell(45, MissingCellPolicy.CREATE_NULL_AS_BLANK)));
		String discountPlanType = commonUtil
				.getStringFormatCell(currentRow.getCell(46, MissingCellPolicy.CREATE_NULL_AS_BLANK));
		if (discountPlanType != null && !discountPlanType.isEmpty()) {
			sreconRecord.setDiscountPlanType(discountPlanType);
		}
		sreconRecord.setPostedAmount(
				commonUtil.getStringFormatCell(currentRow.getCell(47, MissingCellPolicy.CREATE_NULL_AS_BLANK)));
		sreconRecord.setPostedDateTime(
				commonUtil.getStringFormatCell(currentRow.getCell(48, MissingCellPolicy.CREATE_NULL_AS_BLANK)));

		String transFlatFee = commonUtil
				.getStringFormatCell(currentRow.getCell(49, MissingCellPolicy.CREATE_NULL_AS_BLANK));

		if (transFlatFee != null && !transFlatFee.isEmpty()) {
			sreconRecord.setTransFlatFee(transFlatFee);
		} else {
			sreconRecord.setTransFlatFee("0");
		}
		String transPercentFee = commonUtil
				.getStringFormatCell(currentRow.getCell(50, MissingCellPolicy.CREATE_NULL_AS_BLANK));

		if (transPercentFee != null && !transPercentFee.isEmpty()) {
			sreconRecord.setTransPercentFee(transPercentFee);
		} else {
			sreconRecord.setTransPercentFee("0");
		}

		String spare = commonUtil.getStringFormatCell(currentRow.getCell(51, MissingCellPolicy.CREATE_NULL_AS_BLANK));
		if (spare != null && !spare.isEmpty()) {
			sreconRecord.setSpare1(spare);
		}
		spare = commonUtil.getStringFormatCell(currentRow.getCell(52, MissingCellPolicy.CREATE_NULL_AS_BLANK));
		if (spare != null && !spare.isEmpty()) {
			sreconRecord.setSpare2(spare);
		}

		spare = commonUtil.getStringFormatCell(currentRow.getCell(53, MissingCellPolicy.CREATE_NULL_AS_BLANK));
		if (spare != null && !spare.isEmpty()) {
			sreconRecord.setSpare3(spare);
		}

		spare = commonUtil.getStringFormatCell(currentRow.getCell(54, MissingCellPolicy.CREATE_NULL_AS_BLANK));
		if (spare != null && !spare.isEmpty()) {
			sreconRecord.setSpare4(spare);
		}

		spare = commonUtil.getStringFormatCell(currentRow.getCell(55, MissingCellPolicy.CREATE_NULL_AS_BLANK));
		if (spare != null && !spare.isEmpty()) {
			sreconRecord.setSpare5(spare);
		}

	}


	private void setSTRANreconValue(ReconciliationRecord sreconRecord,Row currentRow,FileValidationParam validateParam) {

		// sreconRecord.setTxnDataSeqNo(commonUtil.getStringFormatCell(currentRow.getCell(0,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
		txnDataSeqNo = commonUtil.getStringFormatCell(currentRow.getCell(0, MissingCellPolicy.CREATE_NULL_AS_BLANK));
		sreconRecord.setTxnReferenceID(
				commonUtil.getStringFormatCell(currentRow.getCell(2, MissingCellPolicy.CREATE_NULL_AS_BLANK)));

		String adjCount = commonUtil
				.getStringFormatCell(currentRow.getCell(33, MissingCellPolicy.CREATE_NULL_AS_BLANK));
		if (adjCount != null && !adjCount.isEmpty()) {
			sreconRecord.setAdjustmentCount(adjCount);
		} else {
			sreconRecord.setAdjustmentCount("0");
		}

		String resubmitCount = commonUtil
				.getStringFormatCell(currentRow.getCell(34, MissingCellPolicy.CREATE_NULL_AS_BLANK));
		if (resubmitCount != null && !resubmitCount.isEmpty()) {
			sreconRecord.setResubmitCount(resubmitCount);
		} else {
			sreconRecord.setResubmitCount("0");
		}

		String reconHomeAgencyID = commonUtil
				.getStringFormatCell(currentRow.getCell(35, MissingCellPolicy.CREATE_NULL_AS_BLANK));
		if (reconHomeAgencyID != null && !reconHomeAgencyID.isEmpty()) {
			sreconRecord.setReconHomeAgencyID(reconHomeAgencyID);
		} else {
			sreconRecord.setReconHomeAgencyID(validateParam.getFromAgency());
		}

		String homeAgencyTxnRefID = commonUtil
				.getStringFormatCell(currentRow.getCell(36, MissingCellPolicy.CREATE_NULL_AS_BLANK));
		if (homeAgencyTxnRefID != null && !homeAgencyTxnRefID.isEmpty()) {
			sreconRecord.setHomeAgencyTxnRefID(homeAgencyTxnRefID);
		}

		sreconRecord.setPostingDisposition(
				commonUtil.getStringFormatCell(currentRow.getCell(37, MissingCellPolicy.CREATE_NULL_AS_BLANK)));
		String discountPlanType = commonUtil
				.getStringFormatCell(currentRow.getCell(38, MissingCellPolicy.CREATE_NULL_AS_BLANK));
		if (discountPlanType != null && !discountPlanType.isEmpty()) {
			sreconRecord.setDiscountPlanType(discountPlanType);
		}
		sreconRecord.setPostedAmount(
				commonUtil.getStringFormatCell(currentRow.getCell(39, MissingCellPolicy.CREATE_NULL_AS_BLANK)));
		sreconRecord.setPostedDateTime(
				commonUtil.getStringFormatCell(currentRow.getCell(40, MissingCellPolicy.CREATE_NULL_AS_BLANK)));

		String transFlatFee = commonUtil
				.getStringFormatCell(currentRow.getCell(41, MissingCellPolicy.CREATE_NULL_AS_BLANK));

		if (transFlatFee != null && !transFlatFee.isEmpty()) {
			sreconRecord.setTransFlatFee(adjCount);
		} else {
			sreconRecord.setTransFlatFee("0");
		}
		String transPercentFee = commonUtil
				.getStringFormatCell(currentRow.getCell(42, MissingCellPolicy.CREATE_NULL_AS_BLANK));

		if (transPercentFee != null && !transPercentFee.isEmpty()) {
			sreconRecord.setTransPercentFee(transPercentFee);
		} else {
			sreconRecord.setTransPercentFee("0");
		}

		String spare = commonUtil.getStringFormatCell(currentRow.getCell(43, MissingCellPolicy.CREATE_NULL_AS_BLANK));
		if (spare != null && !spare.isEmpty()) {
			sreconRecord.setSpare1(spare);
		}
		spare = commonUtil.getStringFormatCell(currentRow.getCell(44, MissingCellPolicy.CREATE_NULL_AS_BLANK));
		if (spare != null && !spare.isEmpty()) {
			sreconRecord.setSpare2(spare);
		}

		spare = commonUtil.getStringFormatCell(currentRow.getCell(45, MissingCellPolicy.CREATE_NULL_AS_BLANK));
		if (spare != null && !spare.isEmpty()) {
			sreconRecord.setSpare3(spare);
		}

		spare = commonUtil.getStringFormatCell(currentRow.getCell(46, MissingCellPolicy.CREATE_NULL_AS_BLANK));
		if (spare != null && !spare.isEmpty()) {
			sreconRecord.setSpare4(spare);
		}

		spare = commonUtil.getStringFormatCell(currentRow.getCell(47, MissingCellPolicy.CREATE_NULL_AS_BLANK));
		if (spare != null && !spare.isEmpty()) {
			sreconRecord.setSpare5(spare);
		}

	}
}
