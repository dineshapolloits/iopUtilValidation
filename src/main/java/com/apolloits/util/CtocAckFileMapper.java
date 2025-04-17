package com.apolloits.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

import com.apolloits.util.modal.IagAckFile;
import com.apolloits.util.utility.CommonUtil;

@Component
public class CtocAckFileMapper {
	
	
	 public IagAckFile mapToCtocAckFile(String fileName, String returnCode, String ackFilePath, String toAgencyId,String fromAgencyId,String fileType,String ackDate) {
	        IagAckFile iagAckFile = new IagAckFile();
	        iagAckFile.setFileType("ACK");
	        iagAckFile.setFileVersion("REV A2.1.1");
	        iagAckFile.setRecordType(String.format("%10s", fileType));//CommonUtil.formatStringRightPad(fileType, 10, ' '));
	        iagAckFile.setFromAgencyId(fromAgencyId.toUpperCase());
	        iagAckFile.setToAgencyId(toAgencyId.toUpperCase());
	        iagAckFile.setOrigFileName(String.format("%45s", fileName));//fileName.substring(0, 2)+","+fileName.substring(2, 4)+"_"+convertDateTime(fileName.substring(5, 20))+fileName.substring(20, 24));
			/*
			 * iagAckFile.setFileCreationDate(convertDateTime(fileName.substring(5,20)));
			 * iagAckFile.setAckDate(convertDateTime(ackDate)); //This is going to be create
			 * date
			 */	   
	        iagAckFile.setFileCreationDate(CommonUtil.convertDatetoUTC(ackDate));
			iagAckFile.setAckDate(CommonUtil.getCtocUTCDateandTime());
			iagAckFile.setReturnCode(returnCode);
	        writeCtocAckFile(iagAckFile, ackFilePath);
	        return iagAckFile;

	    }

	
	//writer to write IagAck file in FixedLength format
    public void writeCtocAckFile(IagAckFile iagAckFile, String ackFilePath) {
        try {
            File file = new File(ackFilePath);
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(iagAckFile.getFileType());
            fileWriter.write(",");
            fileWriter.write(iagAckFile.getRecordType());
            fileWriter.write(",");
            fileWriter.write(iagAckFile.getOrigFileName());
            fileWriter.write(",");
            fileWriter.write(iagAckFile.getFromAgencyId());
            fileWriter.write(",");
            fileWriter.write(iagAckFile.getToAgencyId());
            fileWriter.write(",");
            fileWriter.write(iagAckFile.getFileCreationDate());
            fileWriter.write(",");
            fileWriter.write(iagAckFile.getAckDate());
            fileWriter.write(",");
            fileWriter.write(iagAckFile.getReturnCode());
            fileWriter.write(",");
            fileWriter.write(iagAckFile.getFileVersion());
            fileWriter.write("\n");
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static String convertDateTimeToOffset(String entryDateTime) {
        // Parse the input string to an OffsetDateTime in UTC
        OffsetDateTime dateTime = OffsetDateTime.parse(entryDateTime).withOffsetSameInstant(ZoneOffset.UTC);
        // Define the formatter to produce the desired format
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX");

        // Format the OffsetDateTime to the desired string representation
        return dateTime.format(formatter);
    }

    public static String convertDateTime()
    {
    	// Get the current date and time in the default system timezone (including the offset)
    	ZonedDateTime now = ZonedDateTime.now();        
    	// Define the formatter with the desired pattern
    	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");      
    	// Format the current date and time with timezone offset
    	String	formattedDate = now.format(formatter);        
    	// Output the formatted date 
    	System.out.println(formattedDate);
    	return formattedDate;
    }
    public String convertDateTimeFormat() {
    	// Get the current date and time
    	LocalDateTime now = LocalDateTime.now();        
    	// Define the formatter with the desired pattern
    	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");        
    	// Format the current date and time
    	String formattedDateTime = now.format(formatter);        
    	// Output the formatted date-time 
    	System.out.println(formattedDateTime);
    	return formattedDateTime;
    }
public String convertDate(String originalDateStr) {
	     
	// Define the formatter for the original date string
	DateTimeFormatter originalFormatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");     
	// Parse the original string into a LocalDateTime object
	LocalDateTime localDateTime = LocalDateTime.parse(originalDateStr, originalFormatter);        
	// Convert LocalDateTime to ZonedDateTime with a specific time zone offset (-08:00)
	ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneOffset.ofHours(-8));       
	// Define the formatter for the target date format (yyyy-MM-dd'T'HH:mm:ssXXX)
	DateTimeFormatter targetFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");        
	// Format the ZonedDateTime into the desired format
	String formattedDate = zonedDateTime.format(targetFormatter);
	// Output the formatted date 
	System.out.println(formattedDate);
	return formattedDate;
}
public static String convertDateTime(String input) {       
	// Extract date and time parts
	String date = input.substring(0, 8); 
	// "20250204"
	String time = input.substring(9);    // "011644"
	// Format the date to yyyy-MM-dd
	String formattedDate = date.substring(0, 4) + "-" + date.substring(4, 6) + "-" + date.substring(6, 8);      
	// Format the time to HH:mm:ss
	String formattedTime = time.substring(0, 2) + ":" + time.substring(2, 4) + ":" + time.substring(4, 6);     
	LocalDateTime localDateTime = LocalDateTime.parse(formattedDate+"T"+formattedTime);
	ZoneId zoneId= ZoneId.of("America/Los_Angeles");
	ZonedDateTime zonedDateTime = localDateTime.atZone(zoneId);  
	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
	String formattedDateTime = zonedDateTime.format(formatter);
	/*
	 * ZonedDateTime now = ZonedDateTime.now(); ZoneOffset offSet= now.getOffset();
	 * System.out.println("MOUNIKAAAAAA"+offSet);
	 */
	// Combine the date and time
	//return formattedDate + "T" + formattedTime+offSet; 
	return formattedDateTime;
	}
public static String convertDateTimeFormat(String input) {
    // Extract the date and time parts
    String date = input.substring(0, 8);  // "20250204"
    String time = input.substring(9);     // "011644"
    // Format the date and time parts using String.format
    String formattedDate = String.format("%s/%s/%s", date.substring(4, 6), date.substring(6, 8), date.substring(0, 4));
    String formattedTime = String.format("%s:%s:%s", time.substring(0, 2), time.substring(2, 4), time.substring(4, 6));

    // Combine formatted date and time
    return formattedDate + "," + formattedTime;
}


   

}
