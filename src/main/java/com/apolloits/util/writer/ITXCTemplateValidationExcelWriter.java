package com.apolloits.util.writer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import com.apolloits.util.modal.FileValidationParam;
import com.apolloits.util.modal.ICTXTemplate;
import com.apolloits.util.modal.ITXCTemplate;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ITXCTemplateValidationExcelWriter {
	
	public void createItxcTemplateExcel(List<ITXCTemplate> itxcTempList, String fileName,
			FileValidationParam validateParam) throws IOException {
		
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheet = workbook.createSheet("IRXC");
		XSSFRow row = sheet.createRow(0);

	    XSSFCellStyle style= workbook.createCellStyle();
	    XSSFFont defaultFont= workbook.createFont();
	    defaultFont.setBold(true);
	    style.setFont(defaultFont);
	   
	    row.createCell(0).setCellValue("ITXC_FILE_NUM");
	    row.getCell(0).setCellStyle(style);
	    row.createCell(1).setCellValue("CORR_REASON");
	    row.getCell(1).setCellStyle(style);
	    row.createCell(2).setCellValue("ETC_TRX_SERIAL_NUM");
	    row.getCell(2).setCellStyle(style);
	    row.createCell(3).setCellValue("ETC_REVENUE_DATE");
	    row.getCell(3).setCellStyle(style);
	    row.createCell(4).setCellValue("ETC_TAG_AGENCY");
	    row.getCell(4).setCellStyle(style);
	    row.createCell(5).setCellValue("ETC_TAG_SERIAL_NUMBER");
	    row.getCell(5).setCellStyle(style);
	    row.createCell(6).setCellValue("ETC_VALIDATION_STATUS");
	    row.getCell(6).setCellStyle(style);
	    row.createCell(7).setCellValue("ETC_LIC_STATE");
	    row.getCell(7).setCellStyle(style);
	    row.createCell(8).setCellValue("ETC_LIC_NUMBER");
	    row.getCell(8).setCellStyle(style);
	    
	    row.createCell(9).setCellValue("ETC_CLASS_CHARGED");
	    row.getCell(9).setCellStyle(style);
	    row.createCell(10).setCellValue("ETC_EXIT_DATE_TIME");
	    row.getCell(10).setCellStyle(style);
	    row.createCell(11).setCellValue("ETC_EXIT_PLAZA");
	    row.getCell(11).setCellStyle(style);
	    row.createCell(12).setCellValue("ETC_EXIT_LANE");
	    row.getCell(12).setCellStyle(style);
	    row.createCell(13).setCellValue("TRX_TYPE");
	    row.getCell(13).setCellStyle(style);
	    row.createCell(14).setCellValue("ETC_ENTRY_DATE_TIME");
	    row.getCell(14).setCellStyle(style);
	    row.createCell(15).setCellValue("ENTRY_PLAZA");
	    row.getCell(15).setCellStyle(style);
	    row.createCell(16).setCellValue("ENTRY_LANE");
	    row.getCell(16).setCellStyle(style);
	    
	    row.createCell(17).setCellValue("READ_PERF");
	    row.getCell(17).setCellStyle(style);
	    row.createCell(18).setCellValue("WRITE_PERF");
	    row.getCell(18).setCellStyle(style);
	    row.createCell(19).setCellValue("TAG_PGM_STATUS");
	    row.getCell(19).setCellStyle(style);
	    
	    row.createCell(20).setCellValue("LANE_MODE");
	    row.getCell(20).setCellStyle(style);
	    row.createCell(21).setCellValue("OVER_SPEED");
	    row.getCell(21).setCellStyle(style);
	    row.createCell(22).setCellValue("DEBIT_CREDIT");
	    row.getCell(22).setCellStyle(style);
	    row.createCell(23).setCellValue("ETC_TOLL_AMOUNT");
	    row.getCell(23).setCellStyle(style);
	    
	    //ICRX Data
	    row.createCell(24).setCellValue("ETC_POST_STATUS");
	    row.getCell(24).setCellStyle(style);
	    row.createCell(25).setCellValue("ETC_POST_PLAN");
	    row.getCell(25).setCellStyle(style);
	    row.createCell(26).setCellValue("DEBIT_CREDIT");
	    row.getCell(26).setCellStyle(style);
	    row.createCell(27).setCellValue("ETC_OWED_AMOUNT");
	    row.getCell(27).setCellStyle(style);
	    row.createCell(28).setCellValue("ETC_DUP_SERIAL_NUM");
	    row.getCell(28).setCellStyle(style);
	    
int dataRowIndex = 1;
	    
	    for (ITXCTemplate ictxTemp : itxcTempList) {
	    	XSSFRow dataRow = sheet.createRow(dataRowIndex);
	        dataRow.createCell(0).setCellValue(ictxTemp.getItxcFileNum());
	        dataRow.createCell(1).setCellValue(ictxTemp.getCorrReason());
	        dataRow.createCell(2).setCellValue(ictxTemp.getEtcTrxSerialNo());
	        dataRow.createCell(3).setCellValue(ictxTemp.getEtcRevenueDate());
	        dataRow.createCell(4).setCellValue(ictxTemp.getEtcTagAgency());
	        dataRow.createCell(5).setCellValue(ictxTemp.getEtcTagSerialNumber());
	        dataRow.createCell(6).setCellValue(ictxTemp.getEtcValidationStatus());
	        dataRow.createCell(7).setCellValue(ictxTemp.getEtcLicState());
	        dataRow.createCell(8).setCellValue(ictxTemp.getEtcLicNumber());
	        dataRow.createCell(9).setCellValue(ictxTemp.getEtcClassCharged());
	        dataRow.createCell(10).setCellValue(ictxTemp.getEtcExitDateTime());
	        dataRow.createCell(11).setCellValue(ictxTemp.getEtcExitPlaza());
	        dataRow.createCell(12).setCellValue(ictxTemp.getEtcExitLane());
	        dataRow.createCell(13).setCellValue(ictxTemp.getEtcTrxType());
	        dataRow.createCell(14).setCellValue(ictxTemp.getEtcEntryDateTime());
	        dataRow.createCell(15).setCellValue(ictxTemp.getEtcEntryPlaza());
	        dataRow.createCell(16).setCellValue(ictxTemp.getEtcEntryLane());
	        dataRow.createCell(17).setCellValue(ictxTemp.getEtcReadPerformance());
	        dataRow.createCell(18).setCellValue(ictxTemp.getEtcWritePerf());
	        dataRow.createCell(19).setCellValue(ictxTemp.getEtcTagPgmStatus());
	        dataRow.createCell(20).setCellValue(ictxTemp.getEtcLaneMode());
	        dataRow.createCell(21).setCellValue(ictxTemp.getEtcOverSpeed());
	        dataRow.createCell(22).setCellValue(ictxTemp.getEtcDebitCredit());
	        dataRow.createCell(23).setCellValue(ictxTemp.getEtcTollAmount());
	        
	        //ICRX cells
	        
	        dataRowIndex++;
	    }
	    
	    for (int i = 0; i < 24; i++) {
	        sheet.autoSizeColumn(i);
	    }
	    
	  //ServletOutputStream ops = response.getOutputStream();
	    try (FileOutputStream outputStream = new FileOutputStream(fileName)) {
	        workbook.write(outputStream);
	    }catch (Exception e) {
	    	log.error("Exception in ICTX Template excel creation filename ::"+fileName);
			e.printStackTrace();
			
		}
	    workbook.close();
	    log.info("ITXC Template list file generated ::"+fileName);
	    

	}

}
