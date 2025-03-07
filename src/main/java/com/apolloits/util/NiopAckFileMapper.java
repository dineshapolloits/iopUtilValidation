package com.apolloits.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.time.ZonedDateTime;

import org.springframework.stereotype.Component;

import com.apolloits.util.controller.NiopValidationController;
import com.apolloits.util.modal.FileValidationParam;
import com.apolloits.util.modal.niop.NiopAckFile;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class NiopAckFileMapper {

	
	public NiopAckFile setNiopAckFile(FileValidationParam validationParam,String fileType,String originalSubmissionDateTime,String ackCode,String ackFileName) {
        NiopAckFile niopAckFile = new NiopAckFile();
        niopAckFile.setSubmissionType("ACK");
        niopAckFile.setOriginalSubmissionType(fileType);
        niopAckFile.setOriginalSubmissionDateTime(originalSubmissionDateTime);
        niopAckFile.setNiopHubID(String.valueOf(NiopValidationController.allCscIdNiopAgencyMap.get(validationParam.getToAgency()).getHubId()));
        niopAckFile.setFromAgencyID(validationParam.getToAgency());
        niopAckFile.setToAgencyID(validationParam.getFromAgency());
        niopAckFile.setAckDateTime(IagAckFileMapper.convertDateTimeToOffset(Instant.now().toString()));
        niopAckFile.setAckReturnCode(ackCode);
        generateAckXmlFile(niopAckFile,validationParam,ackFileName);
        return niopAckFile;
    }
	
	private void generateAckXmlFile(NiopAckFile niopAckFile,FileValidationParam validationParam,String ackFileName) {

        String outputAckFilePath = validationParam.getOutputFilePath()+ File.separator+ackFileName;
        log.info("outputAckFilePath :: "+outputAckFilePath);
        try (FileWriter fileWriter = new FileWriter(outputAckFilePath)) {
            // Create JAXB context and marshaller
            JAXBContext jaxbContext = JAXBContext.newInstance(NiopAckFile.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

            // Set marshaller properties
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jaxbMarshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");

            // Marshal the object to XML
            jaxbMarshaller.marshal(niopAckFile, fileWriter);

        } catch (JAXBException | IOException e) {
        	e.printStackTrace();
        	validationParam.setResponseMsg("ACK file not created");
        }
    }

}
