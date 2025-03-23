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
import com.apolloits.util.modal.niop.TagValidationList;
import com.apolloits.util.modal.niop.stran.EntryData;
import com.apolloits.util.modal.niop.stran.PlateInfo;
import com.apolloits.util.modal.niop.stran.TagInfo;
import com.apolloits.util.modal.niop.stran.TransactionData;
import com.apolloits.util.modal.niop.stran.TransactionDetail;
import com.apolloits.util.modal.niop.stran.TransactionHeader;
import com.apolloits.util.modal.niop.stran.TransactionRecord;
import com.apolloits.util.utility.CommonUtil;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class STRANFileGenerator {

	private String filename = "";
	private String fileCreateDateandTime = "";
	long tagSequenceStart = 0;
	int tagSequenceEnd = 0;
	NiopAgencyEntity agency;
	List<TransactionRecord> TranRecordList;
	
	
	@Autowired
	CommonUtil commonUtil;
	
	public boolean stranGen(FileValidationParam validateParam) {

		/*if (!commonUtil.validateInfoFileGenParameter(validateParam)) {
			return false;
		}*/
		long start = System.currentTimeMillis();
		TranRecordList = getSTRANTemplateExcel(validateParam);
		TransactionHeader header = getStranHeader(validateParam,TranRecordList);
		
		//Set value to object to write file
		TransactionData tranData= new TransactionData();
		tranData.setTransactionHeader(header);
		
		TransactionDetail transactionDetail = new TransactionDetail();
		transactionDetail.setTransactionRecord(TranRecordList);
		
		tranData.setTransactionDetail(transactionDetail);
		writeFile(tranData,validateParam);
		long end = System.currentTimeMillis();
		log.info("File Creation time ::"+(end - start) / 1000f + " seconds");
		return true;
	}

	private boolean writeFile(TransactionData tranData, FileValidationParam validateParam) {
		
		try {
			String filePath = validateParam.getOutputFilePath() + File.separator + filename;
            // Step 2: Create JAXB context and instantiate marshaller
            JAXBContext context = JAXBContext.newInstance(TransactionData.class);
            Marshaller marshaller = context.createMarshaller();

            // Optional: Set the marshaller property to format the XML output
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

            // Step 3: Create an TVL object and marshal it to XML
            marshaller.marshal(tranData, new File(filePath));
            
            String zipFilename = commonUtil.moveToZipFile(filePath,validateParam);
            validateParam.setResponseMsg(" File created ::\t "+zipFilename);
            
        } catch (JAXBException e) {
            e.printStackTrace();
            validateParam.setResponseMsg("STRAN file creation issue. Please check logs");
            return  false;
        }
		return true;
	}

	private List<TransactionRecord> getSTRANTemplateExcel(FileValidationParam validateParam) {
		String STRAN_SHEET = "STRAN";
		
		try {
			log.info("Excel data path localtion form getInputFilePath ::"+validateParam.getInputFilePath());
			FileInputStream is = new FileInputStream(validateParam.getInputFilePath());
			Workbook workbook = new XSSFWorkbook(is);
			log.info("Number of sheets : " + workbook.getNumberOfSheets());

			TranRecordList = excelToSTRANList(workbook.getSheet(STRAN_SHEET),validateParam);

		} catch (Exception e) {
			validateParam.setResponseMsg("File not generated Please check input excel data");
			e.printStackTrace();
			return TranRecordList = new ArrayList<>();
		}
		return TranRecordList;
	}

	private List<TransactionRecord> excelToSTRANList(Sheet sheet, FileValidationParam validateParam) {
		log.info("Inside ****************** excelToITXCList()");
        try {
       	
          Iterator<Row> rows = sheet.iterator();
          TranRecordList = new ArrayList<>();
          int rowNumber = 0;
          while (rows.hasNext()) {
            Row currentRow = rows.next();
            // skip header
            if (rowNumber == 0) {
              rowNumber++;
              continue;
            }
           // Iterator<Cell> cellsInRow = currentRow.iterator();
            TransactionRecord tranRecord = new TransactionRecord();
            tranRecord.setTxnDataSeqNo(commonUtil.getStringFormatCell(currentRow.getCell(0,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
            tranRecord.setRecordType(commonUtil.getStringFormatCell(currentRow.getCell(1,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
            tranRecord.setTxnReferenceID(commonUtil.getStringFormatCell(currentRow.getCell(2,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
            tranRecord.setExitDateTime(commonUtil.getStringFormatCell(currentRow.getCell(3,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
            tranRecord.setFacilityID(commonUtil.getStringFormatCell(currentRow.getCell(4,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
            tranRecord.setFacilityDesc(commonUtil.getStringFormatCell(currentRow.getCell(5,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
            tranRecord.setExitPlaza(commonUtil.getStringFormatCell(currentRow.getCell(6,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
            tranRecord.setExitPlazaDesc(commonUtil.getStringFormatCell(currentRow.getCell(7,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
            tranRecord.setExitLane(commonUtil.getStringFormatCell(currentRow.getCell(8,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
            
           String entryDateTime = commonUtil.getStringFormatCell(currentRow.getCell(9,MissingCellPolicy.CREATE_NULL_AS_BLANK));
            if(entryDateTime != null && !entryDateTime.isEmpty()) {
            	log.info("Entry detail found");
        	   EntryData entryData = new EntryData();
        	   entryData.EntryDateTime = entryDateTime;
        	   entryData.setEntryPlaza(commonUtil.getStringFormatCell(currentRow.getCell(10,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
        	   entryData.setEntryPlazaDesc(commonUtil.getStringFormatCell(currentRow.getCell(11,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
        	   entryData.setEntryLane(commonUtil.getStringFormatCell(currentRow.getCell(12,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
        	   tranRecord.setEntryData(entryData);
            }
            TagInfo tagInfo = new TagInfo();
            tagInfo.setTagAgencyID(commonUtil.getStringFormatCell(currentRow.getCell(13,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
            tagInfo.setTagSerialNo(commonUtil.getStringFormatCell(currentRow.getCell(14,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
            tagInfo.setTagStatus(commonUtil.getStringFormatCell(currentRow.getCell(15,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
            tranRecord.setTagInfo(tagInfo);
            
            String occupancyInd = commonUtil.getStringFormatCell(currentRow.getCell(16,MissingCellPolicy.CREATE_NULL_AS_BLANK));
            if(occupancyInd != null && !occupancyInd.isEmpty()) {
            	tranRecord.setOccupancyInd(occupancyInd);
            }
            String vehicleClass = commonUtil.getStringFormatCell(currentRow.getCell(17,MissingCellPolicy.CREATE_NULL_AS_BLANK));
            if(vehicleClass != null && !vehicleClass.isEmpty()) {
            tranRecord.setVehicleClass(vehicleClass);
            }
            tranRecord.setTollAmount(commonUtil.getStringFormatCell(currentRow.getCell(18,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
            
            String discountPlanType = commonUtil.getStringFormatCell(currentRow.getCell(19,MissingCellPolicy.CREATE_NULL_AS_BLANK));
			if (discountPlanType != null && !discountPlanType.isEmpty()) {
				tranRecord.setDiscountPlanType(discountPlanType);
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
            	tranRecord.setPlateInfo(plateInfo);
            }
            String vehicleClassAdj = commonUtil.getStringFormatCell(currentRow.getCell(24,MissingCellPolicy.CREATE_NULL_AS_BLANK));
			
            if (vehicleClassAdj != null && !vehicleClassAdj.isEmpty()) {
				tranRecord.setVehicleClassAdj(vehicleClassAdj);
			}
            String systemMatchInd = commonUtil.getStringFormatCell(currentRow.getCell(25,MissingCellPolicy.CREATE_NULL_AS_BLANK));
            if(systemMatchInd!= null && !systemMatchInd.isEmpty()) {
            tranRecord.setSystemMatchInd(systemMatchInd);
            }
            
            String spare = commonUtil.getStringFormatCell(currentRow.getCell(26,MissingCellPolicy.CREATE_NULL_AS_BLANK));
			if (spare != null && !spare.isEmpty()) {
				tranRecord.setSpare1(spare);
			}
			spare = commonUtil.getStringFormatCell(currentRow.getCell(27,MissingCellPolicy.CREATE_NULL_AS_BLANK));
			if (spare != null && !spare.isEmpty()) {
				tranRecord.setSpare2(spare);
			}
			
			spare = commonUtil.getStringFormatCell(currentRow.getCell(28,MissingCellPolicy.CREATE_NULL_AS_BLANK));
			if (spare != null && !spare.isEmpty()) {
				tranRecord.setSpare3(spare);
			}
			
			spare = commonUtil.getStringFormatCell(currentRow.getCell(29,MissingCellPolicy.CREATE_NULL_AS_BLANK));
			if (spare != null && !spare.isEmpty()) {
				tranRecord.setSpare4(spare);
			}
			
			spare = commonUtil.getStringFormatCell(currentRow.getCell(30,MissingCellPolicy.CREATE_NULL_AS_BLANK));
			if (spare != null && !spare.isEmpty()) {
				tranRecord.setSpare5(spare);
			}
            
            tranRecord.setExitDateTimeTZ(commonUtil.getStringFormatCell(currentRow.getCell(31,MissingCellPolicy.CREATE_NULL_AS_BLANK)));
            
            String entryTimeZone = commonUtil.getStringFormatCell(currentRow.getCell(32,MissingCellPolicy.CREATE_NULL_AS_BLANK));
            if(entryTimeZone != null && !entryTimeZone.isEmpty()) {
            	tranRecord.setEntryDateTimeTZ(entryTimeZone);
            }
            
            TranRecordList.add(tranRecord);
            System.out.println("tranRecord :: "+tranRecord.toString());
          }
         
          if(TranRecordList != null && TranRecordList.size()>0) {
          log.info("@@@@ STRAN input data  loaded sucessfully:: ******************** ::"+TranRecordList.size());
          }else {
       	   throw new IopTranslatorException("STRAN input data not loaded");
          }
          
        }catch (NullPointerException e) {
        	validateParam.setResponseMsg("Excel STRAN  sheet not found. Please check sheet");
	       	log.error("NullPointerException:: ******************** STRAN Sheet");
				e.printStackTrace();
			}catch (Exception e) {
       	log.error("Exception:: ******************** STRAN Sheet");
			e.printStackTrace();
		}
      
		return TranRecordList;
	}

	private TransactionHeader getStranHeader(FileValidationParam validateParam,
			List<TransactionRecord> TranRecordList) {
		
		TransactionHeader stranHeader = null;
		fileCreateDateandTime = commonUtil.getUTCDateandTime();
		log.info("fileCreateDateandTime ::" + fileCreateDateandTime);
		fileCreateDateandTime = validateParam.getFileDate()
				+ fileCreateDateandTime.substring(fileCreateDateandTime.indexOf("T"), fileCreateDateandTime.length());
		log.info("After append fileCreateDateandTime ::" + fileCreateDateandTime);
		NiopAgencyEntity agEntity = NiopValidationController.allCscIdNiopAgencyMap.get(validateParam.getFromAgency());
		if (agEntity == null) {
			validateParam.setResponseMsg("Please check agency configuration");
			return stranHeader;
		}
		// Set file name to class variable
		filename = agEntity.getHubId() + "_" + validateParam.getFromAgency() + "_" + validateParam.getToAgency() + "_"
				+ fileCreateDateandTime.replaceAll("[-T:Z]", "") + NIOPConstants.STRAN_FILE_EXTENSION;
		
		log.info("STRAN file Name ::"+filename);
		
		stranHeader = new TransactionHeader();
		stranHeader.setSubmissionType(NIOPConstants.STRAN_FILE_TYPE);
		stranHeader.setSubmissionDateTime(fileCreateDateandTime);
		stranHeader.setSsiopHubID(agEntity.getHubId().toString());
		stranHeader.setAwayAgencyID(validateParam.getFromAgency());
		stranHeader.setHomeAgencyID(validateParam.getToAgency());
		stranHeader.setTxnDataSeqNo(TranRecordList.get(0).getTxnDataSeqNo());
		stranHeader.setRecordCount(String.valueOf(TranRecordList.size()));
		return stranHeader;
	}
}
