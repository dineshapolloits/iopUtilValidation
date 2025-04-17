package com.apolloits.util.reader;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.apolloits.util.IopTranslatorException;
import com.apolloits.util.modal.AgencyEntity;
import com.apolloits.util.modal.NiopAgencyEntity;
import com.apolloits.util.modal.PlateTypeEntity;
import com.apolloits.util.modal.UtilityParamEntity;
import com.apolloits.util.service.DatabaseLogger;
import com.apolloits.util.utility.CommonUtil;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Component
@Getter
@Slf4j
public class AgencyDataExcelReader {
	
	@Autowired
	DatabaseLogger dbLog;
	
	private List<AgencyEntity> agList;
	
	private List<NiopAgencyEntity> niopAgList;
	private List<AgencyEntity> ctocAgList;
	
	public static HashSet<String> agencyCode;
	public static HashSet<String> tagAgencyCode;
	private Map<String,UtilityParamEntity> utilParamMap;
	
	private HashSet<String> plateStateTypeSet; //This one will concat  plate state and type 
	
	private HashSet<String> plateStateSet; //This one only plate state store
	
	public static int tagValid = 0;
	public static int tagLowBal = 0;
	public static int tagZeroNegativeBal = 0;
	public static int tagInvalidBal = 0;
	//public static int tagInvalid = 0;
	public static String TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
	String AgencyListing_SHEET = "UtilityAgencyListing";
	String UtilityParam_SHEET = "UtilityParam";
	String PlateType_SHEET = "AppJ_PlateType_1_60";
	String ctocUtilityParam_SHEET = "Utility Param";
	@Value("${excel.data}")
	String dataPath;
	
	@Value("${excel.data.niop}")
	String niopDataPath;
	
	@Value("${CTOCexcel.data}")
	String ctocDataPath;
     //C:\Users\dselvaraj\git\iopUtilValidation\src\main\resources\Utlity Table.xlsx
     @PostConstruct
     public void init() throws IopTranslatorException {
    	 	//ClassPathResource resource = new ClassPathResource("Utlity Table.xlsx");
			InputStream inputStream;
			try {
				//inputStream = resource.getInputStream();
				log.info("Excel data path localtion form property file ::"+dataPath);
				FileInputStream is = new FileInputStream(dataPath);
				Workbook workbook = new XSSFWorkbook(is);
				log.info("Number of sheets : " + workbook.getNumberOfSheets());

				excelToAgencyList(workbook.getSheet(AgencyListing_SHEET));
				loadagencyCodeList();
				createUtilityParamtable(workbook.getSheet(UtilityParam_SHEET));
				
				loadPlateTypefromExcel(workbook.getSheet(PlateType_SHEET));
				//After all sheet read close workbook
				 workbook.close();
				 
				 //NIOP Utility Excel load
				 
				 log.info("NIOP Excel data path localtion form property file ::"+niopDataPath);
					FileInputStream niopIs = new FileInputStream(niopDataPath);
					Workbook niopWorkbook = new XSSFWorkbook(niopIs);
					log.info("NIOP Number of sheets : " + niopWorkbook.getNumberOfSheets());
					
					excelToNiopAgencyList(niopWorkbook.getSheet(AgencyListing_SHEET));

					log.info("Ctoc Excel data path location form property file ::"+ctocDataPath);
					FileInputStream fis = new FileInputStream(ctocDataPath);
					Workbook workBookCtoc = new XSSFWorkbook(fis);
					log.info("Number of sheets CTOC: " + workBookCtoc.getNumberOfSheets());
					excelToCtocAgencyList(workBookCtoc.getSheet(AgencyListing_SHEET),"CTOC");
					workBookCtoc.close();
					
				} catch (IOException e) {
					log.error("init exception in excel reader");
					e.printStackTrace();
					throw new RuntimeException("fail to parse Excel file: " + e.getMessage());

				}
			
    	 
     }
     
     private void excelToNiopAgencyList(Sheet sheet) {
    	 log.info("Inside NIOP****************** excelToNiopAgencyList()");
    	 CommonUtil commonUtil = new CommonUtil();
         try {
        	
           Iterator<Row> rows = sheet.iterator();
           List<NiopAgencyEntity> agList = new ArrayList<NiopAgencyEntity>();
           int rowNumber = 0;
           while (rows.hasNext()) {
             Row currentRow = rows.next();
             // skip header
             if (rowNumber == 0) {
               rowNumber++;
               continue;
             }
             Iterator<Cell> cellsInRow = currentRow.iterator();
             NiopAgencyEntity agency = new NiopAgencyEntity();
             int cellIdx = 0;
             while (cellsInRow.hasNext()) {
               Cell currentCell = cellsInRow.next();
               switch (cellIdx) {
               case 0:
            	   agency.setHomeAgency((int) Math.round(currentCell.getNumericCellValue()));
            	   System.out.println("case 0::"+currentCell.getNumericCellValue());
                 break;
               case 1:
                   agency.setAwayAgency((int) Math.round(currentCell.getNumericCellValue()));
                   System.out.println("case 1::"+currentCell.getNumericCellValue());
                 break;
               case 2:
                   agency.setCSCID(commonUtil.getStringFormatCell(currentCell));
                   System.out.println("case 2::"+agency.getCSCID());
                 break;
               case 3:
                 agency.setCSCName(commonUtil.getStringFormatCell(currentCell));
                 System.out.println("case 3::"+currentCell.getStringCellValue());
                 break;
               case 4:
                   agency.setCSCAgencyShortName(commonUtil.getStringFormatCell(currentCell));
                   System.out.println("case 4::"+currentCell.getStringCellValue());
                   break;
               case 5:
                   agency.setVersionNumber(commonUtil.getStringFormatCell(currentCell));
                   System.out.println("case 5::"+agency.getVersionNumber());
                   break;
               case 6:
                   agency.setHomeAgencyID(commonUtil.getStringFormatCell(currentCell));
                   System.out.println("case 6::"+agency.getHomeAgencyID());
                   break;
               case 7:
                   agency.setTagAgencyID(commonUtil.getStringFormatCell(currentCell));
                   System.out.println("case 7::"+currentCell.getStringCellValue());
                   break;
               case 8:
                   agency.setTagSequenceStart(commonUtil.getStringFormatCell(currentCell));
                   System.out.println("case 8::"+currentCell.getStringCellValue());
                   break;
               case 9:
                   agency.setTagSequenceEnd(commonUtil.getStringFormatCell(currentCell));
                   System.out.println("case 9::"+currentCell.getStringCellValue());
                   break;
               case 10:
            	   agency.setBTVL((int)currentCell.getNumericCellValue());
                   System.out.println("case 10::"+currentCell.getNumericCellValue());
                   break;
              /* case 11:
                   agency.setICLP((int) Math.round(currentCell.getNumericCellValue()));
                   System.out.println("case 11::"+currentCell.getNumericCellValue());
                   break;*/
               case 11:
                   agency.setHubName(commonUtil.getStringFormatCell(currentCell));
                   System.out.println("case 11::"+currentCell.getStringCellValue());
                   break;
               case 12:
                   agency.setHubId((int) currentCell.getNumericCellValue());
                   System.out.println("case 12::"+agency.getHubId());
                   break;
               default:
            	 //  System.out.println("Default:: ********************");
                 break;
               }
               cellIdx++;
             }
             agList.add(agency);
           }
          
           if(agList != null && agList.size()>0) {
           dbLog.saveNiopAgencyList(agList);
           log.info("@@@@ Agency List loaded sucessfully:: ********************");
           }else {
        	   throw new IopTranslatorException("Agency data not loaded");
           }
           this.niopAgList = agList;
          
           
         }catch (Exception e) {
        	log.error("NIOP Exception:: ******************** UtilityAgencyListing_SHEET");
			e.printStackTrace();
		}
       
	}

	public void excelToAgencyList(Sheet sheet) {
    	 log.info("Inside ****************** excelToAgencyList()");
         try {
        	
           Iterator<Row> rows = sheet.iterator();
           List<AgencyEntity> agList = new ArrayList<AgencyEntity>();
           int rowNumber = 0;
           while (rows.hasNext()) {
             Row currentRow = rows.next();
             // skip header
             if (rowNumber == 0) {
               rowNumber++;
               continue;
             }
             Iterator<Cell> cellsInRow = currentRow.iterator();
             AgencyEntity agency = new AgencyEntity();
             int cellIdx = 0;
             while (cellsInRow.hasNext()) {
               Cell currentCell = cellsInRow.next();
               switch (cellIdx) {
               case 0:
            	   agency.setHomeAgency((int) Math.round(currentCell.getNumericCellValue()));
            	   System.out.println("case 0::"+currentCell.getNumericCellValue());
                 break;
               case 1:
                   agency.setAwayAgency((int) Math.round(currentCell.getNumericCellValue()));
                   System.out.println("case 1::"+currentCell.getNumericCellValue());
                 break;
               case 2:
                   agency.setCSCID(currentCell.getStringCellValue());
                   System.out.println("case 2::"+currentCell.getStringCellValue());
                 break;
               case 3:
                 agency.setCSCName(currentCell.getStringCellValue());
                 System.out.println("case 3::"+currentCell.getStringCellValue());
                 break;
               case 4:
                   agency.setCSCAgencyShortName(currentCell.getStringCellValue());
                   System.out.println("case 4::"+currentCell.getStringCellValue());
                   break;
               case 5:
                   agency.setVersionNumber(currentCell.getStringCellValue());
                   System.out.println("case 5::"+currentCell.getStringCellValue());
                   break;
               case 6:
                   agency.setHomeAgencyID(currentCell.getStringCellValue());
                   System.out.println("case 6::"+currentCell.getStringCellValue());
                   break;
               case 7:
                   agency.setTagAgencyID(currentCell.getStringCellValue());
                   System.out.println("case 7::"+currentCell.getStringCellValue());
                   break;
               case 8:
                   agency.setTagSequenceStart(currentCell.getStringCellValue());
                   System.out.println("case 8::"+currentCell.getStringCellValue());
                   break;
               case 9:
                   agency.setTagSequenceEnd(currentCell.getStringCellValue());
                   System.out.println("case 9::"+currentCell.getStringCellValue());
                   break;
               case 10:
                   agency.setITAG((int) Math.round(currentCell.getNumericCellValue()));
                   System.out.println("case 10::"+currentCell.getNumericCellValue());
                   break;
               case 11:
                   agency.setICLP((int) Math.round(currentCell.getNumericCellValue()));
                   System.out.println("case 11::"+currentCell.getNumericCellValue());
                   break;
               case 12:
                   agency.setHubName(currentCell.getStringCellValue());
                   System.out.println("case 12::"+currentCell.getStringCellValue());
                   break;
               case 13:
                   agency.setHubId((int) Math.round(currentCell.getNumericCellValue()));
                   System.out.println("case 13::"+currentCell.getNumericCellValue());
                   break;
               default:
            	 //  System.out.println("Default:: ********************");
                 break;
               }
               cellIdx++;
             }
             agList.add(agency);
           }
          
           if(agList != null && agList.size()>0) {
           dbLog.saveAgencyList(agList);
           log.info("@@@@ Agency List loaded sucessfully:: ********************");
           }else {
        	   throw new IopTranslatorException("Agency data not loaded");
           }
           this.agList = agList;
          
           
         }catch (Exception e) {
        	log.error("Exception:: ******************** UtilityAgencyListing_SHEET");
			e.printStackTrace();
		}
       }
     /**
      *  Create table for UtilityParam table from UtilityParam sheet
      * @param sheet
      * @return
      */
     private void createUtilityParamtable(Sheet sheet) {
    	 log.info("Inside ****************** createUtilityParamtable()");
			try {
				utilParamMap = new HashMap<>();
				Iterator<Row> rows = sheet.iterator();
				List<UtilityParamEntity> utilParamList = new ArrayList<UtilityParamEntity>();
				int rowNumber = 0;
				while (rows.hasNext()) {
					Row currentRow = rows.next();
					// skip header
					if (rowNumber == 0) {
						rowNumber++;
						continue;
					}
					Iterator<Cell> cellsInRow = currentRow.iterator();
					UtilityParamEntity utilParam = new UtilityParamEntity();
					int cellIdx = 0;
					DataFormatter formatter = new DataFormatter();
					//String val = formatter.formatCellValue(
					while (cellsInRow.hasNext()) {
						Cell currentCell = cellsInRow.next();
						switch (cellIdx) {
						case 0:
							utilParam.setType(currentCell.getStringCellValue().trim());
							System.out.println("Type case 0::" + currentCell.getStringCellValue());
							break;
						case 1:
							utilParam.setSubType(formatter.formatCellValue(currentCell).trim());
							System.out.println("SubType case 1::" +formatter.formatCellValue(currentCell));
							break;
						case 2:
							utilParam.setTypeValue(currentCell.getStringCellValue().trim());
							System.out.println("value case 2::" + currentCell.getStringCellValue());
							break;
						default:
							// System.out.println("Default:: ********************");
							break;
						}
						cellIdx++;
					}
					utilParamList.add(utilParam);
					utilParamMap.put(utilParam.getType(), utilParam);
					if(utilParam.getType().equals("ITAG_Valid")) {
						tagValid = Integer.parseInt(utilParam.getTypeValue());
					}else if(utilParam.getType().equals("ITAG_LOW_Bal")) {
						tagLowBal = Integer.parseInt(utilParam.getTypeValue());
					}else if(utilParam.getType().equals("ITAG_ZERO_Negative")) {
						tagZeroNegativeBal = Integer.parseInt(utilParam.getTypeValue());
					}else if(utilParam.getType().equals("ITAG_Invalid")) {
						tagInvalidBal = Integer.parseInt(utilParam.getTypeValue());
					}
				}

				if (utilParamList != null && utilParamList.size() > 0) {
					dbLog.saveUtilParamList(utilParamList);
					log.info("@@@@ utilParam List loaded sucessfully:: ********************");
				} else {
					throw new IopTranslatorException("Agency data not loaded");
				}
				log.info("utilParamMap ::"+utilParamMap.toString());

			}catch (Exception e) {
        	log.error("Exception:: ******************** UtilityAgencyListing_SHEET");
			e.printStackTrace();
		}
    	 
     }
     
     private void loadPlateTypefromExcel(Sheet sheet) {
    	 

    	 log.info("Inside ****************** loadPlateTypefromExcel()");
			try {
				plateStateTypeSet = new HashSet<>();
				plateStateSet = new HashSet<>();
				Iterator<Row> rows = sheet.iterator();
				List<PlateTypeEntity> plateTypList = new ArrayList<PlateTypeEntity>();
				int rowNumber = 0;
				while (rows.hasNext()) {
					Row currentRow = rows.next();
					// skip header
					if (rowNumber == 0) {
						rowNumber++;
						continue;
					}
					Iterator<Cell> cellsInRow = currentRow.iterator();
					PlateTypeEntity plateTypeObj = new PlateTypeEntity();
					int cellIdx = 0;
					DataFormatter formatter = new DataFormatter();
					//String val = formatter.formatCellValue(
					while (cellsInRow.hasNext()) {
						Cell currentCell = cellsInRow.next();
						switch (cellIdx) {
						case 0:
							plateTypeObj.setLicState(currentCell.getStringCellValue().trim());
							System.out.println("Lic State case 0::" + currentCell.getStringCellValue());
							break;
						case 1:
							plateTypeObj.setLicType(currentCell.getStringCellValue());
							System.out.println("setLicType case 1::" +currentCell.getStringCellValue().trim());
							break;
						case 2:
							plateTypeObj.setPlateDesc(currentCell.getStringCellValue());
							System.out.println("setPlateDesc case 2::" + currentCell.getStringCellValue().trim());
							break;
						default:
							// System.out.println("Default:: ********************");
							break;
						}
						cellIdx++;
					}
					plateTypList.add(plateTypeObj);
					plateStateSet.add(plateTypeObj.getLicState());
					plateStateTypeSet.add(plateTypeObj.getLicState()+plateTypeObj.getLicType());
				}

				if (plateTypList != null && plateTypList.size() > 0) {
					dbLog.savePlateTypeList(plateTypList);
					log.info("@@@@ plateTypList List loaded sucessfully:: ******************** size ::"+plateTypList.size());
				} else {
					throw new IopTranslatorException("Plate type sheet  data not loaded");
				}
				log.info("plateStateTypeSet size ::"+plateStateTypeSet.size()+"\t plateStateTypeSet ::"+plateStateTypeSet.toString());

			}catch (Exception e) {
        	log.error("Exception:: ******************** UtilityAgencyListing_SHEET");
			e.printStackTrace();
		}
    	 
     
    	 
     }
     
    public  void loadagencyCodeList() throws IopTranslatorException{
    	 List<AgencyEntity> agList= dbLog.getAllAgencyList();
    	 if(agList.size()>0) {
    		 agencyCode = (HashSet<String>) agList.stream().map(ag -> ag.getHomeAgencyID().trim()).collect(Collectors.toSet());
    		 tagAgencyCode = (HashSet<String>) agList.stream().map(ag -> ag.getTagAgencyID().trim()).collect(Collectors.toSet());
    	 }else {
    		 throw new IopTranslatorException("Fail to load from Data base *********************** ");
    	 }
    	 System.out.println("set agencyCode:: "+agencyCode);
    	 
    	 for (String code : agencyCode) {
    		 System.out.println("set agencyCode:: "+code.length() +"\t code ::"+code);
		}
     }
    public void excelToCtocAgencyList(Sheet sheet,String HUB) {
   	 log.info("Inside ****************** excelToAgencyList():"+sheet);
        try {
       	
          Iterator<Row> rows = sheet.iterator();
          List<AgencyEntity> agList = new ArrayList<AgencyEntity>();
          int rowNumber = 0;
          while (rows.hasNext()) {
            Row currentRow = rows.next();
            // skip header
            if (rowNumber == 0) {
              rowNumber++;
              continue;
            }
            Iterator<Cell> cellsInRow = currentRow.iterator();
            AgencyEntity agency = new AgencyEntity();
            int cellIdx = 0;
            while (cellsInRow.hasNext()) {
              Cell currentCell = cellsInRow.next();
              switch (cellIdx) {
              case 0:
           	   agency.setHomeAgency((int) Math.round(currentCell.getNumericCellValue()));
           	   System.out.println("case 0::"+currentCell.getNumericCellValue());
                break;
              case 1:
                  agency.setAwayAgency((int) Math.round(currentCell.getNumericCellValue()));
                  System.out.println("case 1::"+currentCell.getNumericCellValue());
                break;
              case 2:
                  agency.setCSCID(currentCell.getStringCellValue());
                  System.out.println("case 2::"+currentCell.getStringCellValue());
                break;
              case 3:
                agency.setCSCName(currentCell.getStringCellValue());
                System.out.println("case 3::"+currentCell.getStringCellValue());
                break;
              case 4:
                  agency.setCSCAgencyShortName(currentCell.getStringCellValue());
                  System.out.println("case 4::"+currentCell.getStringCellValue());
                  break;
              case 5:
                  agency.setVersionNumber(currentCell.getStringCellValue());
                  System.out.println("case 5::"+currentCell.getStringCellValue());
                  break;
              case 6:
                  agency.setHomeAgencyID(currentCell.getStringCellValue());
                  System.out.println("case 6::"+currentCell.getStringCellValue());
                  break;
              case 7:
                  agency.setTagAgencyID(currentCell.getStringCellValue());
                  System.out.println("case 7::"+currentCell.getStringCellValue());
                  break;
              case 8:
                  agency.setTagSequenceStart(currentCell.getStringCellValue());
                  System.out.println("case 8::"+currentCell.getStringCellValue());
                  break;
              case 9:
                  agency.setTagSequenceEnd(currentCell.getStringCellValue());
                  System.out.println("case 9::"+currentCell.getStringCellValue());
                  break;
              case 10:
                  agency.setITAG((int) Math.round(currentCell.getNumericCellValue()));
                  System.out.println("case 10::"+currentCell.getNumericCellValue());
                  break;
              case 11:
                  agency.setICLP((int) Math.round(currentCell.getNumericCellValue()));
                  System.out.println("case 11::"+currentCell.getNumericCellValue());
                  break;
              case 12:
                  agency.setHubName(currentCell.getStringCellValue());
                  System.out.println("case 12::"+currentCell.getStringCellValue());
                  break;
              case 13:
                  agency.setHubId((int) Math.round(currentCell.getNumericCellValue()));
                  System.out.println("case 13::"+currentCell.getNumericCellValue());
                  break;
              default:
           	 //  System.out.println("Default:: ********************");
                break;
              }
              cellIdx++;
            }
            if (HUB.equals("CTOC"))
               agList.add(agency);
            else
           	 agList.add(agency);
          }
         
          if(agList != null && agList.size()>0) {
          dbLog.saveAgencyList(agList);
          log.info("@@@@ Agency List loaded sucessfully:: ********************");
          }else {
       	   throw new IopTranslatorException("Agency data not loaded");
          }
         
          if (HUB.equals("CTOC"))
              this.ctocAgList = agList;
          else
       	   this.agList = agList;
         
          
        }catch (Exception e) {
       	log.error("Exception:: ******************** UtilityAgencyListing_SHEET");
			e.printStackTrace();
		}
      }
}
