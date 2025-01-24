package com.apolloits.util.generate;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeParseException;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
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
public class ITAGFileGenerator {
	
	private String filename = "";
	private String fileCreateDateandTime = "";
	long tagSequenceStart = 0;
	int tagSequenceEnd = 0;
	AgencyEntity agency;
	public boolean itagGen(FileValidationParam validateParam) {

		if (!validateParameter(validateParam)) {
			return false;
		}
		String Header = getITAGHeader(validateParam);
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
			validateParam.setResponseMsg("ITAG file created ::\t "+zipFilename);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			validateParam.setResponseMsg("ITAG file creation issue. Please check logs");
			log.error("ITAG file creation issue. Please check logs");
			e.printStackTrace();
			return false;
		}

		return true;
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

	private void writeDetails(FileValidationParam validateParam, String header, Writer writer)
			throws IOException {
		long start = System.currentTimeMillis();
		try {
			writer.write(header);
			writer.write(System.lineSeparator());
		/*	for (int count = 1; count <= validateParam.getRecordCount(); count++) {
				writer.write(getITAGDetailRecord(validateParam));
				writer.write(System.lineSeparator());
			}*/
			
			for (Map.Entry<String, AgencyEntity> entry : ValidationController.cscIdTagAgencyMap.entrySet()) {
				AgencyEntity agEntity = entry.getValue();
				System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());
				long tagRangeCount = (Integer.parseInt(agEntity.getTagSequenceEnd()) - Integer.parseInt(agEntity.getTagSequenceStart()));
				log.info("AgencyDataExcelReader.tagValid --- "+AgencyDataExcelReader.tagValid +"\t AgencyDataExcelReader.tagLowBal -- "+AgencyDataExcelReader.tagLowBal +"\t AgencyDataExcelReader.tagInvalid -- "+AgencyDataExcelReader.tagInvalid);
				long validCount = (tagRangeCount * AgencyDataExcelReader.tagValid) /100; 
				long lowbalCount = (tagRangeCount * AgencyDataExcelReader.tagLowBal) /100; 
				long invalidCount = (tagRangeCount * AgencyDataExcelReader.tagInvalid) /100; 
				log.info("tagRangeCount ## "+tagRangeCount);
				log.info("Tag Valid count ## "+validCount +"\t lowbalCount ## "+lowbalCount +"\t invalidCount ## "+invalidCount);
				this.tagSequenceStart = Integer.parseInt(agEntity.getTagSequenceStart());
				log.info("agEntity.getTagAgencyID() ::"+agEntity.getTagAgencyID() +"\t length ::"+agEntity.getTagAgencyID().trim().length());
				for (long count = 1; count <= validCount; count++) {
					writer.write(getITAGDetailRecord(validateParam,"1",agEntity.getTagAgencyID().trim()));
					writer.write(System.lineSeparator());
				}
				for (long count = 1; count <= lowbalCount; count++) {
					writer.write(getITAGDetailRecord(validateParam,"2",agEntity.getTagAgencyID().trim()));
					writer.write(System.lineSeparator());
				}
				for (long count = 1; count <= invalidCount; count++) {
					writer.write(getITAGDetailRecord(validateParam,"3",agEntity.getTagAgencyID().trim()));
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
		System.out.println((end - start) / 1000f + " seconds");
	}


	private String getITAGDetailRecord(FileValidationParam validateParam,String tagStatus,String tagAgencyId) {
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
		 //validate tag start and end tag range based on record count
		/*	try {
				agency = ValidationController.cscIdTagAgencyMap.get(validateParam.getFromAgency());
				long tagSerialNoStart = Long.valueOf(agency.getTagSequenceStart());
				long tagSerialNoEnd = Long.valueOf(agency.getTagSequenceEnd());
				long totalTagCount = tagSerialNoEnd - tagSerialNoStart;
				log.info("## tagSerialNoStart :: " + tagSerialNoStart + "\t tagSerialNoEnd ::" + tagSerialNoEnd);
				if (totalTagCount < validateParam.getRecordCount()) {
					log.error("## tagSerialNoStart :: " + tagSerialNoStart + "\t tagSerialNoEnd ::" + tagSerialNoEnd);
					log.error("This Agency ITAG file generation record count should be less than or equal  ::"
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

	private String getITAGHeader(FileValidationParam validateParam) {
		
		fileCreateDateandTime = getUTCDateandTime();
		log.info("fileCreateDateandTime ::"+fileCreateDateandTime);
		fileCreateDateandTime = validateParam.getFileDate()+fileCreateDateandTime.substring(fileCreateDateandTime.indexOf("T"),fileCreateDateandTime.length());
		log.info("After append fileCreateDateandTime ::"+fileCreateDateandTime);
		//Set file name to class variable
		filename =  validateParam.getFromAgency() +"_"+ fileCreateDateandTime.replaceAll("[-T:Z]", "")+IAGConstants.ITAG_FILE_EXTENSION;
		StringBuilder itagHeader = new StringBuilder();
		this.agency = ValidationController.cscIdTagAgencyMap.get(validateParam.getFromAgency());
		//this.tagSequenceStart = Integer.parseInt(agency.getTagSequenceStart());
		//this.tagSequenceEnd = Integer.parseInt(agency.getTagSequenceEnd());
		
		long recordcount = 0;
		log.info("AgencyDataExcelReader.tagValid :: "+AgencyDataExcelReader.tagValid +"\t AgencyDataExcelReader.tagLowBal :: "+AgencyDataExcelReader.tagLowBal +"\t AgencyDataExcelReader.tagInvalid :: "+AgencyDataExcelReader.tagInvalid);
		for (Map.Entry<String, AgencyEntity> entry : ValidationController.cscIdTagAgencyMap.entrySet()) {
			AgencyEntity agEntity = entry.getValue();
			System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());
		/*	recordcount = recordcount
					+ (Integer.parseInt(agEntity.getTagSequenceEnd()) - Integer.parseInt(agEntity.getTagSequenceStart())) ;*/
			long tagRangeCount = (Integer.parseInt(agEntity.getTagSequenceEnd()) - Integer.parseInt(agEntity.getTagSequenceStart()));
			long validCount = (tagRangeCount * AgencyDataExcelReader.tagValid) /100; 
			long lowbalCount = (tagRangeCount * AgencyDataExcelReader.tagLowBal) /100; 
			long invalidCount = (tagRangeCount * AgencyDataExcelReader.tagInvalid) /100; 
			log.info("tagRangeCount --- "+tagRangeCount);
			log.info("Tag Valid count :: "+validCount +"\t lowbalCount :: "+lowbalCount +"\t invalidCount :: "+invalidCount);
			recordcount = recordcount + validCount + lowbalCount +invalidCount;
		}
		log.info("Record count ::" +recordcount);
		validateParam.setRecordCount(recordcount);
		
		itagHeader.append(IAGConstants.ITAG_FILE_TYPE);
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
}
