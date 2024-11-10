package com.apolloits.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
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

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

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
	
	
	public static void main(String args[]) throws IOException {
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
	
	public static void generateNIOPAck(String fileName, String returnCode, 
			 String fromAgencyID, String fromAgencyId, String toAgencyName, String toInterAgencyCode, Long xferControlId, Long atpFileId, String targetDirectory,
			 Long fileSeqNumber,String fileDate) {
		String ACK="ACK";
		String STVL="STVL";
		//String ackDate=CommonUtil.getSSIOPDateFormate(new Date());
		String agency="9002";
		Date currentDate = new Date();
		String currentTime = CommonUtil.getFormatedDate(currentDate, "HHMMSS_FORMAT");
		
		
		GregorianCalendar gCalendar = new GregorianCalendar();
		gCalendar.setTime(currentDate);
		XMLGregorianCalendar xmlCalendar = null;
		xmlCalendar = CommonUtil.getXMLGregCalenByDate(gCalendar.getTime()); //FLCSS_24737
		
		StringBuilder acknowledgementBuilder = new StringBuilder();
		acknowledgementBuilder.append( "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n");// FILE_TYPE
		acknowledgementBuilder.append("\t <Acknowledgement> \n");// FROM_AGENCY_ID
		acknowledgementBuilder.append("\t\t <SubmissionType>"+ACK+"</SubmissionType> \n");
		acknowledgementBuilder.append("\t\t <OrigSubmissionType>"+STVL+"</OrigSubmissionType> \n");
		acknowledgementBuilder.append("\t\t <OrigSubmissionDateTime>"+fileDate+"</OrigSubmissionDateTime> \n");
		acknowledgementBuilder.append("\t\t <SSIOPHubID>"+9001+"</SSIOPHubID> \n");
		acknowledgementBuilder.append("\t\t <FromAgencyID>"+fromAgencyID+"</FromAgencyID> \n");
		acknowledgementBuilder.append("\t\t <ToAgencyID>"+agency+"</ToAgencyID> \n");
		acknowledgementBuilder.append("\t\t <AckDateTime>"+xmlCalendar+"</AckDateTime> \n");
		acknowledgementBuilder.append("\t\t <AckReturnCode>"+returnCode+"</AckReturnCode> \n");
		acknowledgementBuilder.append("\t </Acknowledgement>");
		System.out.println("From CommonUtil generateNIOPAck() :: acknowledgementBuilder==>" +acknowledgementBuilder );
		
		
        String [] file = fileName.split("\\.");
        String [] names= fileName.split("_");
        String ackFileName="9001_9001_"+file[0]+"_"+returnCode+"_"+file[1]+"."+"ACK";
        File targetDirectoryFile = new File(targetDirectory);

        if (!targetDirectoryFile.exists()) {
                targetDirectoryFile.mkdirs();
        }
		File ackFile = new File(targetDirectory + "/"+ ackFileName);
		CommonUtil.writeStringToFile(ackFile, acknowledgementBuilder.toString());
		if (returnCode != null && !(returnCode.equals("00") || returnCode.equals("10"))) {
			
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
}
