package com.apolloits.util.generate;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.apolloits.util.IAGConstants;
import com.apolloits.util.controller.ValidationController;
import com.apolloits.util.modal.AgencyEntity;
import com.apolloits.util.modal.FileValidationParam;
import com.apolloits.util.reader.AgencyDataExcelReader;
import com.apolloits.util.utility.CommonUtil;

import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.CompressionMethod;

@Slf4j
@Component
public class ITGUFileGenerator {

	private String filename = "";
	private String fileCreateDateandTime = "";
	long tagSequenceStart = 0;
	int tagSequenceEnd = 0;
	AgencyEntity agency;
	
	@Autowired
	CommonUtil commonUtil;
	
	public boolean itguGen(FileValidationParam validateParam) {
		
		if (!commonUtil.validateInfoFileGenParameter(validateParam)) {
			return false;
		}
		
		String Header = getITGUHeader(validateParam);
		log.info("ITAG file name ::"+filename);
		log.info("ITAG Header :: " + Header);
		
		if(Header.length() != 66 ) {
			return false;
		}
		FileWriter writer;
		try {
			String filePath = validateParam.getOutputFilePath() + File.separator + filename;
			writer = new FileWriter(filePath, true);
			System.out.print("Writing record raw... ");
			writeDetails(validateParam, Header, writer);
			
			String zipFilename = commonUtil.moveToZipFile(filePath,validateParam);
			log.info("ITAG Zip file name :: "+zipFilename);
			validateParam.setResponseMsg("ITGU file created ::\t "+zipFilename);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			validateParam.setResponseMsg("ITGU file creation issue. Please check logs");
			log.error("ITAG file creation issue. Please check logs");
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	private void writeDetails(FileValidationParam validateParam, String header, Writer writer)
			throws IOException {
		long start = System.currentTimeMillis();
		try {
			writer.write(header);
			writer.write(System.lineSeparator());
		
			for (Map.Entry<String, AgencyEntity> entry : ValidationController.cscIdTagAgencyMap.entrySet()) {
				AgencyEntity agEntity = entry.getValue();
				System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());
				long tagRangeCount = (Integer.parseInt(agEntity.getTagSequenceEnd()) - Integer.parseInt(agEntity.getTagSequenceStart()));
				log.info("AgencyDataExcelReader.tagValid --- " + AgencyDataExcelReader.tagValid
						+ "\t AgencyDataExcelReader.tagLowBal -- " + AgencyDataExcelReader.tagLowBal
						+ "\t AgencyDataExcelReader.tagZeroNegativeBal -- " + AgencyDataExcelReader.tagZeroNegativeBal
						+ "\t AgencyDataExcelReader.tagInvalidBal -- " + AgencyDataExcelReader.tagInvalidBal);
				long validCount = (tagRangeCount * (AgencyDataExcelReader.tagValid - AgencyDataExcelReader.tagInvalidBal)) /100; 
				long lowbalCount = (tagRangeCount * AgencyDataExcelReader.tagLowBal) /100; 
				long zeroNegativeCount = (tagRangeCount * AgencyDataExcelReader.tagZeroNegativeBal) /100; 
				long invaidCount = (tagRangeCount * AgencyDataExcelReader.tagInvalidBal) / 100;
				log.info("tagRangeCount ## "+tagRangeCount);
				log.info("Tag Valid count ## " + validCount + "\t lowbalCount ## " + lowbalCount
						+ "\t zeroNegativeCount ## " + zeroNegativeCount +"\t invaidCount ## "+invaidCount);
				this.tagSequenceStart = Integer.parseInt(agEntity.getTagSequenceStart());
				log.info("agEntity.getTagAgencyID() ::"+agEntity.getTagAgencyID() +"\t length ::"+agEntity.getTagAgencyID().trim().length());
				for (long count = 1; count <= validCount; count++) {
					writer.write(getITGUDetailRecord(validateParam,"1",agEntity.getTagAgencyID().trim()));
					writer.write(System.lineSeparator());
				}
				for (long count = 1; count <= lowbalCount; count++) {
					writer.write(getITGUDetailRecord(validateParam,"2",agEntity.getTagAgencyID().trim()));
					writer.write(System.lineSeparator());
				}
				for (long count = 1; count <= zeroNegativeCount; count++) {
					writer.write(getITGUDetailRecord(validateParam,"3",agEntity.getTagAgencyID().trim()));
					writer.write(System.lineSeparator());
				}
				for (long count = 1; count <= invaidCount; count++) {
					writer.write(getITGUDetailRecord(validateParam,"4",agEntity.getTagAgencyID().trim()));
					writer.write(System.lineSeparator());
				}
			}
			
			// writer.flush(); // close() should take care of this
			writer.flush();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		resetGlobalValue();
		long end = System.currentTimeMillis();
		log.info("File Writing Over all time ::"+(end - start) / 1000f + " seconds");
	}
	
	private String getITGUDetailRecord(FileValidationParam validateParam,String tagStatus,String tagAgencyId) {
		StringBuilder itagDetail = new StringBuilder();
		itagDetail.append(CommonUtil.formatStringLeftPad(tagAgencyId,4,'0')); // Tag_Agnecny_ID
		itagDetail.append(CommonUtil.formatStringLeftPad(String.valueOf(tagSequenceStart),10,'0'));
		itagDetail.append(tagStatus); //TAG_STATUS
		itagDetail.append(CommonUtil.formatStringLeftPad("",6,'0')); //TAG_ACCT_INFO
		itagDetail.append(validateParam.getFromAgency()); //TAG_HOME_AGENCY
		itagDetail.append("*"); //TAG_AC_TYPE_IND
		itagDetail.append(CommonUtil.formatStringLeftPad("",50,'*'));//TAG_ACCOUNT_NO
		itagDetail.append(CommonUtil.formatStringLeftPad("",3,'*')); //TAG_PROTOCOL
		itagDetail.append("T"); //TAG_TYPE
		itagDetail.append("*"); //TAG_MOUNT
		itagDetail.append("****"); //TAG_CLASS
		tagSequenceStart++;
		return itagDetail.toString();
	}
	
	private String getITGUHeader(FileValidationParam validateParam) {

		fileCreateDateandTime = getUTCDateandTime();
		log.info("fileCreateDateandTime ::" + fileCreateDateandTime);
		fileCreateDateandTime = validateParam.getFileDate()
				+ fileCreateDateandTime.substring(fileCreateDateandTime.indexOf("T"), fileCreateDateandTime.length());
		log.info("After append fileCreateDateandTime ::" + fileCreateDateandTime);
		// Set file name to class variable
		filename = validateParam.getFromAgency() + "_" + fileCreateDateandTime.replaceAll("[-T:Z]", "")
				+ IAGConstants.ITGU_FILE_EXTENSION;
		StringBuilder itagHeader = new StringBuilder();
		this.agency = ValidationController.cscIdTagAgencyMap.get(validateParam.getFromAgency());
		if(this.agency == null) {
			validateParam.setResponseMsg("Please check agency configuation");
			return "invalidAgency";
		}
		
		long recordcount = 0;
		log.info("AgencyDataExcelReader.tagValid :: "
				+ (AgencyDataExcelReader.tagValid - AgencyDataExcelReader.tagInvalidBal)
				+ "\t AgencyDataExcelReader.tagLowBal :: " + AgencyDataExcelReader.tagLowBal
				+ "\t AgencyDataExcelReader.tagZeroNegativeBal :: " + AgencyDataExcelReader.tagZeroNegativeBal
				+ "\t AgencyDataExcelReader.tagInvalidBal :: " + AgencyDataExcelReader.tagInvalidBal);
		for (Map.Entry<String, AgencyEntity> entry : ValidationController.cscIdTagAgencyMap.entrySet()) {
			AgencyEntity agEntity = entry.getValue();
			System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());
			/*
			 * recordcount = recordcount + (Integer.parseInt(agEntity.getTagSequenceEnd()) -
			 * Integer.parseInt(agEntity.getTagSequenceStart())) ;
			 */
			long tagRangeCount = (Integer.parseInt(agEntity.getTagSequenceEnd())
					- Integer.parseInt(agEntity.getTagSequenceStart()));
			long validCount = (tagRangeCount * (AgencyDataExcelReader.tagValid - AgencyDataExcelReader.tagInvalidBal))
					/ 100;
			long lowbalCount = (tagRangeCount * AgencyDataExcelReader.tagLowBal) / 100;
			long zeroNegativeCount = (tagRangeCount * AgencyDataExcelReader.tagZeroNegativeBal) / 100;
			long invaidCount = (tagRangeCount * AgencyDataExcelReader.tagInvalidBal) / 100;
			log.info("tagRangeCount --- " + tagRangeCount);
			log.info("Tag Valid count :: " + validCount + "\t lowbalCount :: " + lowbalCount
					+ "\t zeroNegativeCount :: " + zeroNegativeCount + "\t invaidCount ::" + invaidCount);
			recordcount = recordcount + validCount + lowbalCount + zeroNegativeCount + invaidCount;
		}
		log.info("Record count ::" + recordcount);
		validateParam.setRecordCount(recordcount);

		itagHeader.append(IAGConstants.ITGU_FILE_TYPE);
		itagHeader.append(CommonUtil.formatStringLeftPad(agency.getVersionNumber(), 8, '0'));
		itagHeader.append(validateParam.getFromAgency());
		itagHeader.append(CommonUtil.formatStringLeftPad(fileCreateDateandTime, 20, '0'));
		itagHeader.append(CommonUtil.formatStringLeftPad(getPreviousDateUTCFormat(fileCreateDateandTime), 20, '0')); //PREV_FILE_DATE_TIME
		itagHeader.append(CommonUtil.formatStringLeftPad(String.valueOf(recordcount), 10, '0'));
		return itagHeader.toString();
	}

	private String getUTCDateandTime() {
		try {
			DateTime dateTime = new DateTime().withZone(DateTimeZone.UTC);
			return dateTime.toString("yyyy-MM-dd'T'HH:mm:ss'Z'");
		} catch (DateTimeParseException e) {
			e.printStackTrace();
			return null;
		}
	}

	private void resetGlobalValue() {
		filename = "";
		fileCreateDateandTime = "";
		tagSequenceStart = 0;
		tagSequenceEnd = 0;
	}
	
	private String getPreviousDateUTCFormat(String date) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX");
		try {
		// Parse the current date string
		LocalDateTime currentDate = LocalDateTime.parse(date, formatter);
		// Subtract one day to get the previous date
		LocalDateTime previousDate = currentDate.minusDays(1);
		System.out.println("Previous: " + previousDate.toString());
		return previousDate.toString()+"Z";
		}catch (Exception e) {
			log.error("Exception in getPreviousDateUTCFormat ::"+date);
			e.printStackTrace();
			return fileCreateDateandTime;
		}
		
	}

}
