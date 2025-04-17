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
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.apolloits.util.IopTranslatorException;
import com.apolloits.util.modal.AgencyEntity;
import com.apolloits.util.modal.FileValidationParam;
import com.apolloits.util.modal.PBPTemplate;
import com.apolloits.util.utility.CommonUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PBPFileGenerator {

	@Autowired
	CommonUtil commonUtil;

	
	
	private String filename = "";
	private String fileCreateDateandTime = "";
	AgencyEntity agency;
	double detailAmt = 0.00;
	
	private List<PBPTemplate> pbpTemplateList;
public boolean pbpGenenerate(FileValidationParam validateParam) throws IOException {
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
		
		pbpTemplateList = getTolTemplateExcel(validateParam);
		String Header = generateTolHeader(validateParam,pbpTemplateList,shortFromAgency, shortToAgency);
		log.info("Tol Header :: " + Header);
		writeDetails(validateParam,Header,pbpTemplateList,shortFromAgency, shortToAgency);
		String filePath = validateParam.getOutputFilePath() + File.separator + filename;
		//String zipFilename = commonUtil.moveToZipFile(filePath,validateParam);
		File file = new File(filePath);
		if(file.length()==0) {
			validateParam.setResponseMsg("PBP file not created. Please check the input data ");
			file.delete();
		}else {
		log.info("PBP file name :: "+filePath);
		validateParam.setResponseMsg("PBP file created ::\t "+filePath);
		}
		return true;
	}
private List<PBPTemplate> getTolTemplateExcel(FileValidationParam validateParam) {
		//ictxTemplateList = new ArrayList<>();
		//String TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
		String PBP_SHEET = "pbp";
		//InputStream inputStream;

		try {
			log.info("Excel data path localtion form getInputFilePath ::"+validateParam.getInputFilePath());
			FileInputStream is = new FileInputStream(validateParam.getInputFilePath());
			Workbook workbook = new XSSFWorkbook(is);
			log.info("Number of sheets : " + workbook.getNumberOfSheets());

			pbpTemplateList = excelToTolList(workbook.getSheet(PBP_SHEET));

		} catch (Exception e) {
			validateParam.setResponseMsg("File not generated Please check input excel data");
			e.printStackTrace();
			return pbpTemplateList = new ArrayList<>();
		}

		return pbpTemplateList;
	}
private List<PBPTemplate> excelToTolList(Sheet sheet) {

  	 log.info("Inside ****************** excelToTolList()");
       try {
      	
         Iterator<Row> rows = sheet.iterator();
         pbpTemplateList = new ArrayList<>();
         int rowNumber = 0;
         while (rows.hasNext()) {
           Row currentRow = rows.next();
           // skip header
           if (rowNumber == 0) {
             rowNumber++;
             continue;
           }
         //  Iterator<Cell> cellsInRow = currentRow.iterator();
           PBPTemplate tolTemp = new PBPTemplate();
           
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
      	 tolTemp.setVehicleType(commonUtil.getStringFormatCell(currentRow.getCell(12,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
      	 tolTemp.setLpType(commonUtil.getStringFormatCell(currentRow.getCell(13,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
      	 tolTemp.setWrTranFee(commonUtil.getStringFormatCell(currentRow.getCell(14,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
      	 tolTemp.setWrFeeType(commonUtil.getStringFormatCell(currentRow.getCell(15,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
      	 tolTemp.setGuarantee(commonUtil.getStringFormatCell(currentRow.getCell(16,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
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
			 * case 12: tolTemp.setVehicleType(commonUtil.getStringFormatCell(currentCell));
			 * System.out.println("case 12::"+tolTemp.getVehicleType()); break; case 13:
			 * tolTemp.setLpType(commonUtil.getStringFormatCell(currentCell));
			 * System.out.println("case 13::"+tolTemp.getLpType()); break; case 14:
			 * tolTemp.setWrTranFee(commonUtil.getStringFormatCell(currentCell));
			 * System.out.println("case 14::"+tolTemp.getWrTranFee()); break; case 15:
			 * tolTemp.setWrFeeType(commonUtil.getStringFormatCell(currentCell));
			 * System.out.println("case 15::"+tolTemp.getWrFeeType()); break; case 16:
			 * tolTemp.setGuarantee(commonUtil.getStringFormatCell(currentCell));
			 * System.out.println("case 16::"+tolTemp.getGuarantee()); break; default: //
			 * System.out.println("Default:: ********************"); break; } cellIdx++;
			 * 
			 * }
			 */         
      	 	pbpTemplateList.add(tolTemp);
           System.out.println(tolTemp.toString());
         }
        
         if(pbpTemplateList != null && pbpTemplateList.size()>0) {
       	  System.out.println("tolTemplateList ::"+pbpTemplateList);
         log.info("@@@@ TOL input data  loaded sucessfully:: ******************** ::"+pbpTemplateList.size());
         }else {
      	   throw new IopTranslatorException("TOL input data not loaded");
         }
         
       }catch (Exception e) {
      	log.error("Exception:: ******************** TOL Sheet");
			e.printStackTrace();
		}
     
		return pbpTemplateList;
	
}
private String generateTolHeader(FileValidationParam validateParam,List<PBPTemplate> tolTempList, String fromAgnecy, String toAgency) {

	fileCreateDateandTime = getUTCDateandTime();
	//2025-04-10T13:31:08-07:00
	System.out.println("fileCreateDateandTime::::" + fileCreateDateandTime);
	
	fileCreateDateandTime = validateParam.getFileDate()
			+ fileCreateDateandTime.substring(fileCreateDateandTime.indexOf("T"), fileCreateDateandTime.length());
	filename = fromAgnecy + toAgency + "_" + fileCreateDateandTime.replaceAll("[-:]", "").substring(0, 15) + ".pbp";

	StringBuilder tagHeader = new StringBuilder();
	//atoc_20250410T133108.pbp
	System.out.println("filename:::" + filename);

	tagHeader.append("#HEADER,");
	tagHeader.append("PAYBYPLATE,");
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
private void writeDetails(FileValidationParam validateParam, String header,List<PBPTemplate> pbpTempList,String shortFromAgency,String shortToAgency)
		throws IOException {
	long start = System.currentTimeMillis();
	FileWriter writer;
	try {
		String filePath = validateParam.getOutputFilePath() + File.separator + filename;
		writer = new FileWriter(filePath, true);
		writer.write(header);
		writer.write(System.lineSeparator());
		System.out.print("Writing record raw... ");
		
		for (PBPTemplate pbpTemplate : pbpTempList) {
			writer.write(setTolDetailValues(pbpTemplate,validateParam,detailAmt));
			writer.write(System.lineSeparator());
		}
		
		String trailer = generateTolTrailer(validateParam, shortFromAgency, shortToAgency,detailAmt,pbpTempList);
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
private String setTolDetailValues(PBPTemplate tolTemplate,FileValidationParam validateParam,double detailAmt) {

	StringBuilder tolDetail = new StringBuilder();
	System.out.println("detailAmt Start:::::::"+detailAmt);
	tolDetail.append(CommonUtil.formatStringLeftPad(tolTemplate.getLicensePlate(),10,' ')+','); //License Plate
	tolDetail.append(CommonUtil.formatStringLeftPad(tolTemplate.getTran(),10,'0')+',');//TRAN
	tolDetail.append(CommonUtil.formatStringLeftPad(tolTemplate.getState(),2,' ')+',');//State
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
		tolDetail.append(CommonUtil.formatStringLeftPad("",2,'0')+',');//ENTRY LANE
	else tolDetail.append(CommonUtil.formatStringLeftPad(tolTemplate.getEntryLane(),2,'0')+',');//ENTRY LANE
	tolDetail.append(CommonUtil.formatStringLeftPad(tolTemplate.getExitTranDate(),25,' ')+',');//EXIT TRAN DATE
	tolDetail.append(CommonUtil.formatStringLeftPad(tolTemplate.getExitPlaza(),22,' ')+',');//EXIT PLAZA
	tolDetail.append(CommonUtil.formatStringLeftPad(tolTemplate.getExitLane(),2,'0')+',');//EXIT LANE
	tolDetail.append(CommonUtil.formatStringLeftPad(tolTemplate.getAxleCount(),2,'0')+',');//AXLE COUNT
	tolDetail.append(CommonUtil.formatStringLeftPad(tolTemplate.getVehicleType(),1,'0')+',');//VEHICLE TYPE
	if (tolTemplate.getLpType().equalsIgnoreCase("null") || tolTemplate.getLpType().isEmpty() || tolTemplate.getLpType().isBlank())
		tolDetail.append(CommonUtil.formatStringLeftPad("",30,' ')+',');//Lp TYpe
	else
		tolDetail.append(CommonUtil.formatStringLeftPad(tolTemplate.getLpType(),30,' ')+',');//Lp TYpe
	
	double WrTranFee = Double.parseDouble(tolTemplate.getWrTranFee());
	WrTranFee = WrTranFee/100;
	tolDetail.append(CommonUtil.formatStringLeftPad(String.format("%.2f", WrTranFee),8,'0')+',');//WR TRAN FEE
	
		//tolDetail.append(CommonUtil.formatStringLeftPad(tolTemplate.getWrTranFee(),8,'0')+',');//WR TRAN FEE
	tolDetail.append(CommonUtil.formatStringLeftPad(tolTemplate.getWrFeeType(),1,'0')+',');//WR FEE TYPE
	tolDetail.append(CommonUtil.formatStringLeftPad(tolTemplate.getGuarantee(),1,' '));//GUARANTEE
	double toll=Double.parseDouble(tolTemplate.getTranAmount());
	this.detailAmt = detailAmt+toll;
	
	System.out.println("detailAmt last:::::::"+detailAmt);
	return tolDetail.toString();
}
private String generateTolTrailer(FileValidationParam validateParam, String fromAgnecy, String toAgency,double detailAmt,List<PBPTemplate> tolTempList) {
	
	StringBuilder tagTrailer = new StringBuilder();
	tagTrailer.append("#TRAILER,");
	tagTrailer.append(CommonUtil.formatStringLeftPad(String.valueOf(tolTempList.get(0).getSequence()),6,'0')+',');
	tagTrailer.append(validateParam.getFileDate().replaceAll("-", "/") + ',');
	tagTrailer.append(CommonUtil.formatStringLeftPad(String.valueOf(tolTempList.size()), 6, '0')+',' );
	DecimalFormat df = new DecimalFormat("#.00");
	//df.format(detailAmt);
	double tranFee = detailAmt/100;//Double.parseDouble(detailAmt);
	tagTrailer.append(CommonUtil.formatStringLeftPad(String.format("%.2f", tranFee),10,'0'));
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
