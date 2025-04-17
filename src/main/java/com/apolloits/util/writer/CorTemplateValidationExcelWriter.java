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

import com.apolloits.util.modal.CORTemplate;
import com.apolloits.util.modal.FileValidationParam;
import com.apolloits.util.modal.TOLTemplate;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CorTemplateValidationExcelWriter {



	public void createCorTemplateExcel(List<CORTemplate> tolTempList,String fileName,FileValidationParam validateParam) throws FileNotFoundException, IOException {
		
		log.info("Inside createTolTemplateExcel() :: tolTempList size ::"+tolTempList.size() +"\t FileName ::"+fileName);
		
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheet = workbook.createSheet("CRC");
		XSSFRow row = sheet.createRow(0);

	    XSSFCellStyle style= workbook.createCellStyle();
	    XSSFFont defaultFont= workbook.createFont();
	    defaultFont.setBold(true);
	    style.setFont(defaultFont);
	   
	    row.createCell(0).setCellValue("SEQUENCE#");
	    row.getCell(0).setCellStyle(style);
	    row.createCell(1).setCellValue("CORRECTION DATE");
	    row.getCell(1).setCellStyle(style);
	    row.createCell(2).setCellValue("CORRECTION REASON");
	    row.getCell(2).setCellStyle(style);
	    row.createCell(3).setCellValue("RESUBMIT REASON");
	    row.getCell(3).setCellStyle(style);
	    row.createCell(4).setCellValue("CORRECTION COUNT");
	    row.getCell(4).setCellStyle(style);
	    row.createCell(5).setCellValue("RESUBMIT COUNT");
	    row.getCell(5).setCellStyle(style);
	    row.createCell(6).setCellValue("HOME AGENCY SEQUENCE#");
	    row.getCell(6).setCellStyle(style);
	    row.createCell(7).setCellValue("ORIGINAL TAG ID");
	    row.getCell(7).setCellStyle(style);
	    row.createCell(8).setCellValue("ORIGINAL LICENSE PLATE");
	    row.getCell(8).setCellStyle(style);
	    row.createCell(9).setCellValue("ORIGINAL STATE");
	    row.getCell(9).setCellStyle(style);
	    row.createCell(10).setCellValue("ORIGINAL TRAN#");
	    row.getCell(10).setCellStyle(style);
	    row.createCell(11).setCellValue("ORIGINAL TRAN AMOUNT");
	    row.getCell(11).setCellStyle(style);
	    row.createCell(12).setCellValue("ORIGINAL ENTRY TRAN DATE");
	    row.getCell(12).setCellStyle(style);
	    row.createCell(13).setCellValue("ORIGINAL ENTRY PLAZA");
	    row.getCell(13).setCellStyle(style);
	    row.createCell(14).setCellValue("ORIGINAL ENTRY LANE");
	    row.getCell(14).setCellStyle(style);
	    row.createCell(15).setCellValue("ORIGINAL EXIT TRAN DATE");
	    row.getCell(15).setCellStyle(style);
	    row.createCell(16).setCellValue("ORIGINAL EXIT PLAZA");
	    row.getCell(16).setCellStyle(style);
	    row.createCell(17).setCellValue("ORIGINAL EXIT LANE");
	    row.getCell(17).setCellStyle(style);
	    row.createCell(18).setCellValue("ORIGINAL AXLE COUNT");
	    row.getCell(18).setCellStyle(style);
	    row.createCell(19).setCellValue("ORIGINAL OCCUPANCY");
	    row.getCell(19).setCellStyle(style);
	    row.createCell(20).setCellValue("ORIGINAL PROTOCOL TYPE");
	    row.getCell(20).setCellStyle(style);
	    row.createCell(21).setCellValue("ORIGINAL VEHICLE TYPE");
	    row.getCell(21).setCellStyle(style);
	    row.createCell(22).setCellValue("ORIGINAL LP TYPE");
	    row.getCell(22).setCellStyle(style);
	    row.createCell(23).setCellValue("ORIGINAL TRAN FEE");
	    row.getCell(23).setCellStyle(style);
	    row.createCell(24).setCellValue("ORIGINAL TRAN FEE TYPE");
	    row.getCell(24).setCellStyle(style);
	    
	    row.createCell(25).setCellValue("CORR TAG ID");
	    row.getCell(25).setCellStyle(style);
	    row.createCell(26).setCellValue("CORR LICENSE PLATE");
	    row.getCell(26).setCellStyle(style);
	    row.createCell(27).setCellValue("CORR STATE");
	    row.getCell(27).setCellStyle(style);
	    row.createCell(28).setCellValue("CORR TRAN#");
	    row.getCell(28).setCellStyle(style);
	    row.createCell(29).setCellValue("CORR TRAN AMOUNT");
	    row.getCell(29).setCellStyle(style);
	    row.createCell(30).setCellValue("CORR ENTRY TRAN DATE");
	    row.getCell(30).setCellStyle(style);
	    row.createCell(31).setCellValue("CORR ENTRY PLAZA");
	    row.getCell(31).setCellStyle(style);
	    row.createCell(32).setCellValue("CORR ENTRY LANE");
	    row.getCell(32).setCellStyle(style);
	    row.createCell(33).setCellValue("CORR EXIT TRAN DATE");
	    row.getCell(33).setCellStyle(style);
	    row.createCell(34).setCellValue("CORR EXIT PLAZA");
	    row.getCell(34).setCellStyle(style);
	    row.createCell(35).setCellValue("CORR EXIT LANE");
	    row.getCell(35).setCellStyle(style);
	    row.createCell(36).setCellValue("CORR AXLE COUNT");
	    row.getCell(36).setCellStyle(style);
	    row.createCell(37).setCellValue("CORR OCCUPANCY");
	    row.getCell(37).setCellStyle(style);
	    row.createCell(38).setCellValue("CORR PROTOCOL TYPE");
	    row.getCell(38).setCellStyle(style);
	    row.createCell(39).setCellValue("CORR VEHICLE TYPE");
	    row.getCell(39).setCellStyle(style);
	    row.createCell(40).setCellValue("CORR LP TYPE");
	    row.getCell(40).setCellStyle(style);
	    row.createCell(41).setCellValue("CORR TRAN FEE");
	    row.getCell(41).setCellStyle(style);
	    row.createCell(42).setCellValue("CORR TRAN FEE TYPE");
	    row.getCell(42).setCellStyle(style);
	  
	    
	    //RECON DATA
	    row.createCell(43).setCellValue("POST AMT");
	    row.getCell(43).setCellStyle(style);
	    row.createCell(44).setCellValue("RESPONSE CODE");
	    row.getCell(44).setCellStyle(style);
	    row.createCell(45).setCellValue("ORIGINAL COR FILENAME");
	    row.getCell(45).setCellStyle(style);
	    
	   // XSSFCellStyle dateCellStyle = workbook.createCellStyle();
	    //XSSFDataFormat dateFormat = workbook.createDataFormat();
	    
	    int dataRowIndex = 1;
	    
	    for (CORTemplate tolTemp : tolTempList) {
	    	XSSFRow dataRow = sheet.createRow(dataRowIndex);
	       // dataRow.createCell(0).setCellValue(dataRowIndex);
	        dataRow.createCell(0).setCellValue(tolTemp.getSequence());
	        dataRow.createCell(1).setCellValue(tolTemp.getCorrectionDate());
	        dataRow.createCell(2).setCellValue(tolTemp.getCorrectionReason());
	        dataRow.createCell(3).setCellValue(tolTemp.getResubmitReason());
	        dataRow.createCell(4).setCellValue(tolTemp.getCorrectionCount());
	        dataRow.createCell(5).setCellValue(tolTemp.getResubmitCount());
	        dataRow.createCell(6).setCellValue(tolTemp.getHomeAgencySequence());
	        dataRow.createCell(7).setCellValue(tolTemp.getOriginalTagId());
	        dataRow.createCell(8).setCellValue(tolTemp.getOriginalLicensePlate());
	        dataRow.createCell(9).setCellValue(tolTemp.getOriginalState());
	        dataRow.createCell(10).setCellValue(tolTemp.getOriginalTran());
	        dataRow.createCell(11).setCellValue(tolTemp.getOriginalTranAmount());
	        dataRow.createCell(12).setCellValue(tolTemp.getOriginalEntryTranDate());
	        dataRow.createCell(13).setCellValue(tolTemp.getOriginalEntryPlaza());
	        dataRow.createCell(14).setCellValue(tolTemp.getOriginalEntryLane());
	        dataRow.createCell(15).setCellValue(tolTemp.getOriginalExitTranDate());
	        dataRow.createCell(16).setCellValue(tolTemp.getOriginalExitPlaza());
	        dataRow.createCell(17).setCellValue(tolTemp.getOriginalExitLane());
	        dataRow.createCell(18).setCellValue(tolTemp.getOriginalAxleCount());
	        dataRow.createCell(19).setCellValue(tolTemp.getOriginalOccupancy());
	        dataRow.createCell(20).setCellValue(tolTemp.getOriginalProtocolType());
	        dataRow.createCell(21).setCellValue(tolTemp.getOriginalvehicleType());
	        dataRow.createCell(22).setCellValue(tolTemp.getOriginalLPtype());
	        dataRow.createCell(23).setCellValue(tolTemp.getOriginalTranFee());
	        dataRow.createCell(24).setCellValue(tolTemp.getOriginalTranFeeType());
	        dataRow.createCell(25).setCellValue(tolTemp.getCorrTagId());
	        dataRow.createCell(26).setCellValue(tolTemp.getCorrLicensePlate());
	        dataRow.createCell(27).setCellValue(tolTemp.getCorrState());
	        dataRow.createCell(28).setCellValue(tolTemp.getCorrTran());
	        dataRow.createCell(29).setCellValue(tolTemp.getCorrTranAmount());
	        dataRow.createCell(30).setCellValue(tolTemp.getCorrEntryTranDate());
	        dataRow.createCell(31).setCellValue(tolTemp.getCorrEntryPlaza());
	        dataRow.createCell(32).setCellValue(tolTemp.getCorrEntryLane());
	        dataRow.createCell(33).setCellValue(tolTemp.getCorrExitTranDate());
	        dataRow.createCell(34).setCellValue(tolTemp.getCorrExitPlaza());
	        dataRow.createCell(35).setCellValue(tolTemp.getCorrExitLane());
	        dataRow.createCell(36).setCellValue(tolTemp.getCorrAxleCount());
	        dataRow.createCell(37).setCellValue(tolTemp.getCorrOccupancy());
	        dataRow.createCell(38).setCellValue(tolTemp.getCorrProtocolType());
	        dataRow.createCell(39).setCellValue(tolTemp.getCorrvehicleType());
	        dataRow.createCell(40).setCellValue(tolTemp.getCorrLPtype());
	        dataRow.createCell(41).setCellValue(tolTemp.getCorrTranFee());
	        dataRow.createCell(42).setCellValue(tolTemp.getCorrTranFeeType());
	        
	       
	        
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
	    	log.error("Exception in COR Template excel creation filename ::"+fileName);
			e.printStackTrace();
			
		}
	    workbook.close();
	    log.info("COR Template list file generated ::"+fileName);
	}
			


}
