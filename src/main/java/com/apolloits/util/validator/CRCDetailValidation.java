package com.apolloits.util.validator;

import static com.apolloits.util.IAGConstants.FILE_RECORD_TYPE;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
import com.apolloits.util.modal.CORTemplate;
import com.apolloits.util.modal.ErrorMsgDetail;
import com.apolloits.util.modal.FileValidationParam;
import com.apolloits.util.utility.CommonUtil;
import com.apolloits.util.writer.CorTemplateValidationExcelWriter;

import lombok.extern.slf4j.Slf4j;

@Slf4j

@Component
public class CRCDetailValidation {

	  
	  @Autowired 
	  private CtocAckFileMapper ctocAckMapper;
	  
	  @Autowired
	  @Lazy 
	  CtocController controller;
	  
	  @Autowired 
	  CommonUtil commonUtil;
	  @Autowired
		CorTemplateValidationExcelWriter toltempExcel;
		List<CORTemplate> corTempList;
	  public boolean crcValidation(FileValidationParam validateParam) {

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
				// Regular expression to match the file name format: PREFIX_YYYYMMDD_HHMMSS.crc
				String regex = "^([a-z]{4})_(\\d{8})T(\\d{6})_([a-z]{4})_(\\d{8})T(\\d{6})\\.([a-z]{3})$";
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
					ackFileName = shortToAgency + shortFromAgency + "_" + ackDate + "_" + fileName.substring(0, 20)
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
											shortFromAgency, shortToAgency, "CORRECON", ackDate);
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
						log.error("CRC file Name validation is failed");
						controller.getErrorMsglist().add(
								new ErrorMsgDetail(FILE_RECORD_TYPE, "File Name", "CRC file Name validation is failed"));
						ctocAckMapper.mapToCtocAckFile(fileName, "02",
								validateParam.getOutputFilePath() + File.separator + ackFileName, shortFromAgency,
								shortToAgency, "CORRECON", ackDate);
						return false;
					}
				} else {
					log.error("CRC file Name format validation is failed");
					controller.getErrorMsglist()
							.add(new ErrorMsgDetail(FILE_RECORD_TYPE, "File Name", "CRC file Name format is failed"));
					ctocAckMapper.mapToCtocAckFile(fileName, "02",
							validateParam.getOutputFilePath() + File.separator + fileName.substring(0, 2)
									+ fileName.substring(2, 4) + "_" + ackDate + "_" + fileName.substring(0, 2)
									+ fileName.substring(2, 4) + "_" + fileName.substring(5, 13) + "T"
									+ fileName.substring(14, 20) + IAGConstants.ACK_FILE_EXTENSION// validateParam.getToAgency()
																									// + "_" +
																									// fileName.replace(".",
																									// "_") +
																									// IAGConstants.ACK_FILE_EXTENSION;
							, fileName.substring(0, 2), fileName.substring(2, 4), "CORRECON", ackDate);
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
			int acceptedCount = 0;
	        double acceptedAmount = 0.0;
	        
			while ((line = reader.readLine()) != null) {
				// line = line.trim();
				if (line.startsWith("#HEADER")) {
					if (!validateHeader(line, validateParam, fileName)) {
						log.info("Invalid header record");
						ctocAckMapper.mapToCtocAckFile(fileName, "01", ackFilePath, shortFromAgency, shortToAgency, "CORRECON",
								ackDate);
						return false;
					} else if (validateParam.getValidateType().equals("header")) {
						ctocAckMapper.mapToCtocAckFile(fileName, "01", ackFilePath, shortFromAgency, shortToAgency, "CORRECON",
								ackDate);
						log.info("Only file name and header validation");
						return true;
					}
					String[] fields = line.split(",");
					headerSequence = fields[2].trim(); // Sequence # from header
					// headerBusinessDay = fields[4].trim(); // Business day from header
				}
			
				
				else if (!line.startsWith("#HEADER") && !line.startsWith("#TRAILER")) {
					log.info("INSIDE detail record");
					if (!validateDetail(line, validateParam)) {
						log.info("Invalid detail record");
						ctocAckMapper.mapToCtocAckFile(fileName, "03", ackFilePath, shortFromAgency, shortToAgency, "CORRECON",
								ackDate);
						System.out.println("Invalid detail record in DETAIL");
						return false;
					}
					double detailAmount = Double.parseDouble(getDetailAmount(line));
					 totalAmount += detailAmount;
					detailCount++; // Increment the detail record count
					if (isAccepted(line)) {
	                    acceptedCount++;
	                    acceptedAmount += detailAmount;
					} 
					/*String[] fields = line.split(",");
					totalAmount += Double.parseDouble(fields[28].trim());
					detailCount++; // Increment the detail record count */
				} else if (line.startsWith("#TRAILER")) {
					if (!validateTrailer(line, headerSequence, detailCount, totalAmount,acceptedCount, acceptedAmount, validateParam)) {
						log.info("Invalid Trailer record");
						ctocAckMapper.mapToCtocAckFile(fileName, "03", ackFilePath, shortFromAgency, shortToAgency, "CORRECON",
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

			if (fields[1].trim().equals("CORRECON")) {
				log.info(" header validations File type");
			} else {
				addErrorMsg("HEADER", "File type", "Invalid File type- " + fields[1]);
				validateParam.setResponseMsg("Invalid File type::" + fields[1]);
			}
			if (fields[2].matches("\\d{6}")) {
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

			return fields[0].trim().equals("#HEADER") && fields[1].trim().equals("CORRECON")
					&& fields[2].matches("\\d{6}") && // SEQUENCE #
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
			if (fields.length == 44) {
				log.info(" Detail validations");
			} else {
				addErrorMsg("DETAIL", "length", "Invalid Detail number of fields" + line);
				validateParam.setResponseMsg("Invalid Detail number of fields::" + line);
				return false;
			}
			if (fields[0].trim().matches("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}[-+]\\d{2}:\\d{2}$") || fields[0].matches("[ ]{25}")) {
				if (CommonUtil.validateUTCDateTime(fields[0])) {
			log.info(" Detail validations CORRECTION DATE");
				} else {
			isValid= false;
			addErrorMsg("DETAIL", "CORRECTION DATE", "Invalid CORRECTION DATE " + fields[0]);
			validateParam.setResponseMsg("Invalid CORRECTION DATE::" + fields[0]);
				}}else {
					isValid= false;
			addErrorMsg("DETAIL", "CORRECTION DATE", "Invalid CORRECTION DATE" + fields[0]);
			validateParam.setResponseMsg("Invalid CORRECTION DATE::" + fields[0]);
				}
			if (fields[1].matches("[CILT ]")) {
				log.info(" Detail validations CORRECTION REASON");
			} else {
				isValid= false;
				addErrorMsg("DETAIL", "CORRECTION REASON", "Invalid CORRECTION REASON- " + fields[1]);
				validateParam.setResponseMsg("Invalid CORRECTION REASON::" + fields[1]);
			}
			if (fields[2].matches("[RI ]")) {
				log.info(" Detail validations RESUBMIT REASON");
			} else {
				isValid= false;
				addErrorMsg("DETAIL", "RESUBMIT REASON", "Invalid RESUBMIT REASON- " + fields[2]);
				validateParam.setResponseMsg("Invalid RESUBMIT REASON::" + fields[2]);
			}
			
			if (fields[3].trim().matches("\\d{1,3}")) {
				log.info(" Detail validations CORRECTION COUNT");
			} else {
				isValid= false;
				addErrorMsg("DETAIL", "CORRECTION COUNT", "Invalid CORRECTION COUNT- " + fields[3]);
				validateParam.setResponseMsg("Invalid CORRECTION COUNT::" + fields[3]);
			}
		
			if (fields[4].trim().matches("\\d{1,3}")) {
				log.info(" Detail validations RESUBMIT COUNT");
			} else {
				isValid= false;
				addErrorMsg("DETAIL", "RESUBMIT COUNT", "Invalid RESUBMIT COUNT- " + fields[4]);
				validateParam.setResponseMsg("Invalid RESUBMIT COUNT::" + fields[4]);
			}
			if (fields[5].trim().matches("\\d{6}") && fields[5].length()==6) {
				log.info(" Detail validations HOME AGENCY SEQUENCE #");
			} else {
				isValid= false;
				addErrorMsg("DETAIL", "HOME AGENCY SEQUENCE #", "Invalid HOME AGENCY SEQUENCE #- " + fields[5]);
				validateParam.setResponseMsg("Invalid HOME AGENCY SEQUENCE #::" + fields[5]);
			}
			if (fields[6].matches("[A-F0-9 ]{10}") || fields[6].trim().isEmpty()) {
				log.info(" Detail validations ORIGINAL TAG ID");
			} else {
				isValid= false;
				addErrorMsg("DETAIL", "ORIGINAL TAG ID", "Invalid ORIGINAL TAG ID- " + fields[6]);
				validateParam.setResponseMsg("Invalid ORIGINAL TAG ID::" + fields[6]);
			}
			if (fields[7].matches("[A-F0-9 ]{10}")) {
				log.info(" Detail validations ORIGINAL LICENSE PLATE");
			} else {
				isValid= false;
				addErrorMsg("DETAIL", "ORIGINAL LICENSE PLATE", "Invalid ORIGINAL LICENSE PLATE- " + fields[7]);
				validateParam.setResponseMsg("Invalid ORIGINAL LICENSE PLATE::" + fields[7]);
			}
		
			if (fields[8].length() == 2 || (fields[8].length() == 2 && fields[8].trim().isEmpty()) ) {
				log.info(" Detail validations ORIGINAL STATE");
			} else {
				isValid= false;
				addErrorMsg("DETAIL", "ORIGINAL STATE", "Invalid ORIGINAL STATE- " + fields[8]);
				validateParam.setResponseMsg("Invalid ORIGINAL STATE::" + fields[8]);
			}
			if (fields[9].trim().matches("\\d{10}")) {
				log.info(" Detail validations ORIGINAL TRAN #");
			} else {
				isValid= false;
				addErrorMsg("DETAIL", "ORIGINAL TRAN #", "Invalid ORIGINAL TRAN #- " + fields[9]);
				validateParam.setResponseMsg("Invalid ORIGINAL TRAN #::" + fields[9]);
			}
			if (fields[10].trim().matches("\\d{1,7}\\.\\d{2}")) {
				log.info(" Detail validations ORIGINAL TRAN AMOUNT");
			} else {
				isValid= false;
				addErrorMsg("DETAIL", "ORIGINAL TRAN AMOUNT", "Invalid ORIGINAL TRAN AMOUNT- " + fields[10]);
				validateParam.setResponseMsg("Invalid ORIGINAL TRAN AMOUNT::" + fields[10]);
			}
			
			if (fields[11].trim().matches("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}[-+]\\d{2}:\\d{2}$") || fields[11].matches("[ ]{25}")) {
				if (CommonUtil.validateUTCDateTime(fields[11])) {
					
				log.info(" Detail validations  ORIGINAL ENTRY TRAN DATE");
			} else {
				isValid= false;
				addErrorMsg("DETAIL", " ORIGINAL ENTRY TRAN DATE", "Invalid  ORIGINAL ENTRY TRAN DATE " + fields[11]);
				validateParam.setResponseMsg("Invalid  ORIGINAL ENTRY TRAN DATE::" + fields[11]);
			}}else {
				isValid= false;
				addErrorMsg("DETAIL", " ORIGINAL ENTRY TRAN DATE", "Invalid  ORIGINAL ENTRY TRAN DATE" + fields[11]);
				validateParam.setResponseMsg("Invalid  ORIGINAL ENTRY TRAN DATE::" + fields[11]);
			}
			if (fields[12].length() <= 22) {
				log.info(" Detail validations ORIGINAL ENTRY PLAZA");
			} else {
				isValid= false;
				addErrorMsg("DETAIL", "ORIGINAL ENTRY PLAZA", "Invalid ORIGINAL ENTRY PLAZA- " + fields[12]);
				validateParam.setResponseMsg("Invalid ORIGINAL ENTRY PLAZA::" + fields[12]);
			}
			if (fields[13].matches("\\d{2}") || fields[13].matches("[ ]{2}")) {
				log.info(" Detail validations ORIGINAL ENTRY LANE");
			} else {
				isValid= false;
				addErrorMsg("DETAIL", "ORIGINAL ENTRY LANE", "Invalid ORIGINAL ENTRY LANE- " + fields[13]);
				validateParam.setResponseMsg("Invalid ORIGINAL ENTRY LANE::" + fields[13]);
			}
			if (fields[14].trim().matches("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}[-+]\\d{2}:\\d{2}$")) {
				log.info(" Detail validations ORIGINAL EXIT DATE");
			} else {
				isValid= false;
				addErrorMsg("DETAIL", "ORIGINAL EXIT DATE", "Invalid ORIGINAL EXIT DATE- " + fields[14]);
				validateParam.setResponseMsg("Invalid ORIGINAL EXIT DATE::" + fields[14]);
			}
			if ( fields[15].trim().length() <= 22 && !fields[15].trim().isEmpty()) {
				log.info(" Detail validations ORIGINAL EXIT PLAZA");
			} else {
				isValid= false;
				addErrorMsg("DETAIL", "ORIGINAL EXIT PLAZA", "Invalid ORIGINAL EXIT PLAZA- " + fields[15]);
				validateParam.setResponseMsg("Invalid ORIGINAL EXIT PLAZA::" + fields[15]);
			}
			if (fields[16].trim().matches("\\d{2}") && !fields[16].trim().isEmpty()) {
				log.info(" Detail validations ORIGINAL EXIT LANE");
			} else {
				isValid= false;
				addErrorMsg("DETAIL", "ORIGINAL EXIT LANE", "Invalid ORIGINAL EXIT LANE- " + fields[16]);
				validateParam.setResponseMsg("Invalid ORIGINAL EXIT LANE::" + fields[16]);
			}
			if (fields[17].trim().matches("\\d{1,2}") || fields[17].trim().isEmpty()) {
				log.info(" Detail validations ORIGINAL AXLE COUNT");
			} else {
				isValid= false;
				addErrorMsg("DETAIL", "ORIGINAL AXLE COUNT", "Invalid ORIGINAL AXLE COUNT- " + fields[17]);
				validateParam.setResponseMsg("Invalid ORIGINAL AXLE COUNT::" + fields[17]);
			}
			if (fields[18].trim().matches("[0-3]") || fields[18].trim().isEmpty()) {
				log.info(" Detail validations ORIGINAL OCCUPANCY");
			} else {
				isValid= false;
				addErrorMsg("DETAIL", "ORIGINAL OCCUPANCY", "Invalid ORIGINAL OCCUPANCY- " + fields[18]);
				validateParam.setResponseMsg("Invalid ORIGINAL OCCUPANCY::" + fields[18]);
			}
			if (fields[19].trim().matches("[0-3]") || fields[19].trim().isEmpty()) {
				log.info(" Detail validations ORIGINAL PROTOCOL TYPE");
			} else {
				isValid= false;
				addErrorMsg("DETAIL", "ORIGINAL PROTOCOL TYPE", "Invalid ORIGINAL PROTOCOL TYPE- " + fields[19]);
				validateParam.setResponseMsg("Invalid ORIGINAL PROTOCOL TYPE::" + fields[19]);
			}
			if (fields[20].trim().matches("[0-1]")) {
				log.info(" Detail validations ORIGINAL VEHICLE TYPE");
			} else {
				isValid= false;
				addErrorMsg("DETAIL", "ORIGINAL VEHICLE TYPE", "Invalid ORIGINAL VEHICLE TYPE- " + fields[20]);
				validateParam.setResponseMsg("Invalid ORIGINAL VEHICLE TYPE::" + fields[20]);
			}
			if (fields[21].length() == 30 || fields[21].trim().isEmpty()) {
				log.info(" Detail validations ORIGINAL LP TYPE");
			} else {
				isValid= false;
				addErrorMsg("DETAIL", "ORIGINAL LP TYPE", "Invalid ORIGINAL LP TYPE- " + fields[21]);
				validateParam.setResponseMsg("Invalid ORIGINAL LP TYPE::" + fields[21]);
			}

			if (fields[22].trim().matches("\\d{1,8}\\.\\d{2}")) {
				log.info(" Detail validations ORIGINAL TRAN FEE");
			} else {
				isValid= false;
				addErrorMsg("DETAIL", "ORIGINAL TRAN FEE", "Invalid ORIGINAL TRAN FEE- " + fields[22]);
				validateParam.setResponseMsg("Invalid ORIGINAL TRAN FEE::" + fields[22]);
			}
			if (fields[23].trim().matches("[0-2]")) {
				log.info(" Detail validations ORIGINAL TRAN FEE TYPE");
			} else {
				isValid= false;
				log.info(" INVALID validations ORIGINAL TRAN FEE TYPE");
				addErrorMsg("DETAIL", "ORIGINAL TRAN FEE TYPE", "Invalid ORIGINAL TRAN FEE TYPE- " + fields[23]);
				validateParam.setResponseMsg("Invalid ORIGINAL TRAN FEE TYPE::" + fields[23]);
			}
			if (fields[24].matches("[A-F0-9 ]{10}") || fields[24].trim().isEmpty()) {
				log.info(" Detail validations CORR TAG ID");
			} else {
				isValid= false;
				addErrorMsg("DETAIL", "CORR TAG ID", "Invalid CORR TAG ID- " + fields[24]);
				validateParam.setResponseMsg("Invalid CORR TAG ID::" + fields[24]);
			}
			if (fields[25].matches("[A-F0-9 ]{10}")) {
				log.info(" Detail validations CORR LICENSE PLATE");
			} else {
				isValid= false;
				addErrorMsg("DETAIL", "CORR LICENSE PLATE", "Invalid CORR LICENSE PLATE- " + fields[25]);
				validateParam.setResponseMsg("Invalid CORR LICENSE PLATE::" + fields[25]);
			}
		
			if (fields[26].length() == 2 || (fields[26].length() == 2 && fields[26].trim().isEmpty()) ) {
				log.info(" Detail validations CORR STATE");
			} else {
				isValid= false;
				addErrorMsg("DETAIL", "CORR STATE", "Invalid CORR STATE- " + fields[26]);
				validateParam.setResponseMsg("Invalid CORR STATE::" + fields[26]);
			}
			if (fields[27].trim().matches("\\d{10}")) {
				log.info(" Detail validations CORR TRAN #");
			} else {
				isValid= false;
				addErrorMsg("DETAIL", "CORR TRAN #", "Invalid CORR TRAN #- " + fields[27]);
				validateParam.setResponseMsg("Invalid CORR TRAN #::" + fields[27]);
			}
			if (fields[28].trim().matches("\\d{1,7}\\.\\d{2}")) {
				log.info(" Detail validations CORR TRAN AMOUNT");
			} else {
				isValid= false;
				addErrorMsg("DETAIL", "CORR TRAN AMOUNT", "Invalid CORR TRAN AMOUNT- " + fields[28]);
				validateParam.setResponseMsg("Invalid CORR TRAN AMOUNT::" + fields[28]);
			}
			
			if (fields[29].trim().matches("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}[-+]\\d{2}:\\d{2}$") || fields[29].matches("[ ]{25}")) {
				if (CommonUtil.validateUTCDateTime(fields[29])) {
					
				log.info(" Detail validations  CORR ENTRY TRAN DATE");
			} else {
				isValid= false;
				addErrorMsg("DETAIL", " CORR ENTRY TRAN DATE", "Invalid  CORR ENTRY TRAN DATE " + fields[29]);
				validateParam.setResponseMsg("Invalid  CORR ENTRY TRAN DATE::" + fields[29]);
			}}else {
				isValid= false;
				addErrorMsg("DETAIL", " CORR ENTRY TRAN DATE", "Invalid  CORR ENTRY TRAN DATE" + fields[29]);
				validateParam.setResponseMsg("Invalid  CORR ENTRY TRAN DATE::" + fields[29]);
			}
			if (fields[30].length() <= 22) {
				log.info(" Detail validations CORR ENTRY PLAZA");
			} else {
				isValid= false;
				addErrorMsg("DETAIL", "CORR ENTRY PLAZA", "Invalid CORR ENTRY PLAZA- " + fields[30]);
				validateParam.setResponseMsg("Invalid CORR ENTRY PLAZA::" + fields[30]);
			}
			if (fields[31].matches("\\d{2}") || fields[31].matches("[ ]{2}")) {
				log.info(" Detail validations CORR ENTRY LANE");
			} else {
				isValid= false;
				addErrorMsg("DETAIL", "CORR ENTRY LANE", "Invalid CORR ENTRY LANE- " + fields[31]);
				validateParam.setResponseMsg("Invalid CORR ENTRY LANE::" + fields[31]);
			}
			if (fields[32].trim().matches("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}[-+]\\d{2}:\\d{2}$")) {
				log.info(" Detail validations CORR EXIT DATE");
			} else {
				isValid= false;
				addErrorMsg("DETAIL", "CORR EXIT DATE", "Invalid CORR EXIT DATE- " + fields[32]);
				validateParam.setResponseMsg("Invalid CORR EXIT DATE::" + fields[32]);
			}
			if ( fields[33].trim().length() <= 22 && !fields[33].trim().isEmpty()) {
				log.info(" Detail validations CORR EXIT PLAZA");
			} else {
				isValid= false;
				addErrorMsg("DETAIL", "CORR EXIT PLAZA", "Invalid CORR EXIT PLAZA- " + fields[33]);
				validateParam.setResponseMsg("Invalid CORR EXIT PLAZA::" + fields[33]);
			}
			if (fields[34].trim().matches("\\d{2}") && !fields[34].trim().isEmpty()) {
				log.info(" Detail validations CORR EXIT LANE");
			} else {
				isValid= false;
				addErrorMsg("DETAIL", "CORR EXIT LANE", "Invalid CORR EXIT LANE- " + fields[34]);
				validateParam.setResponseMsg("Invalid CORR EXIT LANE::" + fields[34]);
			}
			if (fields[35].trim().matches("\\d{1,2}") || fields[35].trim().isEmpty()) {
				log.info(" Detail validations CORR AXLE COUNT");
			} else {
				isValid= false;
				addErrorMsg("DETAIL", "CORR AXLE COUNT", "Invalid CORR AXLE COUNT- " + fields[35]);
				validateParam.setResponseMsg("Invalid CORR AXLE COUNT::" + fields[35]);
			}
			if (fields[36].trim().matches("[0-3]") || fields[36].trim().isEmpty()) {
				log.info(" Detail validations CORR OCCUPANCY");
			} else {
				isValid= false;
				addErrorMsg("DETAIL", "CORR OCCUPANCY", "Invalid CORR OCCUPANCY- " + fields[36]);
				validateParam.setResponseMsg("Invalid CORR OCCUPANCY::" + fields[36]);
			}
			if (fields[37].trim().matches("[0-3]") || fields[37].trim().isEmpty()) {
				log.info(" Detail validations CORR PROTOCOL TYPE");
			} else {
				isValid= false;
				addErrorMsg("DETAIL", "CORR PROTOCOL TYPE", "Invalid CORR PROTOCOL TYPE- " + fields[37]);
				validateParam.setResponseMsg("Invalid CORR PROTOCOL TYPE::" + fields[37]);
			}
			if (fields[38].trim().matches("[0-1]")) {
				log.info(" Detail validations CORR VEHICLE TYPE");
			} else {
				isValid= false;
				addErrorMsg("DETAIL", "CORR VEHICLE TYPE", "Invalid CORR VEHICLE TYPE- " + fields[38]);
				validateParam.setResponseMsg("Invalid CORR VEHICLE TYPE::" + fields[38]);
			}
			if (fields[39].length() == 30 || fields[39].trim().isEmpty()) {
				log.info(" Detail validations CORR LP TYPE");
			} else {
				isValid= false;
				addErrorMsg("DETAIL", "CORR LP TYPE", "Invalid CORR LP TYPE- " + fields[39]);
				validateParam.setResponseMsg("Invalid CORR LP TYPE::" + fields[39]);
			}

			if (fields[40].trim().matches("\\d{1,8}\\.\\d{2}")) {
				log.info(" Detail validations CORR TRAN FEE");
			} else {
				isValid= false;
				addErrorMsg("DETAIL", "CORR TRAN FEE", "Invalid CORR TRAN FEE- " + fields[40]);
				validateParam.setResponseMsg("Invalid CORR TRAN FEE::" + fields[40]);
			}
			if (fields[41].trim().matches("[0-2]")) {
				log.info(" Detail validations CORR TRAN FEE TYPE");
			} else {
				isValid= false;
				log.info(" INVALID validations CORR TRAN FEE TYPE");
				addErrorMsg("DETAIL", "CORR TRAN FEE TYPE", "Invalid CORR TRAN FEE TYPE- " + fields[41]);
				validateParam.setResponseMsg("Invalid CORR TRAN FEE TYPE::" + fields[41]);
			}
			if (fields[42].trim().matches("\\d{1,8}\\.\\d{2}")) {
				log.info(" Detail validations POST AMT");
			} else {
				isValid= false;
				addErrorMsg("DETAIL", "POST AMT", "Invalid POST AMT- " + fields[42]);
				validateParam.setResponseMsg("Invalid POST AMT::" + fields[42]);
			}String responseCode = "AODFIL";
			if (responseCode.contains(fields[43].trim())) {
				log.info(" Detail validations RESPONSE CODE");
			} else {
				isValid= false;
				addErrorMsg("DETAIL", "RESPONSE CODE", "Invalid RESPONSE CODE- " + fields[43]);
				validateParam.setResponseMsg("Invalid RESPONSE CODE::" + fields[43]);
			}

			try {
				return isValid; 
				
			} catch (Exception e) {
				return false;
			}
		}
		 // Check if the record is accepted
	    private static boolean isAccepted(String line) {
	        String[] fields = line.split(",");
	        return fields[43].trim().equals("A");
	    }
	    // Get the TRAN AMOUNT from DETAIL record
	    private static String getDetailAmount(String line) {
	        String[] fields = line.split(",");
	        System.out.println("Mounika :: fields[2].trim()"+String.format("%08.2f", Double.parseDouble(fields[28].trim())));
	        return String.format("%08.2f", Double.parseDouble(fields[28].trim()));
	        //return fields[2].trim();
	    }
		// Validate TRAILER record
		private boolean validateTrailer(String line, String headerSequence, int detailCount, double totalDetailAmount,int acceptedCount, double acceptedAmount,
				FileValidationParam validateParam) {
			String[] fields = line.split(",");
			boolean isValid = true;
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
				if (CommonUtil.isValidFormat(fields[2])) {
					log.info(" Trailer validations BUSINESS DAY");
				} else {
					isValid= false;
					addErrorMsg("TRAILER", "BUSINESS DAY", "Invalid Trailer BUSINESS DAY- " + fields[2]);
					validateParam.setResponseMsg("Invalid Trailer BUSINESS DAY::" + fields[2]);
				}
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
			if (String.format("%.1f", Double.parseDouble(fields[6].trim())).equals(String.format("%.1f", acceptedAmount)) && fields[6].trim().length() == 10) {
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
	        ( String.format("%.1f", Double.parseDouble(fields[6].trim())).equals(String.format("%.1f", acceptedAmount)) && fields[6].trim().length() == 10) && //ACCEPTED AMT
	         isValid;
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
				ctocAckMapper.mapToCtocAckFile(fileName, "01", ackFilePath, shortFromAgency, shortToAgency, "CORRECON",
						ackDate);
				return false;
			} else if (!lastLine.startsWith("#TRAILER")) {
				log.info("Invalid Trailer record");
				addErrorMsg("TRAILER", "Record Type", "Invalid Trailer Return type " + lastLine);
				validateParam.setResponseMsg("Invalid Trailer Return type::" + lastLine);
				ctocAckMapper.mapToCtocAckFile(fileName, "03", ackFilePath, shortFromAgency, shortToAgency, "CORRECON",
						ackDate);
				return false;
			}
			return true;
		}
		
	  
}
