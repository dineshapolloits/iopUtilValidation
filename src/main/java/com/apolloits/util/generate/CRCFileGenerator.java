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
import com.apolloits.util.modal.CRCTemplate;
import com.apolloits.util.modal.FileValidationParam;
import com.apolloits.util.utility.CommonUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CRCFileGenerator {



	@Autowired
	CommonUtil commonUtil;

	private String filename = "";
	private String fileCreateDateandTime = "";
	AgencyEntity agency;
	double detailAmt = 0.00;
	double acceptedAmt = 0.00;
	int acceptedCount;
	
	private List<CRCTemplate> crcTemplateList;
public boolean crcGenenerate(FileValidationParam validateParam) throws IOException {
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

	System.out.println("validateParam.getFromAgency()" + validateParam.getFromAgency() + "" + validateParam.getToAgency());
	String shortFromAgency = ctocShortAgency.get(validateParam.getFromAgency());
	String shortToAgency = ctocShortAgency.get(validateParam.getToAgency());

	System.out.println("validateParam..." + validateParam.getFileSequence());
		if (!commonUtil.validateParameter(validateParam)) {
			return false;
		}
		
		crcTemplateList = getCrcTemplateExcel(validateParam);
		String Header = generateCrcHeader(validateParam,crcTemplateList,shortFromAgency, shortToAgency);
		log.info("CRC Header :: " + Header);
		writeDetails(validateParam,Header,crcTemplateList,shortFromAgency, shortToAgency);
		String filePath = validateParam.getOutputFilePath() + File.separator + filename;
		//String zipFilename = commonUtil.moveToZipFile(filePath,validateParam);
		log.info("CRC file name :: "+filePath);
		validateParam.setResponseMsg("CRC file created ::\t "+filePath);
		return true;
	}
private List<CRCTemplate> getCrcTemplateExcel(FileValidationParam validateParam) {
		//ictxTemplateList = new ArrayList<>();
		//String TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
		String CRC_SHEET = "CRC";
		//InputStream inputStream;
		try {
			log.info("Excel data path localtion form getInputFilePath ::"+validateParam.getInputFilePath());
			FileInputStream is = new FileInputStream(validateParam.getInputFilePath());
			Workbook workbook = new XSSFWorkbook(is);
			log.info("Number of sheets : " + workbook.getNumberOfSheets());

			crcTemplateList = excelToTolList(workbook.getSheet(CRC_SHEET));

		} catch (Exception e) {
			validateParam.setResponseMsg("File not generated Please check input excel data");
			e.printStackTrace();
			return crcTemplateList = new ArrayList<>();
		}

		return crcTemplateList;
	}
private List<CRCTemplate> excelToTolList(Sheet sheet) {

  	 log.info("Inside ****************** excelToTolList()");
     /*  try {
      	
         Iterator<Row> rows = sheet.iterator();
         crcTemplateList = new ArrayList<>();
         int rowNumber = 0;
         while (rows.hasNext()) {
           Row currentRow = rows.next();
           // skip header
           if (rowNumber == 0) {
             rowNumber++;
             continue;
           }*/
  	try {
        Iterator<Row> rows = sheet.iterator();
        crcTemplateList = new ArrayList<>();
        int rowNumber = 0;
        while (rows.hasNext()) {
          Row currentRow = rows.next();
          // skip header
          if (rowNumber == 0) {
            rowNumber++;
            continue;
          }
          // Iterator<Cell> cellsInRow = currentRow.iterator();
           CRCTemplate tolTemp = new CRCTemplate();
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
            tolTemp.setPostAmt(commonUtil.getStringFormatCell(currentRow.getCell(43,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
            tolTemp.setResponseCode(commonUtil.getStringFormatCell(currentRow.getCell(44,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
            tolTemp.setOriginalFilename(commonUtil.getStringFormatCell(currentRow.getCell(45,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
          /* int cellIdx = 0;
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
            	 //  commonUtil.getStringFormatCell(currentRow.getCell(5,MissingCellPolicy.CREATE_NULL_AS_BLANK))
              	 tolTemp.setCorrLPtype(commonUtil.getStringFormatCell(currentRow.getCell(40,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
              	 System.out.println("currentCell"+currentCell);
              	 System.out.println("commonUtil.getStringFormatCell(currentCell)"+commonUtil.getStringFormatCell(currentCell));
                   System.out.println("case 40::"+tolTemp.getCorrLPtype());
                   break;
               case 41:
              	 tolTemp.setCorrTranFee(commonUtil.getStringFormatCell(currentCell));
                   System.out.println("case 41::"+tolTemp.getCorrTranFee());
                   System.out.println("currentCell"+currentCell);
                	 System.out.println("commonUtil.getStringFormatCell(currentCell)"+commonUtil.getStringFormatCell(currentCell));
                   
                   break; 
               case 42:
              	 tolTemp.setCorrTranFeeType(commonUtil.getStringFormatCell(currentCell));
                   System.out.println("case 42::"+tolTemp.getCorrTranFeeType());
                   System.out.println("currentCell"+currentCell);
                	 System.out.println("commonUtil.getStringFormatCell(currentCell)"+commonUtil.getStringFormatCell(currentCell));
                   
                   break; 
               case 43:
                	 tolTemp.setPostAmt(commonUtil.getStringFormatCell(currentCell));
                     System.out.println("case 43::"+tolTemp.getPostAmt());
                     System.out.println("currentCell"+currentCell);
                  	 System.out.println("commonUtil.getStringFormatCell(currentCell)"+commonUtil.getStringFormatCell(currentCell));
                     
                     break; 
                 case 44:
                	 tolTemp.setResponseCode(commonUtil.getStringFormatCell(currentCell));
                     System.out.println("case 44::"+tolTemp.getResponseCode());
                     System.out.println("currentCell"+currentCell);
                  	 System.out.println("commonUtil.getStringFormatCell(currentCell)"+commonUtil.getStringFormatCell(currentCell));
                     
                     break; 
               default:
            	 //  System.out.println("Default:: ********************");
                 break;
              }*/ 
              /* cellIdx++;
              
             }*/
           crcTemplateList.add(tolTemp);
           System.out.println(tolTemp.toString());
         }
        
         if(crcTemplateList != null && crcTemplateList.size()>0) {
       	  System.out.println("tolTemplateList ::"+crcTemplateList);
         log.info("@@@@ CRC input data  loaded sucessfully:: ******************** ::"+crcTemplateList.size());
         }else {
      	   throw new IopTranslatorException("CRC input data not loaded");
         }
         
       }catch (Exception e) {
      	log.error("Exception:: ******************** CRC Sheet");
			e.printStackTrace();
		}
     
		return crcTemplateList;
	
}
private String generateCrcHeader(FileValidationParam validateParam,List<CRCTemplate> tolTempList, String fromAgnecy, String toAgency) {

	fileCreateDateandTime = getUTCDateandTime();
	System.out.println("fileCreateDateandTime::::" + fileCreateDateandTime);
	// 0108_202503061430460800.ITAG
	// rcsr_2025-03-06T14:48:16-08:00.tag
	// validateParam.getFromAgency() +"_"+
	// fileCreateDateandTime.replaceAll("[-T:Z]",
	// "")+IAGConstants.ITAG_FILE_EXTENSION;
	fileCreateDateandTime = validateParam.getFileDate()
			+ fileCreateDateandTime.substring(fileCreateDateandTime.indexOf("T"), fileCreateDateandTime.length());
	String fileCreateDateandTimeCurrent = getUTCDateandTime();
	//tcsr_20250214_011955.pbp
	
	filename = fromAgnecy + toAgency + "_" + fileCreateDateandTime.replaceAll("[-:]", "").substring(0, 15) + 
			"_"+tolTempList.get(0).getOriginalFilename().substring(0,20)+".crc";
			//"_"+toAgency +fromAgnecy+"_"+fileCreateDateandTimeCurrent.replaceAll("[-:]", "").substring(0, 15)+".crc";

	//filename = fromAgnecy + toAgency + "_" + fileCreateDateandTime.replaceAll("[-:]", "").substring(0, 15) + ".crc";

	StringBuilder tagHeader = new StringBuilder();
	System.out.println("filename:::" + filename);

	tagHeader.append("#HEADER,");
	tagHeader.append("CORRECON,");
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
private void writeDetails(FileValidationParam validateParam, String header,List<CRCTemplate> trcTempList,String shortFromAgency,String shortToAgency)
		throws IOException {
	long start = System.currentTimeMillis();
	FileWriter writer;
	try {
		String filePath = validateParam.getOutputFilePath() + File.separator + filename;
		writer = new FileWriter(filePath, true);
		writer.write(header);
		writer.write(System.lineSeparator());
		System.out.print("Writing record raw... ");
		
		for (CRCTemplate trcTemplate : trcTempList) {
			writer.write(setTolDetailValues(trcTemplate,validateParam,detailAmt));
			writer.write(System.lineSeparator());
		}
		
		String trailer = generateCrcTrailer(validateParam, shortFromAgency, shortToAgency,detailAmt,trcTempList);
		writer.write(trailer);
		writer.write("\n");
		writer.flush();
		writer.close();
		detailAmt = 0.00;
		acceptedAmt = 0.00;
		acceptedCount = 0;
	}catch (Exception e) {
		validateParam.setResponseMsg("File not generated Please check log");
		e.printStackTrace();
	}
}
private String setTolDetailValues(CRCTemplate crcTemplate,FileValidationParam validateParam,double detailAmt) {

	StringBuilder tolDetail = new StringBuilder();
	System.out.println("detailAmt Start:::::::"+detailAmt);
	tolDetail.append(CommonUtil.formatStringLeftPad(crcTemplate.getCorrectionDate(),25,' ')+','); //CORRECTION DATE
	tolDetail.append(CommonUtil.formatStringLeftPad(crcTemplate.getCorrectionReason(),1,' ')+','); //CORRECTION REASON
	tolDetail.append(CommonUtil.formatStringLeftPad(crcTemplate.getResubmitReason(),1,' ')+',');//RESUBMIT REASON
	tolDetail.append(CommonUtil.formatStringLeftPad(crcTemplate.getCorrectionCount(),3,'0')+',');//CORRECTION COUNT
	tolDetail.append(CommonUtil.formatStringLeftPad(crcTemplate.getResubmitCount(),3,'0')+',');//RESUBMIT COUNT
	tolDetail.append(CommonUtil.formatStringLeftPad(crcTemplate.getHomeAgencySequence(),6,'0')+',');//HOME AGENCY SEQUENCE
	if(crcTemplate.getOriginalTagId().equalsIgnoreCase("null") || crcTemplate.getOriginalTagId().isEmpty() || crcTemplate.getOriginalTagId().isBlank())
		tolDetail.append(CommonUtil.formatStringLeftPad("",10,' ')+',');//ORIGINAL TAG ID
	else
		tolDetail.append(CommonUtil.formatStringLeftPad(crcTemplate.getOriginalTagId(),10,' ')+','); //ORIGINAL TAG ID
	if(crcTemplate.getOriginalLicensePlate().equalsIgnoreCase("null") || crcTemplate.getOriginalLicensePlate().isEmpty() || crcTemplate.getOriginalLicensePlate().isBlank())
		tolDetail.append(CommonUtil.formatStringLeftPad("",10,' ')+',');//ORIGINAL License Plate
	else
	   tolDetail.append(CommonUtil.formatStringLeftPad(crcTemplate.getOriginalLicensePlate(),10,' ')+','); //ORIGINAL License Plate
	if(crcTemplate.getOriginalState().equalsIgnoreCase("null") || crcTemplate.getOriginalState().isEmpty() || crcTemplate.getOriginalState().isBlank())
		tolDetail.append(CommonUtil.formatStringLeftPad("",2,' ')+',');//ORIGINAL State
	else
	   tolDetail.append(CommonUtil.formatStringLeftPad(crcTemplate.getOriginalState(),2,' ')+',');//ORIGINAL State
	tolDetail.append(CommonUtil.formatStringLeftPad(crcTemplate.getOriginalTran(),10,'0')+',');// ORIGINAL TRAN
	
	double tranAmount = Double.parseDouble(crcTemplate.getOriginalTranAmount());
	tranAmount = tranAmount/100;
	tolDetail.append(CommonUtil.formatStringLeftPad(String.format("%.2f", tranAmount),8,'0')+',');//ORIGINAL TRAN amount
	
	//tolDetail.append(CommonUtil.formatStringLeftPad(crcTemplate.getOriginalTranAmount(),8,' ')+',');//ORIGINAL TRAN amount
	
	if(crcTemplate.getOriginalEntryTranDate().equalsIgnoreCase("null") || crcTemplate.getOriginalEntryTranDate().isEmpty() || crcTemplate.getOriginalEntryTranDate().isBlank())
		tolDetail.append(CommonUtil.formatStringLeftPad("",25,' ')+',');//ENTRY TRAN DATE
	else tolDetail.append(CommonUtil.formatStringLeftPad(crcTemplate.getOriginalEntryTranDate(),25,' ')+',');//ENTRY TRAN DATE
	if(crcTemplate.getOriginalEntryPlaza().equalsIgnoreCase("null") || crcTemplate.getOriginalEntryPlaza().isEmpty() || crcTemplate.getOriginalEntryPlaza().isBlank())
		 tolDetail.append(CommonUtil.formatStringLeftPad("",22,' ')+',');//ENTRY PLAZA
	else tolDetail.append(CommonUtil.formatStringLeftPad(crcTemplate.getOriginalEntryPlaza(),22,' ')+',');//ENTRY PLAZA
	if(crcTemplate.getOriginalEntryLane().equalsIgnoreCase("null") || crcTemplate.getOriginalEntryLane().isEmpty() || crcTemplate.getOriginalEntryLane().isBlank())
		tolDetail.append(CommonUtil.formatStringLeftPad("",2,'0')+',');//ENTRY LANE
	else tolDetail.append(CommonUtil.formatStringLeftPad(crcTemplate.getOriginalEntryLane(),2,'0')+',');//ENTRY LANE
	tolDetail.append(CommonUtil.formatStringLeftPad(crcTemplate.getOriginalExitTranDate(),25,' ')+',');//EXIT TRAN DATE
	tolDetail.append(CommonUtil.formatStringLeftPad(crcTemplate.getOriginalExitPlaza(),22,' ')+',');//EXIT PLAZA
	tolDetail.append(CommonUtil.formatStringLeftPad(crcTemplate.getOriginalExitLane(),2,'0')+',');//EXIT LANE
	tolDetail.append(CommonUtil.formatStringLeftPad(crcTemplate.getOriginalAxleCount(),2,'0')+',');//AXLE COUNT
	tolDetail.append(CommonUtil.formatStringLeftPad(crcTemplate.getOriginalOccupancy(),1,'0')+',');//OCCUPANCY
	tolDetail.append(CommonUtil.formatStringLeftPad(crcTemplate.getOriginalProtocolType(),1,'0')+',');//PROTOCOL TYPE
	tolDetail.append(CommonUtil.formatStringLeftPad(crcTemplate.getOriginalvehicleType(),1,'0')+',');//VEHICLE TYPE
	if(crcTemplate.getOriginalLPtype().equalsIgnoreCase("null") || crcTemplate.getOriginalLPtype().isEmpty() || crcTemplate.getOriginalLPtype().isBlank())
		tolDetail.append(CommonUtil.formatStringLeftPad("",30,' ')+',');//ORIGINAL LP TYPE
	else
		tolDetail.append(CommonUtil.formatStringLeftPad(crcTemplate.getOriginalLPtype(),30,' ')+',');// ORIGINAL LP TYPE
	
	//tolDetail.append(CommonUtil.formatStringLeftPad(crcTemplate.getOriginalLPtype(),30,' ')+',');// ORIGINAL LP TYPE
	double tranFee = Double.parseDouble(crcTemplate.getOriginalTranFee());
	tranFee = tranFee/100;
	tolDetail.append(CommonUtil.formatStringLeftPad(String.format("%.2f", tranFee),8,'0')+',');//ORIGINAL TRAN FEE
	//tolDetail.append(CommonUtil.formatStringLeftPad(crcTemplate.getOriginalTranFee(),8,'0')+',');//ORIGINAL TRAN FEE
	tolDetail.append(CommonUtil.formatStringLeftPad(crcTemplate.getOriginalTranFeeType(),1,'0')+',');//ORIGINAL TRAN FEE TYPE
	
	if(crcTemplate.getCorrTagId().equalsIgnoreCase("null") || crcTemplate.getCorrTagId().isEmpty() || crcTemplate.getCorrTagId().isBlank())
		tolDetail.append(CommonUtil.formatStringLeftPad("",10,' ')+',');//ORIGINAL TAG ID
	else
		tolDetail.append(CommonUtil.formatStringLeftPad(crcTemplate.getCorrTagId(),10,' ')+','); //CORR TAG ID
	if(crcTemplate.getCorrLicensePlate().equalsIgnoreCase("null") || crcTemplate.getCorrLicensePlate().isEmpty() || crcTemplate.getCorrLicensePlate().isBlank())
		tolDetail.append(CommonUtil.formatStringLeftPad("",10,' ')+',');//CORR License Plate
	else
		tolDetail.append(CommonUtil.formatStringLeftPad(crcTemplate.getCorrLicensePlate(),10,' ')+','); //CORR License Plate
	if(crcTemplate.getCorrState().equalsIgnoreCase("null") || crcTemplate.getCorrState().isEmpty() || crcTemplate.getCorrState().isBlank())
		tolDetail.append(CommonUtil.formatStringLeftPad("",2,' ')+',');//CORR State
	else
		tolDetail.append(CommonUtil.formatStringLeftPad(crcTemplate.getCorrState(),2,' ')+',');//CORR State
	tolDetail.append(CommonUtil.formatStringLeftPad(crcTemplate.getCorrTran(),10,'0')+',');// CORR TRAN
	double tranAmountCor = Double.parseDouble(crcTemplate.getCorrTranAmount());
	tranAmountCor = tranAmountCor/100;
	tolDetail.append(CommonUtil.formatStringLeftPad(String.format("%.2f", tranAmountCor),8,'0')+',');//ORIGINAL TRAN amount
	
	//tolDetail.append(CommonUtil.formatStringLeftPad(crcTemplate.getCorrTranAmount(),8,' ')+',');//CORR TRAN amount
	
	if(crcTemplate.getCorrEntryTranDate().equalsIgnoreCase("null") || crcTemplate.getCorrEntryTranDate().isEmpty() || crcTemplate.getCorrEntryTranDate().isBlank())
		tolDetail.append(CommonUtil.formatStringLeftPad("",25,' ')+',');//ENTRY TRAN DATE
	else tolDetail.append(CommonUtil.formatStringLeftPad(crcTemplate.getCorrEntryTranDate(),25,' ')+',');//ENTRY TRAN DATE
	if(crcTemplate.getCorrEntryPlaza().equalsIgnoreCase("null") || crcTemplate.getCorrEntryPlaza().isEmpty() || crcTemplate.getCorrEntryPlaza().isBlank())
		 tolDetail.append(CommonUtil.formatStringLeftPad("",22,' ')+',');//ENTRY PLAZA
	else tolDetail.append(CommonUtil.formatStringLeftPad(crcTemplate.getCorrEntryPlaza(),22,' ')+',');//ENTRY PLAZA
	if(crcTemplate.getCorrEntryLane().equalsIgnoreCase("null") || crcTemplate.getCorrEntryLane().isEmpty() || crcTemplate.getCorrEntryLane().isBlank())
		tolDetail.append(CommonUtil.formatStringLeftPad("",2,'0')+',');//ENTRY LANE
	else tolDetail.append(CommonUtil.formatStringLeftPad(crcTemplate.getCorrEntryLane(),2,'0')+',');//ENTRY LANE
	tolDetail.append(CommonUtil.formatStringLeftPad(crcTemplate.getCorrExitTranDate(),25,' ')+',');//EXIT TRAN DATE
	tolDetail.append(CommonUtil.formatStringLeftPad(crcTemplate.getCorrExitPlaza(),22,' ')+',');//EXIT PLAZA
	tolDetail.append(CommonUtil.formatStringLeftPad(crcTemplate.getCorrExitLane(),2,'0')+',');//EXIT LANE
	tolDetail.append(CommonUtil.formatStringLeftPad(crcTemplate.getCorrAxleCount(),2,'0')+',');//AXLE COUNT
	tolDetail.append(CommonUtil.formatStringLeftPad(crcTemplate.getCorrOccupancy(),1,'0')+',');//OCCUPANCY
	tolDetail.append(CommonUtil.formatStringLeftPad(crcTemplate.getCorrProtocolType(),1,'0')+',');//PROTOCOL TYPE
	tolDetail.append(CommonUtil.formatStringLeftPad(crcTemplate.getCorrvehicleType(),1,'0')+',');//VEHICLE TYPE
	if(crcTemplate.getCorrLPtype().equalsIgnoreCase("null") || crcTemplate.getCorrLPtype().isEmpty() || crcTemplate.getCorrLPtype().isBlank())
		tolDetail.append(CommonUtil.formatStringLeftPad("",30,' ')+',');//ORIGINAL LP TYPE
	else
		tolDetail.append(CommonUtil.formatStringLeftPad(crcTemplate.getCorrLPtype(),30,' ')+',');// ORIGINAL LP TYPE

	double corrTranFee = Double.parseDouble(crcTemplate.getCorrTranFee());
	corrTranFee = corrTranFee/100;
	tolDetail.append(CommonUtil.formatStringLeftPad(String.format("%.2f", corrTranFee),8,'0')+',');//CORR TRAN FEE
	
	//tolDetail.append(CommonUtil.formatStringLeftPad(crcTemplate.getCorrTranFee(),8,'0')+',');//ORIGINAL TRAN FEE
	tolDetail.append(CommonUtil.formatStringLeftPad(crcTemplate.getCorrTranFeeType(),1,'0')+',');//ORIGINAL TRAN FEE TYPE
	double postAmt = Double.parseDouble(crcTemplate.getPostAmt());
	postAmt = postAmt/100;
	tolDetail.append(CommonUtil.formatStringLeftPad(String.format("%.2f", postAmt),8,'0')+',');//POST AMT
	tolDetail.append(CommonUtil.formatStringLeftPad(crcTemplate.getResponseCode(),1,' '));//RESPONSE CODE
	double toll=Double.parseDouble(crcTemplate.getCorrTranAmount());
	this.detailAmt = detailAmt+toll;
	
	if(crcTemplate.getResponseCode().trim().equalsIgnoreCase("A")) {
		double acceptedToll=Double.parseDouble(crcTemplate.getCorrTranAmount());
		this.acceptedAmt = acceptedAmt+acceptedToll;
		acceptedCount++;
	}
	
	System.out.println("detailAmt last:::::::"+detailAmt);
	return tolDetail.toString();
}
private String generateCrcTrailer(FileValidationParam validateParam, String fromAgnecy, String toAgency,double detailAmt,List<CRCTemplate> tolTempList) {
	
	StringBuilder tagTrailer = new StringBuilder();
	tagTrailer.append("#TRAILER,");
	tagTrailer.append(CommonUtil.formatStringLeftPad(String.valueOf(tolTempList.get(0).getSequence()),6,'0')+',');
	tagTrailer.append(validateParam.getFileDate().replaceAll("-", "/") + ',');
	tagTrailer.append(CommonUtil.formatStringLeftPad(String.valueOf(tolTempList.size()), 6, '0')+',' );
	DecimalFormat df = new DecimalFormat("#.00");
	//df.format(detailAmt);
	double tranFee = detailAmt/100;//Double.parseDouble(detailAmt);
	tagTrailer.append(CommonUtil.formatStringLeftPad(String.format("%.2f", tranFee),10,'0')+',');
	//tagTrailer.append(CommonUtil.formatStringLeftPad(String.valueOf(df.format(detailAmt)), 10, '0')+',');
	tagTrailer.append(CommonUtil.formatStringLeftPad(String.valueOf(acceptedCount), 6, '0')+','); //Accepted Count
	acceptedAmt = acceptedAmt/100;
	tagTrailer.append(CommonUtil.formatStringLeftPad(String.format("%.2f", acceptedAmt),10,'0')); //Accepted Amt
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
