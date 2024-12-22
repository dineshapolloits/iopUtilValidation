package com.apolloits.util.generate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.apolloits.util.IAGConstants;
import com.apolloits.util.IopTranslatorException;
import com.apolloits.util.controller.ValidationController;
import com.apolloits.util.modal.AgencyEntity;
import com.apolloits.util.modal.FileValidationParam;
import com.apolloits.util.modal.Header;
import com.apolloits.util.modal.ICTX;
import com.apolloits.util.modal.ICTXTemplate;
import com.apolloits.util.modal.ITXC;
import com.apolloits.util.modal.ITXCTemplate;
import com.apolloits.util.utility.CommonUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ITXCFileGenerator {
	
	@Autowired
	CommonUtil commonUtil;
	
	@Autowired
	@Lazy
	ValidationController controller;
	
	private String filename = "";
	private String fileCreateDateandTime = "";
	AgencyEntity agency;
	
	private List<ITXCTemplate> itxcTemplateList;

	public boolean itxcGen(FileValidationParam validateParam, String itxcFileType) throws IOException {
		long start = System.currentTimeMillis();
		if (!commonUtil.validateParameter(validateParam)) {
			return false;
		}
		
		itxcTemplateList = getITXCTemplateExcel(validateParam);
		String Header = getITXCHeader(validateParam,itxcTemplateList);
		log.info("ITXC Header :: " + Header);
		writeDetails(validateParam,Header,itxcTemplateList);
		
		String filePath = validateParam.getOutputFilePath() + File.separator + filename;
		String zipFilename = commonUtil.moveToZipFile(filePath,validateParam);
		log.info("ITXC Zip file name :: "+zipFilename);
		validateParam.setResponseMsg("ITXC file created ::\t "+zipFilename);
		
		long end = System.currentTimeMillis();
		log.info("File create process time ::"+(end - start) / 1000f + " seconds");
		return true;
	}
	
	private void writeDetails(FileValidationParam validateParam, String header,List<ITXCTemplate> itxcTempList)
			throws IOException {
		
		FileWriter writer;
		try {
			String filePath = validateParam.getOutputFilePath() + File.separator + filename;
			writer = new FileWriter(filePath, true);
			writer.write(header);
			writer.write(System.lineSeparator());
			System.out.println("ITXC Writing record raw... ");
			for (ITXCTemplate itxcTemplate : itxcTempList) {
				writer.write(setITXCDetailValues(itxcTemplate,validateParam));
				writer.write(System.lineSeparator());
			}
			writer.flush();
			writer.close();
		}catch (Exception e) {
			validateParam.setResponseMsg("File not generated Please check log");
			e.printStackTrace();
		}
	}
	
	private String setITXCDetailValues(ITXCTemplate itxcTemplate,FileValidationParam validateParam) {
		ITXC itxc = new ITXC();
		itxc.setCorrReason(CommonUtil.formatStringLeftPad(itxcTemplate.getCorrReason(),2,'0'));
		itxc.setEtcTrxSerialNum(CommonUtil.formatStringLeftPad(itxcTemplate.getEtcTrxSerialNo(),20,'0'));
		itxc.setEtcRevenueDate(CommonUtil.formatStringLeftPad(itxcTemplate.getEtcRevenueDate(),8,' '));
		itxc.setEtcFacAgency(validateParam.getFromAgency());
		itxc.setEtcTrxType(CommonUtil.formatStringLeftPad(itxcTemplate.getEtcTrxType(), 1, ' '));
		itxc.setEtcEntryDateTime(CommonUtil.formatStringLeftPad(itxcTemplate.getEtcEntryDateTime(),25,'*'));
		itxc.setEtcEntryPlaza(CommonUtil.formatStringRightPad(itxcTemplate.getEtcEntryPlaza(),15,'*'));
		itxc.setEtcEntryLane(CommonUtil.formatStringRightPad(itxcTemplate.getEtcEntryLane(),3,'*'));
		itxc.setEtcTagAgency(CommonUtil.formatStringLeftPad(itxcTemplate.getEtcTagAgency(),4,' '));
		itxc.setEtcTagSerialNumber(CommonUtil.formatStringLeftPad(itxcTemplate.getEtcTagSerialNumber(),10,'0'));
		itxc.setEtcReadPerformance(CommonUtil.formatStringRightPad(itxcTemplate.getEtcReadPerformance(),2,'*'));
		itxc.setEtcWritePerf(CommonUtil.formatStringLeftPad(itxcTemplate.getEtcWritePerf(),2,'*'));
		itxc.setEtcTagPgmStatus(CommonUtil.formatStringLeftPad(itxcTemplate.getEtcTagPgmStatus(),1,'*'));
		itxc.setEtcLaneMode(CommonUtil.formatStringRightPad(itxcTemplate.getEtcLaneMode(),1,'O'));
		itxc.setEtcValidationStatus(CommonUtil.formatStringLeftPad(itxcTemplate.getEtcValidationStatus(),1,'*'));
		itxc.setEtcLicState(CommonUtil.formatStringRightPad(itxcTemplate.getEtcLicState(),2,' '));
		itxc.setEtcLicNumber(CommonUtil.formatStringRightPad(itxcTemplate.getEtcLicNumber(),10,' '));
		itxc.setEtcLicType(CommonUtil.formatStringRightPad("",30,'*')); //
		itxc.setEtcClassCharged(CommonUtil.formatStringRightPad(itxcTemplate.getEtcClassCharged(),3,' '));
		itxc.setEtcActualAxles("02");
		itxc.setEtcExitSpeed("000");
		itxc.setEtcOverSpeed(CommonUtil.formatStringRightPad(itxcTemplate.getEtcOverSpeed(),1,'N'));
		itxc.setEtcExitDateTime(CommonUtil.formatStringLeftPad(itxcTemplate.getEtcExitDateTime(),25,' '));
		itxc.setEtcExitPlaza(CommonUtil.formatStringRightPad(itxcTemplate.getEtcExitPlaza(),15,' '));
		itxc.setEtcExitLane(CommonUtil.formatStringRightPad(itxcTemplate.getEtcExitLane(),3,' '));
		itxc.setEtcDebitCredit(CommonUtil.formatStringRightPad(itxcTemplate.getEtcDebitCredit(),1,' '));
		itxc.setEtcTollAmount(CommonUtil.formatStringLeftPad(itxcTemplate.getEtcTollAmount(),9,'0'));
		return itxc.toString();
	}
	

	private String getITXCHeader(FileValidationParam validateParam, List<ITXCTemplate> itxcTempList) {

		fileCreateDateandTime = commonUtil.getCurrentUTCDateandTime();
		log.info("ITXC fileCreateDateandTime ::" + fileCreateDateandTime);
		fileCreateDateandTime = validateParam.getFileDate()
				+ fileCreateDateandTime.substring(fileCreateDateandTime.indexOf("T"), fileCreateDateandTime.length());
		log.info("ITXC After append fileCreateDateandTime ::" + fileCreateDateandTime);
		// Set file name to class variable
		filename = validateParam.getFromAgency() + "_" + validateParam.getToAgency() + "_"
				+ fileCreateDateandTime.replaceAll("[-T:Z]", "") + IAGConstants.ITXC_FILE_EXTENSION;
		log.info("File name creation ::" + filename);
		this.agency = ValidationController.cscIdTagAgencyMap.get(validateParam.getFromAgency());
		Header header = new Header();
		header.setFileType(IAGConstants.ITXC_FILE_TYPE);
		header.setVersion(agency.getVersionNumber());
		header.setFromAgencyId(validateParam.getFromAgency());
		header.setFileDateTime(fileCreateDateandTime);
		header.setToAgencyId(validateParam.getToAgency());
		header.setRecordCount(CommonUtil.formatStringLeftPad(String.valueOf(itxcTempList.size()), 8, '0'));
		header.setIctxfileNum(
				CommonUtil.formatStringLeftPad(String.valueOf(itxcTempList.get(0).getItxcFileNum()), 12, '0'));
		return header.toString();
	}

	private List<ITXCTemplate> getITXCTemplateExcel(FileValidationParam validateParam) {

		final String ITXC_SHEET = "IRXC";

		try {
			log.info("ITXC Excel data path localtion form getInputFilePath ::"+validateParam.getInputFilePath());
			FileInputStream is = new FileInputStream(validateParam.getInputFilePath());
			Workbook  workbook = new XSSFWorkbook(is);
			log.info("Number of sheets : " + workbook.getNumberOfSheets());
			log.info("IRXC sheet name ::"+workbook.getSheetName(0));
			itxcTemplateList = excelToITXCList(workbook.getSheet(ITXC_SHEET),validateParam);
			if(workbook != null)
				workbook.close();
				
		} catch (Exception e) {
			validateParam.setResponseMsg("File not generated Please check input excel data");
			e.printStackTrace();
			return itxcTemplateList = new ArrayList<>();
		}

		return itxcTemplateList;
	
	}

	private List<ITXCTemplate> excelToITXCList(Sheet sheet,FileValidationParam validateParam) {
	   	 log.info("Inside ****************** excelToITXCList()");
	        try {
	       	
	          Iterator<Row> rows = sheet.iterator();
	          itxcTemplateList = new ArrayList<>();
	          int rowNumber = 0;
	          while (rows.hasNext()) {
	            Row currentRow = rows.next();
	            // skip header
	            if (rowNumber == 0) {
	              rowNumber++;
	              continue;
	            }
	           // Iterator<Cell> cellsInRow = currentRow.iterator();
	            ITXCTemplate itxcTemp = new ITXCTemplate();
	            itxcTemp.setItxcFileNum(commonUtil.getStringFormatCell(currentRow.getCell(0,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
	            itxcTemp.setCorrReason(commonUtil.getStringFormatCell(currentRow.getCell(1,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
	            itxcTemp.setEtcTrxSerialNo(commonUtil.getStringFormatCell(currentRow.getCell(2,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
	            itxcTemp.setEtcRevenueDate(commonUtil.getStringFormatCell(currentRow.getCell(3,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
	            itxcTemp.setEtcTagAgency(commonUtil.getStringFormatCell(currentRow.getCell(4,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
	            itxcTemp.setEtcTagSerialNumber(commonUtil.getStringFormatCell(currentRow.getCell(5,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
	            itxcTemp.setEtcValidationStatus(commonUtil.getStringFormatCell(currentRow.getCell(6,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
	            itxcTemp.setEtcLicState(commonUtil.getStringFormatCell(currentRow.getCell(7,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
	            itxcTemp.setEtcLicNumber(commonUtil.getStringFormatCell(currentRow.getCell(8,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
	            itxcTemp.setEtcClassCharged(commonUtil.getStringFormatCell(currentRow.getCell(9,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
	            itxcTemp.setEtcExitDateTime(commonUtil.getStringFormatCell(currentRow.getCell(10,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
	            itxcTemp.setEtcExitPlaza(commonUtil.getStringFormatCell(currentRow.getCell(11,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
	            itxcTemp.setEtcExitLane(commonUtil.getStringFormatCell(currentRow.getCell(12,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
	            itxcTemp.setEtcTrxType(commonUtil.getStringFormatCell(currentRow.getCell(13,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
	            itxcTemp.setEtcEntryDateTime(commonUtil.getStringFormatCell(currentRow.getCell(14,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
	            itxcTemp.setEtcEntryPlaza(commonUtil.getStringFormatCell(currentRow.getCell(15,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
	            itxcTemp.setEtcEntryLane(commonUtil.getStringFormatCell(currentRow.getCell(16,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
	            itxcTemp.setEtcReadPerformance(commonUtil.getStringFormatCell(currentRow.getCell(17,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
	            itxcTemp.setEtcWritePerf(commonUtil.getStringFormatCell(currentRow.getCell(18,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
	            itxcTemp.setEtcTagPgmStatus(commonUtil.getStringFormatCell(currentRow.getCell(19,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
	            itxcTemp.setEtcLaneMode(commonUtil.getStringFormatCell(currentRow.getCell(20,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
	            itxcTemp.setEtcOverSpeed(commonUtil.getStringFormatCell(currentRow.getCell(21,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
	            itxcTemp.setEtcDebitCredit(commonUtil.getStringFormatCell(currentRow.getCell(22,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
	            itxcTemp.setEtcTollAmount(commonUtil.getStringFormatCell(currentRow.getCell(23,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
	            
	            itxcTemplateList.add(itxcTemp);
	          }
	         
	          if(itxcTemplateList != null && itxcTemplateList.size()>0) {
	          log.info("@@@@ ICTX input data  loaded sucessfully:: ******************** ::"+itxcTemplateList.size());
	          }else {
	       	   throw new IopTranslatorException("ICTX input data not loaded");
	          }
	          
	        }catch (NullPointerException e) {
	        	validateParam.setResponseMsg("Excel IRXC  sheet not found. Please check sheet");
		       	log.error("NullPointerException:: ******************** ITXC Sheet");
					e.printStackTrace();
				}catch (Exception e) {
	       	log.error("Exception:: ******************** ICTX Sheet");
				e.printStackTrace();
			}
	      
			return itxcTemplateList;
		}
	
}
