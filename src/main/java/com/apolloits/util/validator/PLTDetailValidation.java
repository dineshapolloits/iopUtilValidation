
package com.apolloits.util.validator;

import static com.apolloits.util.IAGConstants.FILE_RECORD_TYPE;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
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
public class PLTDetailValidation {

	@Autowired
	private CtocAckFileMapper ctocAckMapper;
	@Autowired
	@Lazy
	CtocController controller;

	@Autowired
	CommonUtil commonUtil;

	int invalidRecordCount = 0;

	public boolean pltValidation(FileValidationParam validateParam) throws IOException {

		invalidRecordCount = 0;
		File inputtagFile = new File(validateParam.getInputFilePath());
		String ackFileName = null;
		HashMap<String, String> ctocShortAgency = new HashMap<>();
		ctocShortAgency.put("UDOT", "ud");
		ctocShortAgency.put("WSDOT", "wd"); // WashDOT
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
			// Regular expression to match the file name format: PREFIX_YYYYMMDD_HHMMSS.tag
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
						+ IAGConstants.ACK_FILE_EXTENSION;// validateParam.getToAgency() + "_" + fileName.replace(".",
															// "_") + IAGConstants.ACK_FILE_EXTENSION;
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
										shortFromAgency, shortToAgency, "PLATES", ackDate);
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
					log.error("PLT file Name validation is failed");
					controller.getErrorMsglist().add(
							new ErrorMsgDetail(FILE_RECORD_TYPE, "File Name", "PLT file Name validation is failed"));
					ctocAckMapper.mapToCtocAckFile(fileName, "02",
							validateParam.getOutputFilePath() + File.separator + ackFileName, shortFromAgency,
							shortToAgency, "PLATES", ackDate);
					return false;
				}
			} else {
				log.error("PLT file Name format validation is failed");
				controller.getErrorMsglist()
						.add(new ErrorMsgDetail(FILE_RECORD_TYPE, "File Name", "PLT file Name format is failed"));
				ctocAckMapper.mapToCtocAckFile(fileName, "02",
						validateParam.getOutputFilePath() + File.separator + fileName.substring(0, 2)
								+ fileName.substring(2, 4) + "_" + ackDate + "_" + fileName.substring(0, 2)
								+ fileName.substring(2, 4) + "_" + fileName.substring(5, 13) + "T"
								+ fileName.substring(14, 20) + IAGConstants.ACK_FILE_EXTENSION// validateParam.getToAgency()
																								// + "_" +
																								// fileName.replace(".",
																								// "_") +
																								// IAGConstants.ACK_FILE_EXTENSION;
						, fileName.substring(0, 2), fileName.substring(2, 4), "PLATES", ackDate);
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
		// String headerBusinessDay = null;
		int detailCount = 0;

		while ((line = reader.readLine()) != null) {
			// line = line.trim();
			if (line.startsWith("#HEADER")) {
				if (!validateHeaderRecord(line, validateParam, fileName)) {
					log.info("Invalid header record");
					ctocAckMapper.mapToCtocAckFile(fileName, "01", ackFilePath, shortFromAgency, shortToAgency,
							"PLATES", ackDate);
					return false;
				} else if (validateParam.getValidateType().equals("header")) {
					ctocAckMapper.mapToCtocAckFile(fileName, "01", ackFilePath, shortFromAgency, shortToAgency,
							"PLATES", ackDate);
					log.info("Only file name and header validation");
					return true;
				}
				String[] fields = line.split(",");
				headerSequence = fields[3].trim(); // Sequence # from header
				// headerBusinessDay = fields[4].trim(); // Business day from header

			}
			// Validate Detail Records
			// else if(line.matches("[0-9A-Fa-f]{8},\\s*\\d{7},[A],.[A-Z],.*")) {
			// else if (line.matches("\\s*[0-9A-Fa-f]{8},\\s*\\d{7},[A],.[A-Z],.*")) {

			else if (!line.startsWith("#HEADER") && !line.startsWith("#TRAILER")) {
				log.info("INSIDE detail record");
				if (!validateDetailRecord(line, validateParam)) {
					log.info("Invalid detail record");
					ctocAckMapper.mapToCtocAckFile(fileName, "03", ackFilePath, shortFromAgency, shortToAgency,
							"PLATES", ackDate);

					return false;
				}
				detailCount++; // Increment the detail record count
			} else if (line.startsWith("#TRAILER")) {
				if (!validateTrailerRecord(line, headerSequence, detailCount, validateParam)) {
					log.info("Invalid Trailer record");
					ctocAckMapper.mapToCtocAckFile(fileName, "03", ackFilePath, shortFromAgency, shortToAgency,
							"PLATES", ackDate);
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
		if (fields.length == 8) {
			log.info(" header validations");
		} else {
			addErrorMsg("HEADER", "length", "Invalid Header number of fields- " + record);
			validateParam.setResponseMsg("Invalid Header number of fields::" + record);
			return false;
		}

		if (fields[4].length() == 2 && fields[5].length() == 2) {

			if (fileName.substring(0, 2).equalsIgnoreCase(fields[4])
					&& fileName.substring(2, 4).equalsIgnoreCase(fields[5])) {
				log.info(" header validations");
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
		if (fields[1].trim().equals("PLATES")) {
			log.info(" header validations");
		} else {
			addErrorMsg("HEADER", "Filetype", "Invalid Header Filetype- " + fields[1]);
			validateParam.setResponseMsg("Invalid Header Filetype::" + fields[1]);
			isValid = false;
		}

		if (fields[2].trim().equals("INIT")) {
			log.info(" header validations");
		} else {
			addErrorMsg("HEADER", "Update code", "Invalid Update code- " + fields[2]);
			validateParam.setResponseMsg("Invalid Update code::" + fields[2]);
			isValid = false;
		}
		if (fields[3].trim().matches("\\d{6}")) {
			log.info(" header validations");
		} else {
			addErrorMsg("HEADER", "Header Sequence", "Invalid Header Sequence- " + fields[3]);
			validateParam.setResponseMsg("Invalid Header Sequence::" + fields[3]);
			isValid = false;
		}
		if (fields[4].trim().length() == 2) {
			log.info(" header validations");
		} else {
			addErrorMsg("HEADER", "Header Source", "Invalid Header Source  " + fields[4]);
			validateParam.setResponseMsg("Invalid Header Source :" + fields[4]);
			isValid = false;
		}
		if (fields[5].trim().length() == 2) {// T\\d{2}:\\d{2}:\\d{2}-\\d{1,2}:\\d{2}")) {
			log.info(" header validations");
		} else {
			addErrorMsg("HEADER", "Destination", "Invalid  Destination " + fields[5]);
			validateParam.setResponseMsg("Invalid Destination::" + fields[5]);
			isValid = false;
		}
		if (fields[6].trim().matches("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}[-+]\\d{2}:\\d{2}$")) {// {//T\\d{2}:\\d{2}:\\d{2}-\\d{1,2}:\\d{2}")) {
			if (CommonUtil.validateUTCDateTime(fields[6]) && CommonUtil.convertUTCToDate(fields[6].trim().substring(0,25)).equals(fileName.substring(5, 20))) {
				log.info(" header validations");
			} else {
				addErrorMsg("HEADER", "Create date", "Invalid Header Create date- " + fields[6]);
				validateParam.setResponseMsg("Invalid Header Create date::" + fields[6]);
				isValid = false;
			}
		} else {
			addErrorMsg("HEADER", "Create date", "Invalid Header Create date- " + fields[6]);
			validateParam.setResponseMsg("Invalid Header Create date::" + fields[6]);
			isValid = false;
		}
		if (fields[7].length() == 10 && fields[7].trim().equals("REV A2.1.1")) {
			log.info(" header validations");
		} else {
			addErrorMsg("HEADER", "Version", "Invalid Header Version- " + fields[7]);
			validateParam.setResponseMsg("Invalid Header Version::" + fields[7]);
			isValid = false;
		}

		return isValid;
	}

	// Validate DETAIL record
	public boolean validateDetailRecord(String record, FileValidationParam validateParam) {
		String[] fields = record.split(",");
		boolean isValid = true;
		if (fields.length == 10) {
			log.info(" header Detail validations" + "TRUE 1");
		} else {
			addErrorMsg("DETAIL", "length", "Invalid Detail number of fields" + record);
			validateParam.setResponseMsg("Invalid Detail number of fields::" + record);
			return false;
		}
		
		if (fields[0].length() == 10 && fields[0].trim().matches("\\d+")) {
			log.info(" Detail validations:: Account ID");
		} else {
			addErrorMsg("DETAIL", "length", "Invalid Account ID- " + fields[0]);
			validateParam.setResponseMsg("Invalid Account ID::" + fields[0]);
			isValid= false;
		}
		if (fields[1].length() == 10 && !fields[1].trim().contains(" ")) {
			log.info("  Detail validations License Plate");
		} else {
			addErrorMsg("DETAIL", "License Plate", "Invalid License Plate " + fields[1]);
			validateParam.setResponseMsg("Invalid License Plate::" + fields[1]);
			isValid= false;
		}
		if (fields[2].trim().length() == 2 && fields[2].trim().matches("^[A-Za-z]+$")) {
			log.info("  Detail validations::::State");
		} else {
			addErrorMsg("DETAIL", "Invalid State", "Invalid State " + fields[2]);
			validateParam.setResponseMsg("Invalid State::" + fields[2]);
			isValid= false;
		}
		if (fields[3].trim().equals("A")) {
			log.info("  Detail validations::: Action Code");
		} else {
			addErrorMsg("DETAIL", "Action Code", "Invalid Action Code " + fields[3]);
			validateParam.setResponseMsg("Invalid Action Code::" + fields[3]);
			isValid= false;
		}
		if (fields[4].trim().matches("\\d{4}/\\d{2}/\\d{2}")) {
			if(CommonUtil.isValidFormat(fields[4].trim())){
			log.info("  Detail validations::: Effective Start Date");
		}else{
			isValid= false;
			addErrorMsg("DETAIL", "Start Date", "Invalid Effective Start Date" + fields[4]);
			validateParam.setResponseMsg("Invalid Effective Start Date::" + fields[4]);
			} }
		else {
			addErrorMsg("DETAIL", "Start Date", "Invalid Effective Start Date" + fields[4]);
			validateParam.setResponseMsg("Invalid Effective Start Date::" + fields[4]);
			isValid= false;
		}

		if (fields[5].trim().isEmpty() || fields[5].trim().matches("\\d{4}/\\d{2}/\\d{2}")) {
			if(!fields[5].trim().isEmpty()) {
			if(CommonUtil.isValidFormat(fields[5].trim())){
			log.info(" Detail validations:: Effective End Date");
			}else{
			
			isValid= false;
			addErrorMsg("DETAIL", "End Date", "Invalid Effective End Date" + fields[5]);
			validateParam.setResponseMsg("Invalid Effective End Date::" + fields[5]);
			}} else{
				log.info(" Detail validations:: Effective End Date");
				}}
			else {
			addErrorMsg("DETAIL", "End Date", "Invalid Effective End Date " + fields[5]);
			validateParam.setResponseMsg("Invalid Effective End Date::" + fields[5]);
			isValid= false;
		}
		if (fields[6].trim().equals("N") || fields[6].trim().equals("R")) {
			log.info("  Detail validations:: PlateType");
		} else {
			addErrorMsg("DETAIL", "PlateType", "Invalid PlateType- " + fields[6]);
			validateParam.setResponseMsg("Invalid PlateType::" + fields[6]);
			isValid= false;
		}
		if (fields[7].trim().equals("N") || fields[7].trim().equals("C") || fields[7].trim().equals("M")
				|| fields[7].trim().equals("X") || fields[7].trim().equals("Y") || fields[7].trim().equals("Z")) {
			log.info("  Detail validations SubType");
		} else {
			addErrorMsg("DETAIL", "SubType", "Invalid SubType - " + fields[7]);
			validateParam.setResponseMsg("Invalid SubType::" + fields[7]);
			isValid= false;
		}
		if (fields[8].trim().length() <= 30) {
			log.info("  Detail validations:: LP Type");
		} else {
			addErrorMsg("DETAIL", "LP Type", "Invalid LP Type - " + fields[8]);
			validateParam.setResponseMsg("Invalid LP Type::" + fields[8]);
			isValid= false;
		}
		if (fields[9].length() == 10  && fields[9].matches("[0-9 ]{10}")) {
			log.info("  Detail validations : PlateID");
		} else {
			addErrorMsg("DETAIL", "PlateID", "Invalid PlateID - " +  fields[9]);
			validateParam.setResponseMsg("Invalid PlateID ::" + fields[9]);
			isValid= false;
		}
		
			return  isValid;
		

	}

	// Validate TRAILER record
	public boolean validateTrailerRecord(String record, String headerSequence, int detailCount,
			FileValidationParam validateParam) {
		String[] fields = record.split(",");
		boolean isValid = true;
		if (fields.length == 4) {
			log.info(" Trailer Record length is valid");
		} else {
			addErrorMsg("TRAILER", "length", "Invalid Trailer number of fields " + record);
			validateParam.setResponseMsg("Trailer record length number of fields::" + fields[1]);
			return false;
		}
		if (fields[1].equals(headerSequence)) {
			log.info(" Trailer Sequeence headerSequence are  matched");
		} else {
			validateParam.setResponseMsg("headerSequence is invalid and not matching with Header::" + fields[1]);
			addErrorMsg("TRAILER", "headerSequence", "Invalid headerSequence - " + fields[1]);
			isValid = false;
		}
		if (fields[2].trim().matches("\\d{4}/\\d{2}/\\d{2}")) {
			log.info(" Trailer Business Day  is valid");
		} else {
			validateParam
					.setResponseMsg("Trailer Record count is invalid or not matching with detail count::" + fields[3]);
			addErrorMsg("TRAILER", "Business Day", "Trailer Business Day  is invalid" + fields[2]);
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

		return fields[0].trim().equals("#TRAILER") && isValid;
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
		// Read the file till the end to capture the last line
		while ((line = reader.readLine()) != null) {
			lastLine = line;
		}
		log.info("First Line" + firstLine);
		log.info("Last Line" + lastLine);
		if (!firstLine.startsWith("#HEADER")) {
			if (!validateHeaderRecord(firstLine, validateParam, fileName)) {
				
			}
			log.info("Invalid header record");
			addErrorMsg("HEADER", "Record Type", "Invalid Header Return type " + firstLine.split(",")[0]);
			validateParam.setResponseMsg("Invalid Header Return type::" + firstLine.split(",")[0]);
			ctocAckMapper.mapToCtocAckFile(fileName, "01", ackFilePath, shortFromAgency, shortToAgency, "PLATES",
					ackDate);
			return false;
		} else if (!lastLine.startsWith("#TRAILER")) {
			log.info("Invalid Trailer record");
			addErrorMsg("TRAILER", "Record Type", "Invalid Trailer Return type " + lastLine);
			validateParam.setResponseMsg("Invalid Trailer Return type::" + lastLine);
			ctocAckMapper.mapToCtocAckFile(fileName, "03", ackFilePath, shortFromAgency, shortToAgency, "PLATES",
					ackDate);
			return false;
		}
		return true;
	}
}