package com.apolloits.util.writer.niop;

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
import com.apolloits.util.modal.niop.scorr.CorrectionRecord;
import com.apolloits.util.modal.niop.scorr.OriginalTransactionDetail;
import com.apolloits.util.modal.niop.stran.TransactionRecord;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SCORRTemplateValidationExcelWriter {
	
	public void createScorrTemplateExcel(List<CorrectionRecord> scorrTempList, String scorrTempExcelFileName,
			FileValidationParam validateParam) throws IOException {
		log.info("Inside createScorrTemplateExcel() :: scorrTempList size ::" + scorrTempList.size() + "\t FileName ::"
				+ scorrTempExcelFileName);

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
		
		setSCORRHeaderRowCreation(row,style);
		//start to set value for detail record
		int dataRowIndex = 1;
		for (CorrectionRecord scorrnTemp : scorrTempList) {
	    	XSSFRow dataRow = sheet.createRow(dataRowIndex);
	    	setSCORRDetailRowCreation(dataRow,scorrnTemp);
	    	dataRowIndex++;
	    }
		
		for (int i = 0; i < 48; i++) {
	        sheet.autoSizeColumn(i);
	    }
		
	    try (FileOutputStream outputStream = new FileOutputStream(scorrTempExcelFileName)) {
	        workbook.write(outputStream);
	    }catch (Exception e) {
	    	log.error("Exception in SCORR Template excel creation filename ::"+scorrTempExcelFileName);
			e.printStackTrace();
			
		}
	    workbook.close();
	    log.info("SCORR Template list file generated ::"+scorrTempExcelFileName);
	    

	}

	private void setSCORRDetailRowCreation(XSSFRow dataRow, CorrectionRecord scorrnTemp) {

		int index =0;
		dataRow.createCell(index++).setCellValue(scorrnTemp.getTxnDataSeqNo());
		dataRow.createCell(index++).setCellValue(scorrnTemp.getRecordType());
		dataRow.createCell(index++).setCellValue(scorrnTemp.getCorrectionDateTime());
		dataRow.createCell(index++).setCellValue(scorrnTemp.getCorrectionReason());
		dataRow.createCell(index++).setCellValue(scorrnTemp.getResubmitReason());
		dataRow.createCell(index++).setCellValue(scorrnTemp.getCorrectionOtherDesc());
		dataRow.createCell(index++).setCellValue(scorrnTemp.getCorrectionSeqNo());
		dataRow.createCell(index++).setCellValue(scorrnTemp.getResubmitCount());
		dataRow.createCell(index++).setCellValue(scorrnTemp.getHomeAgencyTxnRefID());
		// Original Transaction details
		OriginalTransactionDetail orginalTran = scorrnTemp.getOriginalTransactionDetail();
		dataRow.createCell(index++).setCellValue(orginalTran.getRecordType());
		dataRow.createCell(index++).setCellValue(orginalTran.getTxnReferenceID());
		dataRow.createCell(index++).setCellValue(orginalTran.getExitDateTime());
		dataRow.createCell(index++).setCellValue(orginalTran.getFacilityID());
		dataRow.createCell(index++).setCellValue(orginalTran.getFacilityDesc());
		dataRow.createCell(index++).setCellValue(orginalTran.getExitPlaza());
		dataRow.createCell(index++).setCellValue(orginalTran.getExitPlazaDesc());
		dataRow.createCell(index++).setCellValue(orginalTran.getExitLane());
		//Entry Details
		if (orginalTran.getEntryData() != null) {
			dataRow.createCell(index++).setCellValue(orginalTran.getEntryData().getEntryDateTime());
			dataRow.createCell(index++).setCellValue(orginalTran.getEntryData().getEntryPlaza());
			dataRow.createCell(index++).setCellValue(orginalTran.getEntryData().getEntryPlazaDesc());
			dataRow.createCell(index++).setCellValue(orginalTran.getEntryData().getEntryLane());
		} else {
			dataRow.createCell(index++).setCellValue("");
			dataRow.createCell(index++).setCellValue("");
			dataRow.createCell(index++).setCellValue("");
			dataRow.createCell(index++).setCellValue("");
		}
		//tag Info Details
		if (orginalTran.getTagInfo() != null) {
			dataRow.createCell(index++).setCellValue(orginalTran.getTagInfo().getTagAgencyID());
			dataRow.createCell(index++).setCellValue(orginalTran.getTagInfo().getTagSerialNo());
			dataRow.createCell(index++).setCellValue(orginalTran.getTagInfo().getTagStatus());
		} else {
			dataRow.createCell(index++).setCellValue("");
			dataRow.createCell(index++).setCellValue("");
			dataRow.createCell(index++).setCellValue("");
		}
		
		dataRow.createCell(index++).setCellValue(orginalTran.getOccupancyInd());
		dataRow.createCell(index++).setCellValue(orginalTran.getVehicleClass());
		dataRow.createCell(index++).setCellValue(orginalTran.getTollAmount());
		dataRow.createCell(index++).setCellValue(orginalTran.getDiscountPlanType());
		//Plate info Deatils
		if (orginalTran.getPlateInfo() != null) {
			dataRow.createCell(index++).setCellValue(orginalTran.getPlateInfo().getPlateCountry());
			dataRow.createCell(index++).setCellValue(orginalTran.getPlateInfo().getPlateState());
			dataRow.createCell(index++).setCellValue(orginalTran.getPlateInfo().getPlateNumber());
			dataRow.createCell(index++).setCellValue(orginalTran.getPlateInfo().getPlateType());
		} else {
			dataRow.createCell(index++).setCellValue("");
			dataRow.createCell(index++).setCellValue("");
			dataRow.createCell(index++).setCellValue("");
			dataRow.createCell(index++).setCellValue("");
		}
		
		dataRow.createCell(index++).setCellValue(orginalTran.getVehicleClassAdj());
		dataRow.createCell(index++).setCellValue(orginalTran.getSystemMatchInd());
		dataRow.createCell(index++).setCellValue(orginalTran.getSpare1());
		dataRow.createCell(index++).setCellValue(orginalTran.getSpare2());
		dataRow.createCell(index++).setCellValue(orginalTran.getSpare3());
		dataRow.createCell(index++).setCellValue(orginalTran.getSpare4());
		dataRow.createCell(index++).setCellValue(orginalTran.getSpare5());
		dataRow.createCell(index++).setCellValue(orginalTran.getExitDateTimeTZ());
		dataRow.createCell(index).setCellValue(orginalTran.getEntryDateTimeTZ());
		dataRow.createCell(45).setCellValue(scorrnTemp.getPostingDisposition());
		
	
		
	}

	private void setSCORRHeaderRowCreation(XSSFRow row, XSSFCellStyle style) {
		int index = 0;
		row.createCell(index).setCellValue("Transaction Data Sequence Number");
	    row.getCell(index++).setCellStyle(style);
	    System.out.println("index ::"+index);
	    row.createCell(index).setCellValue("Record Type");
	    row.getCell(index++).setCellStyle(style);
	    row.createCell(index).setCellValue("Correction Date/Time");
	    row.getCell(index++).setCellStyle(style);
	    
	    row.createCell(index).setCellValue("Correction Reason");
	    row.getCell(index++).setCellStyle(style);
	    row.createCell(index).setCellValue("Resubmit Reason");
	    row.getCell(index++).setCellStyle(style);
	    row.createCell(index).setCellValue("Other Correction Description");
	    row.getCell(index++).setCellStyle(style);
	    row.createCell(index).setCellValue("Correction Count");
	    row.getCell(index++).setCellStyle(style);
	    row.createCell(index).setCellValue("Resubmit Count");
	    row.getCell(index++).setCellStyle(style);
	    row.createCell(index).setCellValue("Home Agency Reference ID");
	    row.getCell(index++).setCellStyle(style);
	    
	    row.createCell(index).setCellValue("Record Type");
	    row.getCell(index++).setCellStyle(style);
	    row.createCell(index).setCellValue("Transaction Reference ID");
	    row.getCell(index++).setCellStyle(style);
	    row.createCell(index).setCellValue("Exit Date/Time");
	    row.getCell(index++).setCellStyle(style);
	    row.createCell(index).setCellValue("Facility ID");
	    row.getCell(index++).setCellStyle(style);
	    row.createCell(index).setCellValue("Facility Description");
	    row.getCell(index++).setCellStyle(style);
	    row.createCell(index).setCellValue("Exit Plaza");
	    row.getCell(index++).setCellStyle(style);
	    row.createCell(index).setCellValue("Exit Plaza Description");
	    row.getCell(index++).setCellStyle(style);
	    row.createCell(index).setCellValue("Exit Lane");
	    row.getCell(index++).setCellStyle(style);
	    row.createCell(index).setCellValue("Entry Date/Time");
	    row.getCell(index++).setCellStyle(style);
	    row.createCell(index).setCellValue("Entry Plaza");
	    row.getCell(index++).setCellStyle(style);
	    row.createCell(index).setCellValue("Entry Plaza Description");
	    row.getCell(index++).setCellStyle(style);
	    row.createCell(index).setCellValue("Entry Lane");
	    row.getCell(index++).setCellStyle(style);
	    row.createCell(index).setCellValue("Tag Agency ID");
	    row.getCell(index++).setCellStyle(style);
	    row.createCell(index).setCellValue("Tag Serial Numbe");
	    row.getCell(index++).setCellStyle(style);
	    row.createCell(index).setCellValue("Tag Status");
	    row.getCell(index++).setCellStyle(style);
	    
	    row.createCell(index).setCellValue("Occupancy Indicator");
	    row.getCell(index++).setCellStyle(style);
	    row.createCell(index).setCellValue("Vehicle Classification");
	    row.getCell(index++).setCellStyle(style);
	    row.createCell(index).setCellValue("Toll Amount");
	    row.getCell(index++).setCellStyle(style);
	    row.createCell(index).setCellValue("Discount Plan");
	    row.getCell(index++).setCellStyle(style);
	    
	    row.createCell(index).setCellValue("License Plate Country");
	    row.getCell(index++).setCellStyle(style);
	    row.createCell(index).setCellValue("License Plate State");
	    row.getCell(index++).setCellStyle(style);
	    row.createCell(index).setCellValue("License Plate Number");
	    row.getCell(index++).setCellStyle(style);
	    row.createCell(index).setCellValue("License Plate Type");
	    row.getCell(index++).setCellStyle(style);
	    
	    row.createCell(index).setCellValue("Vehicle Classification Adjustment Flag");
	    row.getCell(index++).setCellStyle(style);
	    row.createCell(index).setCellValue("System Matched Flag");
	    row.getCell(index++).setCellStyle(style);
	    row.createCell(index).setCellValue("Spare 1");
	    row.getCell(index++).setCellStyle(style);
	    row.createCell(index).setCellValue("Spare 2");
	    row.getCell(index++).setCellStyle(style);
	    
	    row.createCell(index).setCellValue("Spare 3");
	    row.getCell(index++).setCellStyle(style);
	    row.createCell(index).setCellValue("Spare 4");
	    row.getCell(index++).setCellStyle(style);
	    row.createCell(index).setCellValue("Spare 5");
	    row.getCell(index++).setCellStyle(style);
	    
	    
	    row.createCell(index).setCellValue("Exit Date/Time w/TZ");
	    row.getCell(index++).setCellStyle(style);
	    row.createCell(index).setCellValue("Entry Date/Time w/TZ");
	    row.getCell(index++).setCellStyle(style);
	    
	    //SRECON
	    row.createCell(index).setCellValue("Adjustment Count");
	    row.getCell(index++).setCellStyle(style);
	    row.createCell(index).setCellValue("Resubmit Count");
	    row.getCell(index++).setCellStyle(style);
	    row.createCell(index).setCellValue("Reconciliation Home Agency ID");
	    row.getCell(index++).setCellStyle(style);
	    row.createCell(index).setCellValue("Home Agency Reference ID");
	    row.getCell(index++).setCellStyle(style);
	    row.createCell(index).setCellValue("Posting Disposition");
	    row.getCell(index++).setCellStyle(style);
	    row.createCell(index).setCellValue("Posted Discount Plan");
	    row.getCell(index++).setCellStyle(style);
	    
	    row.createCell(index).setCellValue("Posted Amount");
	    row.getCell(index++).setCellStyle(style);
	    
	    row.createCell(index).setCellValue("Posted Date/Time");
	    row.getCell(index++).setCellStyle(style);
	    row.createCell(index).setCellValue("Transaction Flat Fee");
	    row.getCell(index++).setCellStyle(style);
	    row.createCell(index).setCellValue("Transaction Percent Fee");
	    row.getCell(index++).setCellStyle(style);
	    row.createCell(index).setCellValue("Spare 1");
	    row.getCell(index++).setCellStyle(style);
	    row.createCell(index).setCellValue("Spare 2");
	    row.getCell(index++).setCellStyle(style);
	    row.createCell(index).setCellValue("Spare 3");
	    row.getCell(index++).setCellStyle(style);
	    row.createCell(index).setCellValue("Spare 4");
	    row.getCell(index++).setCellStyle(style);
	    row.createCell(index).setCellValue("Spare 5");
	    row.getCell(index++).setCellStyle(style);		
	}

}
