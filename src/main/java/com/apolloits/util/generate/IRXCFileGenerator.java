package com.apolloits.util.generate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.apolloits.util.IopTranslatorException;
import com.apolloits.util.controller.ValidationController;
import com.apolloits.util.modal.AgencyEntity;
import com.apolloits.util.modal.FileValidationParam;
import com.apolloits.util.modal.Header;
import com.apolloits.util.modal.ICRX;
import com.apolloits.util.modal.ICRXTemplate;
import com.apolloits.util.utility.CommonUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class IRXCFileGenerator {
	
	@Autowired
	CommonUtil commonUtil;
	
	@Autowired
	@Lazy
	ValidationController controller;
	
	private String filename = "";
	private String fileCreateDateandTime = "";
	AgencyEntity agency;
	
	private List<ICRXTemplate> irxcTemplateList;
	
	public boolean irxcGen(FileValidationParam validateParam,String fileType) throws IOException {
		if (!commonUtil.validateParameter(validateParam)) {
			return false;
		}
		
		irxcTemplateList = getIRXCTemplateExcel(validateParam);
		String Header = getIRXCHeader(validateParam,irxcTemplateList);
		log.info("IRXC Header :: " + Header);
		
		writeDetails(validateParam,Header,irxcTemplateList);
		
		String filePath = validateParam.getOutputFilePath() + File.separator + filename;
		String zipFilename = commonUtil.moveToZipFile(filePath,validateParam);
		log.info("IRXC ZIP file name :: "+zipFilename);
		validateParam.setResponseMsg("IRXC File created ::\t "+zipFilename);
		
		return true;
		
	}
	
	private void writeDetails(FileValidationParam validateParam, String header,List<ICRXTemplate> irxcTempList)
			throws IOException {
		long start = System.currentTimeMillis();
		FileWriter writer;
		try {
			String filePath = validateParam.getOutputFilePath() + File.separator + filename;
			writer = new FileWriter(filePath, true);
			writer.write(header);
			writer.write(System.lineSeparator());
			System.out.println("Writing record raw... ");
			for (ICRXTemplate icrxTemplate : irxcTempList) {
				writer.write(setIRXCDetailValues(icrxTemplate,validateParam));
				writer.write(System.lineSeparator());
			}
			//writer.flush();
			writer.close();
		}catch (Exception e) {
			validateParam.setResponseMsg("IRXC File not generated Please check log");
			e.printStackTrace();
		}
		long end = System.currentTimeMillis();
		log.info("IRXC file("+filename+") generation time :: "+(end - start) / 1000f + " seconds");
	}
	
	private String setIRXCDetailValues(ICRXTemplate icrxTemplate,FileValidationParam validateParam) {
		ICRX icrx = new ICRX();
		
		icrx.setEtcTrxSerialNum(CommonUtil.formatStringLeftPad(icrxTemplate.getEtcTrxSerialNo(),20,'0'));
		icrx.setEtcPostStatus(CommonUtil.formatStringLeftPad(icrxTemplate.getEtcPostStatus(),4,' '));
		icrx.setEtcPostPlan(CommonUtil.formatStringLeftPad(icrxTemplate.getEtcPostPlan(),5,' '));
		icrx.setEtcDebitCredit(CommonUtil.formatStringLeftPad(icrxTemplate.getEtcDebitCredit(),1,' '));
		icrx.setEtcOwedAmount(CommonUtil.formatStringLeftPad(icrxTemplate.getEtcTollAmount(),9,'0'));
		icrx.setEtcDupSerialNum(CommonUtil.formatStringLeftPad(null,20,'0'));
		
		return icrx.toString();
	}
	
	private String getIRXCHeader(FileValidationParam validateParam, List<ICRXTemplate> irxcTemplateList) {

		fileCreateDateandTime = commonUtil.getCurrentUTCDateandTime();
		log.info("fileCreateDateandTime ::" + fileCreateDateandTime);
		fileCreateDateandTime = validateParam.getFileDate()
				+ fileCreateDateandTime.substring(fileCreateDateandTime.indexOf("T"), fileCreateDateandTime.length());
		log.info("After append fileCreateDateandTime ::" + fileCreateDateandTime);
		// Set file name to class variable
		filename = validateParam.getFromAgency() + "_" + validateParam.getToAgency() + "_"
				+ fileCreateDateandTime.replaceAll("[-T:Z]", "") + "." + validateParam.getFileType();
		log.info("IRXC File name creation ::" + filename);
		this.agency = ValidationController.cscIdTagAgencyMap.get(validateParam.getFromAgency());
		Header header = new Header();
		header.setFileType(validateParam.getFileType());
		header.setVersion(agency.getVersionNumber());
		header.setFromAgencyId(validateParam.getFromAgency());
		header.setFileDateTime(fileCreateDateandTime);
		header.setToAgencyId(validateParam.getToAgency());
		header.setRecordCount(CommonUtil.formatStringLeftPad(String.valueOf(irxcTemplateList.size()), 8, '0'));
		header.setIctxfileNum(
				CommonUtil.formatStringLeftPad(String.valueOf(irxcTemplateList.get(0).getIctxFileNum()), 12, '0'));
		return header.toString();
	}
	
	private List<ICRXTemplate> getIRXCTemplateExcel(FileValidationParam validateParam) {
		String ICRX_SHEET = "IRXC";
		try {
			log.info("IRXC Excel data path localtion form getInputFilePath ::"+validateParam.getInputFilePath());
			FileInputStream is = new FileInputStream(validateParam.getInputFilePath());
			try (Workbook workbook = new XSSFWorkbook(is)) {
				log.info("Number of sheets : " + workbook.getNumberOfSheets());
				workbook.setMissingCellPolicy(MissingCellPolicy.CREATE_NULL_AS_BLANK);
				irxcTemplateList = excelToIRXCList(workbook.getSheet(ICRX_SHEET),validateParam);
			}
			log.info("irxcTemplateList :::::::::::::"+irxcTemplateList.size());
		} catch (Exception e) {
			validateParam.setResponseMsg("IRXC File not generated Please check input excel data");
			e.printStackTrace();
		}

		return irxcTemplateList;
	}
	
	private List<ICRXTemplate> excelToIRXCList(Sheet sheet,FileValidationParam validateParam) throws IopTranslatorException {
	   	 log.info("Inside ****************** excelToIRXCList()");
	        try {
	       	
	          Iterator<Row> rows = sheet.iterator();
	          irxcTemplateList = new ArrayList<>();
	          int rowNumber = 0;
	          while (rows.hasNext()) {
	            Row currentRow = rows.next();
	            // skip header
	            if (rowNumber == 0) {
	              rowNumber++;
	              continue;
	            }
	            ICRXTemplate icrxTemp = new ICRXTemplate();
	            icrxTemp.setIctxFileNum(commonUtil.getStringFormatCell(currentRow.getCell(0,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
	            icrxTemp.setEtcTrxSerialNo(commonUtil.getStringFormatCell(currentRow.getCell(2,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
	            icrxTemp.setEtcPostStatus(commonUtil.getStringFormatCell(currentRow.getCell(24,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
	            icrxTemp.setEtcPostPlan(commonUtil.getStringFormatCell(currentRow.getCell(25,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
	            icrxTemp.setEtcDebitCredit(commonUtil.getStringFormatCell(currentRow.getCell(26,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
	            icrxTemp.setEtcTollAmount(commonUtil.getStringFormatCell(currentRow.getCell(27,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
	            icrxTemp.setEtcDupSerialNum(commonUtil.getStringFormatCell(currentRow.getCell(28,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
	            irxcTemplateList.add(icrxTemp);
	          }
	         
	          if(irxcTemplateList != null && irxcTemplateList.size()>0) {
	          log.info("@@@@ IRXC input data  loaded sucessfully:: ******************** ::"+irxcTemplateList.size());
	          }else {
	       	   throw new IopTranslatorException("IRXC input data not loaded");
	          }
	          
			} catch (NullPointerException e) {
				validateParam.setResponseMsg("Excel IRXC  sheet not found. Please check sheet");
				log.error("NullPointerException:: ******************** IRXC Sheet");
				e.printStackTrace();
			} catch (Exception e) {
				log.error("Exception:: ******************** ICTX Sheet");
				e.printStackTrace();
				throw new IopTranslatorException("IRXC Excel input data not loaded.  Please check log file");
			}
	      
			return irxcTemplateList;
		}
	

}
