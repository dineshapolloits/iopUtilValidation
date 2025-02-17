package com.apolloits.util;

import org.springframework.stereotype.Component;

import com.apolloits.util.modal.IagAckFile;
import com.apolloits.util.utility.CommonUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Component
public class IagAckFileMapper {


    public IagAckFile mapToIagAckFile(String fileName, String returnCode, String ackFilePath, String toAgencyId,String fromAgencyId,String iagVersion) {
        IagAckFile iagAckFile = new IagAckFile();
        iagAckFile.setFileType("ACK ");
        iagAckFile.setFileVersion(iagVersion);
        iagAckFile.setFromAgencyId(fromAgencyId);
        iagAckFile.setToAgencyId(toAgencyId);
        iagAckFile.setOrigFileName(CommonUtil.formatStringRightPad(fileName, 50, ' '));
        iagAckFile.setFileCreationDate(convertDateTimeToOffset(Instant.now().toString()));
        iagAckFile.setReturnCode(returnCode);
        writeIagAckFile(iagAckFile, ackFilePath);
        return iagAckFile;

    }

    //writer to write IagAck file in FixedLength format
    public void writeIagAckFile(IagAckFile iagAckFile, String ackFilePath) {
        try {
            File file = new File(ackFilePath);
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(iagAckFile.getFileType());
            fileWriter.write(iagAckFile.getFileVersion());
            fileWriter.write(iagAckFile.getFromAgencyId());
            fileWriter.write(iagAckFile.getToAgencyId());
            fileWriter.write(iagAckFile.getOrigFileName());
            fileWriter.write(iagAckFile.getFileCreationDate());
            fileWriter.write(iagAckFile.getReturnCode());
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


}
