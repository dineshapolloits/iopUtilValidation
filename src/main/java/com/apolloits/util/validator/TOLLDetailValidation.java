package com.apolloits.util.validator;

import static com.apolloits.util.IAGConstants.FILE_RECORD_TYPE;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.apolloits.util.CtocAckFileMapper;
import com.apolloits.util.IAGConstants;
import com.apolloits.util.controller.CtocController;
import com.apolloits.util.modal.ErrorMsgDetail;
import com.apolloits.util.modal.FileValidationParam;
import com.apolloits.util.modal.TOLTemplate;
import com.apolloits.util.utility.CommonUtil;
import com.apolloits.util.writer.TolTemplateValidationExcelWriter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class TOLLDetailValidation {
	@Autowired
	private CtocAckFileMapper ctocAckMapper;
	@Autowired
	@Lazy
	CtocController controller;

	@Autowired
	CommonUtil commonUtil;
	@Autowired
	TolTemplateValidationExcelWriter toltempExcel;
	List<TOLTemplate> tolTempList;
	int invalidRecordCount = 0;

	public boolean tolValidation(FileValidationParam validateParam) {
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
								if(controller.getErrorMsglist().size()== 0 ) {
									// generate ICTXTemplate format excel file.
									log.info("tolTempList size ::" + tolTempList.size());
										String tolTempExcelFileName =validateParam.getOutputFilePath()+File.separator+FilenameUtils.removeExtension(fileName)+"_TOL_TRC_Template.xlsx";
										toltempExcel.createTolTemplateExcel(tolTempList, tolTempExcelFileName, validateParam);
								}
								System.out.println("File is valid.");
								ctocAckMapper.mapToCtocAckFile(fileName, "00",
										validateParam.getOutputFilePath() + File.separator + ackFileName,
										shortFromAgency, shortToAgency, "TOLL", ackDate);
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
					log.error("TOLL file Name validation is failed");
					controller.getErrorMsglist().add(
							new ErrorMsgDetail(FILE_RECORD_TYPE, "File Name", "TOLL file Name validation is failed"));
					ctocAckMapper.mapToCtocAckFile(fileName, "02",
							validateParam.getOutputFilePath() + File.separator + ackFileName, shortFromAgency,
							shortToAgency, "TOLL", ackDate);
					return false;
				}
			} else {
				log.error("TOLL file Name format validation is failed");
				controller.getErrorMsglist()
						.add(new ErrorMsgDetail(FILE_RECORD_TYPE, "File Name", "TOLL file Name format is failed"));
				ctocAckMapper.mapToCtocAckFile(fileName, "02",
						validateParam.getOutputFilePath() + File.separator + fileName.substring(0, 2)
								+ fileName.substring(2, 4) + "_" + ackDate + "_" + fileName.substring(0, 2)
								+ fileName.substring(2, 4) + "_" + fileName.substring(5, 13) + "T"
								+ fileName.substring(14, 20) + IAGConstants.ACK_FILE_EXTENSION// validateParam.getToAgency()
																								// + "_" +
																								// fileName.replace(".",
																								// "_") +
																								// IAGConstants.ACK_FILE_EXTENSION;
						, fileName.substring(0, 2), fileName.substring(2, 4), "TOLL", ackDate);
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
		double totalAmount = 0;

		while ((line = reader.readLine()) != null) {
			// line = line.trim();
			if (line.startsWith("#HEADER")) {
				if (!validateHeader(line, validateParam, fileName)) {
					log.info("Invalid header record");
					ctocAckMapper.mapToCtocAckFile(fileName, "01", ackFilePath, shortFromAgency, shortToAgency, "TOLL",
							ackDate);
					return false;
				} else if (validateParam.getValidateType().equals("header")) {
					ctocAckMapper.mapToCtocAckFile(fileName, "01", ackFilePath, shortFromAgency, shortToAgency, "TOLL",
							ackDate);
					log.info("Only file name and header validation");
					return true;
				}
				String[] fields = line.split(",");
				headerSequence = fields[2].trim(); // Sequence # from header
				// headerBusinessDay = fields[4].trim(); // Business day from header
				tolTempList = new LinkedList<TOLTemplate>();
			}
			// Validate Detail Records
			// else if(line.matches("[0-9A-Fa-f]{8},\\s*\\d{7},[A],.[A-Z],.*")) {
			// else if (line.matches("\\s*[0-9A-Fa-f]{8},\\s*\\d{7},[A],.[A-Z],.*")) {
			
			else if (!line.startsWith("#HEADER") && !line.startsWith("#TRAILER")) {
				log.info("INSIDE detail record");
				if (!validateDetail(line, validateParam)) {
					log.info("Invalid detail record");
					ctocAckMapper.mapToCtocAckFile(fileName, "03", ackFilePath, shortFromAgency, shortToAgency, "TOLL",
							ackDate);
					System.out.println("Invalid detail record in DETAIL");
					return false;
				}
				addTolTemplate(line,headerSequence); //This method add TOLtemplate value in list
				String[] fields = line.split(",");
				totalAmount += Double.parseDouble(fields[2].trim());
				detailCount++; // Increment the detail record count
			} else if (line.startsWith("#TRAILER")) {
				if (!validateTrailer(line, headerSequence, detailCount, totalAmount, validateParam)) {
					log.info("Invalid Trailer record");
					ctocAckMapper.mapToCtocAckFile(fileName, "03", ackFilePath, shortFromAgency, shortToAgency, "TOLL",
							ackDate);

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
		boolean isValid = true;
		if (fields.length == 8) {
			log.info(" Header validations length");
		} else {
			addErrorMsg("HEADER", "length", "Invalid Header number of fields" + line);
			validateParam.setResponseMsg("Invalid Header number of fields::" + line);
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

		if (fields[1].trim().equals("TOLL")) {
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
			if (CommonUtil.validateUTCDateTime(fields[6])) {
				
				if (!CommonUtil.convertUTCToDate(fields[6].trim()).equals(fileName.substring(5, 20)))
				{
					System.out.println("Invalid CREATE DATE: " + fields[5]);
					isValid = false;
					addErrorMsg("HEADER", "CREATE DATE", "Invalid CREATE DATE - Not matching with File Date " + fields[6]);
					validateParam.setResponseMsg("Invalid CREATE DATE - Not matching with File Date::" + fields[6]);
				}
				
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

		return fields[0].trim().equals("#HEADER") && fields[1].trim().equals("TOLL")
				&& fields[2].trim().matches("\\d{6}") && // SEQUENCE #
				fields[3].trim().matches("\\d{4}/\\d{2}/\\d{2}") && // BUSINESS DAY
				fields[4].trim().length() == 2 && // SOURCE
				fields[5].trim().length() == 2 && // DESTINATION
				// fields[6].trim().matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}-\\d{2}:\\d{2}")
				// && // CREATE DATE
				fields[6].trim().matches("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}[-+]\\d{2}:\\d{2}$")
			//	&& fields[7].matches("^\\d{2}:\\d{2}:\\d{2}[+-]\\d{2}:\\d{2}$") 
				&& (fields[7].length() == 10 && fields[7].trim().equals("REV A2.1.1"))&&
				isValid; // VERSION
	}

	// Validate DETAIL record
	private boolean validateDetail(String line, FileValidationParam validateParam) {
		String[] fields = line.split(",");
		boolean isValid = true;
		if (fields.length == 16) {
			log.info(" Detail validations");
		} else {
			addErrorMsg("DETAIL", "length", "Invalid Detail number of fields" + line);
			validateParam.setResponseMsg("Invalid Detail number of fields::" + line);
			return false;
		}
		if (fields[0].matches("[A-F0-9 ]{10}")) {
			log.info(" header validations TAG ID");
		} else {
			addErrorMsg("DETAIL", "TAG ID", "Invalid TAG ID- " + fields[0]);
			validateParam.setResponseMsg("Invalid TAG ID::" + fields[0]);
		}
		if (fields[1].trim().matches("\\d{10}")) {
			log.info(" header validations TRAN");
		} else {
			addErrorMsg("DETAIL", "TRAN", "Invalid TRAN- " + fields[1]);
			validateParam.setResponseMsg("Invalid TRAN::" + fields[1]);
		}
		if (fields[2].trim().matches("\\d{1,7}\\.\\d{2}")) {
			log.info(" header validations TRANSACTION AMOUNT");
		} else {
			addErrorMsg("DETAIL", "TRANSACTION AMOUNT", "Invalid TRANSACTION AMOUNT- " + fields[2]);
			validateParam.setResponseMsg("Invalid TRANSACTION AMOUNT::" + fields[2]);
		}
		if (fields[3].trim().matches("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}[-+]\\d{2}:\\d{2}$") || fields[3].matches("[ ]{25}")) {
			if (CommonUtil.validateUTCDateTime(fields[3])) {
				
			log.info(" header validations ENTRY TRAN DATE");
		} else {
			isValid= false;
			addErrorMsg("DETAIL", "ENTRY TRAN DATE", "Invalid ENTRY TRAN DATE " + fields[3]);
			validateParam.setResponseMsg("Invalid ENTRY DATE::" + fields[3]);
		}}else {
			addErrorMsg("DETAIL", "ENTRY TRAN DATE", "Invalid ENTRY TRAN DATE" + fields[3]);
			validateParam.setResponseMsg("Invalid ENTRY DATE::" + fields[3]);
		}

		/*
		 * if (fields[4].matches("^\\d{2}:\\d{2}:\\d{2}[+-]\\d{2}:\\d{2}$") ||
		 * fields[4].matches("[ ]{14}")) {
		 * log.info(" header validations ENTRY DATE Time"); } else {
		 * addErrorMsg("DETAIL", "ENTRY TRAN DATE", "Invalid ENTRY TRAN DATE Time- " +
		 * fields[4]); validateParam.setResponseMsg("Invalid ENTRY TRAN DATE Time::" +
		 * fields[4]); }
		 */
		if (fields[4].length() <= 22) {
			log.info(" header validations ENTRY PLAZA");
		} else {
			addErrorMsg("DETAIL", "ENTRY PLAZA", "Invalid ENTRY PLAZA- " + fields[4]);
			validateParam.setResponseMsg("Invalid ENTRY PLAZA::" + fields[4]);
		}
		if (fields[5].matches("\\d{2}") || fields[5].matches("[ ]{2}")) {
			log.info(" header validations ENTRY LANE");
		} else {
			addErrorMsg("DETAIL", "ENTRY LANE", "Invalid ENTRY LANE- " + fields[5]);
			validateParam.setResponseMsg("Invalid ENTRY LANE::" + fields[5]);
		}
		if (fields[6].trim().matches("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}[-+]\\d{2}:\\d{2}$")) {
			if (CommonUtil.validateUTCDateTime(fields[6])) {
			log.info(" header validations EXIT DATE");
		} else {
			isValid= false;
			addErrorMsg("DETAIL", "ENTRY TRAN DATE", "Invalid ENTRY TRAN DATE " + fields[6]);
			validateParam.setResponseMsg("Invalid ENTRY DATE::" + fields[6]);
		}} else {
			addErrorMsg("DETAIL", "EXIT DATE", "Invalid EXIT DATE- " + fields[6]);
			validateParam.setResponseMsg("Invalid EXIT DATE::" + fields[6]);
		}
		/*
		 * if (fields[8].trim().matches("^\\d{2}:\\d{2}:\\d{2}[+-]\\d{2}:\\d{2}$")) {
		 * log.info(" header validations EXIT DATE Time"); } else {
		 * addErrorMsg("DETAIL", "EXIT DATE", "Invalid EXIT DATE Time- " + fields[8]);
		 * validateParam.setResponseMsg("Invalid EXIT DATE Time::" + fields[8]); }
		 */

		if ( fields[7].trim().length() <= 22 && !fields[7].trim().isEmpty()) {
			log.info(" header validations EXIT PLAZA");
		} else {
			addErrorMsg("DETAIL", "EXIT PLAZA", "Invalid EXIT PLAZA- " + fields[7]);
			validateParam.setResponseMsg("Invalid EXIT PLAZA::" + fields[7]);
		}
		if (fields[8].trim().matches("\\d{2}") && !fields[8].trim().isEmpty()) {
			log.info(" header validations EXIT LANE");
		} else {
			addErrorMsg("DETAIL", "EXIT LANE", "Invalid EXIT LANE- " + fields[8]);
			validateParam.setResponseMsg("Invalid EXIT LANE::" + fields[8]);
		}
		if (fields[9].trim().matches("\\d{1,2}") || fields[9].trim().isEmpty()) {
			log.info(" header validations AXLE COUNT");
		} else {
			addErrorMsg("DETAIL", "AXLE COUNT", "Invalid AXLE COUNT- " + fields[9]);
			validateParam.setResponseMsg("Invalid AXLE COUNT::" + fields[9]);
		}
		if (fields[10].trim().matches("[0-3]")) {
			log.info(" header validations OCCUPANCY");
		} else {
			addErrorMsg("DETAIL", "OCCUPANCY", "Invalid OCCUPANCY- " + fields[10]);
			validateParam.setResponseMsg("Invalid OCCUPANCY::" + fields[10]);
		}
		if (fields[11].trim().matches("[0-3]")) {
			log.info(" header validations PROTOCOL TYPE");
		} else {
			addErrorMsg("DETAIL", "PROTOCOL TYPE", "Invalid PROTOCOL TYPE- " + fields[11]);
			validateParam.setResponseMsg("Invalid PROTOCOL TYPE::" + fields[11]);
		}
		if (fields[12].trim().matches("[0-1]")) {
			log.info(" header validations VEHICLE TYPE");
		} else {
			addErrorMsg("DETAIL", "VEHICLE TYPE", "Invalid VEHICLE TYPE- " + fields[12]);
			validateParam.setResponseMsg("Invalid VEHICLE TYPE::" + fields[12]);
		}
		if (fields[13].trim().matches("\\d{1,7}\\.\\d{2}")) {
			log.info(" header validations WR TRAN FEE");
		} else {
			addErrorMsg("DETAIL", "WR TRAN FEE", "Invalid WR TRAN FEE- " + fields[13]);
			validateParam.setResponseMsg("Invalid WR TRAN FEE::" + fields[13]);
		}
		if (fields[14].trim().matches("[0-2]")) {
			log.info(" header validations WR FEE TYPE");
		} else {
			log.info(" INVALID validations WR FEE TYPE");
			addErrorMsg("DETAIL", "WR FEE TYPE", "Invalid WR FEE TYPE- " + fields[14]);
			validateParam.setResponseMsg("Invalid WR FEE TYPE::" + fields[14]);
		}
		//int guaranteeValue = Integer.parseInt(fields[15].trim());        
		//if (guaranteeValue == 0 || guaranteeValue == 1 || guaranteeValue == 2) {   
			if(fields[15].trim().equals("0") || fields[15].trim().equals("1") || fields[15].trim().equals("2")) {
			log.info(" Detail validations Guarantee");
		} else {
			addErrorMsg("DETAIL", "GUARANTEE", "Invalid GUARANTEE- " + fields[15]);
			validateParam.setResponseMsg("Invalid GUARANTEE::" + fields[15]);
		}
		try {
			return fields[0].matches("[A-F0-9 ]{10}") && // TAG ID
					fields[1].trim().matches("\\d{10}") && // TRANSACTION #
					fields[2].trim().matches("\\d{1,7}\\.\\d{2}") && // TRANSACTION AMOUNT
					// fields[3].trim().matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}-\\d{2}:\\d{2}")
					// && // ENTRY DATE
					(fields[3].trim().matches("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}[-+]\\d{2}:\\d{2}$") || fields[3].matches("[ ]{25}"))
					//&& (fields[4].matches("^\\d{2}:\\d{2}:\\d{2}[+-]\\d{2}:\\d{2}$") || fields[4].matches("[ ]{14}") )
					&& fields[4].length() <= 22 && // ENTRY PLAZA
							(fields[5].matches("\\d{2}") || fields[5].matches("[ ]{2}")) && // ENTRY LANE
					// fields[6].trim().matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}-\\d{2}:\\d{2}")
					// && // EXIT DATE
							//fields[7].trim().matches("\\d{2}/\\d{2}/\\d{4}")
							//&& fields[8].trim().matches("^\\d{2}:\\d{2}:\\d{2}[+-]\\d{2}:\\d{2}$")
							fields[6].trim().matches("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}[-+]\\d{2}:\\d{2}$")
							&& ( fields[7].trim().length() <= 22 && !fields[7].trim().isEmpty())&& // EXIT PLAZA
							(fields[8].trim().matches("\\d{2}") && !fields[8].trim().isEmpty()) && // EXIT LANE
							(fields[9].trim().matches("\\d{1,2}") || fields[9].trim().isEmpty()) && // AXLE COUNT
							fields[10].trim().matches("[0-3]") && // OCCUPANCY
							fields[11].trim().matches("[0-3]") && // PROTOCOL TYPE
							fields[12].trim().matches("[0-1]") && // VEHICLE TYPE
							fields[13].trim().matches("\\d{1,7}\\.\\d{2}") && // WR TRAN FEE
							fields[14].trim().matches("[0-2]") && // WR FEE TYPE
							(fields[15].trim().equals("0") || fields[15].trim().equals("1") || fields[15].trim().equals("2")) && //GUARANTEE
							isValid; 
			
		} catch (Exception e) {
			return false;
		}
	}

	// Validate TRAILER record
	private boolean validateTrailer(String line, String headerSequence, int detailCount, double totalAmount,
			FileValidationParam validateParam) {
		String[] fields = line.split(",");

		log.info(" header Trailer validations detail count" + fields.length);
		if (fields.length == 5) {
			log.info(" Trailer Record length is valid");
		} else {
			addErrorMsg("TRAILER", "length", "Invalid Trailer number of fields" + line);
			validateParam.setResponseMsg("Trailer record length number of fields:::" + fields[1]);
			return false;
		}
		if (fields[1].equals(headerSequence)) {
			log.info(" Trailer Sequeence headerSequence are  matched");
		} else {
			validateParam.setResponseMsg("Trailer Sequence is invalid and not matching with Header::" + fields[1]);
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
		if (fields[4].trim().matches("\\d{1,7}\\.\\d{2}")
				&& Math.abs(Double.parseDouble(fields[4].trim()) - totalAmount) < 0.01) {
			log.info(" Trailer Record count is valid");

		} else {
			validateParam
					.setResponseMsg("Trailer DETAIL AMOUNT is invalid or not matching with detail count::" + fields[4]);
			addErrorMsg("TRAILER", "DETAIL AMOUNT", "Invalid DETAIL AMOUNT- " + fields[4]);
		}

		return fields[0].trim().equals("#TRAILER") && fields[1].trim().equals(headerSequence) && // Sequence number
				fields[2].trim().matches("\\d{4}/\\d{2}/\\d{2}") && // BUSINESS DAY
				fields[3].trim().equals(String.format("%06d", detailCount)) && // DETAIL COUNT
				fields[4].trim().matches("\\d{1,7}\\.\\d{2}") && // DETAIL AMOUNT
				Math.abs(Double.parseDouble(fields[4].trim()) - totalAmount) < 0.01; // Check total amount matches
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
			ctocAckMapper.mapToCtocAckFile(fileName, "01", ackFilePath, shortFromAgency, shortToAgency, "TOLL",
					ackDate);
			return false;
		} else if (!lastLine.startsWith("#TRAILER")) {
			log.info("Invalid Trailer record");
			addErrorMsg("TRAILER", "Record Type", "Invalid Trailer Return type " + lastLine);
			validateParam.setResponseMsg("Invalid Trailer Return type::" + lastLine);
			ctocAckMapper.mapToCtocAckFile(fileName, "03", ackFilePath, shortFromAgency, shortToAgency, "TOLL",
					ackDate);
			return false;
		}
		return true;
	}
	private void addTolTemplate(String line, String sequence) {
		if (controller.getErrorMsglist().size() == 0) {
			String[] fields = line.split(",");
			TOLTemplate tolTemplate = new TOLTemplate();
			tolTemplate.setSequence(sequence);
			tolTemplate.setTagID(fields[0]);
			tolTemplate.setTran(fields[1]);
			tolTemplate.setTranAmount(fields[2]);
			tolTemplate.setEntryTranDate(fields[3]);
			tolTemplate.setEntryPlaza(fields[4]);
			tolTemplate.setEntryLane(fields[5]);
			tolTemplate.setExitTranDate(fields[6]);
			tolTemplate.setExitPlaza(fields[7]);
			tolTemplate.setExitLane(fields[8]);
			tolTemplate.setAxleCount(fields[9]);
			tolTemplate.setOccupancy(fields[10]);
			tolTemplate.setProtocolType(fields[11]);
			tolTemplate.setVehicleType(fields[12]);
			tolTemplate.setWrTranFee(fields[13]);
			tolTemplate.setWrFeeType(fields[14]);
			tolTemplate.setGuarantee(fields[15]);
	
	
			tolTempList.add(tolTemplate);
		}
	}

}
