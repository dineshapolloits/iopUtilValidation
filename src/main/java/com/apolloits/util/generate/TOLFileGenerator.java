package com.apolloits.util.generate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
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

import com.apolloits.util.IopTranslatorException;
import com.apolloits.util.controller.CtocController;
import com.apolloits.util.modal.AgencyEntity;
import com.apolloits.util.modal.ErrorMsgDetail;
import com.apolloits.util.modal.FileValidationParam;
import com.apolloits.util.modal.TOLTemplate;
import com.apolloits.util.utility.CommonUtil;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@Component
public class TOLFileGenerator {
	@Autowired
	CommonUtil commonUtil;
	@Autowired
	@Lazy
	CtocController controller;
	
	private String filename = "";
	private String fileCreateDateandTime = "";
	AgencyEntity agency;
	double detailAmt = 0.00;
	
	private List<TOLTemplate> tolTemplateList;
public boolean tolGenenerate(FileValidationParam validateParam) throws IOException {
	HashMap<String, String> ctocShortAgency = new HashMap<>();
	ctocShortAgency.put("0058", "ud");
	ctocShortAgency.put("0077", "wd"); // WashDOT
	ctocShortAgency.put("0101", "at");
	ctocShortAgency.put("0103", "tc");
	ctocShortAgency.put("0105", "gg");
	ctocShortAgency.put("0106", "la");
	ctocShortAgency.put("0104", "oc");
	ctocShortAgency.put("0108", "rc");
	ctocShortAgency.put("0109", "sd");
	ctocShortAgency.put("0110", "vt");
	ctocShortAgency.put("0111", "sx");
	ctocShortAgency.put("0112", "ac");
	ctocShortAgency.put("0113", "sf");
	ctocShortAgency.put("0114", "sb");
	ctocShortAgency.put("0116", "hr"); 
	ctocShortAgency.put("0107", "sr");
	ctocShortAgency.put("0085", "od");

	System.out.println(
			"validateParam.getFromAgency()" + validateParam.getFromAgency() + "" + validateParam.getToAgency());
	String shortFromAgency = ctocShortAgency.get(validateParam.getFromAgency());
	String shortToAgency = ctocShortAgency.get(validateParam.getToAgency());

	System.out.println("validateParam..." + validateParam.getFileSequence());
		if (!commonUtil.validateParameter(validateParam)) {
			return false;
		}
		
		tolTemplateList = getTolTemplateExcel(validateParam);
		String Header = generateTolHeader(validateParam,tolTemplateList,shortFromAgency, shortToAgency);
		log.info("Tol Header :: " + Header);
		writeDetails(validateParam,Header,tolTemplateList,shortFromAgency, shortToAgency);
		String filePath = validateParam.getOutputFilePath() +'/'+ filename;//+ File.separator 
		log.info("Tol file name :: "+filePath);
		validateParam.setResponseMsg("Tol file created ::\t "+filePath);
		return true;
	}
private List<TOLTemplate> getTolTemplateExcel(FileValidationParam validateParam) {
		String TOL_SHEET = "Tol";
		try {
			log.info("Excel data path localtion form getInputFilePath ::"+validateParam.getInputFilePath());
			FileInputStream is = new FileInputStream(validateParam.getInputFilePath());
			Workbook workbook = new XSSFWorkbook(is);
			log.info("Number of sheets : " + workbook.getNumberOfSheets());

			tolTemplateList = excelToTolList(workbook.getSheet(TOL_SHEET),validateParam );

		} catch (Exception e) {
			validateParam.setResponseMsg("File not generated Please check input excel data");
			e.printStackTrace();
			return tolTemplateList = new ArrayList<>();
		}

		return tolTemplateList;
	}
private List<TOLTemplate> excelToTolList(Sheet sheet,FileValidationParam validateParam) {

  	 log.info("Inside ****************** excelToTolList()");
       try {
      	
         Iterator<Row> rows = sheet.iterator();
         tolTemplateList = new ArrayList<>();
         int rowNumber = 0;
         while (rows.hasNext()) {
           Row currentRow = rows.next();
           if (rowNumber == 0) {
             rowNumber++;
             continue;
           }
           TOLTemplate tolTemp = new TOLTemplate();
           String sequence = commonUtil.getStringFormatCell(currentRow.getCell(0,MissingCellPolicy.CREATE_NULL_AS_BLANK));
        	   tolTemp.setSequence(sequence);
           
           String tagID=commonUtil.getStringFormatCell(currentRow.getCell(1,MissingCellPolicy.CREATE_NULL_AS_BLANK));
           
        	  tolTemp.setTagID(tagID);
          
           String tran=commonUtil.getStringFormatCell(currentRow.getCell(2,MissingCellPolicy.CREATE_NULL_AS_BLANK));
        	   tolTemp.setTran(tran);
         String tranAmount = commonUtil.getStringFormatCell(currentRow.getCell(3,MissingCellPolicy.CREATE_NULL_AS_BLANK));
        	 tolTemp.setTranAmount(tranAmount);
        
      	 String entryTranDate = commonUtil.getStringFormatCell(currentRow.getCell(4,MissingCellPolicy.CREATE_NULL_AS_BLANK));
      			 tolTemp.setEntryTranDate(entryTranDate);
      		
      	 String entryPlaza = commonUtil.getStringFormatCell(currentRow.getCell(5,MissingCellPolicy.CREATE_NULL_AS_BLANK));
      		 tolTemp.setEntryPlaza(entryPlaza);
      	
      	 String entryLane= commonUtil.getStringFormatCell(currentRow.getCell(6,MissingCellPolicy.CREATE_NULL_AS_BLANK));
      	 tolTemp.setEntryLane(entryLane);
      	
      	 String exitTranDate = commonUtil.getStringFormatCell(currentRow.getCell(7,MissingCellPolicy.CREATE_NULL_AS_BLANK));
      	
      			 tolTemp.setExitTranDate(exitTranDate);
      		 
      	 
      	String exitPlaza = commonUtil.getStringFormatCell(currentRow.getCell(8,MissingCellPolicy.CREATE_NULL_AS_BLANK));
      	 tolTemp.setExitPlaza(exitPlaza);
      	
      	String exitLane= commonUtil.getStringFormatCell(currentRow.getCell(9,MissingCellPolicy.CREATE_NULL_AS_BLANK));
      	 tolTemp.setExitLane(exitLane);
      	
      	String axleCount = commonUtil.getStringFormatCell(currentRow.getCell(10,MissingCellPolicy.CREATE_NULL_AS_BLANK));
      		tolTemp.setAxleCount(axleCount);
      	
      	String occupancy = commonUtil.getStringFormatCell(currentRow.getCell(11,MissingCellPolicy.CREATE_NULL_AS_BLANK)); 
      
      	 tolTemp.setOccupancy(occupancy);
      	 String protocolType = commonUtil.getStringFormatCell(currentRow.getCell(12,MissingCellPolicy.CREATE_NULL_AS_BLANK));
    	 tolTemp.setProtocolType(protocolType);
    	 String vehicleType= commonUtil.getStringFormatCell(currentRow.getCell(13,MissingCellPolicy.CREATE_NULL_AS_BLANK));
    	 tolTemp.setVehicleType(vehicleType);
    	 String wrTranFee = commonUtil.getStringFormatCell(currentRow.getCell(14,MissingCellPolicy.CREATE_NULL_AS_BLANK));
    	 tolTemp.setWrTranFee(wrTranFee);
    	 String wrFeeType = commonUtil.getStringFormatCell(currentRow.getCell(15,MissingCellPolicy.CREATE_NULL_AS_BLANK));
    	 tolTemp.setWrFeeType(wrFeeType);
    	 String guarantee = commonUtil.getStringFormatCell(currentRow.getCell(16,MissingCellPolicy.CREATE_NULL_AS_BLANK));
    	 tolTemp.setGuarantee(guarantee);
      	
           tolTemplateList.add(tolTemp);
           System.out.println(tolTemp.toString());
         }
        
         if(tolTemplateList != null && tolTemplateList.size()>0) {
       	  System.out.println("tolTemplateList ::"+tolTemplateList);
         log.info("@@@@ TOL input data  loaded sucessfully:: ******************** ::"+tolTemplateList.size());
         }else {
      	   throw new IopTranslatorException("TOL input data not loaded");
         }
         
       }catch (Exception e) {
      	log.error("Exception:: ******************** TOL Sheet");
			e.printStackTrace();
		}
     
		return tolTemplateList;
	
}
private String generateTolHeader(FileValidationParam validateParam,List<TOLTemplate> tolTempList, String fromAgnecy, String toAgency) {

	fileCreateDateandTime = getUTCDateandTime();
	fileCreateDateandTime = validateParam.getFileDate()
			+ fileCreateDateandTime.substring(fileCreateDateandTime.indexOf("T"), fileCreateDateandTime.length());
	filename = fromAgnecy + toAgency + "_" + fileCreateDateandTime.replaceAll("[-:]", "").substring(0, 15) + ".tol";
	String createUTCdate = CommonUtil.convertDatetoUTC(filename.substring(5,20));
	System.out.println("createUTCdate::::"+createUTCdate);
	StringBuilder tagHeader = new StringBuilder();
	System.out.println("filename:::" + filename);

	tagHeader.append("#HEADER,");
	tagHeader.append("TOLL,");
	tagHeader.append(CommonUtil.formatStringLeftPad(String.valueOf(tolTempList.get(0).getSequence()),6,'0')+','); // SEQUENCE
	tagHeader.append(validateParam.getFileDate().replaceAll("-", "/") + ','); // BUSINESS DAY
	tagHeader.append(fromAgnecy.toUpperCase() + ','); // SOURCE
	tagHeader.append(toAgency.toUpperCase() + ','); // DESTINATION
	tagHeader.append(CommonUtil.formatStringLeftPad(createUTCdate, 25, ' ') + ','); // CREATE DATE
	tagHeader.append("REV A2.1.1"); // VERSION
	System.out.println("tagHeader.toString()" + tagHeader.toString());
	System.out.println("tagHeader.toString():::" + tagHeader.toString());
	return tagHeader.toString();

}
private void writeDetails(FileValidationParam validateParam, String header,List<TOLTemplate> tolTempList,String shortFromAgency,String shortToAgency)
		throws IOException {
	long start = System.currentTimeMillis();
	FileWriter writer;
	try {
		String filePath = validateParam.getOutputFilePath() + File.separator + filename;
		writer = new FileWriter(filePath, true);
		writer.write(header);
		writer.write(System.lineSeparator());
		System.out.print("Writing record raw... ");
		
		for (TOLTemplate tolTemplate : tolTempList) {
			writer.write(setTolDetailValues(tolTemplate,validateParam,detailAmt));
			writer.write(System.lineSeparator());
		}
		
		String trailer = generateTolTrailer(validateParam, shortFromAgency, shortToAgency,detailAmt,tolTempList);
		writer.write(trailer);
		writer.write("\n");
		writer.flush();
		writer.close();
		detailAmt = 0.00;
	}catch (Exception e) {
		validateParam.setResponseMsg("File not generated Please check log");
		e.printStackTrace();
	}
}
private String setTolDetailValues(TOLTemplate tolTemplate,FileValidationParam validateParam,double detailAmt) {

	StringBuilder tolDetail = new StringBuilder();
	System.out.println("detailAmt Start:::::::"+detailAmt);
	tolDetail.append(CommonUtil.formatStringLeftPad(tolTemplate.getTagID(),10,' ')+','); //TAG ID
	tolDetail.append(CommonUtil.formatStringLeftPad(tolTemplate.getTran(),10,'0')+',');//TRAN
	double tranAmount = Double.parseDouble(tolTemplate.getTranAmount());
	tranAmount = tranAmount/100;
	tolDetail.append(CommonUtil.formatStringLeftPad(String.format("%.2f", tranAmount),8,'0')+',');//TRAN amount
	if(tolTemplate.getEntryTranDate().equalsIgnoreCase("null") || tolTemplate.getEntryTranDate().isEmpty() || tolTemplate.getEntryTranDate().isBlank())
		tolDetail.append(CommonUtil.formatStringLeftPad("",25,' ')+',');//ENTRY TRAN DATE
	else tolDetail.append(CommonUtil.formatStringLeftPad(tolTemplate.getEntryTranDate(),25,' ')+',');//ENTRY TRAN DATE
	if(tolTemplate.getEntryPlaza().equalsIgnoreCase("null") || tolTemplate.getEntryPlaza().isEmpty() || tolTemplate.getEntryPlaza().isBlank())
		 tolDetail.append(CommonUtil.formatStringLeftPad("",22,' ')+',');//ENTRY PLAZA
	else tolDetail.append(CommonUtil.formatStringLeftPad(tolTemplate.getEntryPlaza(),22,' ')+',');//ENTRY PLAZA
	if(tolTemplate.getEntryLane().equalsIgnoreCase("null") || tolTemplate.getEntryLane().isEmpty() || tolTemplate.getEntryLane().isBlank())
		tolDetail.append(CommonUtil.formatStringLeftPad("",2,' ')+',');//ENTRY LANE
	else tolDetail.append(CommonUtil.formatStringLeftPad(tolTemplate.getEntryLane(),2,'0')+',');//ENTRY LANE
	tolDetail.append(CommonUtil.formatStringLeftPad(tolTemplate.getExitTranDate(),25,' ')+',');//EXIT TRAN DATE
	tolDetail.append(CommonUtil.formatStringLeftPad(tolTemplate.getExitPlaza(),22,' ')+',');//EXIT PLAZA
	tolDetail.append(CommonUtil.formatStringLeftPad(tolTemplate.getExitLane(),2,'0')+',');//EXIT LANE
	tolDetail.append(CommonUtil.formatStringLeftPad(tolTemplate.getAxleCount(),2,'0')+',');//AXLE COUNT
	tolDetail.append(CommonUtil.formatStringLeftPad(tolTemplate.getOccupancy(),1,'0')+',');//OCCUPANCY
	tolDetail.append(CommonUtil.formatStringLeftPad(tolTemplate.getProtocolType(),1,'0')+',');//PROTOCOL TYPE
	tolDetail.append(CommonUtil.formatStringLeftPad(tolTemplate.getVehicleType(),1,' ')+',');//VEHICLE TYPE
	double WrTranFee = Double.parseDouble(tolTemplate.getWrTranFee());
	WrTranFee = WrTranFee/100;
	tolDetail.append(CommonUtil.formatStringLeftPad(String.format("%.2f", WrTranFee),8,'0')+',');//WR TRAN FEE
	tolDetail.append(CommonUtil.formatStringLeftPad(tolTemplate.getWrFeeType(),1,'0')+',');//WR FEE TYPE
	tolDetail.append(CommonUtil.formatStringLeftPad(tolTemplate.getGuarantee(),1,'0'));//GUARANTEE
	double toll=Double.parseDouble(tolTemplate.getTranAmount());
	this.detailAmt = detailAmt+toll;
	
	System.out.println("detailAmt last:::::::"+detailAmt);
	return tolDetail.toString();
}
private String generateTolTrailer(FileValidationParam validateParam, String fromAgnecy, String toAgency,double detailAmt,List<TOLTemplate> tolTempList) {
	
	StringBuilder tagTrailer = new StringBuilder();
	tagTrailer.append("#TRAILER,");
	tagTrailer.append(CommonUtil.formatStringLeftPad(String.valueOf(tolTempList.get(0).getSequence()),6,'0')+',');
	tagTrailer.append(validateParam.getFileDate().replaceAll("-", "/") + ',');
	tagTrailer.append(CommonUtil.formatStringLeftPad(String.valueOf(tolTempList.size()), 6, '0')+',' );
	DecimalFormat df = new DecimalFormat("#.00");
	double tranFee = detailAmt/100;//Double.parseDouble(detailAmt);
	tagTrailer.append(CommonUtil.formatStringLeftPad(String.format("%.2f", tranFee),10,'0'));
	System.out.println("tagTrailer::::" + tagTrailer);
	return tagTrailer.toString();
}

private String getUTCDateandTime() {
	try {
		ZonedDateTime now = ZonedDateTime.now(ZoneId.of("America/Los_Angeles"));
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
		return now.format(formatter);
	} catch (DateTimeParseException e) {
		e.printStackTrace();
		return null;
	}
}
private void addErrorMsg(String fileType, String fieldName, String errorMsg) {
	controller.getErrorMsglist().add(new ErrorMsgDetail(fileType, fieldName, errorMsg));
}

}
