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
import org.springframework.stereotype.Component;

import com.apolloits.util.IopTranslatorException;
import com.apolloits.util.modal.AgencyEntity;
import com.apolloits.util.modal.CORTemplate;
import com.apolloits.util.modal.FileValidationParam;
import com.apolloits.util.utility.CommonUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CORFileGenerator {
	@Autowired
	CommonUtil commonUtil;
	
	
	private String filename = "";
	private String fileCreateDateandTime = "";
	AgencyEntity agency;
	double detailAmt = 0.00;
	
	private List<CORTemplate> corTemplateList;

public boolean corGenenerate(FileValidationParam validateParam) throws IOException {
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
		
		corTemplateList = getCORTemplateExcel(validateParam);
		String Header = generateTolHeader(validateParam,corTemplateList,shortFromAgency, shortToAgency);
		log.info("COR Header :: " + Header);
		writeDetails(validateParam,Header,corTemplateList,shortFromAgency, shortToAgency);
		String filePath = validateParam.getOutputFilePath() + File.separator + filename;
		log.info("COR file name :: "+filePath);
		validateParam.setResponseMsg("COR file created ::\t "+filePath);
		return true;
	}
private List<CORTemplate> getCORTemplateExcel(FileValidationParam validateParam) {
		String TOL_SHEET = "COR";
		try {
			log.info("Excel data path localtion form getInputFilePath ::"+validateParam.getInputFilePath());
			FileInputStream is = new FileInputStream(validateParam.getInputFilePath());
			Workbook workbook = new XSSFWorkbook(is);
			log.info("Number of sheets : " + workbook.getNumberOfSheets());

			corTemplateList = excelToTolList(workbook.getSheet(TOL_SHEET));

		} catch (Exception e) {
			validateParam.setResponseMsg("File not generated Please check input excel data");
			e.printStackTrace();
			return corTemplateList = new ArrayList<>();
		}

		return corTemplateList;
	}
private List<CORTemplate> excelToTolList(Sheet sheet) {

  	 log.info("Inside ****************** excelToTolList()");
       try {
      	
         Iterator<Row> rows = sheet.iterator();
         corTemplateList = new ArrayList<>();
         int rowNumber = 0;
         while (rows.hasNext()) {
           Row currentRow = rows.next();
           // skip header
           if (rowNumber == 0) {
             rowNumber++;
             continue;
           }
           //Iterator<Cell> cellsInRow = currentRow.iterator();
           CORTemplate tolTemp = new CORTemplate();
           tolTemp.setSequence(commonUtil.getStringFormatCell(currentRow.getCell(0,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
      	 tolTemp.setCorrectionDate(commonUtil.getStringFormatCell(currentRow.getCell(1,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
      	 tolTemp.setCorrectionReason(commonUtil.getStringFormatCell(currentRow.getCell(2,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
      	 tolTemp.setResubmitReason(commonUtil.getStringFormatCell(currentRow.getCell(3,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
      	 tolTemp.setCorrectionCount(commonUtil.getStringFormatCell(currentRow.getCell(4,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
      	 tolTemp.setResubmitCount(commonUtil.getStringFormatCell(currentRow.getCell(5,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
      	 tolTemp.setHomeAgencySequence(commonUtil.getStringFormatCell(currentRow.getCell(6,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
      	 tolTemp.setOriginalTagId(commonUtil.getStringFormatCell(currentRow.getCell(7,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
      	 tolTemp.setOriginalLicensePlate(commonUtil.getStringFormatCell(currentRow.getCell(8,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
      	 tolTemp.setOriginalState(commonUtil.getStringFormatCell(currentRow.getCell(9,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
      	 tolTemp.setOriginalTran(commonUtil.getStringFormatCell(currentRow.getCell(10,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
      	 tolTemp.setOriginalTranAmount(commonUtil.getStringFormatCell(currentRow.getCell(11,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
    	 tolTemp.setOriginalEntryTranDate(commonUtil.getStringFormatCell(currentRow.getCell(12,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
    	 tolTemp.setOriginalEntryPlaza(commonUtil.getStringFormatCell(currentRow.getCell(13,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
    	 tolTemp.setOriginalEntryLane(commonUtil.getStringFormatCell(currentRow.getCell(14,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
    	 tolTemp.setOriginalExitTranDate(commonUtil.getStringFormatCell(currentRow.getCell(15,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
    	 tolTemp.setOriginalExitPlaza(commonUtil.getStringFormatCell(currentRow.getCell(16,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
    	 tolTemp.setOriginalExitLane(commonUtil.getStringFormatCell(currentRow.getCell(17,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
    	 tolTemp.setOriginalAxleCount(commonUtil.getStringFormatCell(currentRow.getCell(18,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
    	 tolTemp.setOriginalOccupancy(commonUtil.getStringFormatCell(currentRow.getCell(19,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
    	 tolTemp.setOriginalProtocolType(commonUtil.getStringFormatCell(currentRow.getCell(20,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
    	 tolTemp.setOriginalvehicleType(commonUtil.getStringFormatCell(currentRow.getCell(21,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
    	 tolTemp.setOriginalLPtype(commonUtil.getStringFormatCell(currentRow.getCell(22,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
    	 tolTemp.setOriginalTranFee(commonUtil.getStringFormatCell(currentRow.getCell(23,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
    	 tolTemp.setOriginalTranFeeType(commonUtil.getStringFormatCell(currentRow.getCell(24,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
    	 tolTemp.setCorrTagId(commonUtil.getStringFormatCell(currentRow.getCell(25,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
    	 tolTemp.setCorrLicensePlate(commonUtil.getStringFormatCell(currentRow.getCell(26,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
    	 tolTemp.setCorrState(commonUtil.getStringFormatCell(currentRow.getCell(27,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
    	 tolTemp.setCorrTran(commonUtil.getStringFormatCell(currentRow.getCell(28,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
    	 tolTemp.setCorrTranAmount(commonUtil.getStringFormatCell(currentRow.getCell(29,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
    	 tolTemp.setCorrEntryTranDate(commonUtil.getStringFormatCell(currentRow.getCell(30,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
    	 tolTemp.setCorrEntryPlaza(commonUtil.getStringFormatCell(currentRow.getCell(31,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
    	 tolTemp.setCorrEntryLane(commonUtil.getStringFormatCell(currentRow.getCell(32,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
    	 tolTemp.setCorrExitTranDate(commonUtil.getStringFormatCell(currentRow.getCell(33,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
    	 tolTemp.setCorrExitPlaza(commonUtil.getStringFormatCell(currentRow.getCell(34,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
    	 tolTemp.setCorrExitLane(commonUtil.getStringFormatCell(currentRow.getCell(35,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
    	 tolTemp.setCorrAxleCount(commonUtil.getStringFormatCell(currentRow.getCell(36,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
    	 tolTemp.setCorrOccupancy(commonUtil.getStringFormatCell(currentRow.getCell(37,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
    	 tolTemp.setCorrProtocolType(commonUtil.getStringFormatCell(currentRow.getCell(38,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
    	 tolTemp.setCorrvehicleType(commonUtil.getStringFormatCell(currentRow.getCell(39,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
    	 tolTemp.setCorrLPtype(commonUtil.getStringFormatCell(currentRow.getCell(40,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
    	 tolTemp.setCorrTranFee(commonUtil.getStringFormatCell(currentRow.getCell(41,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
    	 tolTemp.setCorrTranFeeType(commonUtil.getStringFormatCell(currentRow.getCell(42,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
    	 
           corTemplateList.add(tolTemp);
           System.out.println(tolTemp.toString());
         }
        
         if(corTemplateList != null && corTemplateList.size()>0) {
       	  System.out.println("corTemplateList ::"+corTemplateList);
         log.info("@@@@ COR input data  loaded sucessfully:: ******************** ::"+corTemplateList.size());
         }else {
      	   throw new IopTranslatorException("COR input data not loaded");
         }
         
       }catch (Exception e) {
      	log.error("Exception:: ******************** COR Sheet");
			e.printStackTrace();
		}
     
		return corTemplateList;
	
}
private String generateTolHeader(FileValidationParam validateParam,List<CORTemplate> tolTempList, String fromAgnecy, String toAgency) {

	fileCreateDateandTime = getUTCDateandTime();
	fileCreateDateandTime = validateParam.getFileDate()
			+ fileCreateDateandTime.substring(fileCreateDateandTime.indexOf("T"), fileCreateDateandTime.length());
	filename = fromAgnecy + toAgency + "_" + fileCreateDateandTime.replaceAll("[-:]", "").substring(0, 15) + ".cor";
	String createUTCdate = CommonUtil.convertDatetoUTC(filename.substring(5,20));
	StringBuilder tagHeader = new StringBuilder();
	System.out.println("filename:::" + filename);

	tagHeader.append("#HEADER,");
	tagHeader.append("CORR,");
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
private void writeDetails(FileValidationParam validateParam, String header,List<CORTemplate> tolTempList,String shortFromAgency,String shortToAgency)
		throws IOException {
	long start = System.currentTimeMillis();
	FileWriter writer;
	try {
		String filePath = validateParam.getOutputFilePath() + File.separator + filename;
		writer = new FileWriter(filePath, true);
		writer.write(header);
		writer.write(System.lineSeparator());
		System.out.print("Writing record raw... ");
		
		for (CORTemplate corTemplate : tolTempList) {
			writer.write(setTolDetailValues(corTemplate,validateParam,detailAmt));
			writer.write(System.lineSeparator());
		}
		
		String trailer = generateCorTrailer(validateParam, shortFromAgency, shortToAgency,detailAmt,tolTempList);
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
private String setTolDetailValues(CORTemplate corTemplate,FileValidationParam validateParam,double detailAmt) {

	StringBuilder tolDetail = new StringBuilder();
	System.out.println("detailAmt Start:::::::"+detailAmt);
	tolDetail.append(CommonUtil.formatStringLeftPad(corTemplate.getCorrectionDate(),25,' ')+','); //CORRECTION DATE
	tolDetail.append(CommonUtil.formatStringLeftPad(corTemplate.getCorrectionReason(),1,' ')+','); //CORRECTION REASON
	tolDetail.append(CommonUtil.formatStringLeftPad(corTemplate.getResubmitReason(),1,' ')+',');//RESUBMIT REASON
	tolDetail.append(CommonUtil.formatStringLeftPad(corTemplate.getCorrectionCount(),3,'0')+',');//CORRECTION COUNT
	tolDetail.append(CommonUtil.formatStringLeftPad(corTemplate.getResubmitCount(),3,'0')+',');//RESUBMIT COUNT
	tolDetail.append(CommonUtil.formatStringLeftPad(corTemplate.getHomeAgencySequence(),6,'0')+',');//HOME AGENCY SEQUENCE
	if(corTemplate.getOriginalTagId().equalsIgnoreCase("null") || corTemplate.getOriginalTagId().isEmpty() || corTemplate.getOriginalTagId().isBlank())
		tolDetail.append(CommonUtil.formatStringLeftPad("",10,' ')+',');//ORIGINAL TAG ID
	else
		tolDetail.append(CommonUtil.formatStringLeftPad(corTemplate.getOriginalTagId(),10,' ')+','); //ORIGINAL TAG ID
	if(corTemplate.getOriginalLicensePlate().equalsIgnoreCase("null") || corTemplate.getOriginalLicensePlate().isEmpty() || corTemplate.getOriginalLicensePlate().isBlank())
		tolDetail.append(CommonUtil.formatStringLeftPad("",10,' ')+',');//ORIGINAL License Plate
	else
	   tolDetail.append(CommonUtil.formatStringLeftPad(corTemplate.getOriginalLicensePlate(),10,' ')+','); //ORIGINAL License Plate
	if(corTemplate.getOriginalState().equalsIgnoreCase("null") || corTemplate.getOriginalState().isEmpty() || corTemplate.getOriginalState().isBlank())
		tolDetail.append(CommonUtil.formatStringLeftPad("",2,' ')+',');//ORIGINAL State
	else
	   tolDetail.append(CommonUtil.formatStringLeftPad(corTemplate.getOriginalState(),2,' ')+',');//ORIGINAL State
	tolDetail.append(CommonUtil.formatStringLeftPad(corTemplate.getOriginalTran(),10,'0')+',');// ORIGINAL TRAN
	double tranAmount = Double.parseDouble(corTemplate.getOriginalTranAmount());
	tranAmount = tranAmount/100;
	tolDetail.append(CommonUtil.formatStringLeftPad(String.format("%.2f", tranAmount),8,'0')+',');//ORIGINAL TRAN amount
	if(corTemplate.getOriginalEntryTranDate().equalsIgnoreCase("null") || corTemplate.getOriginalEntryTranDate().isEmpty() || corTemplate.getOriginalEntryTranDate().isBlank())
		tolDetail.append(CommonUtil.formatStringLeftPad("",25,' ')+',');//ENTRY TRAN DATE
	else tolDetail.append(CommonUtil.formatStringLeftPad(corTemplate.getOriginalEntryTranDate(),25,' ')+',');//ENTRY TRAN DATE
	if(corTemplate.getOriginalEntryPlaza().equalsIgnoreCase("null") || corTemplate.getOriginalEntryPlaza().isEmpty() || corTemplate.getOriginalEntryPlaza().isBlank())
		 tolDetail.append(CommonUtil.formatStringLeftPad("",22,' ')+',');//ENTRY PLAZA
	else tolDetail.append(CommonUtil.formatStringLeftPad(corTemplate.getOriginalEntryPlaza(),22,' ')+',');//ENTRY PLAZA
	if(corTemplate.getOriginalEntryLane().equalsIgnoreCase("null") || corTemplate.getOriginalEntryLane().isEmpty() || corTemplate.getOriginalEntryLane().isBlank())
		tolDetail.append(CommonUtil.formatStringLeftPad("",2,'0')+',');//ENTRY LANE
	else tolDetail.append(CommonUtil.formatStringLeftPad(corTemplate.getOriginalEntryLane(),2,'0')+',');//ENTRY LANE
	tolDetail.append(CommonUtil.formatStringLeftPad(corTemplate.getOriginalExitTranDate(),25,' ')+',');//EXIT TRAN DATE
	tolDetail.append(CommonUtil.formatStringLeftPad(corTemplate.getOriginalExitPlaza(),22,' ')+',');//EXIT PLAZA
	tolDetail.append(CommonUtil.formatStringLeftPad(corTemplate.getOriginalExitLane(),2,'0')+',');//EXIT LANE
	tolDetail.append(CommonUtil.formatStringLeftPad(corTemplate.getOriginalAxleCount(),2,'0')+',');//AXLE COUNT
	tolDetail.append(CommonUtil.formatStringLeftPad(corTemplate.getOriginalOccupancy(),1,'0')+',');//OCCUPANCY
	tolDetail.append(CommonUtil.formatStringLeftPad(corTemplate.getOriginalProtocolType(),1,'0')+',');//PROTOCOL TYPE
	tolDetail.append(CommonUtil.formatStringLeftPad(corTemplate.getOriginalvehicleType(),1,'0')+',');//VEHICLE TYPE
	tolDetail.append(CommonUtil.formatStringLeftPad(corTemplate.getOriginalLPtype(),30,' ')+',');// ORIGINAL LP TYPE
	double tranFee = Double.parseDouble(corTemplate.getOriginalTranFee());
	tranFee = tranFee/100;
	tolDetail.append(CommonUtil.formatStringLeftPad(String.format("%.2f", tranFee),8,'0')+',');//ORIGINAL TRAN FEE
	tolDetail.append(CommonUtil.formatStringLeftPad(corTemplate.getOriginalTranFeeType(),1,'0')+',');//ORIGINAL TRAN FEE TYPE
	
	if(corTemplate.getCorrTagId().equalsIgnoreCase("null") || corTemplate.getCorrTagId().isEmpty() || corTemplate.getCorrTagId().isBlank())
		tolDetail.append(CommonUtil.formatStringLeftPad("",10,' ')+',');//ORIGINAL TAG ID
	else
		tolDetail.append(CommonUtil.formatStringLeftPad(corTemplate.getCorrTagId(),10,' ')+','); //CORR TAG ID
	if(corTemplate.getCorrLicensePlate().equalsIgnoreCase("null") || corTemplate.getCorrLicensePlate().isEmpty() || corTemplate.getCorrLicensePlate().isBlank())
		tolDetail.append(CommonUtil.formatStringLeftPad("",10,' ')+',');//CORR License Plate
	else
		tolDetail.append(CommonUtil.formatStringLeftPad(corTemplate.getCorrLicensePlate(),10,' ')+','); //CORR License Plate
	if(corTemplate.getCorrState().equalsIgnoreCase("null") || corTemplate.getCorrState().isEmpty() || corTemplate.getCorrState().isBlank())
		tolDetail.append(CommonUtil.formatStringLeftPad("",2,' ')+',');//CORR State
	else
		tolDetail.append(CommonUtil.formatStringLeftPad(corTemplate.getCorrState(),2,' ')+',');//CORR State
	tolDetail.append(CommonUtil.formatStringLeftPad(corTemplate.getCorrTran(),10,'0')+',');// CORR TRAN
	double tranAmountCor = Double.parseDouble(corTemplate.getCorrTranAmount());
	tranAmountCor = tranAmountCor/100;
	tolDetail.append(CommonUtil.formatStringLeftPad(String.format("%.2f", tranAmountCor),8,'0')+',');//ORIGINAL TRAN amount
	
	if(corTemplate.getCorrEntryTranDate().equalsIgnoreCase("null") || corTemplate.getCorrEntryTranDate().isEmpty() || corTemplate.getCorrEntryTranDate().isBlank())
		tolDetail.append(CommonUtil.formatStringLeftPad("",25,' ')+',');//ENTRY TRAN DATE
	else tolDetail.append(CommonUtil.formatStringLeftPad(corTemplate.getCorrEntryTranDate(),25,' ')+',');//ENTRY TRAN DATE
	if(corTemplate.getCorrEntryPlaza().equalsIgnoreCase("null") || corTemplate.getCorrEntryPlaza().isEmpty() || corTemplate.getCorrEntryPlaza().isBlank())
		 tolDetail.append(CommonUtil.formatStringLeftPad("",22,' ')+',');//ENTRY PLAZA
	else tolDetail.append(CommonUtil.formatStringLeftPad(corTemplate.getCorrEntryPlaza(),22,' ')+',');//ENTRY PLAZA
	if(corTemplate.getCorrEntryLane().equalsIgnoreCase("null") || corTemplate.getCorrEntryLane().isEmpty() || corTemplate.getCorrEntryLane().isBlank())
		tolDetail.append(CommonUtil.formatStringLeftPad("",2,'0')+',');//ENTRY LANE
	else tolDetail.append(CommonUtil.formatStringLeftPad(corTemplate.getCorrEntryLane(),2,'0')+',');//ENTRY LANE
	tolDetail.append(CommonUtil.formatStringLeftPad(corTemplate.getCorrExitTranDate(),25,' ')+',');//EXIT TRAN DATE
	tolDetail.append(CommonUtil.formatStringLeftPad(corTemplate.getCorrExitPlaza(),22,' ')+',');//EXIT PLAZA
	tolDetail.append(CommonUtil.formatStringLeftPad(corTemplate.getCorrExitLane(),2,'0')+',');//EXIT LANE
	tolDetail.append(CommonUtil.formatStringLeftPad(corTemplate.getCorrAxleCount(),2,'0')+',');//AXLE COUNT
	tolDetail.append(CommonUtil.formatStringLeftPad(corTemplate.getCorrOccupancy(),1,'0')+',');//OCCUPANCY
	tolDetail.append(CommonUtil.formatStringLeftPad(corTemplate.getCorrProtocolType(),1,'0')+',');//PROTOCOL TYPE
	tolDetail.append(CommonUtil.formatStringLeftPad(corTemplate.getCorrvehicleType(),1,'0')+',');//VEHICLE TYPE
	if(corTemplate.getCorrLPtype().equalsIgnoreCase("null") || corTemplate.getCorrLPtype().isEmpty() || corTemplate.getCorrLPtype().isBlank())
		tolDetail.append(CommonUtil.formatStringLeftPad("",30,' ')+',');//ORIGINAL LP TYPE
	else
		tolDetail.append(CommonUtil.formatStringLeftPad(corTemplate.getCorrLPtype(),30,' ')+',');// ORIGINAL LP TYPE
	
	double corrTranFee = Double.parseDouble(corTemplate.getCorrTranFee());
	corrTranFee = corrTranFee/100;
	tolDetail.append(CommonUtil.formatStringLeftPad(String.format("%.2f", corrTranFee),8,'0')+',');//CORR TRAN FEE
	tolDetail.append(CommonUtil.formatStringLeftPad(corTemplate.getCorrTranFeeType(),1,'0'));//ORIGINAL TRAN FEE TYPE
	double toll=Double.parseDouble(corTemplate.getCorrTranAmount());
	this.detailAmt = detailAmt+toll;
	
	System.out.println("detailAmt last:::::::"+detailAmt);
	return tolDetail.toString();
}
private String generateCorTrailer(FileValidationParam validateParam, String fromAgnecy, String toAgency,double detailAmt,List<CORTemplate> tolTempList) {
	
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


}
