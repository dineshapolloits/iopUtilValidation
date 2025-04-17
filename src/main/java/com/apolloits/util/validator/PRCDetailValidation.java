
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
public class PRCDetailValidation {
	@Autowired
	private CtocAckFileMapper ctocAckMapper;
	@Autowired
	@Lazy
	CtocController controller;

	@Autowired
	CommonUtil commonUtil;

	int invalidRecordCount = 0;

	public boolean prcValidation(FileValidationParam validateParam) {
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
			// Regular expression to match the file name format:
			// atsr_20250204_015555_srat_20250204_014627.trc
			String regex = "^([a-z]{4})_(\\d{8})T(\\d{6})_([a-z]{4})_(\\d{8})T(\\d{6})\\.([a-z]{3})$";
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(fileName);
			if (matcher.matches()) {
				if(IAGConstants.AGENCY_CODES.contains(fileName.substring(21,23)) && IAGConstants.AGENCY_CODES.contains(fileName.substring(23,25))) {
					System.out.println("Valid Agencies");
				}
				else {
					log.error("Plate Recon file Name format validation is failed");
					controller.getErrorMsglist().add(
							new ErrorMsgDetail(FILE_RECORD_TYPE, "File Name", "Plate Recon file Name format is failed"));
					ctocAckMapper.mapToCtocAckFile(fileName, "02",
							validateParam.getOutputFilePath() + File.separator + fileName.substring(2, 4)
									+ fileName.substring(0, 2) + "_" + ackDate + "_" + fileName.substring(0, 2)
									+ fileName.substring(2, 4) + "_" + fileName.substring(5, 13) + "T"
									+ fileName.substring(14, 20) + IAGConstants.ACK_FILE_EXTENSION
							, fileName.substring(0, 2), fileName.substring(2, 4), "PLATERECON", ackDate);
					return false;
				}
				log.info("Valid File format" + fileName);
				log.error("inputtagFile.getName()" + inputtagFile.getName());
				String[] fromAgency = validateParam.getFromAgency().split("-");
				String[] toAgency = validateParam.getToAgency().split("-");

				String shortFromAgency = ctocShortAgency.get(fromAgency[1]);
				String shortToAgency = ctocShortAgency.get(toAgency[1]);
				ackFileName = shortToAgency + shortFromAgency + "_" + ackDate + "_" + fileName.substring(0, 20)	+ IAGConstants.ACK_FILE_EXTENSION;
				log.info("Ack file name:::::" + ackFileName);
				if (fileName.substring(0, 2).equals(shortFromAgency)
						&& fileName.substring(2, 4).equals(shortToAgency)) {

				} else {
					addErrorMsg("Filename", "Filename", "Invalid From or To agency - " + fileName);
					validateParam.setResponseMsg("Invalid From or To agency::" + fileName);
					ctocAckMapper.mapToCtocAckFile(fileName, "02",
							validateParam.getOutputFilePath() + File.separator + fileName.substring(2, 4)
									+ fileName.substring(0, 2) + "_" + ackDate + "_" + fileName.substring(0, 2)
									+ fileName.substring(2, 4) + "_" + fileName.substring(5, 13) + "T"
									+ fileName.substring(14, 20) + IAGConstants.ACK_FILE_EXTENSION
							, fileName.substring(0, 2), fileName.substring(2, 4), "PLATERECON", ackDate);
					return false;
				}
				
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
										shortFromAgency, shortToAgency, "PLATERECON", ackDate);
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
					log.error("Plate Recon file Name validation is failed");
					controller.getErrorMsglist().add(new ErrorMsgDetail(FILE_RECORD_TYPE, "File Name",
							"Plate Recon file Name validation is failed"));
					ctocAckMapper.mapToCtocAckFile(fileName, "02",
							validateParam.getOutputFilePath() + File.separator + ackFileName, shortFromAgency,
							shortToAgency, "PLATERECON", ackDate);
					return false;
				}
			} else {
				log.error("Plate Recon file Name format validation is failed");
				controller.getErrorMsglist().add(
						new ErrorMsgDetail(FILE_RECORD_TYPE, "File Name", "Plate Recon file Name format is failed"));
				ctocAckMapper.mapToCtocAckFile(fileName, "02",
						validateParam.getOutputFilePath() + File.separator + fileName.substring(2, 4)
								+ fileName.substring(0, 2) + "_" + ackDate + "_" + fileName.substring(0, 2)
								+ fileName.substring(2, 4) + "_" + fileName.substring(5, 13) + "T"
								+ fileName.substring(14, 20) + IAGConstants.ACK_FILE_EXTENSION// validateParam.getToAgency()
																								// + "_" +
																								// fileName.replace(".",
																								// "_") +
																								// IAGConstants.ACK_FILE_EXTENSION;
						, fileName.substring(0, 2), fileName.substring(2, 4), "PLATERECON", ackDate);
				return false;
			}

			return true;
		}
	}

	public boolean validateFile(String filePath, FileValidationParam validateParam, String fileName, String ackFilePath,
			String shortFromAgency, String shortToAgency, String ackDate) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(filePath));
		String line;
		int detailCount = 0;
		String headerSequence = null;
		double totalAmount = 0.0;
		int acceptedCount = 0;
        double acceptedAmount = 0.0;
        boolean isValid = true;

		while ((line = reader.readLine()) != null) {
			// line = line.trim();
			if (line.startsWith("#HEADER")) {
				if (!validateHeader(line, validateParam, fileName)) {
					log.info("Invalid header record");
					ctocAckMapper.mapToCtocAckFile(fileName, "01", ackFilePath, shortFromAgency, shortToAgency,
							"PLATERECON", ackDate);
					return false;
				} else if (validateParam.getValidateType().equals("header")) {
					ctocAckMapper.mapToCtocAckFile(fileName, "01", ackFilePath, shortFromAgency, shortToAgency,
							"PLATERECON", ackDate);
					log.info("Only file name and header validation");
					return true;
				}
				String[] fields = line.split(",");
				headerSequence = fields[2].trim(); // Sequence # from header
				// headerBusinessDay = fields[4].trim(); // Business day from header

			}
			// Validate Detail Records
			// else if(line.matches("[0-9A-Fa-f]{8},\\s*\\d{7},[A],.[A-Z],.*")) {
			// else if (line.matches("\\s*[0-9A-Fa-f]{8},\\s*\\d{7},[A],.[A-Z],.*")) {

			else if (!line.startsWith("#HEADER") && !line.startsWith("#TRAILER")) {
				log.info("INSIDE detail record");
				if (!validateDetail(line, validateParam)) {
					log.info("Invalid detail record");
					ctocAckMapper.mapToCtocAckFile(fileName, "03", ackFilePath, shortFromAgency, shortToAgency,
							"PLATERECON", ackDate);

					return false;
				}
				double detailAmount = Double.parseDouble(getDetailAmount(line));
				 totalAmount += detailAmount;
				detailCount++; // Increment the detail record count
				if (isAccepted(line)) {
                    acceptedCount++;
                    acceptedAmount += detailAmount;
                }
			} else if (line.startsWith("#TRAILER")) {
				if (!validateTrailer(line, headerSequence, detailCount, totalAmount, acceptedCount, acceptedAmount,validateParam)) {
					log.info("Invalid Trailer record");
					ctocAckMapper.mapToCtocAckFile(fileName, "03", ackFilePath, shortFromAgency, shortToAgency,
							"PLATERECON", ackDate);

					return false;
				}
			}
		}
		reader.close();
		return true;
	}

	// Validate HEADER record
	private boolean validateHeader(String line, FileValidationParam validateParam, String fileName) {
		String[] fields = line.split(",");
		boolean isValid= true;
		if (fields.length == 8) {
			log.info(" Header validations length");
		} else {
			addErrorMsg("HEADER", "length", "Invalid Header number of fields" + fields);
			validateParam.setResponseMsg("Invalid Header number of fields::" + fields);
			return false;
		}

		if (fileName.substring(0, 2).equalsIgnoreCase(fields[4])
				&& fileName.substring(2, 4).equalsIgnoreCase(fields[5])) {
			log.info(" header validations file name with header asource and destination agencies");
		} else {
			addErrorMsg("HEADER", "Source or Destination mismatch with file ",
					" Source or Destination mismatch with File name " + line);
			validateParam.setResponseMsg("Source or Destination mismatch with File name::" + line);
			isValid = false;
			//return false;
		}

		if (fields[1].trim().equals("PLATERECON")) {
			log.info(" header validations File type");
		} else {
			addErrorMsg("HEADER", "File type", "Invalid File type- " + fields[1]);
			validateParam.setResponseMsg("Invalid File type::" + fields[1]);
		}
		if (fields[2].trim().matches("\\d{6}")) {
			log.info(" header validations SEQUENCE");
		} else {
			addErrorMsg("HEADER", "SEQUENCE", "Invalid SEQUENCE- " + fields[2]);
			validateParam.setResponseMsg("Invalid SEQUENCE:" + fields[2]);
		}
		if (fields[3].trim().matches("\\d{4}/\\d{2}/\\d{2}")) {
			if (CommonUtil.isValidFormat(fields[3])) {
				log.info(" header validations BUSINESS DAY");
			} else {
				isValid= false;
				addErrorMsg("HEADER", "BUSINESS DAY", "Invalid Header BUSINESS DAY- " + fields[3]);
				validateParam.setResponseMsg("Invalid Header BUSINESS DAY::" + fields[3]);
			}
		} else {
			addErrorMsg("HEADER", "BUSINESS DAY", "Invalid BUSINESS DAY- " + fields[3]);
			validateParam.setResponseMsg("Invalid BUSINESS DAY:" + fields[3]);
		}
		if (fields[4].trim().length() == 2) {
			log.info(" header validations SOURCE");
		} else {
			addErrorMsg("HEADER", "SOURCE", "Invalid SOURCE- " + fields[4]);
			validateParam.setResponseMsg("Invalid SOURCE:" + fields[4]);
		}
		if (fields[5].trim().length() == 2) {
			log.info(" header validations DESTINATION");
		} else {
			addErrorMsg("HEADER", "DESTINATION", "Invalid DESTINATION- " + fields[5]);
			validateParam.setResponseMsg("Invalid DESTINATION:" + fields[5]);
		}
		if (fields[6].trim().matches("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}[-+]\\d{2}:\\d{2}$")) {
			if (CommonUtil.validateUTCDateTime(fields[6]) && CommonUtil.convertUTCToDate(fields[6].trim().substring(0,25)).equals(fileName.substring(5, 20))) {
				log.info(" header validations CREATE DATE");
			} else {
				isValid= false;
				addErrorMsg("HEADER", "CREATE DATE", "Invalid Header CREATE DATE- " + fields[6]);
				validateParam.setResponseMsg("Invalid Header CREATE DATE::" + fields[6]);
			}
		} else {
			addErrorMsg("HEADER", "CREATE DATE", "Invalid CREATE DATE- " + fields[6]);
			validateParam.setResponseMsg("Invalid CREATE DATE:" + fields[6]);
		}
		/*
		 * if (fields[7].matches("^\\d{2}:\\d{2}:\\d{2}[+-]\\d{2}:\\d{2}$")) {
		 * log.info(" header validations CREATE DATE Time"); } else {
		 * addErrorMsg("HEADER", "CREATE DATE TIME", "Invalid CREATE DATE Time- " +
		 * fields[7]); validateParam.setResponseMsg("Invalid CREATE DATE Time:" +
		 * fields[7]); }
		 */
		if (fields[7].length() == 10 && fields[7].trim().equals("REV A2.1.1")) {
			log.info(" header validations VERSION");
		} else {
			addErrorMsg("HEADER", "VERSION", "Invalid VERSION- " + fields[7]);
			validateParam.setResponseMsg("Invalid VERSION:" + fields[7]);
		}

		return fields[0].trim().equals("#HEADER") && fields[1].trim().equals("PLATERECON")
				&& fields[2].trim().matches("\\d{6}") && // SEQUENCE #
				fields[3].trim().matches("\\d{4}/\\d{2}/\\d{2}") && // BUSINESS DAY
				fields[4].trim().length() == 2 && // SOURCE
				fields[5].trim().length() == 2 && // DESTINATION
				fields[6].trim().matches("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}[-+]\\d{2}:\\d{2}$") &&
				// fields[6].trim().matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}-\\d{2}:\\d{2}")
				// && // CREATE DATE
				//fields[6].trim().matches("\\d{2}/\\d{2}/\\d{4}")
			//	&& fields[7].matches("^\\d{2}:\\d{2}:\\d{2}[+-]\\d{2}:\\d{2}$") && 
				fields[7].length() == 10 && fields[7].trim().equals("REV A2.1.1") &&
				//fields[8].length() == 10 && fields[8].trim().equals("REV A2.0") &&// VERSION
				isValid;
	}

	// Validate DETAIL record
	private boolean validateDetail(String line, FileValidationParam validateParam) {
		String[] fields = line.split(",");
		boolean isValid = true;
		if (fields.length == 17) {
			log.info(" Detail validations");
		} else {
			addErrorMsg("DETAIL", "length", "Invalid Detail number of fields" + line);
			validateParam.setResponseMsg("Invalid Detail number of fields::" + line);
			return false;
		}
		if (fields[0].matches("[A-F0-9 ]{10}")) {
			log.info(" header validations LICENSE PLATE");
		} else {
			addErrorMsg("DETAIL", "LICENSE PLATE", "Invalid LICENSE PLATE- " + fields[0]);
			validateParam.setResponseMsg("Invalid LICENSE PLATE::" + fields[0]);
		}
		if (fields[1].matches("[0-9 ]{10}")) {
			log.info(" header validations TRANSACTION");
		} else {
			addErrorMsg("DETAIL", "TRANSACTION ID", "Invalid TRANSACTION- " + fields[1]);
			validateParam.setResponseMsg("Invalid TRANSACTION::" + fields[1]);
		}
		if (fields[2].trim().length() == 2 && fields[2].trim().matches("^[A-Za-z]+$")) {
			log.info(" header validations STATE");
		} else {
			addErrorMsg("DETAIL", "STATE", "Invalid STATE- " + fields[2]);
			validateParam.setResponseMsg("Invalid STATE::" + fields[2]);
		}
		if (fields[3].trim().matches("\\d{1,8}\\.\\d{2}")) {
			log.info(" header validations TRANSACTION AMOUNT");
		} else {
			addErrorMsg("DETAIL", "TRANSACTION AMOUNT", "Invalid TRANSACTION AMOUNT- " + fields[3]);
			validateParam.setResponseMsg("Invalid TRANSACTION AMOUNT::" + fields[3]);
		}
		if (fields[4].trim().matches("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}[-+]\\d{2}:\\d{2}$") || fields[4].matches("[ ]{25}")) {
			if (CommonUtil.validateUTCDateTime(fields[4])) {
			log.info(" header validations ENTRY DATE");
		} else {
			isValid= false;		
			addErrorMsg("DETAIL", "ENTRY TRAN DATE", "Invalid ENTRY TRAN DATE- " + fields[4]);
			validateParam.setResponseMsg("Invalid ENTRY DATE::" + fields[4]);
		}}else {
			addErrorMsg("DETAIL", "ENTRY TRAN DATE", "Invalid ENTRY TRAN DATE- " + fields[4]);
			validateParam.setResponseMsg("Invalid ENTRY DATE::" + fields[4]);
		}

		/*
		 * if (fields[5].matches("^\\d{2}:\\d{2}:\\d{2}[+-]\\d{2}:\\d{2}$") ||
		 * fields[5].matches("[ ]{14}")) {
		 * log.info(" header validations ENTRY TRAN DATE Time"); } else {
		 * addErrorMsg("DETAIL", "ENTRY TRAN DATE", "Invalid ENTRY TRAN DATE Time- " +
		 * fields[5]); validateParam.setResponseMsg("Invalid ENTRY TRAN DATE Time::" +
		 * fields[5]); }
		 */
		if (fields[5].length() == 22) {
			log.info(" header validations ENTRY PLAZA");
		} else {
			addErrorMsg("DETAIL", "ENTRY PLAZA", "Invalid ENTRY PLAZA- " + fields[5]);
			validateParam.setResponseMsg("Invalid ENTRY PLAZA::" + fields[5]);
		}
		if (fields[6].matches("\\d{2}") || fields[6].matches("[ ]{2}")) {
			log.info(" header validations ENTRY LANE");
		} else {
			addErrorMsg("DETAIL", "ENTRY LANE", "Invalid ENTRY LANE- " + fields[6]);
			validateParam.setResponseMsg("Invalid ENTRY LANE::" + fields[6]);
		}
		if (fields[7].trim().matches("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}[-+]\\d{2}:\\d{2}$")) {
			if (CommonUtil.validateUTCDateTime(fields[7])) {
			log.info(" header validations EXIT TRAN DATE");
		} else {
			isValid= false;
			addErrorMsg("DETAIL", "EXIT DATE", "Invalid EXIT TRAN DATE- " + fields[7]);
			validateParam.setResponseMsg("Invalid EXIT TRAN DATE::" + fields[7]);
		}}else {
			addErrorMsg("DETAIL", "EXIT DATE", "Invalid EXIT TRAN DATE- " + fields[7]);
			validateParam.setResponseMsg("Invalid EXIT TRAN DATE::" + fields[7]);
		}
		/*
		 * if (fields[9].trim().matches("^\\d{2}:\\d{2}:\\d{2}[+-]\\d{2}:\\d{2}$")) {
		 * log.info(" header validations EXIT TRAN DATE Time"); } else {
		 * addErrorMsg("DETAIL", "EXIT TRAN DATE", "Invalid EXIT TRAN DATE Time- " +
		 * fields[9]); validateParam.setResponseMsg("Invalid EXIT TRAN DATE Time::" +
		 * fields[9]); }
		 */

		if ( fields[8].trim().length() <= 22 && !fields[8].trim().isEmpty()) {
			log.info(" header validations EXIT PLAZA");
		} else {
			addErrorMsg("DETAIL", "EXIT PLAZA", "Invalid EXIT PLAZA- " + fields[8]);
			validateParam.setResponseMsg("Invalid EXIT PLAZA::" + fields[8]);
		}
		if (fields[9].trim().matches("\\d{2}") && !fields[9].trim().isEmpty()) {
			log.info(" header validations EXIT LANE");
		} else {
			addErrorMsg("DETAIL", "EXIT LANE", "Invalid EXIT LANE- " + fields[9]);
			validateParam.setResponseMsg("Invalid EXIT LANE::" + fields[9]);
		}
		if (fields[10].trim().matches("\\d{1,2}") || fields[10].trim().isEmpty()) {
			log.info(" header validations AXLE COUNT");
		} else {
			addErrorMsg("DETAIL", "AXLE COUNT", "Invalid AXLE COUNT- " + fields[10]);
			validateParam.setResponseMsg("Invalid AXLE COUNT::" + fields[10]);
		}
		
		if (fields[11].trim().length() <= 30) {
			log.info(" header validations LP TYPE");
		} else {
			addErrorMsg("DETAIL", "LP TYPE", "Invalid LP TYPE- " + fields[11]);
			validateParam.setResponseMsg("Invalid LP TYPE::" + fields[11]);
		}

		if (fields[12].trim().matches("\\d{1,8}\\.\\d{2}")) {
			log.info(" header validations WR TRAN FEE");
		} else {
			addErrorMsg("DETAIL", "WR TRAN FEE", "Invalid WR TRAN FEE- " + fields[12]);
			validateParam.setResponseMsg("Invalid WR TRAN FEE::" + fields[12]);
		}
		if (fields[13].trim().matches("[0-2]")) {
			log.info(" header validations WR FEE TYPE");
		} else {
			log.info(" INVALID validations WR FEE TYPE");
			addErrorMsg("DETAIL", "WR FEE TYPE", "Invalid WR FEE TYPE- " + fields[13]);
			validateParam.setResponseMsg("Invalid WR FEE TYPE::" + fields[13]);
		}
		
		if (fields[14].trim().matches("\\d{1,8}\\.\\d{2}")) {
			log.info(" header validations POST AMT");
		} else {
			addErrorMsg("DETAIL", "POST AMT", "Invalid POST AMT- " + fields[14]);
			validateParam.setResponseMsg("Invalid POST AMT::" + fields[14]);
		}
		String responseCode = "AODFIL";
		if (responseCode.contains(fields[15].trim())) {
			log.info(" header validations RESPONSE CODE");
		} else {
			addErrorMsg("DETAIL", "RESPONSE CODE", "Invalid RESPONSE CODE- " + fields[15]);
			validateParam.setResponseMsg("Invalid RESPONSE CODE::" + fields[15]);
		}if (fields[16].trim().matches("\\d{1,7}\\.\\d{2}") || fields[16].trim().isEmpty()) {
			log.info(" header validations NIOP FEE");
		} else {
			addErrorMsg("DETAIL", "NIOP FEE", "Invalid NIOP FEE- " + fields[16]);
			validateParam.setResponseMsg("Invalid NIOP FEE::" + fields[16]);
		}
		try {
			return fields[0].matches("[A-F0-9 ]{10}") && // LICENSE PLATE
					fields[1].matches("[0-9 ]{10}") && // TRANSACTION #
					fields[2].trim().length() == 2  && fields[2].trim().matches("^[A-Za-z]+$") &&//STATE
					fields[3].trim().matches("\\d{1,8}\\.\\d{2}")&& // TRANSACTION AMOUNT
				(fields[4].trim().matches("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}[-+]\\d{2}:\\d{2}$") || fields[4].matches("[ ]{25}")) &&
					//fields[4].trim().matches("\\d{2}/\\d{2}/\\d{4}") || fields[4].matches("[ ]{10}") //ENTRY DATE
					//&& (fields[5].matches("^\\d{2}:\\d{2}:\\d{2}[+-]\\d{2}:\\d{2}$") || fields[5].matches("[ ]{14}")) &&//ENTRY DATE TIME
					 fields[5].length() == 22 && // ENTRY PLAZA
							(fields[6].matches("\\d{2}") || fields[6].matches("[ ]{2}")) && // ENTRY LANE
							fields[7].trim().matches("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}[-+]\\d{2}:\\d{2}$") &&
							//fields[8].trim().matches("\\d{2}/\\d{2}/\\d{4}") //ENTRY TRAN DATE
							//&& fields[9].trim().matches("^\\d{2}:\\d{2}:\\d{2}[+-]\\d{2}:\\d{2}$") && //ENTRY TRAN DATE TIME
							 ( fields[8].trim().length() <= 22 && !fields[8].trim().isEmpty()) && // EXIT PLAZA
							(fields[9].trim().matches("\\d{2}") && !fields[9].trim().isEmpty()) && // EXIT LANE
							(fields[10].trim().matches("\\d{1,2}") || fields[10].trim().isEmpty()) && // AXLE COUNT
							fields[11].trim().length() <= 30 && // LP TYPE
							fields[12].trim().matches("\\d{1,8}\\.\\d{2}") && // WR TRAN FEE
							fields[13].trim().matches("[0-2]") && // WR FEE TYPE
							fields[14].trim().matches("\\d{1,8}\\.\\d{2}") && // POST AMT
							responseCode.contains(fields[15].trim()) &&// RESPONSE CODE
						(fields[16].trim().matches("\\d{1,7}\\.\\d{2}") || fields[16].trim().isEmpty()) && // NIOP FEE
						isValid;
		} catch (Exception e) {
			return false;
		}
	}
	 // Check if the record is accepted
    private static boolean isAccepted(String line) {
        String[] fields = line.split(",");
        return fields[15].trim().equals("A");
    }
    // Get the TRAN AMOUNT from DETAIL record
    private static String getDetailAmount(String line) {
        String[] fields = line.split(",");
        System.out.println("Mounika :: fields[3].trim()"+String.format("%08.2f", Double.parseDouble(fields[3].trim())));
        return String.format("%08.2f", Double.parseDouble(fields[3].trim()));
        //return fields[2].trim();
    }

	// Validate TRAILER record
	private boolean validateTrailer(String line, String headerSequence, int detailCount, double totalDetailAmount, int acceptedCount, double acceptedAmount,
			FileValidationParam validateParam) {
		String[] fields = line.split(",");

		log.info(" header Trailer validations detail length" + fields.length);
		if (fields.length == 7) {
			log.info(" Trailer Record length is valid");
		} else {
			addErrorMsg("TRAILER", "length", "Invalid Trailer number of fields" + line);
			validateParam.setResponseMsg("Trailer record length number of fields:::" + fields[1]);
			return false;
		}
		if (fields[1].equals(headerSequence)) {
			log.info(" Trailer Sequeence headerSequence are  matched");
		} else {
			validateParam.setResponseMsg("headerSequence is invalid and not matching with Header::" + fields[1]);
			addErrorMsg("TRAILER", "Trailer Sequence", "Invalid Trailer Sequence - " + fields[1]);
		}
		if (fields[2].trim().matches("\\d{4}/\\d{2}/\\d{2}")) {
			log.info(" Trailer BusinessDay headerBusinessDay are  matched");
		} else {
			validateParam.setResponseMsg("Trailer BusinessDay is invalid ::" + fields[2]);
			addErrorMsg("TRAILER", "TrailerBusinessDay", "Invalid TrailerBusinessDay - " + fields[2]);
		}
		if (fields[3].trim().equals(String.format("%06d", detailCount))) {
			log.info(" Trailer DETAIL COUNT is valid");

		} else {
			validateParam
					.setResponseMsg("Trailer DETAIL COUNT is invalid or not matching with detail count::" + fields[3]);
			addErrorMsg("TRAILER", "DETAIL COUNT", "Invalid DETAIL COUNT - " + fields[3]);
		}

		if ( String.format("%.1f", Double.parseDouble(fields[4].trim())).equals(String.format("%.1f", totalDetailAmount))) {
			log.info(" Trailer Record count is valid");

		} else {
			validateParam
					.setResponseMsg("Trailer DETAIL AMOUNT is invalid or not matching with detail count::" + fields[4]);
			addErrorMsg("TRAILER", "DETAIL AMOUNT", "Invalid DETAIL AMOUNT- " + fields[4]);
		}
		if (fields[5].trim().equals(String.format("%06d", acceptedCount))) {
			log.info(" Trailer ACCEPTED CNT is valid");

		} else {
			validateParam
					.setResponseMsg("Trailer ACCEPTED CNT is invalid or not matching with detail count::" + fields[5]);
			addErrorMsg("TRAILER", "ACCEPTED CNT", "Invalid ACCEPTED CNT- " + fields[5]);
		}
		if (String.format("%.1f", Double.parseDouble(fields[6].trim())).equals(String.format("%.1f", acceptedAmount))) {
			log.info(" Trailer Record count is valid");

		} else {
			validateParam
					.setResponseMsg("Trailer ACCEPTED AMT is invalid or not matching with detail count::" + fields[6]);
			addErrorMsg("TRAILER", "ACCEPTED AMT", "Invalid ACCEPTED AMT- " + fields[6]);
		}


		return fields[0].trim().equals("#TRAILER") && 
				fields[1].trim().equals(headerSequence) && // Sequence number
				fields[2].trim().matches("\\d{4}/\\d{2}/\\d{2}") && // BUSINESS DAY
				fields[3].trim().equals(String.format("%06d", detailCount)) && // DETAIL COUNT
		 String.format("%.1f", Double.parseDouble(fields[4].trim())).equals(String.format("%.1f", totalDetailAmount)) && // DETAIL AMOUNT
         fields[5].trim().equals(String.format("%06d", acceptedCount)) && // ACCEPTED CNT
         String.format("%.1f", Double.parseDouble(fields[6].trim())).equals(String.format("%.1f", acceptedAmount)); //ACCEPTED AMT
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
			log.info("Invalid header record");
			addErrorMsg("HEADER", "Record Type", "Invalid Header Return type " + firstLine);
			validateParam.setResponseMsg("Invalid Header Return type::" + firstLine);
			ctocAckMapper.mapToCtocAckFile(fileName, "01", ackFilePath, shortFromAgency, shortToAgency, "PLATERECON",
					ackDate);
			return false;
		} else if (!lastLine.startsWith("#TRAILER")) {
			log.info("Invalid Trailer record");
			addErrorMsg("TRAILER", "Record Type", "Invalid Trailer Return type " + lastLine);
			validateParam.setResponseMsg("Invalid Trailer Return type::" + lastLine);
			ctocAckMapper.mapToCtocAckFile(fileName, "03", ackFilePath, shortFromAgency, shortToAgency, "PLATERECON",
					ackDate);
			return false;
		}
		return true;
	}



}
