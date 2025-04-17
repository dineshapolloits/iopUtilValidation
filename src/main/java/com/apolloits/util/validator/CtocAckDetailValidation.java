package com.apolloits.util.validator;

import static com.apolloits.util.IAGConstants.FILE_RECORD_TYPE;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
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
public class CtocAckDetailValidation {
	@Autowired
	private CtocAckFileMapper ctocAckMapper;
	@Autowired
	@Lazy
	CtocController controller;

	@Autowired
	CommonUtil commonUtil;

	public boolean ackValidation(FileValidationParam validateParam) {
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
			/*
			 * if (validateParam.getOutputFilePath().isEmpty() ||
			 * validateParam.getToAgency().equals("NONE") ||
			 * validateParam.getFromAgency().isEmpty()) {
			 * controller.getErrorMsglist().add(new ErrorMsgDetail(FILE_RECORD_TYPE,
			 * "Mandatory Fields", "Output path and Toagency are mandatory")); return false;
			 * }
			 */
			String fileName = inputtagFile.getName();
			
			// wdtc_20250305_015842_tcwd_20250214_100210.ACK
			String regex = "^([a-z]{4})_(\\d{8})T(\\d{6})_([a-z]{4})_(\\d{8})T(\\d{6})\\.([a-z]{3})$";
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(fileName);
			if (matcher.matches()) {
				
				if(IAGConstants.AGENCY_CODES.contains(fileName.substring(21,23)) && IAGConstants.AGENCY_CODES.contains(fileName.substring(23,25))) {
					System.out.println("Valid Agencies");
				}
				else {
					log.error("ACK file Name format validation is failed");
					addErrorMsg("FILE NAME", "FILE NAME", "Invalid FILE NAME- " + fileName);
					validateParam.setResponseMsg("Invalid FILE NAME::" + fileName);
					/*
					 * controller.getErrorMsglist().add( new ErrorMsgDetail(FILE_RECORD_TYPE,
					 * "File Name", "TOLL Recon file Name format is failed"));
					 */	return false;
				}
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

				if (commonUtil.validateACKFileName(fileName)) {

					if (validateParam.getValidateType().equals("filename")) {
						validateParam.setResponseMsg("File name validation is sucess");
						return true;
					}
					String filePath = validateParam.getInputFilePath();

					try {
					
							if (validateFile(filePath, validateParam, fileName)) {
								System.out.println("File is valid.");

							} else {
								System.out.println("File is invalid.");
								return false;
							}
						
					} catch (IOException e) {
						System.out.println("Error reading file: " + e.getMessage());
					}
				} else {
					log.error("ACk file Name validation is failed");
					controller.getErrorMsglist().add(new ErrorMsgDetail(FILE_RECORD_TYPE, "File Name",
							"ACK file Name validation is failed"));

					return false;
				}
			} else {
				log.error("ACK file Name format validation is failed");
				controller.getErrorMsglist().add(
						new ErrorMsgDetail(FILE_RECORD_TYPE, "File Name", "ACK file Name format is failed"));

				return false;
			}

			return true;

		}
	}	public boolean validateFile(String filePath, FileValidationParam validateParam, String fileName) throws IOException {
		boolean isValid = true;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(filePath));
			String line;
			

			while ((line = reader.readLine()) != null) {
				//line = line.trim();
				String[] fields = line.split(",");

				// Validate the fields in the record (all in one block)
				if (fields.length != 9) {
					System.out.println("Invalid number of fields in record: " + line);
					isValid = false;
					addErrorMsg("HEADER", "Number of fields", "Invalid Number of fields- " + line);
					validateParam.setResponseMsg("Invalid Number of fields::" + line);
					continue;
				}

				// Validate each field directly
				if (!(fields[0].length() == 3 && fields[0].equals("ACK"))) {
					System.out.println("Invalid RECORD TYPE: " + fields[0]);
					isValid = false;
					addErrorMsg("HEADER", "RECORD TYPE", "Invalid RECORD TYPE- " + fields[0]);
					validateParam.setResponseMsg("Invalid RECORD TYPE::" + fields[0]);
				}

				String[] validFileTypes = { "TAGS", "PLATES", "TOLL", "PAYBYPLATE", "CORR", "RECONCILE", "PLATERECON",
						"CORRECON" };
				boolean isValidFileType = false;
				for (String validType : validFileTypes) {
					if (fields[1].trim().equals(validType) && fields[1].length() == 10) {
						isValidFileType = true;
						break;
					}
				}
				if (!isValidFileType) {
					System.out.println("Invalid FILE TYPE: " + fields[1]);
					isValid = false;
					addErrorMsg("HEADER", "FILE TYPE", "Invalid FILE TYPE- " + fields[1]);
					validateParam.setResponseMsg("Invalid FILE TYPE::" + fields[1]);
				}

				if (fields[2].length() > 45) {
					System.out.println("Invalid FILE NAME: " + fields[2]);
					isValid = false;
					addErrorMsg("HEADER", "FILE NAME", "Invalid FILE NAME- " + fields[2]);
					validateParam.setResponseMsg("Invalid FILE NAME::" + fields[2]);
				}else {
					
					String regex = "^([a-z]{4})_(\\d{8})T(\\d{6})\\.([a-z]{3})$";
					Pattern pattern = Pattern.compile(regex);
					Matcher matcher = pattern.matcher(fields[2].trim());
					String regexRecon = "^([a-z]{4})_(\\d{8})T(\\d{6})_([a-z]{4})_(\\d{8})T(\\d{6})\\.([a-z]{3})$";
					Pattern patternRecon = Pattern.compile(regexRecon);
					Matcher matcherRecon = patternRecon.matcher(fields[2].trim());
					if (matcher.matches() || matcherRecon.matches()) {
						
							if(!fields[2].trim().substring(5, 20).equals(fileName.substring(26, 41)) && !fileName.substring(21, 25).equals(fields[2].trim().substring(0, 5))) {
								System.out.println("Invalid FILE NAME: Doesnot match with file name " + fields[2]);
								isValid = false;
								addErrorMsg("HEADER", "FILE NAME", "Invalid FILE NAME- Doesnot match with file name " + fields[2]);
								validateParam.setResponseMsg("Invalid FILE NAME:: Doesnot match with file name " + fields[2]);
							} 
						
					}else {
						System.out.println("Invalid FILE NAME: " + fields[2]);
						isValid = false;
						addErrorMsg("HEADER", "FILE NAME", "Invalid FILE NAME- " + fields[2]);
						validateParam.setResponseMsg("Invalid FILE NAME::" + fields[2]);
					}
					
				}

				if (!(fields[3].length() == 2 && fields[3].matches("[A-Za-z]{2}") && fileName.substring(0, 2).equalsIgnoreCase(fields[3]))) {
					System.out.println("Invalid FROM AGENCY: " + fields[3]);
					isValid = false;
					addErrorMsg("HEADER", "FROM AGENCY", "Invalid FROM AGENCY- " + fields[3]);
					validateParam.setResponseMsg("Invalid FROM AGENCY::" + fields[3]);
				}

				if (!(fields[4].length() == 2 && fields[4].matches("[A-Za-z]{2}") && fileName.substring(2, 4).equalsIgnoreCase(fields[4]))) {
					System.out.println("Invalid TO AGENCY: " + fields[4]);
					isValid = false;
					addErrorMsg("HEADER", "TO AGENCY", "Invalid TO AGENCY- " + fields[4]);
					validateParam.setResponseMsg("Invalid TO AGENCY::" + fields[4]);
				}
				
				if (!CommonUtil.validateUTCDateTime(fields[5])) {
					System.out.println("Invalid CREATE DATE: " + fields[5]);
					isValid = false;
					addErrorMsg("HEADER", "CREATE DATE", "Invalid CREATE DATE- " + fields[5]);
					validateParam.setResponseMsg("Invalid CREATE DATE::" + fields[5]);
				}
				//2025-03-17T20:02:30-07:00 converted to 20250318T030230
				System.out.println("fileName.substring(26, 41)" + fileName.substring(26, 41)); //20250314T011521
				if (!CommonUtil.convertUTCToDate(fields[5].trim().substring(0,25)).equals(fileName.substring(5, 20)))
				{
					System.out.println("Invalid CREATE DATE: " + fields[5]);
					isValid = false;
					addErrorMsg("HEADER", "CREATE DATE", "Invalid CREATE DATE - Not matching with File Date " + fields[5]);
					validateParam.setResponseMsg("Invalid CREATE DATE - Not matching with File Date::" + fields[5]);
				}
				if (!CommonUtil.validateUTCDateTime(fields[6])) {
					System.out.println("Invalid ACK DATE: " + fields[6]);
					isValid = false;
					addErrorMsg("HEADER", "ACK DATE", "Invalid ACK DATE- " + fields[6]);
					validateParam.setResponseMsg("Invalid ACK DATE::" + fields[6]);
				}
				/*
				 * if
				 * (!fields[6].substring(0,19).replace("-","").replace(":","").equals(fileName.
				 * substring(5,20))) { System.out.println("Invalid ACK DATE: " + fields[5]);
				 * isValid = false; addErrorMsg("HEADER", "ACK DATE",
				 * "Invalid ACK DATE - Not matching with File Date " + fields[6]);
				 * validateParam.
				 * setResponseMsg("Invalid ACK DATE - Not matching with File Date::" +
				 * fields[6]); }
				 */

				if (!(fields[7].matches("00|01|02|03|04") && fields[7].length() == 2)) {
					System.out.println("Invalid RETURN CODE: " + fields[7]);
					isValid = false;
					addErrorMsg("HEADER", "RETURN CODE", "Invalid RETURN CODE- " + fields[7]);
					validateParam.setResponseMsg("Invalid RETURN CODE::" + fields[7]);
				}

				if (!(fields[8].length() == 10 && fields[8].equals("REV A2.1.1"))) {
					System.out.println("Invalid VERSION: " + fields[8]);
					isValid = false;
					addErrorMsg("HEADER", "VERSION", "Invalid VERSION- " + fields[8]);
					validateParam.setResponseMsg("Invalid VERSION::" + fields[8]);
				}
				if (!commonUtil.hasLFLineEndings(filePath)) {
					isValid = false;
					addErrorMsg("HEADER", "LF", "Invalid LF(Line Feed)- " + filePath);
					validateParam.setResponseMsg("Invalid LF(Line Feed)::" + filePath);
				}
			}

			if (isValid) {
				System.out.println("File is valid.");
			} else {
				System.out.println("File is invalid.");
			}

			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return isValid;
	}


	private void addErrorMsg(String fileType, String fieldName, String errorMsg) {
		controller.getErrorMsglist().add(new ErrorMsgDetail(fileType, fieldName, errorMsg));
	}
  
}
