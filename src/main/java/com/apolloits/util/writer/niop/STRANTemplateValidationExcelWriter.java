package com.apolloits.util.writer.niop;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import com.apolloits.util.modal.FileValidationParam;
import com.apolloits.util.modal.niop.stran.TransactionRecord;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class STRANTemplateValidationExcelWriter {

	public void createStranTemplateExcel(List<TransactionRecord> stranTempList, String fileName,
			FileValidationParam validateParam) throws FileNotFoundException, IOException {

		log.info("Inside createStranTemplateExcel() :: stranTempList size ::" + stranTempList.size() + "\t FileName ::"
				+ fileName);

		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheet = workbook.createSheet("SRECON");
		XSSFRow row = sheet.createRow(0);
		XSSFCellStyle style = workbook.createCellStyle();
		XSSFFont defaultFont = workbook.createFont();
		defaultFont.setBold(true);
		style.setFont(defaultFont);
		
		style.setBorderBottom(BorderStyle.THIN);
        style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        style.setBorderTop(BorderStyle.THIN);
        style.setTopBorderColor(IndexedColors.BLACK.getIndex());
        style.setBorderLeft(BorderStyle.THIN);
        style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        style.setBorderRight(BorderStyle.THIN);
        style.setRightBorderColor(IndexedColors.BLACK.getIndex());
		
		setSTRANHeaderRowCreation(row,style);
		//start to set value for detail record
		int dataRowIndex = 1;
	    
	    for (TransactionRecord stranTemp : stranTempList) {
	    	XSSFRow dataRow = sheet.createRow(dataRowIndex);
	    	setSTRANDetailRowCreation(dataRow,stranTemp);
	    	dataRowIndex++;
	    }
	    
	    for (int i = 0; i < 48; i++) {
	        sheet.autoSizeColumn(i);
	    }
		
		//ServletOutputStream ops = response.getOutputStream();
	    try (FileOutputStream outputStream = new FileOutputStream(fileName)) {
	        workbook.write(outputStream);
	    }catch (Exception e) {
	    	log.error("Exception in STRAN Template excel creation filename ::"+fileName);
			e.printStackTrace();
			
		}
	    workbook.close();
	    log.info("STRAN Template list file generated ::"+fileName);
		
	}
	
	private void setSTRANDetailRowCreation(XSSFRow dataRow,TransactionRecord stranTemp) {
		int index =0;
		dataRow.createCell(index++).setCellValue(stranTemp.getTxnDataSeqNo());
		dataRow.createCell(index++).setCellValue(stranTemp.getRecordType());
		dataRow.createCell(index++).setCellValue(stranTemp.getTxnReferenceID());
		dataRow.createCell(index++).setCellValue(stranTemp.getExitDateTime());
		dataRow.createCell(index++).setCellValue(stranTemp.getFacilityID());
		dataRow.createCell(index++).setCellValue(stranTemp.getFacilityDesc());
		dataRow.createCell(index++).setCellValue(stranTemp.getExitPlaza());
		dataRow.createCell(index++).setCellValue(stranTemp.getExitPlazaDesc());
		dataRow.createCell(index++).setCellValue(stranTemp.getExitLane());
		//Entry Details
		if (stranTemp.getEntryData() != null) {
			dataRow.createCell(index++).setCellValue(stranTemp.getEntryData().getEntryDateTime());
			dataRow.createCell(index++).setCellValue(stranTemp.getEntryData().getEntryPlaza());
			dataRow.createCell(index++).setCellValue(stranTemp.getEntryData().getEntryPlazaDesc());
			dataRow.createCell(index++).setCellValue(stranTemp.getEntryData().getEntryLane());
		} else {
			dataRow.createCell(index++).setCellValue("");
			dataRow.createCell(index++).setCellValue("");
			dataRow.createCell(index++).setCellValue("");
			dataRow.createCell(index++).setCellValue("");
		}
		//tag Info Details
		if (stranTemp.getTagInfo() != null) {
			dataRow.createCell(index++).setCellValue(stranTemp.getTagInfo().getTagAgencyID());
			dataRow.createCell(index++).setCellValue(stranTemp.getTagInfo().getTagSerialNo());
			dataRow.createCell(index++).setCellValue(stranTemp.getTagInfo().getTagStatus());
		} else {
			dataRow.createCell(index++).setCellValue("");
			dataRow.createCell(index++).setCellValue("");
			dataRow.createCell(index++).setCellValue("");
		}
		
		dataRow.createCell(index++).setCellValue(stranTemp.getOccupancyInd());
		dataRow.createCell(index++).setCellValue(stranTemp.getVehicleClass());
		dataRow.createCell(index++).setCellValue(stranTemp.getTollAmount());
		dataRow.createCell(index++).setCellValue(stranTemp.getDiscountPlanType());
		//Plate info Deatils
		if (stranTemp.getPlateInfo() != null) {
			dataRow.createCell(index++).setCellValue(stranTemp.getPlateInfo().getPlateCountry());
			dataRow.createCell(index++).setCellValue(stranTemp.getPlateInfo().getPlateState());
			dataRow.createCell(index++).setCellValue(stranTemp.getPlateInfo().getPlateNumber());
			dataRow.createCell(index++).setCellValue(stranTemp.getPlateInfo().getPlateType());
		} else {
			dataRow.createCell(index++).setCellValue("");
			dataRow.createCell(index++).setCellValue("");
			dataRow.createCell(index++).setCellValue("");
			dataRow.createCell(index++).setCellValue("");
		}
		
		dataRow.createCell(index++).setCellValue(stranTemp.getVehicleClassAdj());
		dataRow.createCell(index++).setCellValue(stranTemp.getSystemMatchInd());
		dataRow.createCell(index++).setCellValue(stranTemp.getSpare1());
		dataRow.createCell(index++).setCellValue(stranTemp.getSpare2());
		dataRow.createCell(index++).setCellValue(stranTemp.getSpare3());
		dataRow.createCell(index++).setCellValue(stranTemp.getSpare4());
		dataRow.createCell(index++).setCellValue(stranTemp.getSpare5());
		dataRow.createCell(index++).setCellValue(stranTemp.getExitDateTimeTZ());
		dataRow.createCell(index).setCellValue(stranTemp.getEntryDateTimeTZ());
		dataRow.createCell(37).setCellValue(stranTemp.getPostingDisposition());
		
	}
	
	private void  setSTRANHeaderRowCreation(XSSFRow row,XSSFCellStyle style) {
		row.createCell(0).setCellValue("Transaction Data Sequence Number");
	    row.getCell(0).setCellStyle(style);
	    row.createCell(1).setCellValue("Record Type");
	    row.getCell(1).setCellStyle(style);
	    row.createCell(2).setCellValue("Transaction Reference ID");
	    row.getCell(2).setCellStyle(style);
	    row.createCell(3).setCellValue("Exit Date/Time");
	    row.getCell(3).setCellStyle(style);
	    row.createCell(4).setCellValue("Facility ID");
	    row.getCell(4).setCellStyle(style);
	    row.createCell(5).setCellValue("Facility Description");
	    row.getCell(5).setCellStyle(style);
	    row.createCell(6).setCellValue("Exit Plaza");
	    row.getCell(6).setCellStyle(style);
	    row.createCell(7).setCellValue("Exit Plaza Description");
	    row.getCell(7).setCellStyle(style);
	    row.createCell(8).setCellValue("Exit Lane");
	    row.getCell(8).setCellStyle(style);
	    row.createCell(9).setCellValue("Entry Date/Time");
	    row.getCell(9).setCellStyle(style);
	    row.createCell(10).setCellValue("Entry Plaza");
	    row.getCell(10).setCellStyle(style);
	    row.createCell(11).setCellValue("Entry Plaza Description");
	    row.getCell(11).setCellStyle(style);
	    row.createCell(12).setCellValue("Entry Lane");
	    row.getCell(12).setCellStyle(style);
	    row.createCell(13).setCellValue("Tag Agency ID");
	    row.getCell(13).setCellStyle(style);
	    row.createCell(14).setCellValue("Tag Serial Numbe");
	    row.getCell(14).setCellStyle(style);
	    row.createCell(15).setCellValue("Tag Status");
	    row.getCell(15).setCellStyle(style);
	    
	    row.createCell(16).setCellValue("Occupancy Indicator");
	    row.getCell(16).setCellStyle(style);
	    row.createCell(17).setCellValue("Vehicle Classification");
	    row.getCell(17).setCellStyle(style);
	    row.createCell(18).setCellValue("Toll Amount");
	    row.getCell(18).setCellStyle(style);
	    row.createCell(19).setCellValue("Discount Plan");
	    row.getCell(19).setCellStyle(style);
	    
	    row.createCell(20).setCellValue("License Plate Country");
	    row.getCell(20).setCellStyle(style);
	    row.createCell(21).setCellValue("License Plate State");
	    row.getCell(21).setCellStyle(style);
	    row.createCell(22).setCellValue("License Plate Number");
	    row.getCell(22).setCellStyle(style);
	    row.createCell(23).setCellValue("License Plate Type");
	    row.getCell(23).setCellStyle(style);
	    
	    row.createCell(24).setCellValue("Vehicle Classification Adjustment Flag");
	    row.getCell(24).setCellStyle(style);
	    row.createCell(25).setCellValue("System Matched Flag");
	    row.getCell(25).setCellStyle(style);
	    row.createCell(26).setCellValue("Spare 1");
	    row.getCell(26).setCellStyle(style);
	    row.createCell(27).setCellValue("Spare 2");
	    row.getCell(27).setCellStyle(style);
	    
	    row.createCell(28).setCellValue("Spare 3");
	    row.getCell(28).setCellStyle(style);
	    row.createCell(29).setCellValue("Spare 4");
	    row.getCell(29).setCellStyle(style);
	    row.createCell(30).setCellValue("Spare 5");
	    row.getCell(30).setCellStyle(style);
	    
	    
	    row.createCell(31).setCellValue("Exit Date/Time w/TZ");
	    row.getCell(31).setCellStyle(style);
	    row.createCell(32).setCellValue("Entry Date/Time w/TZ");
	    row.getCell(32).setCellStyle(style);
	    
	    //SRECON
	    row.createCell(33).setCellValue("Adjustment Count");
	    row.getCell(33).setCellStyle(style);
	    row.createCell(34).setCellValue("Resubmit Count");
	    row.getCell(34).setCellStyle(style);
	    row.createCell(35).setCellValue("Reconciliation Home Agency ID");
	    row.getCell(35).setCellStyle(style);
	    row.createCell(36).setCellValue("Home Agency Reference ID");
	    row.getCell(36).setCellStyle(style);
	    row.createCell(37).setCellValue("Posting Disposition");
	    row.getCell(37).setCellStyle(style);
	    row.createCell(38).setCellValue("Posted Discount Plan");
	    row.getCell(38).setCellStyle(style);
	    
	    row.createCell(39).setCellValue("Posted Amount");
	    row.getCell(39).setCellStyle(style);
	    
	    row.createCell(40).setCellValue("Posted Date/Time");
	    row.getCell(40).setCellStyle(style);
	    row.createCell(41).setCellValue("Transaction Flat Fee");
	    row.getCell(41).setCellStyle(style);
	    row.createCell(42).setCellValue("Transaction Percent Fee");
	    row.getCell(42).setCellStyle(style);
	    row.createCell(43).setCellValue("Spare 1");
	    row.getCell(43).setCellStyle(style);
	    row.createCell(44).setCellValue("Spare 2");
	    row.getCell(44).setCellStyle(style);
	    row.createCell(45).setCellValue("Spare 3");
	    row.getCell(45).setCellStyle(style);
	    row.createCell(46).setCellValue("Spare 4");
	    row.getCell(46).setCellStyle(style);
	    row.createCell(47).setCellValue("Spare 5");
	    row.getCell(47).setCellStyle(style);
	    
	}
}
