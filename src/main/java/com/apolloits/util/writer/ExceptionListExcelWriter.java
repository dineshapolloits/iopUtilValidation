package com.apolloits.util.writer;

import static com.apolloits.util.IAGConstants.FILE_RECORD_TYPE;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.apolloits.util.controller.NiopValidationController;
import com.apolloits.util.controller.ValidationController;
import com.apolloits.util.modal.ErrorMsgDetail;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ExceptionListExcelWriter {

	@Autowired
	@Lazy
	ValidationController controller;
	
	@Autowired
	@Lazy
	NiopValidationController niopController;
	
	public void createExceptionExcel(List<ErrorMsgDetail> errorMsglist,String outputFilePath) throws IOException {
		
		log.info("Inside createExceptionExcel() :: errorMsglist size ::"+errorMsglist.size() +"\t outputFilePath ::"+outputFilePath);
		HSSFWorkbook workbook = new HSSFWorkbook();
	    HSSFSheet sheet = workbook.createSheet("Exception List");
	    HSSFRow row = sheet.createRow(0);

	    HSSFCellStyle style= workbook.createCellStyle();
	    HSSFFont defaultFont= workbook.createFont();
	   // font.setFontHeightInPoints((short)10);
	   // font.setFontName("Arial");
	    //font.setColor(IndexedColors.WHITE.getIndex());
	    defaultFont.setBold(true);
	    style.setFont(defaultFont);
	   
	    row.createCell(0).setCellValue("Seq.No");
	    row.getCell(0).setCellStyle(style);
	    row.createCell(1).setCellValue("Record Type");
	    row.getCell(1).setCellStyle(style);
	    row.createCell(2).setCellValue("Field Name");
	    row.getCell(2).setCellStyle(style);
	    row.createCell(3).setCellValue("Error Msg");
	    row.getCell(3).setCellStyle(style);
	    HSSFCellStyle dateCellStyle = workbook.createCellStyle();
	    HSSFDataFormat dateFormat = workbook.createDataFormat();
	    //dateCellStyle.setDataFormat(dateFormat.getFormat("dd-mm-yyyy"));

	    int dataRowIndex = 1;

	    for (ErrorMsgDetail errmsg : errorMsglist) {
	        HSSFRow dataRow = sheet.createRow(dataRowIndex);
	        dataRow.createCell(0).setCellValue(dataRowIndex);
	        dataRow.createCell(1).setCellValue(errmsg.getFileType());
	        dataRow.createCell(2).setCellValue(errmsg.getFieldName());
	        dataRow.createCell(3).setCellValue(errmsg.getErrorMsg().replaceAll("(<b>|</b>)", ""));
	        dataRowIndex++;
	    }

	    for (int i = 0; i < 4; i++) {
	        sheet.autoSizeColumn(i);
	    }

	    //ServletOutputStream ops = response.getOutputStream();
	    try (FileOutputStream outputStream = new FileOutputStream(outputFilePath)) {
	        workbook.write(outputStream);
	    }catch (FileNotFoundException e) {
	    	if(controller.getErrorMsglist() != null ) {
	    		controller.getErrorMsglist().add(new ErrorMsgDetail(FILE_RECORD_TYPE,"ACK","ACK Output Path don't have write access"));
	    	}else {
	    		niopController.getErrorMsglist().add(new ErrorMsgDetail(FILE_RECORD_TYPE,"ACK","ACK Output Path don't have write access"));
	    	}
	    	
			e.printStackTrace();
			
		}
	   // workbook.write(ops);
	    workbook.close();
	    log.info("Exception list file generated ::"+outputFilePath);
	    //ops.close();
	}
}
