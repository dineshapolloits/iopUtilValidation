package com.apolloits.util.validator.niop;

import java.io.File;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.apolloits.util.modal.FileValidationParam;
import com.apolloits.util.modal.niop.TVLDetailList;
import com.apolloits.util.modal.niop.TagValidationList;
import com.apolloits.util.utility.CommonUtil;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class BTVLFileDetailValidation {

	@Autowired
	CommonUtil commonUtil;
	
	int invalidRecordCount = 0;
	
	public boolean btvlValidation(FileValidationParam validateParam) throws IOException, JAXBException {
		log.info("Inside btvlValidation started :"+validateParam.getInputFilePath());
		 File file = new File(validateParam.getInputFilePath());
		 long start = System.currentTimeMillis();
         JAXBContext jaxbContext = JAXBContext.newInstance(TagValidationList.class);
         Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
         TagValidationList list=  (TagValidationList) unmarshaller.unmarshal(file);
         System.out.println("TVL List :: "+list);
         long end = System.currentTimeMillis();
 		System.out.println((end - start) / 1000f + " seconds");
		return true;
	}
}
