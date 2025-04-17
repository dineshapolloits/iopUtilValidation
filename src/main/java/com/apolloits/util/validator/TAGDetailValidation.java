package com.apolloits.util.validator;

import static com.apolloits.util.IAGConstants.FILE_RECORD_TYPE;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.apolloits.util.CtocAckFileMapper;
import com.apolloits.util.IAGConstants;
import com.apolloits.util.controller.CtocController;
import com.apolloits.util.modal.ErrorMsgDetail;
import com.apolloits.util.modal.FileValidationParam;
import com.apolloits.util.utility.CommonUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class TAGDetailValidation {
	@Autowired
	private CtocAckFileMapper ctocAckMapper;
	@Autowired
	@Lazy
	CtocController controller;
	@Autowired
	CommonUtil commonUtil;
	int invalidRecordCount = 0;

	public boolean tagValidation(FileValidationParam validateParam) throws IOException {

		invalidRecordCount = 0;
		File inputtagFile = new File(validateParam.getInputFilePath());
		String ackFileName = null;
		HashMap<String, String> ctocShortAgency = new HashMap<>();
		ctocShortAgency.put("UDOT", "ud");
		ctocShortAgency.put("WSDOT", "wd");
		ctocShortAgency.put("BATA", "at");
		ctocShortAgency.put("TCA", "tc");
		ctocShortAgency.put("GGBHTD", "gg");
		ctocShortAgency.put("LA Metro", "la");
		ctocShortAgency.put("OCTA", "oc");
		ctocShortAgency.put("RCTC", "rc");
		ctocShortAgency.put("SANDAG", "sd");
		ctocShortAgency.put("VTA", "vt");
		ctocShortAgency.put("SBX", "sx");
		ctocShortAgency.put("ACTC", "ac");
		ctocShortAgency.put("SFCTA", "sf");
		ctocShortAgency.put("SBCTA", "sb");
		ctocShortAgency.put("PoHR", "hr");
		ctocShortAgency.put("SR91", "sr");
		ctocShortAgency.put("ODOT", "od");
		ctocShortAgency.put("VTA", "vt");
		ctocShortAgency.put("SBX", "sx");
		ctocShortAgency.put("ACTC", "ac");
		ctocShortAgency.put("SFCTA", "sf");
		ctocShortAgency.put("SBCTA", "sb");

		log.info("inputtagFile" + validateParam.getInputFilePath());
		if (!inputtagFile.exists()) {
			controller.getErrorMsglist().add(new ErrorMsgDetail(FILE_RECORD_TYPE, "File", " file not found"));
			log.error("FAILED Reason::   file not found");
			return false;
		} else {
			if (validateParam.getFromAgency().equals(validateParam.getToAgency())) {
				log.error("From Agency code and To agency code should not be same");
				controller.getErrorMsglist()
						.add(new ErrorMsgDetail(FILE_RECORD_TYPE, "From Agency",
								"From Agency code " + validateParam.getFromAgency()
										+ " and Toagency code should not be same  ::" + validateParam.getToAgency()));
				return false;
			}
			if (validateParam.getOutputFilePath().isEmpty() || validateParam.getToAgency().equals("NONE")
					|| validateParam.getFromAgency().isEmpty()) {
				controller.getErrorMsglist().add(new ErrorMsgDetail(FILE_RECORD_TYPE, "Mandatory Fields",
						"Output path and Toagency are mandatory"));
				return false;
			}
			String fileName = inputtagFile.getName();
			String ackDate = ctocAckMapper.convertDateTimeFormat();
			String regex = "^([a-z]{4})_(\\d{8})T(\\d{6})\\.([a-z]{3})$";
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(fileName);
			if (matcher.matches()) {
				log.info("Valid File format" + fileName);
				log.error("inputtagFile.getName()" + inputtagFile.getName());
				String[] fromAgency = validateParam.getFromAgency().split("-");
				String[] toAgency = validateParam.getToAgency().split("-");
				String shortFromAgency = ctocShortAgency.get(fromAgency[1]);
				String shortToAgency = ctocShortAgency.get(toAgency[1]);
				if (fileName.substring(0, 2).equals(shortFromAgency)
						&& fileName.substring(2, 4).equals(shortToAgency)) {
				} else {
					addErrorMsg("Filename", "Filename", "Invalid From or To agency - " + fileName);
					validateParam.setResponseMsg("Invalid From or To agency::" + fileName);
					return false;
				}
				ackFileName = shortToAgency + shortFromAgency + "_" + ackDate + "_" + shortFromAgency + shortToAgency
						+ "_" + fileName.substring(5, 13) + "T" + fileName.substring(14, 20)
						+ IAGConstants.ACK_FILE_EXTENSION;
				log.info("Ack file name:::::" + ackFileName);
				if (commonUtil.validateTAGFileName(fileName)) {
					if (validateParam.getValidateType().equals("filename")) {
						validateParam.setResponseMsg("File name validation is sucess");
						return true;
					}
					String filePath = validateParam.getInputFilePath();
					try {
						if (validateRecordType(filePath, validateParam, fileName,
								validateParam.getOutputFilePath() + File.separator + ackFileName, shortFromAgency,
								shortToAgency, ackDate)) {
							if (validateFile(filePath, validateParam, fileName,
									validateParam.getOutputFilePath() + File.separator + ackFileName, shortFromAgency,
									shortToAgency, ackDate)) {
								System.out.println("File is valid.");
								ctocAckMapper.mapToCtocAckFile(fileName, "00",
										validateParam.getOutputFilePath() + File.separator + ackFileName,
										shortFromAgency, shortToAgency, "TAGS", ackDate);
							} else {
								System.out.println("File is invalid.");
								return false;
							}
						} else {
							return false;
						}
					} catch (IOException e) {
						System.out.println("Error reading file: " + e.getMessage());
					}
				} else {
					log.error("ITAG file Name validation is failed");
					controller.getErrorMsglist().add(
							new ErrorMsgDetail(FILE_RECORD_TYPE, "File Name", "TAG file Name validation is failed"));
					ctocAckMapper.mapToCtocAckFile(fileName, "02",
							validateParam.getOutputFilePath() + File.separator + ackFileName, shortFromAgency,
							shortToAgency, "TAGS", ackDate);
					return false;
				}
			} else {
				log.error("ITAG file Name format validation is failed");
				controller.getErrorMsglist()
						.add(new ErrorMsgDetail(FILE_RECORD_TYPE, "File Name", "TAG file Name format is failed"));
				ctocAckMapper.mapToCtocAckFile(fileName, "02",
						validateParam.getOutputFilePath() + File.separator + fileName.substring(0, 2)
								+ fileName.substring(2, 4) + "_" + ackDate + "_" + fileName.substring(0, 2)
								+ fileName.substring(2, 4) + "_" + fileName.substring(5, 13) + "T"
								+ fileName.substring(14, 20) + IAGConstants.ACK_FILE_EXTENSION
						, fileName.substring(0, 2), fileName.substring(2, 4), "TAGS", ackDate);
				return false;
			}
			return true;
		}
	}
	public boolean validateFile(String filePath, FileValidationParam validateParam, String fileName, String ackFilePath,
			String shortFromAgency, String shortToAgency, String ackDate) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(filePath));
		String line;
		String headerSequence = null;
		String headerBusinessDay = null;
		int detailCount = 0;
		while ((line = reader.readLine()) != null) {
			if (line.startsWith("#HEADER")) {
				if (!validateHeaderRecord(line, validateParam, fileName)) {
					log.info("Invalid header record");
					ctocAckMapper.mapToCtocAckFile(fileName, "01", ackFilePath, shortFromAgency, shortToAgency, "TAGS",
							ackDate);
					return false;
				} else if (validateParam.getValidateType().equals("header")) {
					ctocAckMapper.mapToCtocAckFile(fileName, "01", ackFilePath, shortFromAgency, shortToAgency, "TAGS",
							ackDate);
					log.info("Only file name and header validation");
					return true;
				}
				String[] fields = line.split(",");
				headerSequence = fields[3].trim(); 
				headerBusinessDay = fields[4].trim(); 
			}
		else if (!line.startsWith("#HEADER") && !line.startsWith("#TRAILER")) {
				log.info("INSIDE detail record");
				if (!validateDetailRecord(line, validateParam)) {
					log.info("Invalid detail record");
					ctocAckMapper.mapToCtocAckFile(fileName, "03", ackFilePath, shortFromAgency, shortToAgency, "TAGS",
							ackDate);
					return false;
				}
				detailCount++; 
			} else if (line.startsWith("#TRAILER")) {
				if (!validateTrailerRecord(line, headerSequence, headerBusinessDay, detailCount, validateParam)) {
					log.info("Invalid Trailer record");
					ctocAckMapper.mapToCtocAckFile(fileName, "03", ackFilePath, shortFromAgency, shortToAgency, "TAGS",
							ackDate);
					return false;
				}
			}
		}
		reader.close();
		return true;
	}

	public boolean validateHeaderRecord(String record, FileValidationParam validateParam, String fileName) {
		String[] fields = record.split(",");
		boolean isValid = true;
		log.info(" header record validations");
		if (fields.length == 9) {
			log.info(" header validations");
		} else {
			addErrorMsg("HEADER", "length", "Invalid Header number of fields" + record);
			validateParam.setResponseMsg("Invalid Header number of fields::" + record);
			return false;
		}
		if (fields[5].length() == 2 && fields[6].length() == 2) {
			if (fileName.substring(0, 2).equalsIgnoreCase(fields[5])
					&& fileName.substring(2, 4).equalsIgnoreCase(fields[6])) {
				log.info(" header validations file and header" + fileName.substring(0, 2).equals(fields[5]));
			} else {
				addErrorMsg("HEADER", "Source or Destination mismatch with file ",
						" Source or Destination mismatch with File name " + record);
				validateParam.setResponseMsg("Source or Destination mismatch with File name::" + record);
				return false;
			}
		} else {
			addErrorMsg("HEADER", "Source or Destination", "Invalid Header Source or Destination- " + fields[5]);
			validateParam.setResponseMsg("Invalid Header Source or destination::" + fields[5]);
			isValid = false;
		}
		if (fields[1].trim().equals("TAGS")) {
			log.info(" header validations");
		} else {
			addErrorMsg("HEADER", "Filetype", "Invalid Header Filetype- " + fields[1]);
			validateParam.setResponseMsg("Invalid Header Filetype::" + fields[1]);
			isValid = false;
		}
		if (fields[2].trim().equals("INIT")) {
			log.info(" header validations");
		} else {
			addErrorMsg("HEADER", "Actioncode", "Invalid Header Actioncode- " + fields[2]);
			validateParam.setResponseMsg("Invalid Header Actioncode::" + fields[2]);
			isValid = false;
		}
		if (fields[3].length() == 6 && fields[3].matches("\\d{6}")) {
			log.info(" header validations");
		} else {
			addErrorMsg("HEADER", "Sequence", "Invalid Header Sequence- " + fields[3]);
			validateParam.setResponseMsg("Invalid Header Sequence::" + fields[3]);
			isValid = false;
		}
		if (fields[4].matches("\\d{4}/\\d{2}/\\d{2}")) {
			if (CommonUtil.isValidFormat(fields[4])) {
				log.info(" header validations");
			} else {
				addErrorMsg("HEADER", "Business day", "Invalid Header Business day- " + fields[4]);
				validateParam.setResponseMsg("Invalid Header Business day::" + fields[4]);
				isValid = false;
			}
		} else {
			addErrorMsg("HEADER", "Business day", "Invalid Header Business day- " + fields[4]);
			validateParam.setResponseMsg("Invalid Header Business day::" + fields[4]);
			isValid = false;
		}
		if (fields[7].trim().matches("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}[-+]\\d{2}:\\d{2}$")) {
			if (CommonUtil.validateUTCDateTime(fields[7]) && CommonUtil
					.convertUTCToDate(fields[7].trim().substring(0, 25)).equals(fileName.substring(5, 20))) {
				log.info(" header validations" + fields[7]);
			} else {
				addErrorMsg("HEADER", "Create date", "Invalid Header Create date- " + fields[7]);
				validateParam.setResponseMsg("Invalid Header Create date::" + fields[7]);
				isValid = false;
			}
		} else {
			addErrorMsg("HEADER", "Create date", "Invalid Header Create date- " + fields[7]);
			validateParam.setResponseMsg("Invalid Header Create date::" + fields[7]);
			isValid = false;
		}
		if (fields[8].length() == 10) {
			log.info(" header validations");
		} else {
			addErrorMsg("HEADER", "Version", "Invalid Header Version- " + fields[8]);
			validateParam.setResponseMsg("Invalid Header Version::" + fields[8]);
			isValid = false;
		}
		return isValid;
	}

	// Validate DETAIL record
	public boolean validateDetailRecord(String record, FileValidationParam validateParam) {
		String[] fields = record.split(",");
		boolean isValid = true;
		if (fields.length == 8) {
			log.info(" header Detail validations" + "TRUE 1");
		} else {
			addErrorMsg("DETAIL", "length", "Invalid Detail number of fields" + record);
			validateParam.setResponseMsg("Invalid Detail number of fields:::" + fields[6]);
			return false;
		}
		if (fields[0].length() == 10 && fields[0].matches("[0-9A-Fa-f ]{10}")) {
			log.info("  Detail validations" + "TRUE 2");
		} else {
			addErrorMsg("DETAIL", "TAG ID", "Invalid TAG ID- " + fields[0]);
			validateParam.setResponseMsg("Invalid TAG ID::" + fields[0]);
			isValid = false;
		}
		log.info(" fields[1]" + fields[1]);
		if (fields[1].length() == 10) {
			log.info("  Detail validations" + "TRUE 3");
		} else {
			addErrorMsg("DETAIL", "Account ID", "Invalid Account ID- " + record);
			validateParam.setResponseMsg("Invalid Account ID::" + fields[1]);
			isValid = false;
		}
		if (fields[2].equals("A")) {
			log.info(" header Detail validations" + "TRUE 4");
		} else {
			addErrorMsg("DETAIL", "Action Code", "Invalid Action Code- " + record);
			validateParam.setResponseMsg("Invalid Action code::" + fields[2]);
			isValid = false;
		}
		if ("NVI".contains(fields[3])) {
			log.info(" header Detail validations" + "TRUE 5");
		} else {
			addErrorMsg("DETAIL", "TAG type", "Invalid TAG type- " + record);
			validateParam.setResponseMsg("Invalid TAG type::" + fields[3]);
			isValid = false;
		}
		if ("HN".contains(fields[4])) {
			log.info(" header Detail validations" + "TRUE 6");
		} else {
			addErrorMsg("DETAIL", "Subtype type A", "Invalid Subtype type A- " + record);
			validateParam.setResponseMsg("Invalid Subtype type A::" + fields[4]);
			isValid = false;
		}

		if ("NCMXYZ".contains(fields[5])) {
			log.info(" header Detail validations" + "TRUE 7");
		} else {
			addErrorMsg("DETAIL", "Subtype type B", "Invalid Subtype type B- " + record);
			validateParam.setResponseMsg("Invalid Subtype type B::" + fields[5]);
			isValid = false;
		}
		if ("VN".contains(fields[6])) {
			log.info(" header Detail validations" + "TRUE 8");
		} else {
			addErrorMsg("DETAIL", "Subtype type C", "Invalid Subtype type C- " + record);
			validateParam.setResponseMsg("Invalid Subtype type C::" + fields[6]);
			isValid = false;
		}
		if (fields[7].matches("[01]")) {
			log.info(" header Detail validations");
		} else {
			addErrorMsg("DETAIL", "Protocol type", "Invalid Protocol type - " + record);
			validateParam.setResponseMsg("Invalid Protocol type::" + fields[7]);
			isValid = false;
		}
		return isValid; 
	}
	// Validate TRAILER record
	public boolean validateTrailerRecord(String record, String headerSequence, String headerBusinessDay,
			int detailCount, FileValidationParam validateParam) {
		String[] fields = record.split(",");
		boolean isValid = true;
		log.info(" header Trailer validations" + fields[1].equals(headerSequence) + "hh"
				+ fields[2].equals(headerBusinessDay));
		log.info(" header Trailer validations detail count" + fields.length);
		if (fields.length == 4) {
			log.info(" Trailer Record length is valid");
		} else {
			addErrorMsg("TRAILER", "length", "Invalid Trailer number of fields" + record);
			validateParam.setResponseMsg("Trailer record length number of fields:::" + fields[1]);
			return false;
		}
		if (fields[1].equals(headerSequence)) {
			log.info(" Trailer Sequeence headerSequence are  matched");
		} else {
			validateParam.setResponseMsg("headerSequence is invalid and not matching with Header::" + fields[1]);
			addErrorMsg("TRAILER", "headerSequence", "Invalid headerSequence - " + fields[1]);
			isValid = false;
		}
		if (fields[2].equals(headerBusinessDay)) {
			log.info(" Trailer BusinessDay headerBusinessDay are  matched");
		} else {
			validateParam.setResponseMsg("headerBusinessDay is invalid and not matching with Header::" + fields[2]);
			addErrorMsg("TRAILER", "headerBusinessDay", "Invalid headerBusinessDay - " + fields[2]);
			isValid = false;
		}
		if (fields[3].length() == 8 && fields[3].matches("\\d{8}") && Integer.parseInt(fields[3]) == detailCount) {
			log.info(" Trailer Record count is valid");

		} else {
			validateParam
					.setResponseMsg("Trailer Record count is invalid or not matching with detail count::" + fields[3]);
			addErrorMsg("TRAILER", "Record Count", "Invalid Record count - " + fields[3]);
			isValid = false;
		}

		return isValid;
	}

	private void addErrorMsg(String fileType, String fieldName, String errorMsg) {
		controller.getErrorMsglist().add(new ErrorMsgDetail(fileType, fieldName, errorMsg));
	}

	public boolean validateRecordType(String filePath, FileValidationParam validateParam, String fileName,
			String ackFilePath, String shortFromAgency, String shortToAgency, String ackDate) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(filePath));
		String line;

		String firstLine = null;
		String lastLine = null;

		if ((line = reader.readLine()) != null) {
			firstLine = line;
		}
		while ((line = reader.readLine()) != null) {
			lastLine = line;
		}
		log.info("First Line" + firstLine);
		log.info("Last Line" + lastLine);
		if (!firstLine.startsWith("#HEADER")) {
			log.info("Invalid header record");
			addErrorMsg("HEADER", "length", "Invalid Header Return type " + firstLine);
			validateParam.setResponseMsg("Invalid Header Return type::" + firstLine);
			ctocAckMapper.mapToCtocAckFile(fileName, "01", ackFilePath, shortFromAgency, shortToAgency, "TAGS",
					ackDate);
			return false;
		} else if (!lastLine.startsWith("#TRAILER")) {
			log.info("Invalid Trailer record");
			addErrorMsg("TRAILER", "length", "Invalid Trailer Return type " + lastLine);
			validateParam.setResponseMsg("Invalid Trailer Return type::" + lastLine);
			ctocAckMapper.mapToCtocAckFile(fileName, "03", ackFilePath, shortFromAgency, shortToAgency, "TAGS",
					ackDate);
			return false;
		}
		return true;
	}

}
