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
import com.apolloits.util.modal.FileValidationParam;
import com.apolloits.util.modal.PRCTemplate;
import com.apolloits.util.utility.CommonUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PRCFileGenerator {


	@Autowired
	CommonUtil commonUtil;

	private String filename = "";
	private String fileCreateDateandTime = "";
	AgencyEntity agency;
	double detailAmt = 0.00;
	double acceptedAmt = 0.00;
	int acceptedCount;
	
	private List<PRCTemplate> prcTemplateList;
public boolean prcGenenerate(FileValidationParam validateParam) throws IOException {
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
		
		prcTemplateList = getTolTemplateExcel(validateParam);
		String Header = generateTolHeader(validateParam,prcTemplateList,shortFromAgency, shortToAgency);
		log.info("Tol Header :: " + Header);
		writeDetails(validateParam,Header,prcTemplateList,shortFromAgency, shortToAgency);
		String filePath = validateParam.getOutputFilePath() + File.separator + filename;
		//String zipFilename = commonUtil.moveToZipFile(filePath,validateParam);
		log.info("Tol file name :: "+filePath);
		validateParam.setResponseMsg("PRC file created ::\t "+filePath);
		return true;
	}
private List<PRCTemplate> getTolTemplateExcel(FileValidationParam validateParam) {
		//ictxTemplateList = new ArrayList<>();
		//String TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
		String PRC_SHEET = "PRC";
		//InputStream inputStream;
		try {
			log.info("Excel data path localtion form getInputFilePath ::"+validateParam.getInputFilePath());
			FileInputStream is = new FileInputStream(validateParam.getInputFilePath());
			Workbook workbook = new XSSFWorkbook(is);
			log.info("Number of sheets : " + workbook.getNumberOfSheets());

			prcTemplateList = excelToTolList(workbook.getSheet(PRC_SHEET));

		} catch (Exception e) {
			validateParam.setResponseMsg("File not generated Please check input excel data");
			e.printStackTrace();
			return prcTemplateList = new ArrayList<>();
		}

		return prcTemplateList;
	}
private List<PRCTemplate> excelToTolList(Sheet sheet) {

  	 log.info("Inside ****************** excelToTolList()");
       try {
      	
         Iterator<Row> rows = sheet.iterator();
         prcTemplateList = new ArrayList<>();
         int rowNumber = 0;
         while (rows.hasNext()) {
           Row currentRow = rows.next();
           // skip header
           if (rowNumber == 0) {
             rowNumber++;
             continue;
           }
          // Iterator<Cell> cellsInRow = currentRow.iterator();
           PRCTemplate tolTemp = new PRCTemplate();
           
           tolTemp.setSequence(commonUtil.getStringFormatCell(currentRow.getCell(0,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
        	 tolTemp.setLicensePlate(commonUtil.getStringFormatCell(currentRow.getCell(1,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
      	 tolTemp.setTran(commonUtil.getStringFormatCell(currentRow.getCell(2,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
           	 tolTemp.setState(commonUtil.getStringFormatCell(currentRow.getCell(3,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
      	 tolTemp.setTranAmount(commonUtil.getStringFormatCell(currentRow.getCell(4,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
        	 tolTemp.setEntryTranDate(commonUtil.getStringFormatCell(currentRow.getCell(5,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
      	 tolTemp.setEntryPlaza(commonUtil.getStringFormatCell(currentRow.getCell(6,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
        	 tolTemp.setEntryLane(commonUtil.getStringFormatCell(currentRow.getCell(7,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
      	 tolTemp.setExitTranDate(commonUtil.getStringFormatCell(currentRow.getCell(8,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
      	 tolTemp.setExitPlaza(commonUtil.getStringFormatCell(currentRow.getCell(9,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
        	 tolTemp.setExitLane(commonUtil.getStringFormatCell(currentRow.getCell(10,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
        	 tolTemp.setAxleCount(commonUtil.getStringFormatCell(currentRow.getCell(11,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
        	 tolTemp.setLpType(commonUtil.getStringFormatCell(currentRow.getCell(12,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
      	 tolTemp.setWrTranFee(commonUtil.getStringFormatCell(currentRow.getCell(13,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
        	 tolTemp.setWrFeeType(commonUtil.getStringFormatCell(currentRow.getCell(14,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
        	 tolTemp.setPostAmt(commonUtil.getStringFormatCell(currentRow.getCell(15,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
          	 tolTemp.setResponseCode(commonUtil.getStringFormatCell(currentRow.getCell(16,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
          	 tolTemp.setNiopFee(commonUtil.getStringFormatCell(currentRow.getCell(17,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
          	 tolTemp.setOriginalFilename(commonUtil.getStringFormatCell(currentRow.getCell(18,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
        	 
				/*
				 * int cellIdx = 0; while (cellsInRow.hasNext()) { Cell currentCell =
				 * cellsInRow.next(); switch (cellIdx) { case 0:
				 * tolTemp.setSequence(commonUtil.getStringFormatCell(currentCell));
				 * System.out.println("case 0::"+tolTemp.getSequence()); break; case 1:
				 * tolTemp.setLicensePlate(commonUtil.getStringFormatCell(currentCell));
				 * System.out.println("case 1::"+tolTemp.getLicensePlate()); break; case 2:
				 * //ictxTemp.setEtcTrxSerialNo(commonUtil.getStringFormatCell(currentRow.
				 * getCell(1,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
				 * tolTemp.setTran(commonUtil.getStringFormatCell(currentCell));
				 * System.out.println("case 2::"+tolTemp.getTran());
				 * 
				 * break; case 3:
				 * //ictxTemp.setEtcTrxSerialNo(commonUtil.getStringFormatCell(currentRow.
				 * getCell(1,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
				 * tolTemp.setState(commonUtil.getStringFormatCell(currentCell));
				 * System.out.println("case 2::"+tolTemp.getState());
				 * 
				 * break; case 4:
				 * tolTemp.setTranAmount(commonUtil.getStringFormatCell(currentCell));
				 * System.out.println("case 4::"+tolTemp.getTranAmount()); break; case 5:
				 * tolTemp.setEntryTranDate(commonUtil.getStringFormatCell(currentCell));
				 * System.out.println("case 5::"+tolTemp.getEntryTranDate()); break; case 6:
				 * tolTemp.setEntryPlaza(commonUtil.getStringFormatCell(currentCell));
				 * System.out.println("case 6::"+tolTemp.getEntryPlaza()); break; case 7:
				 * tolTemp.setEntryLane(commonUtil.getStringFormatCell(currentCell));
				 * System.out.println("case 6::"+tolTemp.getEntryLane()); break; case 8:
				 * tolTemp.setExitTranDate(commonUtil.getStringFormatCell(currentCell));
				 * System.out.println("case 7::"+tolTemp.getExitTranDate()); break; case 9:
				 * tolTemp.setExitPlaza(commonUtil.getStringFormatCell(currentCell));
				 * System.out.println("case 8::"+tolTemp.getExitPlaza()); break; case 10:
				 * tolTemp.setExitLane(commonUtil.getStringFormatCell(currentCell));
				 * System.out.println("case 10::"+tolTemp.getExitLane()); break; case 11:
				 * tolTemp.setAxleCount(commonUtil.getStringFormatCell(currentCell));
				 * System.out.println("case 11::"+tolTemp.getAxleCount()); break;
				 * 
				 * case 12: tolTemp.setLpType(commonUtil.getStringFormatCell(currentCell));
				 * System.out.println("case 12::"+tolTemp.getLpType()); break; case 13:
				 * tolTemp.setWrTranFee(commonUtil.getStringFormatCell(currentCell));
				 * System.out.println("case 13::"+tolTemp.getWrTranFee()); break; case 14:
				 * tolTemp.setWrFeeType(commonUtil.getStringFormatCell(currentCell));
				 * System.out.println("case 14::"+tolTemp.getWrFeeType()); break; case 15:
				 * tolTemp.setPostAmt(commonUtil.getStringFormatCell(currentCell));
				 * System.out.println("case 15::"+tolTemp.getPostAmt()); break; case 16:
				 * tolTemp.setResponseCode(commonUtil.getStringFormatCell(currentCell));
				 * System.out.println("case 16::"+tolTemp.getResponseCode()); break; case 17:
				 * tolTemp.setNiopFee(commonUtil.getStringFormatCell(currentCell));
				 * System.out.println("case 17::"+tolTemp.getNiopFee()); break; case 18:
				 * tolTemp.setOriginalFilename(commonUtil.getStringFormatCell(currentCell));
				 * System.out.println("case 18::"+tolTemp.getOriginalFilename()); break;
				 * default: // System.out.println("Default:: ********************"); break; }
				 * cellIdx++;
				 * 
				 * }
				 */   prcTemplateList.add(tolTemp);
           System.out.println(tolTemp.toString());
         }
        
         if(prcTemplateList != null && prcTemplateList.size()>0) {
       	  System.out.println("tolTemplateList ::"+prcTemplateList);
         log.info("@@@@ TOL input data  loaded sucessfully:: ******************** ::"+prcTemplateList.size());
         }else {
      	   throw new IopTranslatorException("TOL input data not loaded");
         }
         
       }catch (Exception e) {
      	log.error("Exception:: ******************** TOL Sheet");
			e.printStackTrace();
		}
     
		return prcTemplateList;
	
}
private String generateTolHeader(FileValidationParam validateParam,List<PRCTemplate> tolTempList, String fromAgnecy, String toAgency) {

	fileCreateDateandTime = getUTCDateandTime();
	
	// 0108_202503061430460800.ITAG
	// rcsr_2025-03-06T14:48:16-08:00.tag
	// validateParam.getFromAgency() +"_"+
	// fileCreateDateandTime.replaceAll("[-T:Z]",
	// "")+IAGConstants.ITAG_FILE_EXTENSION;
	fileCreateDateandTime = validateParam.getFileDate()
			+ fileCreateDateandTime.substring(fileCreateDateandTime.indexOf("T"), fileCreateDateandTime.length());
	String fileCreateDateandTimeCurrent = getUTCDateandTime();
	filename = fromAgnecy + toAgency + "_" + fileCreateDateandTime.replaceAll("[-:]", "").substring(0, 15) + 
			"_"+tolTempList.get(0).getOriginalFilename().substring(0, 20)+".prc";
		//	"_"+toAgency +fromAgnecy+"_"+fileCreateDateandTimeCurrent.replaceAll("[-:]", "").substring(0, 15)+".prc";

	//filename = fromAgnecy + toAgency + "_" + fileCreateDateandTime.replaceAll("[-:]", "").substring(0, 15) + ".prc";

	StringBuilder tagHeader = new StringBuilder();
	System.out.println("filename:::" + filename);

	tagHeader.append("#HEADER,");
	tagHeader.append("PLATERECON,");
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
private void writeDetails(FileValidationParam validateParam, String header,List<PRCTemplate> trcTempList,String shortFromAgency,String shortToAgency)
		throws IOException {
	long start = System.currentTimeMillis();
	FileWriter writer;
	try {
		String filePath = validateParam.getOutputFilePath() + File.separator + filename;
		writer = new FileWriter(filePath, true);
		writer.write(header);
		writer.write(System.lineSeparator());
		System.out.print("Writing record raw... ");
		
		for (PRCTemplate trcTemplate : trcTempList) {
			writer.write(setTolDetailValues(trcTemplate,validateParam,detailAmt));
			writer.write(System.lineSeparator());
		}
		
		String trailer = generateTolTrailer(validateParam, shortFromAgency, shortToAgency,detailAmt,trcTempList);
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
private String setTolDetailValues(PRCTemplate trcTemplate,FileValidationParam validateParam,double detailAmt) {

	StringBuilder tolDetail = new StringBuilder();
	System.out.println("detailAmt Start:::::::"+detailAmt);
	tolDetail.append(CommonUtil.formatStringLeftPad(trcTemplate.getLicensePlate(),10,' ')+','); //License Plate
	tolDetail.append(CommonUtil.formatStringLeftPad(trcTemplate.getTran().trim(),10,'0')+',');//TRAN
	tolDetail.append(CommonUtil.formatStringLeftPad(trcTemplate.getState(),2,' ')+',');//State
	tolDetail.append(CommonUtil.formatStringLeftPad(trcTemplate.getTranAmount(),8,' ')+',');//TRAN amount
	if(trcTemplate.getEntryTranDate().equalsIgnoreCase("null") || trcTemplate.getEntryTranDate().isEmpty() || trcTemplate.getEntryTranDate().isBlank())
		tolDetail.append(CommonUtil.formatStringLeftPad("",25,' ')+',');//ENTRY TRAN DATE
	else tolDetail.append(CommonUtil.formatStringLeftPad(trcTemplate.getEntryTranDate(),25,' ')+',');//ENTRY TRAN DATE
	if(trcTemplate.getEntryPlaza().equalsIgnoreCase("null") || trcTemplate.getEntryPlaza().isEmpty() || trcTemplate.getEntryPlaza().isBlank())
		 tolDetail.append(CommonUtil.formatStringLeftPad("",22,' ')+',');//ENTRY PLAZA
	else tolDetail.append(CommonUtil.formatStringLeftPad(trcTemplate.getEntryPlaza(),22,' ')+',');//ENTRY PLAZA
	if(trcTemplate.getEntryLane().equalsIgnoreCase("null") || trcTemplate.getEntryLane().isEmpty() || trcTemplate.getEntryLane().isBlank())
		tolDetail.append(CommonUtil.formatStringLeftPad("",2,'0')+',');//ENTRY LANE
	else tolDetail.append(CommonUtil.formatStringLeftPad(trcTemplate.getEntryLane(),2,' ')+',');//ENTRY LANE
	tolDetail.append(CommonUtil.formatStringLeftPad(trcTemplate.getExitTranDate(),25,' ')+',');//EXIT TRAN DATE
	tolDetail.append(CommonUtil.formatStringLeftPad(trcTemplate.getExitPlaza(),22,' ')+',');//EXIT PLAZA
	tolDetail.append(CommonUtil.formatStringLeftPad(trcTemplate.getExitLane(),2,'0')+',');//EXIT LANE
	tolDetail.append(CommonUtil.formatStringLeftPad(trcTemplate.getAxleCount(),2,'0')+',');//AXLE COUNT
	if (trcTemplate.getLpType().equalsIgnoreCase("null") || trcTemplate.getLpType().isEmpty() || trcTemplate.getLpType().isBlank())
		tolDetail.append(CommonUtil.formatStringLeftPad("",30,' ')+',');//Lp TYpe
	else
		tolDetail.append(CommonUtil.formatStringLeftPad(trcTemplate.getLpType(),30,' ')+',');//Lp TYpe
	tolDetail.append(CommonUtil.formatStringLeftPad(trcTemplate.getWrTranFee(),8,' ')+',');//WR TRAN FEE
	tolDetail.append(CommonUtil.formatStringLeftPad(trcTemplate.getWrFeeType(),1,'0')+',');//WR FEE TYPE

	tolDetail.append(CommonUtil.formatStringLeftPad(trcTemplate.getPostAmt(),8,'0')+',');//POST AMT
	if(trcTemplate.getResponseCode().trim().equalsIgnoreCase("A")) {
		double acceptedToll=Double.parseDouble(trcTemplate.getTranAmount());
		this.acceptedAmt = acceptedAmt+acceptedToll;
		acceptedCount++;
	}
	tolDetail.append(CommonUtil.formatStringLeftPad(trcTemplate.getResponseCode(),1,' ')+',');//RESPONSE CODE
	tolDetail.append(CommonUtil.formatStringLeftPad(trcTemplate.getNiopFee(),8,'0'));//NIOP FEE
	
	
	double toll=Double.parseDouble(trcTemplate.getTranAmount());
	this.detailAmt = detailAmt+toll;
	
	System.out.println("detailAmt last:::::::"+detailAmt);
	return tolDetail.toString();
}
private String generateTolTrailer(FileValidationParam validateParam, String fromAgnecy, String toAgency,double detailAmt,List<PRCTemplate> tolTempList) {
	
	StringBuilder tagTrailer = new StringBuilder();
	tagTrailer.append("#TRAILER,");
	tagTrailer.append(CommonUtil.formatStringLeftPad(String.valueOf(tolTempList.get(0).getSequence()),6,'0')+',');
	tagTrailer.append(validateParam.getFileDate().replaceAll("-", "/") + ',');
	tagTrailer.append(CommonUtil.formatStringLeftPad(String.valueOf(tolTempList.size()), 6, '0')+',' );
	DecimalFormat df = new DecimalFormat("#.00");
	//df.format(detailAmt);
	tagTrailer.append(CommonUtil.formatStringLeftPad(String.valueOf(df.format(detailAmt)), 10, '0')+',');
	tagTrailer.append(CommonUtil.formatStringLeftPad(String.valueOf(acceptedCount), 6, '0')+','); //Accepted Count
	tagTrailer.append(CommonUtil.formatStringLeftPad(String.valueOf(acceptedAmt), 10, '0')); //Accepted Amt
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
