package com.apolloits.util.generate;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.apolloits.util.IAGConstants;
import com.apolloits.util.controller.CtocController;
import com.apolloits.util.controller.ValidationController;
import com.apolloits.util.modal.AgencyEntity;
import com.apolloits.util.modal.FileValidationParam;
import com.apolloits.util.reader.AgencyDataExcelReader;
import com.apolloits.util.utility.CommonUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class TAGFileGenerator {
	private String filename = "";
	@Autowired
	CommonUtil commonUtil;
	private String fileCreateDateandTime = "";
	String tagSequenceStart = "0";
	long deciSeqStart = 0;
	int accountID = 546562; //Random number

	public boolean tagGenerate(FileValidationParam validateParam) {
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
	
	System.out.println("validateParam.getFromAgency()"+validateParam.getFromAgency()+""+validateParam.getToAgency());
		String shortFromAgency = ctocShortAgency.get(validateParam.getFromAgency());
		String shortToAgency = ctocShortAgency.get(validateParam.getToAgency());

		System.out.println("validateParam..." + validateParam.getFileSequence());

		if (!commonUtil.validateInfoFileGenParameter(validateParam)) {
			return false;
		}

		String Header = generateTAGHeader(validateParam,shortFromAgency,shortToAgency);
		log.info("ITAG file name ::" + filename);
		log.info("ITAG Header :: " + Header);
		String trailer = generateTAGTrailer(validateParam, shortFromAgency, shortToAgency);
		FileWriter writer;
		try {
			String filePath = validateParam.getOutputFilePath() + File.separator + filename;
			writer = new FileWriter(filePath, true);
			System.out.print("Writing record raw... ");
			writeDetails(validateParam, Header, writer,trailer);
			
			log.info("TAG  file name :: "+filePath);
			validateParam.setResponseMsg("TAG file created ::\t "+filePath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			validateParam.setResponseMsg("ITAG file creation issue. Please check logs");
			log.error("ITAG file creation issue. Please check logs");
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private String generateTAGHeader(FileValidationParam validateParam, String fromAgnecy, String toAgency) {

		fileCreateDateandTime = getUTCDateandTime();
		System.out.println("fileCreateDateandTime::::"+ fileCreateDateandTime);
		//0108_202503061430460800.ITAG
		//rcsr_2025-03-06T14:48:16-08:00.tag
			//	validateParam.getFromAgency() +"_"+ fileCreateDateandTime.replaceAll("[-T:Z]", "")+IAGConstants.ITAG_FILE_EXTENSION;
		fileCreateDateandTime = validateParam.getFileDate()+fileCreateDateandTime.substring(fileCreateDateandTime.indexOf("T"),fileCreateDateandTime.length());
		filename =  fromAgnecy+toAgency+"_"+fileCreateDateandTime.replaceAll("[-:]", "").substring(0,15)+".tag";
		String createUTCdate = CommonUtil.convertDatetoUTC(filename.substring(5,20));
		StringBuilder tagHeader = new StringBuilder();
		System.out.println("filename:::"+filename);
		
		tagHeader.append("#HEADER,");	
		tagHeader.append("TAGS,");
		tagHeader.append("INIT,");
		tagHeader.append(CommonUtil.formatStringLeftPad(validateParam.getFileSequence(),6,' ')+','); //SEQUENCE
		tagHeader.append(validateParam.getFileDate().replaceAll("-", "/")+','); //BUSINESS DAY
		tagHeader.append(fromAgnecy.toUpperCase()+','); //SOURCE
		tagHeader.append(toAgency.toUpperCase()+','); //DESTINATION
		tagHeader.append(CommonUtil.formatStringLeftPad(createUTCdate,25,' ')+','); // CREATE DATE
		tagHeader.append("REV A2.1.1"); //VERSION
		System.out.println("tagHeader.toString()"+tagHeader.toString());
		return tagHeader.toString();

	}
	private void writeDetails(FileValidationParam validateParam, String header, Writer writer,String trailer)
			throws IOException {
		
		long start = System.currentTimeMillis();
		try {
			writer.write(header);
			writer.write(System.lineSeparator());
			for (Map.Entry<String, AgencyEntity> entry : CtocController.cscIdTagAgencyMap.entrySet()) {
				AgencyEntity agEntity = entry.getValue();
				System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());
				
				 deciSeqStart = Long.parseLong(agEntity.getTagSequenceStart(),16);
				long deciSeqEnd =Long.parseLong(agEntity.getTagSequenceEnd(),16);
				long tagRangeCount = deciSeqEnd-deciSeqStart;
				
				long validTagTypeCount = (tagRangeCount * 90) /100; 
				long invalidTagTypeCount = (tagRangeCount * 5) /100; 
				long nonRevenueTagTypeCount = (tagRangeCount * 5) /100; 
				long diff =tagRangeCount- (validTagTypeCount+invalidTagTypeCount+nonRevenueTagTypeCount);
				
				
				this.tagSequenceStart = agEntity.getTagSequenceStart();
				//90% Loop
				for (long count = 1; count <= validTagTypeCount; count++) {
					writer.write(getTAGDetailRecord(validateParam,deciSeqStart,deciSeqEnd,"V","N","N","N","1"));
					writer.write(System.lineSeparator());
				}
				//5% Loop
				for (long count = 1; count <= invalidTagTypeCount; count++) {
					writer.write(getTAGDetailRecord(validateParam,deciSeqStart,deciSeqEnd,"I","H","C","N","0"));
					writer.write(System.lineSeparator());
				}
				//5% Loop
				for (long count = 1; count <= nonRevenueTagTypeCount; count++) {
					writer.write(getTAGDetailRecord(validateParam,deciSeqStart,deciSeqEnd,"N","H","M","V","0"));
					writer.write(System.lineSeparator());
				}
				//Diff Loop
				for (long count = 1; count <= diff; count++) {
					writer.write(getTAGDetailRecord(validateParam,deciSeqStart,deciSeqEnd,"V","N","N","N","1"));
					writer.write(System.lineSeparator());
				}
				
			}
			writer.write(trailer);
			writer.write("\n");
			writer.flush();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		resetGlobalValue();
		long end = System.currentTimeMillis();
		System.out.println((end - start) / 1000f + " seconds");
	}
		
	private String getTAGDetailRecord(FileValidationParam validateParam,long deciSeqStar,long deciSeqEnd,String tagType,String subTypeA,String subTypeB,String subTypeC, String protoType) {
		StringBuilder tagDetail = new StringBuilder();
		String hexValue= Long.toHexString(deciSeqStart).toUpperCase();
		tagDetail.append(CommonUtil.formatStringLeftPad(String.valueOf(hexValue),10,' ')+','); //TAG ID
		tagDetail.append(CommonUtil.formatStringLeftPad(String.valueOf(accountID),10,' ')+',');//ACCOUNT ID
		tagDetail.append("A,"); //ACTION CODE
		tagDetail.append(tagType+','); //TAG TYPE
		tagDetail.append(subTypeA+','); // SUBTYPE-A
		tagDetail.append(subTypeB+','); // SUBTYPE-B
		tagDetail.append(subTypeC+','); // SUBTYPE-C
		tagDetail.append(protoType); // PROTOCOL TYPE
		
		deciSeqStart++;
		accountID++;
		return tagDetail.toString();
	}
	
	private String generateTAGTrailer(FileValidationParam validateParam, String fromAgnecy, String toAgency) {
		long recordcount = 0;
		for (Map.Entry<String, AgencyEntity> entry : CtocController.cscIdTagAgencyMap.entrySet()) {
			AgencyEntity agEntity = entry.getValue();
			long deciSeqStart = Long.parseLong(agEntity.getTagSequenceStart(),16);
			long deciSeqEnd =Long.parseLong(agEntity.getTagSequenceEnd(),16);
			long tagRangeCount = deciSeqEnd-deciSeqStart;
			recordcount = tagRangeCount;
			
		}
		log.info("Record count ::" +recordcount);
		validateParam.setRecordCount(recordcount);
		StringBuilder tagTrailer = new StringBuilder();
		tagTrailer.append("#TRAILER,");
		tagTrailer.append(CommonUtil.formatStringLeftPad(validateParam.getFileSequence(),6,'0')+',');
		tagTrailer.append(validateParam.getFileDate().replaceAll("-", "/")+',');
		tagTrailer.append(CommonUtil.formatStringLeftPad(String.valueOf(recordcount),8,'0'));
		System.out.println("tagTrailer::::"+tagTrailer);
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
	private void resetGlobalValue() {
		filename = "";
		fileCreateDateandTime = "";
		tagSequenceStart = "0";
		 deciSeqStart = 0;
		//tagSequenceEnd = 0;
	}
}
