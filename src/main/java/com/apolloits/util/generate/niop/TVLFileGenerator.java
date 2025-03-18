package com.apolloits.util.generate.niop;

import static com.apolloits.util.IAGConstants.HEADER_RECORD_TYPE;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.apolloits.util.NIOPConstants;
import com.apolloits.util.controller.NiopValidationController;
import com.apolloits.util.modal.FileValidationParam;
import com.apolloits.util.modal.NiopAgencyEntity;
import com.apolloits.util.modal.niop.TVLDetailList;
import com.apolloits.util.modal.niop.TVLHeader;
import com.apolloits.util.modal.niop.TVLPlateDetails;
import com.apolloits.util.modal.niop.TVLTagDetails;
import com.apolloits.util.modal.niop.TagValidationList;
import com.apolloits.util.reader.AgencyDataExcelReader;
import com.apolloits.util.utility.CommonUtil;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class TVLFileGenerator {
	
	private String filename = "";
	private String fileCreateDateandTime = "";
	long tagSequenceStart = 0;
	int tagSequenceEnd = 0;
	NiopAgencyEntity agency;
	
	@Autowired
	CommonUtil commonUtil;
	
	public boolean tvlGen(FileValidationParam validateParam) {

		if (!commonUtil.validateInfoFileGenParameter(validateParam)) {
			return false;
		}
		//This one need to check in controller and check all UI fields like validation 
		if(validateParam.getFromAgency().equals("NONE")) {
			validateParam.setResponseMsg("Please select From Agency Code");
			return false;
		}
		long start = System.currentTimeMillis();
		TVLHeader header = getTVLHeader(validateParam);
		log.info("TVL file name ::"+filename);
		this.tagSequenceStart=0;
		List<TVLTagDetails> TVLTagDetailsList =  getTVLDetailsList(validateParam);
		
		TagValidationList tvlList = new TagValidationList();
		tvlList.setTvlHeader(header);
		
		//Set Detail record value
		TVLDetailList tvlDetail = new TVLDetailList();
		tvlDetail.setTvlTagDetails(TVLTagDetailsList);
		List<TVLDetailList> tvlDetailList = new ArrayList<>();
		tvlDetailList.add(tvlDetail);
		
		tvlList.setTvlDetail(tvlDetailList);
		
		System.out.println("TagValidationList ::"+tvlList.getTvlHeader().getTotalRecordCount()+"\t Detail Count :: \t"+tvlList.getTvlDetail().get(0).getTvlTagDetails().size());
		
		try {
			String filePath = validateParam.getOutputFilePath() + File.separator + filename;
            // Step 2: Create JAXB context and instantiate marshaller
            JAXBContext context = JAXBContext.newInstance(TagValidationList.class);
            Marshaller marshaller = context.createMarshaller();

            // Optional: Set the marshaller property to format the XML output
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

            // Step 3: Create an TVL object and marshal it to XML
            marshaller.marshal(tvlList, new File(filePath));
            
            String zipFilename = commonUtil.moveToZipFile(filePath,validateParam);
            validateParam.setResponseMsg(" File created ::\t "+zipFilename);
            long end = System.currentTimeMillis();
    		log.info("File Creation time ::"+(end - start) / 1000f + " seconds");
        } catch (JAXBException e) {
            e.printStackTrace();
            validateParam.setResponseMsg("TVL file creation issue. Please check logs");
            return  false;
        }
		
		 System.out.println("XML file generated successfully! ::" +validateParam.getOutputFilePath()+File.separator+filename);
		return true;
	}

	private List<TVLTagDetails> getTVLDetailsList(FileValidationParam validateParam) {
		List<TVLTagDetails> tvlTagDetails = new ArrayList<>();
		for (Map.Entry<String, NiopAgencyEntity> entry : NiopValidationController.cscIdTagNiopAgencyMap.entrySet()) {
			NiopAgencyEntity niopAgEntity = entry.getValue();
			System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());
		/*	recordcount = recordcount
					+ (Integer.parseInt(agEntity.getTagSequenceEnd()) - Integer.parseInt(agEntity.getTagSequenceStart())) ;*/
			long tagRangeCount = (Integer.parseInt(niopAgEntity.getTagSequenceEnd()) - Integer.parseInt(niopAgEntity.getTagSequenceStart()))+1;
			long validCount = (tagRangeCount * AgencyDataExcelReader.tagValid) /100; 
			long lowbalCount = (tagRangeCount * AgencyDataExcelReader.tagLowBal) /100; 
			long zeroNegativeCount = (tagRangeCount * AgencyDataExcelReader.tagZeroNegativeBal) /100; 
			log.info("Detail tagRangeCount --- "+tagRangeCount);
			log.info("Detail Tag Valid count :: "+validCount +"\t lowbalCount :: "+lowbalCount +"\t zeroNegativeCount :: "+zeroNegativeCount);
			this.tagSequenceStart = Integer.parseInt(niopAgEntity.getTagSequenceStart());
			//recordcount = recordcount + validCount + lowbalCount +zeroNegativeCount;
			for (long count = 1; count <= validCount; count++) {
				tvlTagDetails.add(getTVLTagDetail(validateParam,"V",niopAgEntity.getTagAgencyID().trim(),count));
			}
			for (long count = 1; count <= lowbalCount; count++) {
				tvlTagDetails.add(getTVLTagDetail(validateParam,"Z",niopAgEntity.getTagAgencyID().trim(),count));
			}
			for (long count = 1; count <= zeroNegativeCount; count++) {
				tvlTagDetails.add(getTVLTagDetail(validateParam,"I",niopAgEntity.getTagAgencyID().trim(),count));
			}
			
		}
		return tvlTagDetails;
	}
	
	private String getPlateRandomNo(int length) {
		//int length = 6;
		String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
		             + "0123456789";
			String plateNo = new Random().ints(length, 0, chars.length())
                    .mapToObj(i -> "" + chars.charAt(i))
                    .collect(Collectors.joining());
		return plateNo;
	
	}

	private TVLTagDetails getTVLTagDetail(FileValidationParam validateParam, String tagStatus, String tagAgencyId,long count) {
		boolean plateFlag = count % 2 == 0;
		TVLTagDetails tvlTagDet = new TVLTagDetails();
		tvlTagDet.setHomeAgencyId(validateParam.getFromAgency());
		tvlTagDet.setTagAgencyId(tagAgencyId);
		tvlTagDet.setTagSerialNumber(String.valueOf(tagSequenceStart));
		tvlTagDet.setTagStatus(tagStatus);
		tvlTagDet.setTagClass(2); //Default to 2
		if(tagStatus.equals("V") && plateFlag) {
			List<TVLPlateDetails> tvlPlateDetails = new ArrayList<>();
			TVLPlateDetails plateDetail = new TVLPlateDetails();
			plateDetail.setPlateCountry("US");
			plateDetail.setPlateState("GA");
			plateDetail.setPlateNumber(getPlateRandomNo(6));
			tvlPlateDetails.add(plateDetail);
			tvlTagDet.setTvlPlateDetails(tvlPlateDetails);
		}
		tagSequenceStart++;
		return tvlTagDet;
	}
	private TVLHeader getTVLHeader(FileValidationParam validateParam) {
		TVLHeader header = null;
		fileCreateDateandTime = commonUtil.getUTCDateandTime();
		log.info("fileCreateDateandTime ::"+fileCreateDateandTime);
		fileCreateDateandTime = validateParam.getFileDate()+fileCreateDateandTime.substring(fileCreateDateandTime.indexOf("T"),fileCreateDateandTime.length());
		log.info("After append fileCreateDateandTime ::"+fileCreateDateandTime);
		NiopAgencyEntity agEntity = NiopValidationController.allCscIdNiopAgencyMap.get(validateParam.getFromAgency());
		if(agEntity ==null ) {
			validateParam.setResponseMsg("Please check agency configuration");
			return header;
		}
		//Set file name to class variable
		filename = agEntity.getHubId() +"_"+ validateParam.getFromAgency() +"_"+ fileCreateDateandTime.replaceAll("[-T:Z]", "");
		
		header = new TVLHeader();
		header.setSubmittedFileType("STVL");
		header.setSubmittedDateTime(fileCreateDateandTime);
		header.setSSIOPHubIdNumber(agEntity.getHubId().toString());
		header.setHomeAgencyIdNumber(validateParam.getFromAgency());
		header.setBulkIdentifierValue(validateParam.getBulkIdentifier().toString());
		if (validateParam.getFileType().equals(NIOPConstants.BTVL_FILE_TYPE)) {
			header.setBulkInd("B");
			filename = filename + NIOPConstants.BTVL_FILE_EXTENSION;
		} else if (validateParam.getFileType().equals(NIOPConstants.DTVL_FILE_TYPE)) {
			filename = filename + NIOPConstants.DTVL_FILE_EXTENSION;
			header.setBulkInd("D");
		}
		log.info("AgencyDataExcelReader.tagValid :: "+AgencyDataExcelReader.tagValid +"\t AgencyDataExcelReader.tagLowBal :: "+AgencyDataExcelReader.tagLowBal +"\t AgencyDataExcelReader.tagZeroNegativeBal :: "+AgencyDataExcelReader.tagZeroNegativeBal);
		log.info("cscIdTagNiopAgencyMap ::"+ NiopValidationController.cscIdTagNiopAgencyMap);
		long recordcount = 0;
		for (Map.Entry<String, NiopAgencyEntity> entry : NiopValidationController.cscIdTagNiopAgencyMap.entrySet()) {
			NiopAgencyEntity niopAgEntity = entry.getValue();
			System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());
		/*	recordcount = recordcount
					+ (Integer.parseInt(agEntity.getTagSequenceEnd()) - Integer.parseInt(agEntity.getTagSequenceStart())) ;*/
			long tagRangeCount = (Integer.parseInt(niopAgEntity.getTagSequenceEnd()) - Integer.parseInt(niopAgEntity.getTagSequenceStart()))+1;
			long validCount = (tagRangeCount * AgencyDataExcelReader.tagValid) /100; 
			long lowbalCount = (tagRangeCount * AgencyDataExcelReader.tagLowBal) /100; 
			long zeroNegativeCount = (tagRangeCount * AgencyDataExcelReader.tagZeroNegativeBal) /100; 
			log.info("tagRangeCount --- "+tagRangeCount);
			log.info("Tag Valid count :: "+validCount +"\t lowbalCount :: "+lowbalCount +"\t zeroNegativeCount :: "+zeroNegativeCount);
			recordcount = recordcount + validCount + lowbalCount +zeroNegativeCount;
		}
		log.info("Record count ::" +recordcount);
		header.setTotalRecordCount(String.valueOf(recordcount));
		return header;
	}

}
