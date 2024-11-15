package com.apolloits.util.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.apolloits.util.IAGConstants;
import com.apolloits.util.generate.ICLPFileGenerator;
import com.apolloits.util.generate.ITAGFileGenerator;
import com.apolloits.util.modal.AgencyEntity;
import com.apolloits.util.modal.FileValidationParam;
import com.apolloits.util.reader.AgencyDataExcelReader;
import com.apolloits.util.service.DatabaseLogger;
import com.apolloits.util.validator.ICLPFileDetailValidation;
import com.apolloits.util.validator.ITAGFileDetailValidation;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class ValidationController {
	@Autowired
	DatabaseLogger dbLog;
	@Autowired 
	AgencyDataExcelReader excelreader;
	public static Map<String, AgencyEntity> cscIdTagAgencyMap;
	
	@Autowired
	ITAGFileDetailValidation fdValidation;
	
	@Autowired
	ICLPFileDetailValidation iclpValidation;
	
	@Autowired
	ITAGFileGenerator itagGen;
	
	@Autowired
	ICLPFileGenerator iclpGen;
	
	@GetMapping("/AgencyList")
	public String greeting(@RequestParam(name="name", required=false, defaultValue="World") String name, Model model) {
		model.addAttribute("name", name);
		log.info("Welcome to ValidationController name:: "+name);
		//List<AgencyEntity> agList= AgencyDataExcelReader.excelToStuList();
		//List<AgencyEntity> agList= dbLog.getAllAgencyList();
		List<AgencyEntity> agList= excelreader.getAgList();
		System.out.println(":: agList ::"+agList);
		//System.out.println(" ::excelreader ::"+excelreader.getAgList());
		//dbLog.saveAgencyList(agList);
		 model.addAttribute("allAgList", agList);
		return "UtilityAgencyListing";
	}
	
	@GetMapping("/Validate")
    public String Validate(Model model) {
		FileValidationParam fileValidationParam = new FileValidationParam();
        model.addAttribute("fileValidationParam", fileValidationParam);
        return "ValidateFile";
    }
	
	@PostMapping("/ValidateFile")
    public String ValidateFile(@ModelAttribute("fileValidationParam") FileValidationParam validateParam,Model model) throws IOException {
		log.info("ValidateFile ::"+validateParam.toString());
		boolean fileValidation = false ;
		validateParam.setResponseMsg("Contact Administrator");
			cscIdTagAgencyMap =  dbLog.getCSCIdbyAgencyMap(validateParam.getFromAgency());
			if(validateParam.getFileType().equals(IAGConstants.ITAG_FILE_TYPE)) {
			fileValidation = fdValidation.itagValidation(validateParam);
			}else if(validateParam.getFileType().equals(IAGConstants.ICLP_FILE_TYPE)) {
				fileValidation = iclpValidation.iclpValidation(validateParam);
			}
			log.info("getResponseMsg ::"+validateParam.getResponseMsg() +"\t fileValidation ::"+fileValidation);
			if(!fileValidation)
				model.addAttribute("result", validateParam.getResponseMsg());
			else
				model.addAttribute("result", "Sucess");
        return "ValidateFile";
    }
	
	@GetMapping("/Generate")
    public String Generate(Model model) {
		FileValidationParam fileValidationParam = new FileValidationParam();
        model.addAttribute("fileValidationParam", fileValidationParam);
        return "GenerateFile";
    }
	
	@PostMapping("/GenerateFile")
    public String GenerateFile(@ModelAttribute("fileValidationParam") FileValidationParam validateParam,Model model) throws IOException {
		log.info("GenerateFile ::"+validateParam.toString());
		boolean fileValidation = false ;
		validateParam.setResponseMsg("Contact Administrator");
			cscIdTagAgencyMap =  dbLog.getCSCIdbyAgencyMap(validateParam.getFromAgency());
			if(cscIdTagAgencyMap == null) {
				model.addAttribute("result", "Invalid From Agency Code. Please refer agency list");
				return "GenerateFile";
			}
			if(validateParam.getFileType().equals(IAGConstants.ITAG_FILE_TYPE)) {
				fileValidation = itagGen.itagGen(validateParam);
				}else if(validateParam.getFileType().equals(IAGConstants.ICLP_FILE_TYPE)) {
					fileValidation = iclpGen.iclpGen(validateParam);
				}
				log.info("getResponseMsg ::"+validateParam.getResponseMsg() +"\t fileValidation ::"+fileValidation);
			
			if(!fileValidation)
				model.addAttribute("result", validateParam.getResponseMsg());
			else
				model.addAttribute("result", validateParam.getResponseMsg());
			
			return "GenerateFile";
	}
	
	@GetMapping("/login")
    public String login(Model model) {
	//	FileValidationParam fileValidationParam = new FileValidationParam();
        return "LoginPage";
    }
	
	@GetMapping("/hubList")
    public String hubList(Model model) {
	//	FileValidationParam fileValidationParam = new FileValidationParam();
        return "HubList";
    }

}
