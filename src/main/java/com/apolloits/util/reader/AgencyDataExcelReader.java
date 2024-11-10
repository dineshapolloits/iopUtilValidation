package com.apolloits.util.reader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.apolloits.util.IopTranslatorException;
import com.apolloits.util.modal.AgencyEntity;
import com.apolloits.util.service.DatabaseLogger;

import jakarta.annotation.PostConstruct;
import lombok.Getter;

@Component
@Getter
public class AgencyDataExcelReader {
	
	@Autowired
	DatabaseLogger dbLog;
	
	private List<AgencyEntity> agList;
	
	public static HashSet<String> agencyCode;
	
	 public static String TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
     static String[] HEADERs = { "HomeAgency", "AwayAgency", "CSCID", "CSCName", "CSCAgencyShortName","VersionNumber"," HomeAgencyID",
    		 "TagAgencyID","TagSequenceStart","TagSequenceEnd","ITAG","ICLP","HubName","HubId" };
     static String SHEET = "UtilityAgencyListing";
     
  /*   public static boolean hasExcelFormat(MultipartFile file) {
         if (!TYPE.equals(file.getContentType())) {
           return false;
         }
         return true;
       }*/
     //C:\Users\dselvaraj\Workspace\iopValidation\src\main\resources\Utlity Table.xlsx
     @PostConstruct
     public void init() throws IopTranslatorException {
    	 excelToAgencyList();
    	 loadagencyCodeList();
     }
     
     public void excelToAgencyList() {
    	 System.out.println("Inside ****************** excelToStuList()");
         try {
        	// FileInputStream is = new FileInputStream(new File("C:\\Users\\dselvaraj\\Workspace\\iopValidation\\src\\main\\resources\\Utlity Table.xlsx"));
        	 ClassPathResource resource = new ClassPathResource("Utlity Table.xlsx");
        	 InputStream inputStream = resource.getInputStream();
           Workbook workbook = new XSSFWorkbook(inputStream);
           System.out.println("Number of sheets: " + workbook.getNumberOfSheets());
           Sheet sheet = workbook.getSheet(SHEET);
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
           workbook.close();
           if(agList != null && agList.size()>0) {
           dbLog.saveAgencyList(agList);
           System.out.println("@@@@ Agency List loaded sucessfully:: ********************");
           }else {
        	   throw new IopTranslatorException("Agency data not loaded");
           }
           this.agList = agList;
           //return agList;
         } catch (IOException e) {
           throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
         }catch (Exception e) {
        	 System.out.println("Exception:: ********************");
			e.printStackTrace();
		}
       }
     
    public  void loadagencyCodeList() throws IopTranslatorException{
    	 List<AgencyEntity> agList= dbLog.getAllAgencyList();
    	 if(agList.size()>0) {
    		 agencyCode = (HashSet<String>) agList.stream().map(ag -> ag.getHomeAgencyID().trim()).collect(Collectors.toSet());
    	 }else {
    		 throw new IopTranslatorException("Fail to load from Data base *********************** ");
    	 }
    	 System.out.println("set agencyCode:: "+agencyCode);
    	 
    	 for (String code : agencyCode) {
    		 System.out.println("set agencyCode:: "+code.length() +"\t code ::"+code);
		}
     }

}
