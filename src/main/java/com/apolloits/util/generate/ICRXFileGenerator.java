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
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
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
import com.apolloits.util.modal.ICRX;
import com.apolloits.util.modal.ICRXTemplate;
import com.apolloits.util.modal.ICTX;
import com.apolloits.util.modal.ICTXTemplate;
import com.apolloits.util.utility.CommonUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ICRXFileGenerator {
	
	@Autowired
	CommonUtil commonUtil;
	
	@Autowired
	@Lazy
	ValidationController controller;
	
	private String filename = "";
	private String fileCreateDateandTime = "";
	AgencyEntity agency;
	
	private List<ICRXTemplate> icrxTemplateList;

	public boolean icrxGen(FileValidationParam validateParam,String fileType) throws IOException {
		if (!commonUtil.validateParameter(validateParam)) {
			return false;
		}
		
		icrxTemplateList = getICRXTemplateExcel(validateParam);
		String Header = getICRXHeader(validateParam,icrxTemplateList);
		log.info("ICTX Header :: " + Header);
		
		writeDetails(validateParam,Header,icrxTemplateList);
		String filePath = validateParam.getOutputFilePath() + File.separator + filename;
		String zipFilename = commonUtil.moveToZipFile(filePath,validateParam);
		log.info("ICRX ZIP file name :: "+zipFilename);
		validateParam.setResponseMsg("ICRX File created ::\t "+zipFilename);
		
		return false;
	}
	
	private void writeDetails(FileValidationParam validateParam, String header,List<ICRXTemplate> icrxTempList)
			throws IOException {
		long start = System.currentTimeMillis();
		FileWriter writer;
		try {
			String filePath = validateParam.getOutputFilePath() + File.separator + filename;
			writer = new FileWriter(filePath, true);
			writer.write(header);
			writer.write(System.lineSeparator());
			System.out.print("Writing record raw... ");
			for (ICRXTemplate icrxTemplate : icrxTempList) {
				writer.write(setICRXDetailValues(icrxTemplate,validateParam));
				writer.write(System.lineSeparator());
			}
			//writer.flush();
			writer.close();
		}catch (Exception e) {
			validateParam.setResponseMsg("ICRX File not generated Please check log");
			e.printStackTrace();
		}
		long end = System.currentTimeMillis();
		log.info("ICRX file("+filename+") generation time :: "+(end - start) / 1000f + " seconds");
	}
	
	private String setICRXDetailValues(ICRXTemplate icrxTemplate,FileValidationParam validateParam) {
		ICRX icrx = new ICRX();
		
		icrx.setEtcTrxSerialNum(CommonUtil.formatStringLeftPad(icrxTemplate.getEtcTrxSerialNo(),20,'0'));
		icrx.setEtcPostStatus(CommonUtil.formatStringLeftPad(icrxTemplate.getEtcPostStatus(),4,' '));
		icrx.setEtcPostPlan(CommonUtil.formatStringLeftPad(icrxTemplate.getEtcPostPlan(),5,' '));
		icrx.setEtcDebitCredit(CommonUtil.formatStringLeftPad(icrxTemplate.getEtcDebitCredit(),1,' '));
		icrx.setEtcOwedAmount(CommonUtil.formatStringLeftPad(icrxTemplate.getEtcTollAmount(),9,'0'));
		icrx.setEtcDupSerialNum(CommonUtil.formatStringLeftPad(null,20,'0'));
		
		return icrx.toString();
	}
	

	private List<ICRXTemplate> getICRXTemplateExcel(FileValidationParam validateParam) {
		String ICRX_SHEET = "ICRX";
		try {
			log.info("ICRX Excel data path localtion form getInputFilePath ::"+validateParam.getInputFilePath());
			FileInputStream is = new FileInputStream(validateParam.getInputFilePath());
			try (Workbook workbook = new XSSFWorkbook(is)) {
				log.info("Number of sheets : " + workbook.getNumberOfSheets());
				workbook.setMissingCellPolicy(MissingCellPolicy.CREATE_NULL_AS_BLANK);
				icrxTemplateList = excelToICRXList(workbook.getSheet(ICRX_SHEET));
			}
			log.info("icrxTemplateList :::::::::::::"+icrxTemplateList.size());
		} catch (Exception e) {
			validateParam.setResponseMsg("ICRX File not generated Please check input excel data");
			e.printStackTrace();
		}

		return icrxTemplateList;
	}
	
	private List<ICRXTemplate> excelToICRXList(Sheet sheet) throws IopTranslatorException {
	   	 log.info("Inside ****************** excelToICRXList()");
	        try {
	       	
	          Iterator<Row> rows = sheet.iterator();
	          icrxTemplateList = new ArrayList<>();
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
	            icrxTemp.setEtcTrxSerialNo(commonUtil.getStringFormatCell(currentRow.getCell(1,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
	            icrxTemp.setEtcPostStatus(commonUtil.getStringFormatCell(currentRow.getCell(23,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
	            icrxTemp.setEtcPostPlan(commonUtil.getStringFormatCell(currentRow.getCell(24,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
	            icrxTemp.setEtcDebitCredit(commonUtil.getStringFormatCell(currentRow.getCell(25,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
	            icrxTemp.setEtcTollAmount(commonUtil.getStringFormatCell(currentRow.getCell(26,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
	            icrxTemp.setEtcDupSerialNum(commonUtil.getStringFormatCell(currentRow.getCell(27,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
	            /* int cellIdx = 0;
	            while (cellsInRow.hasNext()) {
	              Cell currentCell = cellsInRow.next();
	              switch (cellIdx) {
	              case 0:
	            	icrxTemp.setIctxFileNum(commonUtil.getStringFormatCell(currentCell));
	           	   System.out.println("case 0::"+icrxTemp.getIctxFileNum());
	                break;
	              case 1:
	            	  icrxTemp.setEtcTrxSerialNo(commonUtil.getStringFormatCell(currentCell));
	                  System.out.println("case 1::"+icrxTemp.getEtcTrxSerialNo());
	                break;
	              case 23:
	            	  icrxTemp.setEtcPostStatus(commonUtil.getStringFormatCell(currentCell));
	                  System.out.println("case 23 getEtcPostStatus::"+icrxTemp.getEtcPostStatus());
	                break;
	              case 24:
	            	  icrxTemp.setEtcPostPlan(commonUtil.getStringFormatCell(currentCell));
	                  System.out.println("case 24 getEtcPostPlan::"+icrxTemp.getEtcPostPlan());
	                break;
	              case 25:
	            	  icrxTemp.setEtcDebitCredit(commonUtil.getStringFormatCell(currentCell));
	                  System.out.println("case 25 getEtcDebitCredit::"+icrxTemp.getEtcDebitCredit());
	                break;
	              case 26:
	            	  icrxTemp.setEtcTollAmount(commonUtil.getStringFormatCell(currentCell));
	                System.out.println("case 26 getEtcTollAmount::"+icrxTemp.getEtcTollAmount());
	                break;
	              case 27:
	            	  icrxTemp.setEtcDupSerialNum(commonUtil.getStringFormatCell(currentCell));
	                System.out.println("case 27 getEtcDupSerialNum::"+icrxTemp.getEtcDupSerialNum());
	                break;
	              default:
	           	 //  System.out.println("Default:: ********************");
	                break;
	              }
	              cellIdx++;
	             
	            } */
	            icrxTemplateList.add(icrxTemp);
	          }
	         
	          if(icrxTemplateList != null && icrxTemplateList.size()>0) {
	          log.info("@@@@ ICRX input data  loaded sucessfully:: ******************** ::"+icrxTemplateList.size());
	          }else {
	       	   throw new IopTranslatorException("ICRX input data not loaded");
	          }
	          
	        }catch (Exception e) {
	       	log.error("Exception:: ******************** ICTX Sheet");
				e.printStackTrace();
				throw new IopTranslatorException("ICRX Excel input data not loaded.  Please check log file");
			}
	      
			return icrxTemplateList;
		}

	private String getICRXHeader(FileValidationParam validateParam, List<ICRXTemplate> icrxTemplateList) {
		
		fileCreateDateandTime = commonUtil.getCurrentUTCDateandTime();
		log.info("fileCreateDateandTime ::"+fileCreateDateandTime);
		fileCreateDateandTime = validateParam.getFileDate()+fileCreateDateandTime.substring(fileCreateDateandTime.indexOf("T"),fileCreateDateandTime.length());
		log.info("After append fileCreateDateandTime ::"+fileCreateDateandTime);
		//Set file name to class variable
		filename =  validateParam.getFromAgency() +"_"+validateParam.getToAgency()+"_"+ fileCreateDateandTime.replaceAll("[-T:Z]", "")+"."+validateParam.getFileType();
		log.info("ICRX File name creation ::"+filename);
		this.agency = ValidationController.cscIdTagAgencyMap.get(validateParam.getFromAgency());
		Header header = new Header();
		header.setFileType(validateParam.getFileType());
		header.setVersion(agency.getVersionNumber());
		header.setFromAgencyId(validateParam.getFromAgency());
		header.setFileDateTime(fileCreateDateandTime);
		header.setToAgencyId(validateParam.getToAgency());
		header.setRecordCount(CommonUtil.formatStringLeftPad(String.valueOf(icrxTemplateList.size()), 8, '0') );
		header.setIctxfileNum(CommonUtil.formatStringLeftPad(String.valueOf(icrxTemplateList.get(0).getIctxFileNum()),12,'0'));
		return header.toString();
	}

}
