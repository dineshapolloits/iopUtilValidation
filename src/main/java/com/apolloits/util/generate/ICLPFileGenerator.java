package com.apolloits.util.generate;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import org.apache.commons.lang3.RandomStringUtils;
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

@Component
@Slf4j
public class ICLPFileGenerator {

	private String filename = "";
	private String fileCreateDateandTime = "";
	long tagSequenceStart = 0;
	int tagSequenceEnd = 0;
	AgencyEntity agency;
	private int detailCount = 0;
	
	@Autowired
	AgencyDataExcelReader agDataExcelRead;
	
	
	public boolean iclpGen(FileValidationParam validateParam) {

		if (!validateParameter(validateParam)) {
			return false;
		}
		String Header = getICLPHeader(validateParam);
		log.info("ITAG file name ::"+filename);
		log.info("ITAG Header :: " + Header);

		FileWriter writer;
		try {
			String filePath = validateParam.getOutputFilePath() + File.separator + filename;
			writer = new FileWriter(filePath, true);
			System.out.print("Writing record raw... ");
			writeDetails(validateParam, Header, writer);
			
			String zipFilename = moveToZipFile(filePath,validateParam);
			log.info("ITAG Zip file name :: "+zipFilename);
			validateParam.setResponseMsg("ICLP file created :: \t "+zipFilename);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			validateParam.setResponseMsg("ITAG file creation issue. Please check logs");
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
		/*	for (int count = 1; count <= validateParam.getRecordCount(); count++) {
				writer.write(getICLPDetailRecord(validateParam));
				writer.write(System.lineSeparator());
			}*/
				
				for (Map.Entry<String, AgencyEntity> entry : ValidationController.cscIdTagAgencyMap.entrySet()) {
					AgencyEntity agEntity = entry.getValue();
					System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());
					long tagRangeCount = (Integer.parseInt(agEntity.getTagSequenceEnd()) - Integer.parseInt(agEntity.getTagSequenceStart()));
					long validCount = (tagRangeCount * AgencyDataExcelReader.tagValid) /100; 
					long lowbalCount = (tagRangeCount * AgencyDataExcelReader.tagLowBal) /100; 
					long invalidCount = (tagRangeCount * AgencyDataExcelReader.tagInvalid) /100; 
					System.out.println("tagRangeCount ## "+tagRangeCount);
					System.out.println("Tag Valid count ## "+validCount +"\t lowbalCount ## "+lowbalCount +"\t invalidCount ## "+invalidCount);
					this.tagSequenceStart = Integer.parseInt(agEntity.getTagSequenceStart());
					for (long count = 1; count <= validCount; count++) {
						writer.write(getICLPDetailRecord(validateParam,agEntity));
						writer.write(System.lineSeparator());
					}
					for (long count = 1; count <= lowbalCount; count++) {
						writer.write(getICLPDetailRecord(validateParam,agEntity));
						writer.write(System.lineSeparator());
					}
					/*for (int count = 1; count <= invalidCount; count++) {
						writer.write(getICLPDetailRecord(validateParam));
						writer.write(System.lineSeparator());
					}*/
				}
				
			// writer.flush(); // close() should take care of this
			writer.flush();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		resetGlobalValue();
		long end = System.currentTimeMillis();
		System.out.println((end - start) / 1000f + " seconds");
	}
	
	private String getICLPDetailRecord(FileValidationParam validateParam,AgencyEntity agEntity) {
		StringBuilder iclpDetail = new StringBuilder();
		iclpDetail.append("FL");
		iclpDetail.append(CommonUtil.formatStringRightPad(getPlateRandomNo(6),10,' ')); //Lic No
		iclpDetail.append(CommonUtil.formatStringRightPad("",30,'*')); //Lic_Type
		iclpDetail.append(agEntity.getTagAgencyID()); //TAG_agency_ID
		iclpDetail.append(CommonUtil.formatStringLeftPad(String.valueOf(tagSequenceStart),10,'0')); // TAG_SERIAL_NUMBER
		iclpDetail.append(getUTCDateandTime()); //LIC_EFFECTIVE_FROM
		iclpDetail.append(CommonUtil.formatStringLeftPad("",20,'*')); //LIC_EFFECTIVE_TO
		iclpDetail.append(validateParam.getFromAgency()); //LIC_HOME_AGENCY
		iclpDetail.append(CommonUtil.formatStringLeftPad("",50,'*'));//LIC_ACCOUNT_NO
		iclpDetail.append(CommonUtil.formatStringLeftPad("",17,'*')); //LIC_VIN
		iclpDetail.append("*"); //LIC_GUARANTEED
		iclpDetail.append(CommonUtil.formatStringLeftPad("",20,'*')); //LIC_REGISTRATION_DATE
		iclpDetail.append(CommonUtil.formatStringLeftPad("",20,'*')); //LIC_UPDATE_DATE
		tagSequenceStart++;
		return iclpDetail.toString();
	}
	
	private String moveToZipFile(String filePath,FileValidationParam validateParam) {
		File file = new File(filePath);
		ZipParameters parameters = new ZipParameters();
		parameters.setCompressionMethod(CompressionMethod.DEFLATE);
		parameters.setCompressionLevel(CompressionLevel.ULTRA);

		String zipFileName = file.getName().replace(".", "_") + ".ZIP";
		String zipFilePath = file.getParent() + "/" + zipFileName;

		ZipFile zipFile = new ZipFile(zipFilePath);
		try {
			zipFile.addFile(file, parameters);
		} catch (ZipException e) {
			validateParam.setResponseMsg("Exception in ZIP file creation");
			log.error("Exception in ZIP file creation");
			e.printStackTrace();
		}
		file.delete();
		return zipFileName;
		
	}
	
	private boolean validateParameter(FileValidationParam validateParam) {
		//Validate file location
		File isfolder = new File(validateParam.getOutputFilePath());

		  if(!isfolder.exists()) {
			  log.error("Folder not persent. Please check your path");
			  validateParam.setResponseMsg("Folder not persent. Please check your path");
			  return false;
		  }
		  Path path = Path.of(validateParam.getOutputFilePath());
		  if(!Files.isWritable(path)) {
			  log.error("Not able to create file. Please check folder Permisison");
			  validateParam.setResponseMsg("Not able to create file. Please check folder Permisison");
			  return false;
		  }
		  
	/*	//validate tag start and end tag range based on record count
			try {
				agency = ValidationController.cscIdTagAgencyMap.get(validateParam.getFromAgency());
				long tagSerialNoStart = Long.valueOf(agency.getTagSequenceStart());
				long tagSerialNoEnd = Long.valueOf(agency.getTagSequenceEnd());
				long totalTagCount = tagSerialNoEnd - tagSerialNoStart;
				log.info("## tagSerialNoStart :: " + tagSerialNoStart + "\t tagSerialNoEnd ::" + tagSerialNoEnd);
				if (totalTagCount < validateParam.getRecordCount()) {
					log.error("## tagSerialNoStart :: " + tagSerialNoStart + "\t tagSerialNoEnd ::" + tagSerialNoEnd);
					log.error("This Agency ICLP file generation record count should be less than or equal  ::"
							+ totalTagCount + "\t  beacuse tag start ::" + agency.getTagSequenceStart()
							+ "\t End range :: " + agency.getTagSequenceEnd());
					validateParam.setResponseMsg(
							"This Agency ITAG file generation record count should be less than or equal  ::"
									+ totalTagCount + "\t  beacuse tag start ::" + agency.getTagSequenceStart()
									+ "\t End range :: " + agency.getTagSequenceEnd());
					return false;
				}
			} catch (Exception e) {
				e.printStackTrace();
				validateParam.setResponseMsg("Invalid tag rage validation failed. Please cehck log");
				return false;
			} */
		return true;
	}

private String getICLPHeader(FileValidationParam validateParam) {
		
		fileCreateDateandTime = getUTCDateandTime();
		log.info("fileCreateDateandTime ::"+fileCreateDateandTime);
		fileCreateDateandTime = validateParam.getFileDate()+fileCreateDateandTime.substring(fileCreateDateandTime.indexOf("T"),fileCreateDateandTime.length());
		log.info("After append fileCreateDateandTime ::"+fileCreateDateandTime);
		//Set file name to class variable
		filename =  validateParam.getFromAgency() +"_"+ fileCreateDateandTime.replaceAll("[-T:Z]", "")+IAGConstants.ICLP_FILE_EXTENSION;
		StringBuilder itagHeader = new StringBuilder();
		//Header count need to take based below all Map value tag range
		long recordcount = 0;
		for (Map.Entry<String, AgencyEntity> entry : ValidationController.cscIdTagAgencyMap.entrySet()) {
			AgencyEntity agEntity = entry.getValue();
			System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());
		/*	recordcount = recordcount
					+ (Integer.parseInt(agEntity.getTagSequenceEnd()) - Integer.parseInt(agEntity.getTagSequenceStart())) ;*/
			long tagRangeCount = (Integer.parseInt(agEntity.getTagSequenceEnd()) - Integer.parseInt(agEntity.getTagSequenceStart()));
			log.info("AgencyDataExcelReader.tagValid :: "+AgencyDataExcelReader.tagValid +"\t AgencyDataExcelReader.tagLowBal :: "+AgencyDataExcelReader.tagLowBal +"\t AgencyDataExcelReader.tagInvalid :: "+AgencyDataExcelReader.tagInvalid);
			long validCount = (tagRangeCount * AgencyDataExcelReader.tagValid) /100; 
			long lowbalCount = (tagRangeCount * AgencyDataExcelReader.tagLowBal) /100; 
			long invalidCount = (tagRangeCount * AgencyDataExcelReader.tagInvalid) /100; 
			System.out.println("tagRangeCount --- "+tagRangeCount);
			log.info("Tag Valid count --- "+validCount +"\t lowbalCount -- "+lowbalCount +"\t invalidCount -- "+invalidCount);
			recordcount = recordcount + validCount + lowbalCount;// +invalidCount;
		}
		log.info("Record count ::" +recordcount);
		validateParam.setRecordCount(recordcount);
		this.agency = ValidationController.cscIdTagAgencyMap.get(validateParam.getFromAgency());
		//this.tagSequenceStart = Integer.parseInt(agency.getTagSequenceStart());
		//this.tagSequenceEnd = Integer.parseInt(agency.getTagSequenceEnd());
		itagHeader.append(IAGConstants.ICLP_FILE_TYPE);
		itagHeader.append(CommonUtil.formatStringLeftPad(agency.getVersionNumber(),8,'0'));
		itagHeader.append(validateParam.getFromAgency());
		itagHeader.append(CommonUtil.formatStringLeftPad(fileCreateDateandTime,20,'0'));
		itagHeader.append(CommonUtil.formatStringLeftPad(String.valueOf(recordcount),10,'0'));
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
	
	public String getPlateRandomNo(int length) {
		//int length = 6;
		String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
		             + "0123456789";
			String plateNo = new Random().ints(length, 0, chars.length())
                    .mapToObj(i -> "" + chars.charAt(i))
                    .collect(Collectors.joining());
		return plateNo;
	
	}
	
	public static void main (String arg[]) {
		int perCount = 0;
		int totalCount = 12;
		int percentage =10;
		 perCount = (totalCount * percentage)/100;
		System.out.println("result ::"+perCount);
		
	}
}
