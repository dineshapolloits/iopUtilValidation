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
		//String zipFilename = commonUtil.moveToZipFile(filePath,validateParam);
		log.info("COR file name :: "+filePath);
		validateParam.setResponseMsg("COR file created ::\t "+filePath);
		return true;
	}
private List<CORTemplate> getCORTemplateExcel(FileValidationParam validateParam) {
		//ictxTemplateList = new ArrayList<>();
		//String TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
		String TOL_SHEET = "COR";
		//InputStream inputStream;

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
           Iterator<Cell> cellsInRow = currentRow.iterator();
           CORTemplate tolTemp = new CORTemplate();
           int cellIdx = 0;
           while (cellsInRow.hasNext()) {
             Cell currentCell = cellsInRow.next();
             switch (cellIdx) {
             case 0:
            	 tolTemp.setSequence(commonUtil.getStringFormatCell(currentCell));
            	   System.out.println("case 0::"+tolTemp.getSequence());
                 break;
             case 1:
            	 tolTemp.setCorrectionDate(commonUtil.getStringFormatCell(currentCell));
          	   System.out.println("case 1::"+tolTemp.getCorrectionDate());
               break;
             case 2:
           	  //ictxTemp.setEtcTrxSerialNo(commonUtil.getStringFormatCell(currentRow.getCell(1,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
            	 tolTemp.setCorrectionReason(commonUtil.getStringFormatCell(currentCell));
                 System.out.println("case 2::"+tolTemp.getCorrectionReason());
                 
               break;
             case 3:
            	 tolTemp.setResubmitReason(commonUtil.getStringFormatCell(currentCell));
                 System.out.println("case 3::"+tolTemp.getResubmitReason());
               break;
             case 4:
            	 tolTemp.setCorrectionCount(commonUtil.getStringFormatCell(currentCell));
               System.out.println("case 4::"+tolTemp.getCorrectionCount());
               break;
             case 5:
            	 tolTemp.setResubmitCount(commonUtil.getStringFormatCell(currentCell));
                 System.out.println("case 5::"+tolTemp.getResubmitCount());
                 break;
             case 6:
            	 tolTemp.setHomeAgencySequence(commonUtil.getStringFormatCell(currentCell));
                 System.out.println("case 6::"+tolTemp.getHomeAgencySequence());
                 break;
             case 7:
            	 tolTemp.setOriginalTagId(commonUtil.getStringFormatCell(currentCell));
                 System.out.println("case 7::"+tolTemp.getOriginalTagId());
                 break;
             case 8:
            	 tolTemp.setOriginalLicensePlate(commonUtil.getStringFormatCell(currentCell));
                 System.out.println("case 8::"+tolTemp.getOriginalLicensePlate());
                 break;
             case 9:
            	 tolTemp.setOriginalState(commonUtil.getStringFormatCell(currentCell));
                 System.out.println("case 9::"+tolTemp.getOriginalState());
                 break;
             case 10:
            	 tolTemp.setOriginalTran(commonUtil.getStringFormatCell(currentCell));
                 System.out.println("case 10::"+tolTemp.getOriginalTran());
                 break;
             case 11:
            	 tolTemp.setOriginalTranAmount(commonUtil.getStringFormatCell(currentCell));
                 System.out.println("case 11::"+tolTemp.getOriginalTranAmount());
                 break;
             case 12:
            	 tolTemp.setOriginalEntryTranDate(commonUtil.getStringFormatCell(currentCell));
                 System.out.println("case 12::"+tolTemp.getOriginalEntryTranDate());
                 break;
             case 13:
            	 tolTemp.setOriginalEntryPlaza(commonUtil.getStringFormatCell(currentCell));
                 System.out.println("case 13::"+tolTemp.getOriginalEntryPlaza());
                 break;
             case 14:
            	 tolTemp.setOriginalEntryLane(commonUtil.getStringFormatCell(currentCell));
                 System.out.println("case 14::"+tolTemp.getOriginalEntryLane());
                 break;
             case 15:
            	 tolTemp.setOriginalExitTranDate(commonUtil.getStringFormatCell(currentCell));
                 System.out.println("case 15::"+tolTemp.getOriginalExitTranDate());
                 break;
             case 16:
            	 tolTemp.setOriginalExitPlaza(commonUtil.getStringFormatCell(currentCell));
                 System.out.println("case 16::"+tolTemp.getOriginalExitPlaza());
                 break;
                 
             case 17:
            	 tolTemp.setOriginalExitLane(commonUtil.getStringFormatCell(currentCell));
                 System.out.println("case 17::"+tolTemp.getOriginalExitLane());
                 break;
             case 18:
            	 tolTemp.setOriginalAxleCount(commonUtil.getStringFormatCell(currentCell));
                 System.out.println("case 18::"+tolTemp.getOriginalAxleCount());
                 break;
             case 19:
            	 tolTemp.setOriginalOccupancy(commonUtil.getStringFormatCell(currentCell));
                 System.out.println("case 19::"+tolTemp.getOriginalOccupancy());
                 break;
             case 20:
            	 tolTemp.setOriginalProtocolType(commonUtil.getStringFormatCell(currentCell));
                 System.out.println("case 20::"+tolTemp.getOriginalProtocolType());
                 break;
             case 21:
            	 tolTemp.setOriginalvehicleType(commonUtil.getStringFormatCell(currentCell));
                 System.out.println("case 21::"+tolTemp.getOriginalvehicleType());
                 break;
             case 22:
            	 tolTemp.setOriginalLPtype(commonUtil.getStringFormatCell(currentCell));
                 System.out.println("case 22::"+tolTemp.getOriginalLPtype());
                 break;
             case 23:
            	 tolTemp.setOriginalTranFee(commonUtil.getStringFormatCell(currentCell));
                 System.out.println("case 23::"+tolTemp.getOriginalTranFee());
                 break;
             case 24:
            	 tolTemp.setOriginalTranFeeType(commonUtil.getStringFormatCell(currentCell));
                 System.out.println("case 24::"+tolTemp.getOriginalTranFeeType());
                 break;
             case 25:
            	 tolTemp.setCorrTagId(commonUtil.getStringFormatCell(currentCell));
                 System.out.println("case 25::"+tolTemp.getCorrTagId());
                 break;
             case 26:
            	 tolTemp.setCorrLicensePlate(commonUtil.getStringFormatCell(currentCell));
                 System.out.println("case 26::"+tolTemp.getCorrLicensePlate());
                 break;    
             case 27:
            	 tolTemp.setCorrState(commonUtil.getStringFormatCell(currentCell));
                 System.out.println("case 27::"+tolTemp.getCorrState());
                 break;
             case 28:
            	 tolTemp.setCorrTran(commonUtil.getStringFormatCell(currentCell));
                 System.out.println("case 28::"+tolTemp.getCorrTran());
                 break;  
             case 29:
            	 tolTemp.setCorrTranAmount(commonUtil.getStringFormatCell(currentCell));
                 System.out.println("case 29::"+tolTemp.getCorrTranAmount());
                 break;
             case 30:
            	 tolTemp.setCorrEntryTranDate(commonUtil.getStringFormatCell(currentCell));
                 System.out.println("case 30::"+tolTemp.getCorrEntryTranDate());
                 break;    
             case 31:
            	 tolTemp.setCorrEntryPlaza(commonUtil.getStringFormatCell(currentCell));
                 System.out.println("case 31::"+tolTemp.getCorrEntryPlaza());
                 break;
             case 32:
            	 tolTemp.setCorrEntryLane(commonUtil.getStringFormatCell(currentCell));
                 System.out.println("case 32::"+tolTemp.getCorrEntryLane());
                 break;  
             case 33:
            	 tolTemp.setCorrExitTranDate(commonUtil.getStringFormatCell(currentCell));
                 System.out.println("case 33::"+tolTemp.getCorrExitTranDate());
                 break;
             case 34:
            	 tolTemp.setCorrExitPlaza(commonUtil.getStringFormatCell(currentCell));
                 System.out.println("case 34::"+tolTemp.getCorrExitPlaza());
                 break;    
             case 35:
            	 tolTemp.setCorrExitLane(commonUtil.getStringFormatCell(currentCell));
                 System.out.println("case 35::"+tolTemp.getCorrExitLane());
                 break;
             case 36:
            	 tolTemp.setCorrAxleCount(commonUtil.getStringFormatCell(currentCell));
                 System.out.println("case 36::"+tolTemp.getCorrAxleCount());
                 break;  
             case 37:
            	 tolTemp.setCorrOccupancy(commonUtil.getStringFormatCell(currentCell));
                 System.out.println("case 37::"+tolTemp.getCorrOccupancy());
                 break;  
             case 38:
            	 tolTemp.setCorrProtocolType(commonUtil.getStringFormatCell(currentCell));
                 System.out.println("case 38::"+tolTemp.getCorrProtocolType());
                 break;
             case 39:
            	 tolTemp.setCorrvehicleType(commonUtil.getStringFormatCell(currentCell));
                 System.out.println("case 39::"+tolTemp.getCorrvehicleType());
                 break;    
             case 40:
            	 tolTemp.setCorrLPtype(commonUtil.getStringFormatCell(currentCell));
                 System.out.println("case 40::"+tolTemp.getCorrLPtype());
                 break;
             case 41:
            	 tolTemp.setCorrTranFee(commonUtil.getStringFormatCell(currentCell));
                 System.out.println("case 41::"+tolTemp.getCorrTranFee());
                 break; 
             case 42:
            	 tolTemp.setCorrTranFeeType(commonUtil.getStringFormatCell(currentCell));
                 System.out.println("case 41::"+tolTemp.getCorrTranFeeType());
                 break; 
             default:
          	 //  System.out.println("Default:: ********************");
               break;
             }
             cellIdx++;
            
           }
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
	System.out.println("fileCreateDateandTime::::" + fileCreateDateandTime);
	// 0108_202503061430460800.ITAG
	// rcsr_2025-03-06T14:48:16-08:00.tag
	// validateParam.getFromAgency() +"_"+
	// fileCreateDateandTime.replaceAll("[-T:Z]",
	// "")+IAGConstants.ITAG_FILE_EXTENSION;
	fileCreateDateandTime = validateParam.getFileDate()
			+ fileCreateDateandTime.substring(fileCreateDateandTime.indexOf("T"), fileCreateDateandTime.length());
	filename = fromAgnecy + toAgency + "_" + fileCreateDateandTime.replaceAll("[-:]", "").substring(0, 15) + ".cor";

	StringBuilder tagHeader = new StringBuilder();
	System.out.println("filename:::" + filename);

	tagHeader.append("#HEADER,");
	tagHeader.append("CORR,");
	tagHeader.append(CommonUtil.formatStringLeftPad(String.valueOf(tolTempList.get(0).getSequence()),6,'0')+','); // SEQUENCE
	tagHeader.append(validateParam.getFileDate().replaceAll("-", "/") + ','); // BUSINESS DAY
	tagHeader.append(fromAgnecy.toUpperCase() + ','); // SOURCE
	tagHeader.append(toAgency.toUpperCase() + ','); // DESTINATION
	tagHeader.append(CommonUtil.formatStringLeftPad(fileCreateDateandTime, 25, ' ') + ','); // CREATE DATE
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
	
	//tolDetail.append(CommonUtil.formatStringLeftPad(corTemplate.getOriginalTranAmount(),8,' ')+',');//ORIGINAL TRAN amount
	
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
	
	//tolDetail.append(CommonUtil.formatStringLeftPad(corTemplate.getOriginalTranFee(),8,'0')+',');//ORIGINAL TRAN FEE
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
	
//	tolDetail.append(CommonUtil.formatStringLeftPad(corTemplate.getCorrTranAmount(),8,' ')+',');//CORR TRAN amount
	
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
	
	//tolDetail.append(CommonUtil.formatStringLeftPad(corTemplate.getCorrTranFee(),8,'0')+',');//ORIGINAL TRAN 
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
	//df.format(detailAmt);
	double tranFee = detailAmt/100;//Double.parseDouble(detailAmt);
	tagTrailer.append(CommonUtil.formatStringLeftPad(String.format("%.2f", tranFee),10,'0'));
	//tolDetail.append(CommonUtil.formatStringLeftPad(String.format("%.2f", tranFee),8,'0')+',');//ORIGINAL TRAN FEE
	
	//tagTrailer.append(CommonUtil.formatStringLeftPad(String.valueOf(df.format(detailAmt)), 10, '0'));
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
