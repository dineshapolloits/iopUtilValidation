package com.apolloits.util.writer;

import java.io.FileNotFoundException;
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
import com.apolloits.util.modal.PBPTemplate;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PBPTemplateValidationExcelWriter {



	public void createPbpTemplateExcel(List<PBPTemplate> pbpTempList,String fileName,FileValidationParam validateParam) throws FileNotFoundException, IOException {
		
		log.info("Inside createTolTemplateExcel() :: tolTempList size ::"+pbpTempList.size() +"\t FileName ::"+fileName);
		
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheet = workbook.createSheet("PRC");
		XSSFRow row = sheet.createRow(0);

	    XSSFCellStyle style= workbook.createCellStyle();
	    XSSFFont defaultFont= workbook.createFont();
	    defaultFont.setBold(true);
	    style.setFont(defaultFont);
	   
	    row.createCell(0).setCellValue("SEQUENCE");
	    row.getCell(0).setCellStyle(style);
	    row.createCell(1).setCellValue("LICENSE PLATE");
	    row.getCell(1).setCellStyle(style);
	    row.createCell(2).setCellValue("TRAN #");
	    row.getCell(2).setCellStyle(style);
	    row.createCell(3).setCellValue("STATE");
	    row.getCell(3).setCellStyle(style);
	    row.createCell(4).setCellValue("TRAN AMOUNT");
	    row.getCell(4).setCellStyle(style);
	    row.createCell(5).setCellValue("ENTRY TRAN DATE");
	    row.getCell(5).setCellStyle(style);
	    row.createCell(6).setCellValue("ENTRY PLAZA");
	    row.getCell(6).setCellStyle(style);
	    row.createCell(7).setCellValue("ENTRY LANE");
	    row.getCell(7).setCellStyle(style);
	    row.createCell(8).setCellValue("EXIT TRAN DATE");
	    row.getCell(8).setCellStyle(style);
	    row.createCell(9).setCellValue("EXIT PLAZA");
	    row.getCell(9).setCellStyle(style);
	    
	    row.createCell(10).setCellValue("EXIT LANE");
	    row.getCell(10).setCellStyle(style);
	    row.createCell(11).setCellValue("AXLE COUNT");
	    row.getCell(11).setCellStyle(style);
	    row.createCell(12).setCellValue("LP TYPE");
	    row.getCell(12).setCellStyle(style);
	   
	    row.createCell(13).setCellValue("WR TRAN FEE");
	    row.getCell(13).setCellStyle(style);
	    row.createCell(14).setCellValue("WR FEE TYPE");
	    row.getCell(14).setCellStyle(style);
	    
	    //RECON DATA
	    row.createCell(15).setCellValue("POST AMT");
	    row.getCell(15).setCellStyle(style);
	    row.createCell(16).setCellValue("RESPONSE CODE");
	    row.getCell(16).setCellStyle(style);
	    row.createCell(17).setCellValue("NIOP FEE");
	    row.getCell(17).setCellStyle(style);
	    row.createCell(18).setCellValue("ORIGINAL PBP FILENAME");
	    row.getCell(18).setCellStyle(style);
	    
	   // XSSFCellStyle dateCellStyle = workbook.createCellStyle();
	    //XSSFDataFormat dateFormat = workbook.createDataFormat();
	    
	    int dataRowIndex = 1;
	    
	    for (PBPTemplate tolTemp : pbpTempList) {
	    	XSSFRow dataRow = sheet.createRow(dataRowIndex);
	       // dataRow.createCell(0).setCellValue(dataRowIndex);
	        dataRow.createCell(0).setCellValue(tolTemp.getSequence());
	        dataRow.createCell(1).setCellValue(tolTemp.getLicensePlate());
	        dataRow.createCell(2).setCellValue(tolTemp.getTran());
	        dataRow.createCell(3).setCellValue(tolTemp.getState());
	        dataRow.createCell(4).setCellValue(tolTemp.getTranAmount());
	        dataRow.createCell(5).setCellValue(tolTemp.getEntryTranDate());
	        dataRow.createCell(6).setCellValue(tolTemp.getEntryPlaza());
	        dataRow.createCell(7).setCellValue(tolTemp.getEntryLane());
	        dataRow.createCell(8).setCellValue(tolTemp.getExitTranDate());
	        dataRow.createCell(9).setCellValue(tolTemp.getExitPlaza());
	        dataRow.createCell(10).setCellValue(tolTemp.getExitLane());
	        dataRow.createCell(11).setCellValue(tolTemp.getAxleCount());
	        dataRow.createCell(12).setCellValue(tolTemp.getLpType());
	       
	       // dataRow.createCell(13).setCellValue(tolTemp.getVehicleType());
	        dataRow.createCell(13).setCellValue(tolTemp.getWrTranFee());
	        dataRow.createCell(14).setCellValue(tolTemp.getWrFeeType());
	      //  dataRow.createCell(15).setCellValue(tolTemp.getGuarantee());
	       
	        
	        //ICRX cells
	        
	        dataRowIndex++;
	    }
	    
	    for (int i = 0; i < 23; i++) {
	        sheet.autoSizeColumn(i);
	    }
	    
	    //ServletOutputStream ops = response.getOutputStream();
	    try (FileOutputStream outputStream = new FileOutputStream(fileName)) {
	        workbook.write(outputStream);
	    }catch (Exception e) {
	    	log.error("Exception in TOL Template excel creation filename ::"+fileName);
			e.printStackTrace();
			
		}
	    workbook.close();
	    log.info("TOL Template list file generated ::"+fileName);
	}
			


}
