package com.apolloits.util.generate.niop;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.apolloits.util.IopTranslatorException;
import com.apolloits.util.NIOPConstants;
import com.apolloits.util.controller.NiopValidationController;
import com.apolloits.util.modal.FileValidationParam;
import com.apolloits.util.modal.NiopAgencyEntity;
import com.apolloits.util.modal.niop.scorr.CorrectionDetail;
import com.apolloits.util.modal.niop.scorr.CorrectionRecord;
import com.apolloits.util.modal.niop.scorr.OriginalTransactionDetail;
import com.apolloits.util.modal.niop.scorr.ScorrFile;
import com.apolloits.util.modal.niop.scorr.ScorrHeader;
import com.apolloits.util.modal.niop.stran.EntryData;
import com.apolloits.util.modal.niop.stran.PlateInfo;
import com.apolloits.util.modal.niop.stran.TagInfo;
import com.apolloits.util.utility.CommonUtil;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SCORRFileGenerator {

	private String filename = "";
	private String fileCreateDateandTime = "";
	long tagSequenceStart = 0;
	int tagSequenceEnd = 0;
	NiopAgencyEntity agency;
	List<CorrectionRecord> corrRecordList;
	String txnDataSeqNo ="";
	
	@Autowired
	CommonUtil commonUtil;
	
	public boolean scorrGen(FileValidationParam validateParam) {
		long start = System.currentTimeMillis();
		corrRecordList = getSCORRTemplateExcel(validateParam);
		ScorrHeader scorrHeader = getStranHeader(validateParam,corrRecordList);
		
		//set value to object to write file
		ScorrFile scorrFile = new ScorrFile();
		scorrFile.setScorrHeader(scorrHeader);
		
		CorrectionDetail corrDet = new CorrectionDetail();
		corrDet.setCorrectionRecordList(corrRecordList);
		scorrFile.setCorrectionDetail(corrDet);
		writeFile(scorrFile,validateParam);
		long end = System.currentTimeMillis();
		log.info("File Creation time ::"+(end - start) / 1000f + " seconds");
		return true;
	}
	
private boolean writeFile(ScorrFile scorrData, FileValidationParam validateParam) {
		
		try {
			String filePath = validateParam.getOutputFilePath() + File.separator + filename;
            // Step 2: Create JAXB context and instantiate marshaller
            JAXBContext context = JAXBContext.newInstance(ScorrFile.class);
            Marshaller marshaller = context.createMarshaller();

            // Optional: Set the marshaller property to format the XML output
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

            // Step 3: Create an TVL object and marshal it to XML
            marshaller.marshal(scorrData, new File(filePath));
            
            String zipFilename = commonUtil.moveToZipFile(filePath,validateParam);
            validateParam.setResponseMsg(" File created ::\t "+zipFilename);
            
        } catch (JAXBException e) {
            e.printStackTrace();
            validateParam.setResponseMsg("SCORR file creation issue. Please check logs");
            return  false;
        }
		return true;
	}

	private ScorrHeader getStranHeader(FileValidationParam validateParam, List<CorrectionRecord> corrRecordList) {
		
		ScorrHeader scorrHeader = null;
		fileCreateDateandTime = commonUtil.getUTCDateandTime();
		log.info("fileCreateDateandTime ::" + fileCreateDateandTime);
		fileCreateDateandTime = validateParam.getFileDate()
				+ fileCreateDateandTime.substring(fileCreateDateandTime.indexOf("T"), fileCreateDateandTime.length());
		log.info("After append fileCreateDateandTime ::" + fileCreateDateandTime);
		NiopAgencyEntity agEntity = NiopValidationController.allCscIdNiopAgencyMap.get(validateParam.getFromAgency());
		if (agEntity == null) {
			validateParam.setResponseMsg("Please check agency configuration");
			return scorrHeader;
		}
		// Set file name to class variable
		filename = agEntity.getHubId() + "_" + validateParam.getFromAgency() + "_" + validateParam.getToAgency() + "_"
				+ fileCreateDateandTime.replaceAll("[-T:Z]", "") + NIOPConstants.SCORR_FILE_EXTENSION;
		
		log.info("SCORR file Name ::"+filename);
		
		scorrHeader = new ScorrHeader();
		scorrHeader.setSubmissionType(NIOPConstants.SCORR_FILE_TYPE);
		scorrHeader.setSubmissionDateTime(fileCreateDateandTime);
		scorrHeader.setSsiopHubID(agEntity.getHubId().toString());
		scorrHeader.setAwayAgencyID(validateParam.getFromAgency());
		scorrHeader.setHomeAgencyID(validateParam.getToAgency());
		scorrHeader.setTxnDataSeqNo(txnDataSeqNo);
		scorrHeader.setRecordCount(String.valueOf(corrRecordList.size()));
		return scorrHeader;
	}

	private List<CorrectionRecord> getSCORRTemplateExcel(FileValidationParam validateParam) {
		String STRAN_SHEET = "SCORR";

		try {
			log.info("Excel data path localtion form getInputFilePath ::" + validateParam.getInputFilePath());
			FileInputStream is = new FileInputStream(validateParam.getInputFilePath());
			Workbook workbook = new XSSFWorkbook(is);
			log.info("Number of sheets : " + workbook.getNumberOfSheets());

			corrRecordList = excelToSCORRList(workbook.getSheet(STRAN_SHEET), validateParam);

		} catch (Exception e) {
			validateParam.setResponseMsg("File not generated Please check input excel data");
			e.printStackTrace();
			return corrRecordList = new ArrayList<>();
		}
		return corrRecordList;
	}

	private List<CorrectionRecord> excelToSCORRList(Sheet sheet, FileValidationParam validateParam) {
		
		log.info("Inside ****************** excelToSCORRList()");
        try {
       	
          Iterator<Row> rows = sheet.iterator();
          corrRecordList = new ArrayList<>();
          int rowNumber = 0;
          while (rows.hasNext()) {
            Row currentRow = rows.next();
            // skip header
            if (rowNumber == 0) {
              rowNumber++;
              continue;
            }
            
         
          CorrectionRecord corrrecord = new CorrectionRecord();
          //corrrecord.setTxnDataSeqNo(commonUtil.getStringFormatCell(currentRow.getCell(0,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
          txnDataSeqNo = commonUtil.getStringFormatCell(currentRow.getCell(0,MissingCellPolicy.CREATE_NULL_AS_BLANK));
          corrrecord.setRecordType(commonUtil.getStringFormatCell(currentRow.getCell(1,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
          String corrDateTime = commonUtil.getStringFormatCell(currentRow.getCell(2,MissingCellPolicy.CREATE_NULL_AS_BLANK));
          if(corrDateTime != null && !corrDateTime.isEmpty()) {
        	  corrrecord.setCorrectionDateTime(corrDateTime);
          }
          String corrReason = commonUtil.getStringFormatCell(currentRow.getCell(3,MissingCellPolicy.CREATE_NULL_AS_BLANK));
          if(corrReason != null && !corrReason.isEmpty()) {
          corrrecord.setCorrectionReason(corrReason);
          }
          String resubmitReason = commonUtil.getStringFormatCell(currentRow.getCell(4,MissingCellPolicy.CREATE_NULL_AS_BLANK));
          if(resubmitReason != null && !resubmitReason.isEmpty()) {
        	  corrrecord.setResubmitReason(resubmitReason);
          }
          String corrOtherDesc = commonUtil.getStringFormatCell(currentRow.getCell(5,MissingCellPolicy.CREATE_NULL_AS_BLANK));
          if(corrOtherDesc != null && !corrOtherDesc.isEmpty()) {
          corrrecord.setCorrectionOtherDesc(corrOtherDesc);
          }
          String corrSeqNo = commonUtil.getStringFormatCell(currentRow.getCell(6,MissingCellPolicy.CREATE_NULL_AS_BLANK));
          if(corrSeqNo != null && !corrSeqNo.isEmpty()) {
          corrrecord.setCorrectionSeqNo(corrSeqNo);
          }else {
        	  corrrecord.setCorrectionSeqNo("0");
          }
          String resubmitCount = commonUtil.getStringFormatCell(currentRow.getCell(7,MissingCellPolicy.CREATE_NULL_AS_BLANK));
          if(resubmitCount != null && !resubmitCount.isEmpty()) {
          corrrecord.setResubmitCount(resubmitCount);
          }
          String homeAgencyTxnRefID = commonUtil.getStringFormatCell(currentRow.getCell(8,MissingCellPolicy.CREATE_NULL_AS_BLANK));
          if(homeAgencyTxnRefID != null && !homeAgencyTxnRefID.isEmpty()) {
          corrrecord.setHomeAgencyTxnRefID(homeAgencyTxnRefID);
          }
          OriginalTransactionDetail oriTranDet = new OriginalTransactionDetail();
          
          oriTranDet.setRecordType(commonUtil.getStringFormatCell(currentRow.getCell(1,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
          oriTranDet.setTxnReferenceID(commonUtil.getStringFormatCell(currentRow.getCell(2,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
          oriTranDet.setExitDateTime(commonUtil.getStringFormatCell(currentRow.getCell(3,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
          oriTranDet.setFacilityID(commonUtil.getStringFormatCell(currentRow.getCell(4,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
          oriTranDet.setFacilityDesc(commonUtil.getStringFormatCell(currentRow.getCell(5,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
          oriTranDet.setExitPlaza(commonUtil.getStringFormatCell(currentRow.getCell(6,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
          oriTranDet.setExitPlazaDesc(commonUtil.getStringFormatCell(currentRow.getCell(7,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
          oriTranDet.setExitLane(commonUtil.getStringFormatCell(currentRow.getCell(8,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
          
         String entryDateTime = commonUtil.getStringFormatCell(currentRow.getCell(9,MissingCellPolicy.CREATE_NULL_AS_BLANK));
          if(entryDateTime != null && !entryDateTime.isEmpty()) {
          	log.info("Entry detail found");
      	   EntryData entryData = new EntryData();
      	   entryData.EntryDateTime = entryDateTime;
      	   entryData.setEntryPlaza(commonUtil.getStringFormatCell(currentRow.getCell(10,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
      	   entryData.setEntryPlazaDesc(commonUtil.getStringFormatCell(currentRow.getCell(11,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
      	   entryData.setEntryLane(commonUtil.getStringFormatCell(currentRow.getCell(12,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
      	 oriTranDet.setEntryData(entryData);
          }
          TagInfo tagInfo = new TagInfo();
          tagInfo.setTagAgencyID(commonUtil.getStringFormatCell(currentRow.getCell(13,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
          tagInfo.setTagSerialNo(commonUtil.getStringFormatCell(currentRow.getCell(14,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
          tagInfo.setTagStatus(commonUtil.getStringFormatCell(currentRow.getCell(15,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
          oriTranDet.setTagInfo(tagInfo);
          
          String occupancyInd = commonUtil.getStringFormatCell(currentRow.getCell(16,MissingCellPolicy.CREATE_NULL_AS_BLANK));
          if(occupancyInd != null && !occupancyInd.isEmpty()) {
        	  oriTranDet.setOccupancyInd(occupancyInd);
          }
          String vehicleClass = commonUtil.getStringFormatCell(currentRow.getCell(17,MissingCellPolicy.CREATE_NULL_AS_BLANK));
          if(vehicleClass != null && !vehicleClass.isEmpty()) {
        	  oriTranDet.setVehicleClass(vehicleClass);
          }
          oriTranDet.setTollAmount(commonUtil.getStringFormatCell(currentRow.getCell(18,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
          
          String discountPlanType = commonUtil.getStringFormatCell(currentRow.getCell(19,MissingCellPolicy.CREATE_NULL_AS_BLANK));
			if (discountPlanType != null && !discountPlanType.isEmpty()) {
				oriTranDet.setDiscountPlanType(discountPlanType);
			}
          
          String licPlateNo = commonUtil.getStringFormatCell(currentRow.getCell(22,MissingCellPolicy.CREATE_NULL_AS_BLANK));
         
          if(licPlateNo != null && !licPlateNo.isEmpty()) {
          	PlateInfo plateInfo = new PlateInfo();
          	plateInfo.setPlateCountry(commonUtil.getStringFormatCell(currentRow.getCell(20,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
          	plateInfo.setPlateState(commonUtil.getStringFormatCell(currentRow.getCell(21,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
          	plateInfo.setPlateNumber(commonUtil.getStringFormatCell(currentRow.getCell(22,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
          	String plateType = commonUtil.getStringFormatCell(currentRow.getCell(23,MissingCellPolicy.CREATE_NULL_AS_BLANK));
				if (plateType != null && !plateType.isEmpty())
					plateInfo.setPlateType(plateType);
				oriTranDet.setPlateInfo(plateInfo);
          }
          String vehicleClassAdj = commonUtil.getStringFormatCell(currentRow.getCell(24,MissingCellPolicy.CREATE_NULL_AS_BLANK));
			
          if (vehicleClassAdj != null && !vehicleClassAdj.isEmpty()) {
        	  oriTranDet.setVehicleClassAdj(vehicleClassAdj);
			}
          String systemMatchInd = commonUtil.getStringFormatCell(currentRow.getCell(25,MissingCellPolicy.CREATE_NULL_AS_BLANK));
          if(systemMatchInd!= null && !systemMatchInd.isEmpty()) {
        	  oriTranDet.setSystemMatchInd(systemMatchInd);
          }
          
          String spare = commonUtil.getStringFormatCell(currentRow.getCell(26,MissingCellPolicy.CREATE_NULL_AS_BLANK));
			if (spare != null && !spare.isEmpty()) {
				oriTranDet.setSpare1(spare);
			}
			spare = commonUtil.getStringFormatCell(currentRow.getCell(27,MissingCellPolicy.CREATE_NULL_AS_BLANK));
			if (spare != null && !spare.isEmpty()) {
				oriTranDet.setSpare2(spare);
			}
			
			spare = commonUtil.getStringFormatCell(currentRow.getCell(28,MissingCellPolicy.CREATE_NULL_AS_BLANK));
			if (spare != null && !spare.isEmpty()) {
				oriTranDet.setSpare3(spare);
			}
			
			spare = commonUtil.getStringFormatCell(currentRow.getCell(29,MissingCellPolicy.CREATE_NULL_AS_BLANK));
			if (spare != null && !spare.isEmpty()) {
				oriTranDet.setSpare4(spare);
			}
			
			spare = commonUtil.getStringFormatCell(currentRow.getCell(30,MissingCellPolicy.CREATE_NULL_AS_BLANK));
			if (spare != null && !spare.isEmpty()) {
				oriTranDet.setSpare5(spare);
			}
          
			oriTranDet.setExitDateTimeTZ(commonUtil.getStringFormatCell(currentRow.getCell(31,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
          
          String entryTimeZone = commonUtil.getStringFormatCell(currentRow.getCell(32,MissingCellPolicy.CREATE_NULL_AS_BLANK));
          if(entryTimeZone != null && !entryTimeZone.isEmpty()) {
        	  oriTranDet.setEntryDateTimeTZ(entryTimeZone);
          }
          corrrecord.setOriginalTransactionDetail(oriTranDet);
          
          corrRecordList.add(corrrecord);
          }
          if(corrRecordList != null && corrRecordList.size()>0) {
          log.info("@@@@ SCORR input data  loaded sucessfully:: ******************** ::"+corrRecordList.size());
          }else {
       	   throw new IopTranslatorException("SCORR input data not loaded");
          }
          
		} catch (NullPointerException e) {
			validateParam.setResponseMsg("Excel STRAN  sheet not found. Please check sheet");
			log.error("NullPointerException:: ******************** STRAN Sheet");
			e.printStackTrace();
		} catch (Exception e) {
				log.error("Exception:: ******************** SCORR Sheet");
				e.printStackTrace();
			}
      
		return corrRecordList;
	}
}
