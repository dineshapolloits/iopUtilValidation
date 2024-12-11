package com.apolloits.util.generate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import com.apolloits.util.reader.AgencyDataExcelReader;
import com.apolloits.util.utility.CommonUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ICTXFileGenerator {
	
	@Autowired
	CommonUtil commonUtil;
	
	@Autowired
	@Lazy
	ValidationController controller;
	
	private String filename = "";
	private String fileCreateDateandTime = "";
	AgencyEntity agency;
	
	private List<ICTXTemplate> ictxTemplateList;
	
	public boolean ictxGen(FileValidationParam validateParam) throws IOException {
		
		if (!commonUtil.validateParameter(validateParam)) {
			return false;
		}
		
		ictxTemplateList = getICTXTemplateExcel(validateParam);
		String Header = getICTXHeader(validateParam,ictxTemplateList);
		log.info("ICTX Header :: " + Header);
		writeDetails(validateParam,Header,ictxTemplateList);
		String filePath = validateParam.getOutputFilePath() + File.separator + filename;
		String zipFilename = commonUtil.moveToZipFile(filePath,validateParam);
		log.info("ICTX Zip file name :: "+zipFilename);
		validateParam.setResponseMsg("ICTX file created ::\t "+zipFilename);
		return true;
	}
	
	private void writeDetails(FileValidationParam validateParam, String header,List<ICTXTemplate> ictxTempList)
			throws IOException {
		long start = System.currentTimeMillis();
		FileWriter writer;
		try {
			String filePath = validateParam.getOutputFilePath() + File.separator + filename;
			writer = new FileWriter(filePath, true);
			writer.write(header);
			writer.write(System.lineSeparator());
			System.out.print("Writing record raw... ");
			for (ICTXTemplate ictxTemplate : ictxTempList) {
				writer.write(setICTXDetailValues(ictxTemplate,validateParam));
				writer.write(System.lineSeparator());
			}
			writer.flush();
			writer.close();
		}catch (Exception e) {
			validateParam.setResponseMsg("File not generated Please check log");
			e.printStackTrace();
		}
	}
	

	private String setICTXDetailValues(ICTXTemplate ictxTemplate,FileValidationParam validateParam) {
		ICTX ictx = new ICTX();
		
		ictx.setEtcTrxSerialNum(CommonUtil.formatStringLeftPad(ictxTemplate.getEtcTrxSerialNo(),20,'0'));
		ictx.setEtcRevenueDate(CommonUtil.formatStringLeftPad(ictxTemplate.getEtcRevenueDate(),8,' '));
		ictx.setEtcFacAgency(validateParam.getFromAgency());
		ictx.setEtcTrxType(CommonUtil.formatStringLeftPad(ictxTemplate.getEtcTrxType(), 1, ' '));
		ictx.setEtcEntryDateTime(CommonUtil.formatStringLeftPad(ictxTemplate.getEtcEntryDateTime(),25,'*'));
		ictx.setEtcEntryPlaza(CommonUtil.formatStringRightPad(ictxTemplate.getEtcEntryPlaza(),15,'*'));
		ictx.setEtcEntryLane(CommonUtil.formatStringRightPad(ictxTemplate.getEtcEntryLane(),3,'*'));
		ictx.setEtcTagAgency(CommonUtil.formatStringLeftPad(ictxTemplate.getEtcTagAgency(),4,' '));
		ictx.setEtcTagSerialNumber(CommonUtil.formatStringLeftPad(ictxTemplate.getEtcTagSerialNumber(),10,'0'));
		ictx.setEtcReadPerformance(CommonUtil.formatStringRightPad(ictxTemplate.getEtcReadPerformance(),2,'*'));
		ictx.setEtcWritePerf(CommonUtil.formatStringLeftPad(ictxTemplate.getEtcWritePerf(),2,'*'));
		ictx.setEtcTagPgmStatus(CommonUtil.formatStringLeftPad(ictxTemplate.getEtcTagPgmStatus(),1,'*'));
		ictx.setEtcLaneMode(CommonUtil.formatStringRightPad(ictxTemplate.getEtcLaneMode(),1,'O'));
		ictx.setEtcValidationStatus(CommonUtil.formatStringLeftPad(ictxTemplate.getEtcValidationStatus(),1,'*'));
		ictx.setEtcLicState(CommonUtil.formatStringRightPad(ictxTemplate.getEtcLicState(),2,' '));
		ictx.setEtcLicNumber(CommonUtil.formatStringRightPad(ictxTemplate.getEtcLicNumber(),10,' '));
		ictx.setEtcLicType(CommonUtil.formatStringRightPad("",30,'*')); //
		ictx.setEtcClassCharged(CommonUtil.formatStringRightPad(ictxTemplate.getEtcClassCharged(),3,' '));
		ictx.setEtcActualAxles("02");
		ictx.setEtcExitSpeed("000");
		ictx.setEtcOverSpeed(CommonUtil.formatStringRightPad(ictxTemplate.getEtcOverSpeed(),1,'N'));
		ictx.setEtcExitDateTime(CommonUtil.formatStringLeftPad(ictxTemplate.getEtcExitDateTime(),25,' '));
		ictx.setEtcExitPlaza(CommonUtil.formatStringRightPad(ictxTemplate.getEtcExitPlaza(),15,' '));
		ictx.setEtcExitLane(CommonUtil.formatStringRightPad(ictxTemplate.getEtcExitLane(),3,' '));
		ictx.setEtcDebitCredit(CommonUtil.formatStringRightPad(ictxTemplate.getEtcDebitCredit(),1,' '));
		ictx.setEtcTollAmount(CommonUtil.formatStringLeftPad(ictxTemplate.getEtcTollAmount(),9,'0'));
		return ictx.toString();
	}
	
	private List<ICTXTemplate> getICTXTemplateExcel(FileValidationParam validateParam) {
		//ictxTemplateList = new ArrayList<>();
		//String TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
		String ICTX_SHEET = "ICTX";
		//InputStream inputStream;

		try {
			log.info("Excel data path localtion form getInputFilePath ::"+validateParam.getInputFilePath());
			FileInputStream is = new FileInputStream(validateParam.getInputFilePath());
			Workbook workbook = new XSSFWorkbook(is);
			log.info("Number of sheets : " + workbook.getNumberOfSheets());

			ictxTemplateList = excelToICTXList(workbook.getSheet(ICTX_SHEET));

		} catch (Exception e) {
			validateParam.setResponseMsg("File not generated Please check input excel data");
			e.printStackTrace();
		}

		return ictxTemplateList;
	}

	private List<ICTXTemplate> excelToICTXList(Sheet sheet) {
   	 log.info("Inside ****************** excelToICTXList()");
        try {
       	
          Iterator<Row> rows = sheet.iterator();
          ictxTemplateList = new ArrayList<>();
          int rowNumber = 0;
          while (rows.hasNext()) {
            Row currentRow = rows.next();
            // skip header
            if (rowNumber == 0) {
              rowNumber++;
              continue;
            }
            Iterator<Cell> cellsInRow = currentRow.iterator();
            ICTXTemplate ictxTemp = new ICTXTemplate();
            int cellIdx = 0;
            while (cellsInRow.hasNext()) {
              Cell currentCell = cellsInRow.next();
              switch (cellIdx) {
              case 0:
            	ictxTemp.setIctxFileNum(String.valueOf(getStringFormatCell(currentCell)));
           	   System.out.println("case 0::"+ictxTemp.getIctxFileNum());
                break;
              case 1:
            	  ictxTemp.setEtcTrxSerialNo(String.valueOf(getStringFormatCell(currentCell)));
                  System.out.println("case 1::"+currentCell.getNumericCellValue());
                break;
              case 2:
            	  ictxTemp.setEtcRevenueDate(getStringFormatCell(currentCell));
                  System.out.println("case 2::"+ictxTemp.getEtcTrxSerialNo());
                break;
              case 3:
            	  ictxTemp.setEtcTagAgency(getStringFormatCell(currentCell));
                System.out.println("case 3::"+ictxTemp.getEtcTagAgency());
                break;
              case 4:
            	  ictxTemp.setEtcTagSerialNumber(getStringFormatCell(currentCell));
                  System.out.println("case 4::"+ictxTemp.getEtcTagSerialNumber());
                  break;
              case 5:
            	  ictxTemp.setEtcValidationStatus(getStringFormatCell(currentCell));
                  System.out.println("case 5::"+ictxTemp.getEtcValidationStatus());
                  break;
              case 6:
            	  ictxTemp.setEtcLicState(getStringFormatCell(currentCell));
                  System.out.println("case 6::"+ictxTemp.getEtcLicState());
                  break;
              case 7:
            	  ictxTemp.setEtcLicNumber(getStringFormatCell(currentCell));
                  System.out.println("case 7::"+ictxTemp.getEtcLicNumber());
                  break;
              case 8:
            	  ictxTemp.setEtcClassCharged(getStringFormatCell(currentCell));
                  System.out.println("case 8::"+ictxTemp.getEtcClassCharged());
                  break;
              case 9:
            	  ictxTemp.setEtcExitDateTime(getStringFormatCell(currentCell));
                  System.out.println("case 9::"+ictxTemp.getEtcExitDateTime());
                  break;
              case 10:
            	  ictxTemp.setEtcExitPlaza(getStringFormatCell(currentCell));
                  System.out.println("case 10::"+ictxTemp.getEtcExitPlaza());
                  break;
              case 11:
            	  ictxTemp.setEtcExitLane(getStringFormatCell(currentCell));
                  System.out.println("case 11::"+ictxTemp.getEtcExitLane());
                  break;
              case 12:
            	  ictxTemp.setEtcTrxType(getStringFormatCell(currentCell));
                  System.out.println("case 12::"+ictxTemp.getEtcTrxType());
                  break;
              case 13:
            	  ictxTemp.setEtcEntryDateTime(getStringFormatCell(currentCell));
                  System.out.println("case 13::"+ictxTemp.getEtcEntryDateTime());
                  break;
              case 14:
            	  ictxTemp.setEtcEntryPlaza(getStringFormatCell(currentCell));
                  System.out.println("case 14::"+ictxTemp.getEtcEntryPlaza());
                  break;
              case 15:
            	  ictxTemp.setEtcEntryLane(getStringFormatCell(currentCell));
                  System.out.println("case 15::"+ictxTemp.getEtcEntryLane());
                  break;
              case 16:
            	  ictxTemp.setEtcReadPerformance(getStringFormatCell(currentCell));
                  System.out.println("case 16::"+ictxTemp.getEtcReadPerformance());
                  break;
              case 17:
            	  ictxTemp.setEtcWritePerf(getStringFormatCell(currentCell));
                  System.out.println("case 17::"+ictxTemp.getEtcWritePerf());
                  break;
              case 18:
            	  ictxTemp.setEtcTagPgmStatus(getStringFormatCell(currentCell));
                  System.out.println("case 18::"+ictxTemp.getEtcTagPgmStatus());
                  break;
              case 19:
            	  ictxTemp.setEtcLaneMode(getStringFormatCell(currentCell));
                  System.out.println("case 19::"+ictxTemp.getEtcLaneMode());
                  break;
              case 20:
            	  ictxTemp.setEtcOverSpeed(getStringFormatCell(currentCell));
                  System.out.println("case 20::"+ictxTemp.getEtcOverSpeed());
                  break;
              case 21:
            	  ictxTemp.setEtcDebitCredit(getStringFormatCell(currentCell));
                  System.out.println("case 21::"+ictxTemp.getEtcDebitCredit());
                  break;
              case 22:
            	  ictxTemp.setEtcTollAmount(getStringFormatCell(currentCell));
                  System.out.println("case 22::"+ictxTemp.getEtcTollAmount());
                  break;
              default:
           	 //  System.out.println("Default:: ********************");
                break;
              }
              cellIdx++;
             
            }
            ictxTemplateList.add(ictxTemp);
          }
         
          if(ictxTemplateList != null && ictxTemplateList.size()>0) {
          log.info("@@@@ ICTX input data  loaded sucessfully:: ******************** ::"+ictxTemplateList.size());
          }else {
       	   throw new IopTranslatorException("ICTX input data not loaded");
          }
          
        }catch (Exception e) {
       	log.error("Exception:: ******************** ICTX Sheet");
			e.printStackTrace();
		}
      
		return ictxTemplateList;
	}
	
	private String getStringFormatCell(Cell cell) {
		System.out.println("cell type ::"+cell.getCellType());
		String value ="";
		switch (cell.getCellType())               
		{  
		case STRING:    //field that represents string cell type  
		System.out.println("String :: "+cell.getStringCellValue() + "\t\t\t"); 
		value = cell.getStringCellValue();
		break;  
		case NUMERIC:    //field that represents number cell type  
		System.out.println("Number :: "+cell.getNumericCellValue() + "\t\t\t");
		value = String.valueOf((int)cell.getNumericCellValue());
		break; 
		case BLANK:    //field that represents number cell type  
			System.out.println("Blank ");  
			break;
		default:  
		}  
		System.out.println("return Value :: "+value);
		return value;
	}

	private String getICTXHeader(FileValidationParam validateParam,List<ICTXTemplate> ictxTempList) {
		
		fileCreateDateandTime = commonUtil.getCurrentUTCDateandTime();
		log.info("fileCreateDateandTime ::"+fileCreateDateandTime);
		fileCreateDateandTime = validateParam.getFileDate()+fileCreateDateandTime.substring(fileCreateDateandTime.indexOf("T"),fileCreateDateandTime.length());
		log.info("After append fileCreateDateandTime ::"+fileCreateDateandTime);
		//Set file name to class variable
		filename =  validateParam.getFromAgency() +"_"+validateParam.getToAgency()+"_"+ fileCreateDateandTime.replaceAll("[-T:Z]", "")+IAGConstants.ICTX_FILE_EXTENSION;
		log.info("File name creation ::"+filename);
		this.agency = ValidationController.cscIdTagAgencyMap.get(validateParam.getFromAgency());
		Header header = new Header();
		header.setFileType(IAGConstants.ICTX_FILE_TYPE);
		header.setVersion(agency.getVersionNumber());
		header.setFromAgencyId(validateParam.getFromAgency());
		header.setFileDateTime(fileCreateDateandTime);
		header.setToAgencyId(validateParam.getToAgency());
		header.setRecordCount(CommonUtil.formatStringLeftPad(String.valueOf(ictxTempList.size()), 8, '0') );
		header.setIctxfileNum(CommonUtil.formatStringLeftPad(String.valueOf(ictxTempList.get(0).getIctxFileNum()),12,'0'));
		return header.toString();
	}

	

}
