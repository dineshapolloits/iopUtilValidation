package com.apolloits.util.utility;

import static com.apolloits.util.IAGConstants.FILE_RECORD_TYPE;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
//import java.time.ZonedDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Map;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.CompressionMethod;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import com.apolloits.util.IAGConstants;
import com.apolloits.util.controller.ValidationController;
import com.apolloits.util.modal.ErrorMsgDetail;
import com.apolloits.util.modal.FileValidationParam;
import com.apolloits.util.reader.AgencyDataExcelReader;




/**
 * @author DK
 * `
 */

@Slf4j
@Component
public class CommonUtil {


private static AgencyDataExcelReader appConfig;
	
	@Autowired
    public void setApplicationConfig(AgencyDataExcelReader appConfig){

		this.appConfig=appConfig;
    }
	
	@Autowired
	@Lazy
	ValidationController controller;
	
	public static void main1(String args[]) throws IOException {
	/*	String trim = "      ab           ";
		System.out.println(trim.length());
		System.out.println(trim.trim().length());
		String fileRowData ="0008000000001610000010008P900000016                                         6  TI0001";
		System.out.println("fileRowData length ::"+fileRowData.length());
		String tagAgencyId="";
		String tagSerialNo="";
		String tagStatus="";
		String tagAcctInfo="";
		String tagHomeAgency="";
		String tagAcTypeInd="";
		String tagAccountNo="";
		String tagProtocol ="";
		String tagType="";
		String tagMount="";
		String tagClass ="";
		
		try {
			tagAgencyId = fileRowData.substring(0,4);
			System.out.println("tagAgencyId ::"+tagAgencyId);
			tagSerialNo = fileRowData.substring(4,14);
			System.out.println("tagSerialNo ::"+tagSerialNo);
			tagStatus = fileRowData.substring(14,15);
			System.out.println("tagStatus ::"+tagStatus);
			tagAcctInfo = fileRowData.substring(15,21);
			System.out.println("tagAcctInfo ::"+tagAcctInfo);
			tagHomeAgency = fileRowData.substring(21,25);
			System.out.println("tagHomeAgency ::"+tagHomeAgency);
			tagAcTypeInd = fileRowData.substring(25,26);
			System.out.println("tagAcTypeInd ::"+tagAcTypeInd);
			tagAccountNo = fileRowData.substring(26,76);
			System.out.println("tagAccountNo ::"+tagAccountNo);
			tagProtocol = fileRowData.substring(76,79);
			System.out.println("tagProtocol ::"+tagProtocol);
			tagType = fileRowData.substring(79,80);
			System.out.println("tagType ::"+tagType);
			tagMount = fileRowData.substring(80,81);
			System.out.println("tagMount ::"+tagMount);
			tagClass = fileRowData.substring(81,85);
			System.out.println("tagClass ::"+tagClass);
		} catch (Exception e) {
			e.printStackTrace();
		}*/
		String fileRowData ="ITA101.60.0300082024-10-25T00:23:00Z0000000015";
		System.out.println("fileRowData length ::"+fileRowData.length());
		//icrxHeader.getFileDateTime().replaceAll("[-T:Z]", "")
		System.out.println(" headervalue.substring(16, 36);"+ fileRowData.substring(16, 36));
		System.out.println(" headervalue.substring(16, 36);"+ fileRowData.substring(16, 36).replaceAll("[-T:Z]", ""));
		String filename = "0008_20241025002300.ITAG";
		System.out.println(" headervalue.substring(16, 36);"+ filename.substring(5, filename.lastIndexOf(".")));
	}

	
	public static boolean validateZIPFileName(String fileName) {
		if (fileName != null && fileName.length() == 28) {
			String[] fileParams = fileName.split("[_.]");
			System.out.println("validateZIPFileName() ::  fileParams ::"+Arrays.toString(fileParams) );
			System.out.println(" AgencyDataExcelReader.agencyCode ::" + AgencyDataExcelReader.agencyCode );
			if ((fileParams.length == 4 
					&&  IAGConstants.IAG_FILE_TYPES.contains(fileParams[2])
					&& "zip".equalsIgnoreCase(fileParams[3]) ) && AgencyDataExcelReader.agencyCode.contains(fileParams[0])) {
				
				SimpleDateFormat dateFormat = new SimpleDateFormat(IAGConstants.YYYY_MM_DD_HH_MM_SS);
				dateFormat.setLenient(false);
				try {
					dateFormat.parse(fileParams[1].trim());
					return true;
				} catch (ParseException pe) {
					return false;
				}
				
			}
		}
		return false;
 }
	
	 public static boolean validateFileName(String fileName) {
			if (fileName != null && fileName.length() == 24) {
				String[] fileParams = fileName.split("[_.]");
				
				if ( IAGConstants.IAG_FILE_TYPES.contains(fileParams[2]) &&
						AgencyDataExcelReader.agencyCode.contains(fileParams[0])) {
					
					SimpleDateFormat dateFormat = new SimpleDateFormat(IAGConstants.YYYY_MM_DD_HH_MM_SS);
					dateFormat.setLenient(false);
					try {
						dateFormat.parse(fileParams[1].trim());
						return true;
					} catch (ParseException pe) {
						return false;
					}
					
				}
			}
			return false;
	 }


	public static String formatStringLeftPad(String value, int length, char character) {
		if (value == null || value.length() > length)
			return StringUtils.leftPad("", length, character);
		else
			return StringUtils.leftPad(value, length, character);
	}

	public static String formatStringRightPad(String value, int length, char character) {
		if (value == null || value.length() > length)
			return StringUtils.rightPad("", length, character);
		else
			return StringUtils.rightPad(value, length, character);
	}

	public static String formatLong(Long value, int length) {
		if (value == null || value.toString().length() > length)
			return String.format("%0" + length + "d", 0l);
		else
			return String.format("%0" + length + "d", value);
	}

	public static String formatInt(Integer value, int length) {
		if (value == null || value.toString().length() > length)
			return String.format("%0" + length + "d", 0l);
		else
			return String.format("%0" + length + "d", value);
	}

	public static String converIntergertoString(Integer value) {
		if (value != null)
			return value.toString();

		return "0";
	}
	
	public static int convertStringtoInteger(String value) {
		if (value != null && !value.trim().isEmpty())
			return Integer.valueOf(value);

		return 0;
	}
	
	public static String convertLongtoString(Long value) {
		if (value != null)
			return value.toString();

		return "0";
	}
	
	public static long convertStringtoLong(String value) {
		if (value != null && !value.trim().isEmpty())
			return Long.valueOf(value);

		return 0l;
	}
	
	public static String formatWithSubString(Long value, int length) {
		if (value == null)
			return String.format("%0" + length + "d", 0l);
		else if (value.toString().length() > length) {
			String longValue = value.toString();
			return longValue.substring(longValue.length() - length);
		} else
			return String.format("%0" + length + "d", value);

	}

	/**
	 * This method check input string is number
	 * @param str
	 * @return boolean
	 */
	public static boolean isNumeric(String str) {
		try {
			if (str != null) {
				double strDouble = Double.parseDouble(str);
				return true;
			}
		} catch (NumberFormatException nfe) {
			System.out.println("INVALID --> " + str + " is must be number" + nfe.getMessage());
			return false;
		}
		return false;
	}

	public static boolean isNumber(String str) {
		return str.matches("[0-9]{1,13}(\\.[0-9]*)?");
	}

	public static boolean isNotNull(String str) {
		return (str != null) ? true : false;
	}

	public static boolean isDate(String strDate) throws ParseException {
		java.text.SimpleDateFormat simpleDateFormat = new java.text.SimpleDateFormat("yyyyMMdd");
		if (strDate != null) {
			try {
				java.util.Date date = simpleDateFormat.parse(strDate.trim());
				if (simpleDateFormat.format(date).equals(strDate.trim())) {
					return true;
				}
			} catch (ParseException parseException) {
				parseException.printStackTrace();
				System.out.println("Exception in isDate()" + parseException.getMessage());
			}
		}
		return false;
	}

	public static String getFormatedDate(Date date, String dateFormat) {
		SimpleDateFormat sdfDate = new SimpleDateFormat(dateFormat);
		String strDate = sdfDate.format(date);
		return strDate;
	}


	/**
	 * This method is used to convert string to date for given format
	 * 
	 * @param date
	 * @param dateFormat
	 * @return Date
	 */
	public static Date convertStringToFormatedDate(String date, String dateFormat) {
		SimpleDateFormat format = new SimpleDateFormat(dateFormat);
		Date formattedDate;
		try {
			formattedDate = format.parse(date.substring(0, 8));
			return formattedDate;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			System.out.println("Parse exception::::" + e);
		}
		return null;
	}


	public static void copyFile(File in, File out) throws IOException {
		FileUtils.copyFile(in, out);
	}

	public static Date getDate(String date, String dateFormat) {
		SimpleDateFormat format = new SimpleDateFormat(dateFormat);
		Date formattedDate;
		try {
			formattedDate = format.parse(date);
			return formattedDate;
		} catch (ParseException e) {
			return null;
		}
		
	}


	public static void moveFileToUnProcessedFolderAndDelete(File file) throws Exception {
		if (file.exists()) {

			String unProcessedFilePath = file.getParent().replaceAll("INBOUND", "UNPROCESSED");
			File unProcessedFolder = new File(unProcessedFilePath);
			if (!unProcessedFolder.exists()) {
				unProcessedFolder.mkdirs();
			}
			File unProcessedFile = new File(unProcessedFilePath + "/" + file.getName());
			try {
				CommonUtil.copyFile(file, unProcessedFile);
			} catch (IOException e) {
				throw new Exception(e);
			}

			boolean duplicateFileDeleter = file.delete();
			System.out.println("File:" + file.getName() + ": Deleted Indicator::::>>" + duplicateFileDeleter);
			System.out.println("File:" + file.getName() + ": Deleted Indicator::::>>" + duplicateFileDeleter);
		}
	}

	public static void moveFileToProcessedFolderAndDelete(File file) throws Exception {
		if (file.exists()) {

			String processedFilePath = file.getParent().replaceAll("INBOUND", "PROCESSED");
			File processedFolder = new File(processedFilePath);
			if (!processedFolder.exists()) {
				processedFolder.mkdirs();
			}
			File unProcessedFile = new File(processedFilePath + "/" + file.getName());
			try {
				CommonUtil.copyFile(file, unProcessedFile);
			} catch (IOException e) {
				throw new Exception(e);
			}

			boolean duplicateFileDeleter = file.delete();
			System.out.println("File:" + file.getName() + ": Deleted Indicator::::>>" + duplicateFileDeleter);
		}
	}

	// This method use for IAG file header and file name date should be same
	public static boolean validateFileDateAndTime(String fileDate, String headerDate) {
		if (fileDate.equals(headerDate))
			return true;
		else
			return false;
	}


	public static int getAvcClassValue(int actualClass) {
		if (actualClass < 2)
			return 2;
		else if (actualClass <= 2 || actualClass <= 15)
			return actualClass;
		else
			return 15;
	}


	public static boolean createCheckFile(String absolutePath) throws IOException {
		BufferedWriter bw = null;
		FileWriter fw = null;
		if (absolutePath != null && !absolutePath.isEmpty()) {
			String newFileName = absolutePath.substring(0, absolutePath.lastIndexOf('.')) + ".CHECK";
			try {
				fw = new FileWriter(newFileName);
				bw = new BufferedWriter(fw);
				bw.write(CommonUtil.formatStringRightPad(absolutePath.substring(absolutePath.lastIndexOf('/') + 1, absolutePath.length()), 80, ' ')); //length need to check
			} catch (IOException e) {
				//System.out.println("Exception in createCheckFile(String fileName) File name ::" + absolutePath + "\t Error MSG ::" + e.getMessage());
				e.printStackTrace();
				//System.out.println("Check File Not Created Filename ::" + newFileName);
				return false;

			} finally {
				if (bw != null)
					bw.close();
				if (fw != null)
					fw.close();
			}

			System.out.println("Done");
			return true;
		}
		System.out.println("Check File Not Created Filename ::" + absolutePath);
		return false;
	}
	
	
	
	public static String getNIOPDateFormate(Date date)	{
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateAndTime=dateFormat.format(date).toString();
		String [] split =dateAndTime.split(" ");
		dateAndTime=split[0]+"T"+split[1]+"Z";
		return dateAndTime;
		
	}
	

	

	public static boolean isValidDateFormat(String strDate) throws ParseException {
		java.text.SimpleDateFormat simpleDateFormat = new java.text.SimpleDateFormat("yyyyMMddHHmmss");
		if (strDate != null) {
			try {
				java.util.Date date = simpleDateFormat.parse(strDate.trim());
				if (simpleDateFormat.format(date).equals(strDate.trim())) {
					return true;
				}
			} catch (ParseException parseException) {
				parseException.printStackTrace();
			}
		}
		return false;
	}
	
	public static boolean containsSpceialCharacter(String word){
		Pattern p = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(word);
		boolean b = m.find();
		if (!b)
		return true;
		else{
			return false;
		}
	}
	public static boolean isValidFileDate(String dateToValidate, String dateFromat){
		if(dateToValidate == null){
			return false;
		}
		SimpleDateFormat sdf = new SimpleDateFormat(dateFromat);
		sdf.setLenient(false);

		try {

			Date date = sdf.parse(dateToValidate);
			System.out.println("Date======>"+date);

		} catch (ParseException e) {

			e.printStackTrace();
			return false;
		}

		return true;
	}	
	


	public static Date convertToEST(String date) throws Exception{
		String dateString=null;
	    DateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
	    Date date1=formatter.parse(date);  
	    formatter.setTimeZone(TimeZone.getTimeZone("EST"));
	    dateString=formatter.format(date1);
	    Date date2=formatter.parse(dateString);  
	    return date2;
	}
	
	public static void moveFiles(String sourcePath, String destPath,
			String filePathSeparator) throws IOException {
		File source = new File(sourcePath);
		File dest = new File(destPath);
		try {
			String y = (dest.getAbsolutePath());
			String x = source.getAbsolutePath();
			File f1 = new File(x);
			File f2 = new File(y);
			FileCopyUtils.copy(f1, f2);
			if(!f1.delete()){
				 String message = f1.exists() ? "is in use by another app" : "does not exist";
				 throw new IOException("Cannot delete file, because file " + message + ".");
			}
		} catch (Exception exc) {
			System.out.println("Exception in move the files from source "
					+ sourcePath + " to dest " + destPath+exc);
			exc.printStackTrace();
		}

	}
	
	public static void writeStringToFile(File file, String string) {
		BufferedWriter bufferedWriter = null;
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			Writer writer = new FileWriter(file);
			bufferedWriter = new BufferedWriter(writer);
			bufferedWriter.write(string);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (bufferedWriter != null)
					bufferedWriter.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
	public static XMLGregorianCalendar getXMLGregCalenByDate(Date ts) {

		XMLGregorianCalendar xmlGregorianCalendar = null;	
		SimpleDateFormat sDFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss'Z'");
        Calendar cal = Calendar.getInstance(new SimpleTimeZone(0, "UTC"));
        sDFormat.setCalendar(cal);

		String formatted = sDFormat.format(ts.getTime());
		String retDate = formatted.substring(0, formatted.indexOf(" ")) + "T" + formatted.substring(formatted.indexOf(" ") + 1);

		try {
			xmlGregorianCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(retDate);

		} catch (DatatypeConfigurationException e) {
			e.printStackTrace();
		}
		return xmlGregorianCalendar;
	}
	
/*	public static String convertHeaderDateTimetoFormat(String dateTime) {
        if (dateTime != null) {
           ZonedDateTime zonedDateTime = ZonedDateTime.parse(dateTime);
           DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withZone(ZoneOffset.UTC);
            return zonedDateTime.format(formatter);
        }
        return null;
    }
	public static String convertUTCtoESTDate(String utcDate) {
		 if (utcDate != null) {
	           ZonedDateTime zonedDateTime = ZonedDateTime.parse(utcDate);
	           ZoneOffset zoneOffSet = ZoneOffset.of("-05:00");
	           DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd").withZone(zoneOffSet);
	            return zonedDateTime.format(formatter);
	        }
		return null;
	} */
	public static Instant convertUTCStringtoDateFormat(String utcDate, boolean isZone) {
	    DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter
				.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
	    if(isZone) {
	    	DATE_TIME_FORMATTER = DateTimeFormatter
		    		.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
	    }
		if(utcDate !=null) {
			LocalDateTime localDateTime = LocalDateTime.parse(utcDate, DATE_TIME_FORMATTER);
		    // timestamp of the original value represented in UTC
		    Instant utcTimestamp = localDateTime.toInstant(ZoneOffset.UTC);
		    System.out.println("***** localDateTime ::"+localDateTime);
		    return utcTimestamp;
		}
		
		return null;
	}

	public static String convertHeaderDateTimeToFormat(String dateTime) throws Exception {
		if (dateTime != null) {
			if(!isValidDateTimeFormat(dateTime)){
				return null;
			}
			ZonedDateTime zonedDateTime = ZonedDateTime.parse(dateTime);
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
			return zonedDateTime.format(formatter);

		}
		throw new Exception("Invalid Header Date Time Format: "+dateTime);
	}

	public static boolean isValidDateTimeFormat(String originalSubmissionDateTime) throws DateTimeParseException {

		if (!(originalSubmissionDateTime.length() == 20) &&
				!(originalSubmissionDateTime.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z"))) {
			return false;
		}
		return true;

	}


	public String getFileName(String filePath) {
		Path path = Paths.get(filePath);
		return path.getFileName().toString();
	}

	public  void moveFileToArchiveFolderAndDelete(File file,String processedFilePath) throws Exception {
		if (file.exists()) {

			File processedFolder = new File(processedFilePath);
			if (!processedFolder.exists()) {
				processedFolder.mkdirs();
			}
			File unProcessedFile = new File(processedFilePath + "/" + file.getName());
			try {
				CommonUtil.copyFile(file, unProcessedFile);
			} catch (IOException e) {
				throw new Exception(e);
			}

			boolean duplicateFileDeleter = file.delete();
			log.debug("Input File:" + file.getName() + ": Deleted Indicator::::>>" + duplicateFileDeleter);
		}
	}

	public  void moveFileToFailedFolderAndDelete(File file,String failedFilePath) throws Exception {
		if (file.exists()) {
			File failedFilesFolder = new File(failedFilePath);
			if (!failedFilesFolder.exists()) {
				failedFilesFolder.mkdirs();
			}
			File unProcessedFile = new File(failedFilePath + "/" + file.getName());
			try {
				CommonUtil.copyFile(file, unProcessedFile);
			} catch (IOException e) {
				throw new Exception(e);
			}

			boolean duplicateFileDeleter = file.delete();
			log.info("Moved Input File:" + file.getName() + " to failedFiles directory: Deleted Indicator::::>>" + duplicateFileDeleter);
		}
	}
	
	public boolean isTransactionFileFormatValid(String transactionFilename,String fileType ) {
		boolean isvalid = true;
		String fileName="File Name";
		String[] fileParams = transactionFilename.split("[_.]"); //this param need to apply all below if condition
		System.out.println("File name Array :: "+Arrays.toString(fileParams));
	    // check if the file name is not empty
	    if (transactionFilename == null || transactionFilename.isEmpty()) {
	    	controller.getErrorMsglist().add(new ErrorMsgDetail(FILE_RECORD_TYPE,fileName,"File name inValid ::\t "+transactionFilename));
	    	isvalid = false;
	    }

	    // check if the file name has the correct extension
	    if (!transactionFilename.endsWith("."+fileType)) {
	    	controller.getErrorMsglist().add(new ErrorMsgDetail(FILE_RECORD_TYPE,fileName,"File extension should be .ICTX :: \t "+ transactionFilename));
	    	isvalid = false;
	    }
	    // check if the file name has the correct format
	    if (!transactionFilename.matches("\\d{4}_\\d{4}_\\d{14}\\."+fileType)) {
	    	controller.getErrorMsglist().add(new ErrorMsgDetail(FILE_RECORD_TYPE,fileName,"File format is invalid {FROM_AGENCY_ID}_{TO_AGENCY_ID}_YYYYMMDDHHMMSS.ICTX :: \t "+transactionFilename ));
	    	isvalid = false;
	    }
	    // check if the FROM_AGENCY_ID is a number
	    if (!transactionFilename.substring(0, 4).matches("\\d{4}")) {
	    	controller.getErrorMsglist().add(new ErrorMsgDetail(FILE_RECORD_TYPE,fileName,"From agency format is invalid ::\t "+transactionFilename));
	    	isvalid = false;
	    }
	    // check if the TO_AGENCY_ID is a number
	    if (!transactionFilename.substring(5, 9).matches("\\d{4}")) {
	    	controller.getErrorMsglist().add(new ErrorMsgDetail(FILE_RECORD_TYPE,fileName,"To agency format is invalid ::\t "+transactionFilename));
	    	isvalid = false;
	    }
	    // check if the date and time part is a valid date and time
	    if (!isValidDateTime(transactionFilename.substring(10, 24))) {
	    	controller.getErrorMsglist().add(new ErrorMsgDetail(FILE_RECORD_TYPE,fileName,"File Date is invaid format YYYYMMDDHHMMSS :: \t "+transactionFilename));
	    	isvalid = false;
	    }
	    if(!AgencyDataExcelReader.agencyCode.contains(transactionFilename.substring(0, 4))) {
	    	controller.getErrorMsglist().add(new ErrorMsgDetail(FILE_RECORD_TYPE,fileName,"From agency  is not configured ::\t "+transactionFilename.substring(0, 4)));
	    	isvalid = false;
	    }
	    	
	    if(!AgencyDataExcelReader.agencyCode.contains(transactionFilename.substring(5, 9))) {
	    	controller.getErrorMsglist().add(new ErrorMsgDetail(FILE_RECORD_TYPE,fileName,"To agency  is not configured ::\t "+transactionFilename.substring(5, 9)));
	    	isvalid = false;
	    }
	    return isvalid;
	}

	public static boolean isValidDateTime(String dateTime) {
	    // Check if the date and time are valid in the format YYYYMMDDHHMMSS
	    try {
	        LocalDateTime.parse(dateTime, java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
	    } catch (DateTimeParseException e) {
	        return false;
	    }
	    return true;
	}
	
	public boolean isValidDateTimeInDetail(String dateTime) {
        // Check if the date and time are valid in the format YYYY-MM-DDThh:mm:ssZ
        try {
            LocalDateTime.parse(dateTime, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"));
        } catch (DateTimeParseException e){
            return false;
        }
        return true;
    }
	
	public boolean isValidEtcPlaza(String plaza,String plazaType) {
		if(plazaType!= null && plazaType.equals("entry")) {
			if (!plaza.equals("***************") && !plaza.matches(IAGConstants.PLAZA_FORMAT)){
				return false;
			}
		}else if(!plaza.matches(IAGConstants.PLAZA_FORMAT)) {
			return false;
		}
		return true;
	}
	
	public boolean isValidEtcLane(String lane,String plazaType) {
		if(plazaType!= null && plazaType.equals("entry")) {
			if (!lane.equals("***") && !lane.matches(IAGConstants.PLAZA_LANE_FORMAT)){
				return false;
			}
		}else if(!lane.matches(IAGConstants.PLAZA_LANE_FORMAT)) {
			return false;
		}
		return true;
	}
	
	public String getCurrentUTCDateandTime() {
		try {
			DateTime dateTime = new DateTime().withZone(DateTimeZone.UTC);
			return dateTime.toString("yyyy-MM-dd'T'HH:mm:ss'Z'");
		} catch (DateTimeParseException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public String moveToZipFile(String filePath,FileValidationParam validateParam) {
		System.out.println("filePath ::"+filePath);
		File file = new File(filePath);
		ZipParameters parameters = new ZipParameters();
		parameters.setCompressionMethod(CompressionMethod.DEFLATE);
		parameters.setCompressionLevel(CompressionLevel.ULTRA);

		String zipFileName = file.getName().replace(".", "_") + ".ZIP";
		String zipFilePath = file.getParent() + "/" + zipFileName;
		System.out.println("zipFileName ::"+zipFileName +"\n zipFilePath ::"+zipFilePath);
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
	
	public static String getCurrentDateAndTime() {
		LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String strDate = now.format(formatter);
        System.out.println("Converted Date to String: " + strDate);
        return now.format(formatter);
	}
	public static boolean isDate(String date,String format) {
		try {
			LocalDateTime now = LocalDateTime.now();
	        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
	         now.format(formatter);
		}catch (Exception e) {
			return false;
		}
		return true;
	}
	public boolean validateTransactionZIPFileName(String zipFileName,String fileType) {
		
		if (zipFileName != null && zipFileName.length() == 33) {
			String[] fileParams = zipFileName.split("[_.]");
			System.out.println("zipFileName ::"+Arrays.toString(fileParams));
			if ((fileParams.length == 5 && fileParams[3].equals(fileType)
					&& "zip".equalsIgnoreCase(fileParams[4])) && AgencyDataExcelReader.agencyCode.contains(fileParams[0])
							&& AgencyDataExcelReader.agencyCode.contains(fileParams[1])
					) {

				SimpleDateFormat dateFormat = new SimpleDateFormat(IAGConstants.YYYY_MM_DD_HH_MM_SS);
				dateFormat.setLenient(false);
				try {
					dateFormat.parse(fileParams[2].trim());
					return true;
				} catch (ParseException pe) {
					controller.getErrorMsglist().add(new ErrorMsgDetail(FILE_RECORD_TYPE,"File","Zip file Name Date and time invalid :: YYYYMMDDHHMMSS \t ::"+zipFileName));
					return false;
					
				}

			}
			controller.getErrorMsglist().add(new ErrorMsgDetail(FILE_RECORD_TYPE,"File","Zip file Name Validation Failed :: {FROM_AGENCY_ID}_{TO_AGENCY_ID}_YYYYMMDDHHMMSS."+fileType+" \t ::"+zipFileName));
		}else {
		controller.getErrorMsglist().add(new ErrorMsgDetail(FILE_RECORD_TYPE,"File","Zip file Name invalid length :: file lenght should be 33 \t ::"+zipFileName));
		}
			return false;
	}
	
	public boolean validateParameter(FileValidationParam validateParam) {
		//Validate file location
		File isfolder = new File(validateParam.getOutputFilePath());

		  if(!isfolder.exists()) {
			  log.error("Folder not persent. Please check your path");
			  validateParam.setResponseMsg("Folder not persent. Please check your generate path");
			  return false;
		  }
		  Path path = Path.of(validateParam.getOutputFilePath());
		  if(!Files.isWritable(path)) {
			  log.error("Not able to create file. Please check generate folder Permisison");
			  validateParam.setResponseMsg("Not able to create file. Please check generate folder Permisison");
			  return false;
		  }
		  
		  isfolder = new File(validateParam.getInputFilePath());
		  if(!isfolder.exists()) {
			  log.error("Folder not persent. Please check your input path");
			  validateParam.setResponseMsg("Folder not persent. Please check your input path");
			  return false;
		  }
		  
		  if(!Files.isReadable(path)) {
			  log.error("Not able to Read file. Please check input file Permisison");
			  validateParam.setResponseMsg("Not able to Read file. Please check input file  Permisison");
			  return false;
		  }
		  //from agency and to agency value should be there
			if (validateParam.getFromAgency() == null || validateParam.getFromAgency().isEmpty()
					|| validateParam.getFromAgency().length() != 4) {
				 log.error("From Agency code validation failed ");
				  validateParam.setResponseMsg("From agency code should be 4 digit");
				  return false;
			}else {
				if(!AgencyDataExcelReader.agencyCode.contains(validateParam.getFromAgency())) {
	        		 log.error("From Agency code not available. Please check agency Configuration");
	        		 validateParam.setResponseMsg("From Agency code not available. Please check agency Configuration");
	        		 return false;
	        	 }
				
			}
			
			if (validateParam.getToAgency() == null || validateParam.getToAgency().isEmpty()
					|| validateParam.getToAgency().length() != 4) {
				 log.error("To Agency code validation failed ");
				  validateParam.setResponseMsg("To agency code should be 4 digit");
				  return false;
			}else {
				if(!AgencyDataExcelReader.agencyCode.contains(validateParam.getToAgency())) {
	        		 log.error("To Agency code not available. Please check agency Configuration");
	        		 validateParam.setResponseMsg("To Agency code not available. Please check agency Configuration");
	        		 return false;
	        	 }
			}
		 
		return true;
	}
	
	public String getStringFormatCell(Cell cell) {
		log.info("cell type ::"+cell.getCellType());
		String value ="";
		switch (cell.getCellType())               
		{  
		case STRING:    //field that represents string cell type  
		log.debug("String :: "+cell.getStringCellValue()); 
		value = cell.getStringCellValue();
		break;  
		case NUMERIC:    //field that represents number cell type  
			log.debug("Number :: "+cell.getNumericCellValue());
		value = String.valueOf((int)cell.getNumericCellValue());
		break; 
		case BLANK:    //field that represents number cell type  
			log.info("Blank ");  
			break;
		default:  
		}  
		log.info("return Value :: "+value);
		return value;
	}
	
	
}
