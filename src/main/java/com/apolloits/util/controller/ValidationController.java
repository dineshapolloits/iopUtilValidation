package com.apolloits.util.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.apolloits.util.IAGConstants;
import com.apolloits.util.generate.ICLPFileGenerator;
import com.apolloits.util.generate.ICRXFileGenerator;
import com.apolloits.util.generate.ICTXFileGenerator;
import com.apolloits.util.generate.ITAGFileGenerator;
import com.apolloits.util.modal.AgencyEntity;
import com.apolloits.util.modal.ErrorMsgDetail;
import com.apolloits.util.modal.FileValidationParam;
import com.apolloits.util.modal.LoginParam;
import com.apolloits.util.reader.AgencyDataExcelReader;
import com.apolloits.util.service.DatabaseLogger;
import com.apolloits.util.utility.CommonUtil;
import com.apolloits.util.validator.ICLPFileDetailValidation;
import com.apolloits.util.validator.ICRXFileDetailValidation;
import com.apolloits.util.validator.ICTXFileDetailValidation;
import com.apolloits.util.validator.ITAGFileDetailValidation;
import com.apolloits.util.writer.ExceptionListExcelWriter;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
@Setter
@Getter
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
	ICTXFileDetailValidation ictxValidation;
	
	@Autowired
	ICRXFileDetailValidation icrxValidation;
	
	@Autowired
	ITAGFileGenerator itagGen;
	
	@Autowired
	ICLPFileGenerator iclpGen;
	
	@Autowired
	ICTXFileGenerator ictxGen;
	
	@Autowired
	ICRXFileGenerator icrxGen;
	
	List<ErrorMsgDetail> errorMsglist;
	
	@Autowired
	ExceptionListExcelWriter exListExcelWriter;
	
	@Value("${loginId}")
	String userName;
	
	@Value("${pasword}")
	String password;
	
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
        //get Map from DB
       
        model.addAttribute("homeAgencyMap", dbLog.getCscAgencyIdandShortNamebymap());
        return "ValidateFile";
    }
	
	@PostMapping("/ValidateFile")
    public String ValidateFile(@ModelAttribute("fileValidationParam") FileValidationParam validateParam,Model model) throws IOException {
		errorMsglist = new ArrayList<>();
		log.info("ValidateFile ::"+validateParam.toString());
		boolean fileValidation = false ;
		validateParam.setResponseMsg("Contact Administrator");
			cscIdTagAgencyMap =  dbLog.getCSCIdbyAgencyMap(validateParam.getFromAgency());
			if(validateParam.getFileType().equals(IAGConstants.ITAG_FILE_TYPE)) {
			fileValidation = fdValidation.itagValidation(validateParam);
			}else if(validateParam.getFileType().equals(IAGConstants.ICLP_FILE_TYPE)) {
				fileValidation = iclpValidation.iclpValidation(validateParam);
			}else if (validateParam.getFileType().equals(IAGConstants.ICTX_FILE_TYPE)) {
				log.info("Inside ICTX validation started");
				fileValidation = ictxValidation.ictxValidation(validateParam);
			}else if (validateParam.getFileType().equals(IAGConstants.ICRX_FILE_TYPE)) {
				log.info("Inside ICRX validation started");
				fileValidation = icrxValidation.icrxValidation(validateParam);
			}
			log.info("getResponseMsg ::"+validateParam.getResponseMsg() +"\t fileValidation ::"+fileValidation);
			if(!fileValidation || errorMsglist.size()>0) {
				model.addAttribute("result", "Failed"+validateParam.getResponseMsg());
				model.addAttribute("errorMsgList",errorMsglist);
				File testObj = new File(validateParam.getInputFilePath());
				String exceptionFileName;
				String fileNameWithOutExt = FilenameUtils.removeExtension(testObj.getName());
				try {
				 exceptionFileName =validateParam.getOutputFilePath()+File.separator+fileNameWithOutExt+"_Exception.xls";
				}catch (Exception e) {
					e.printStackTrace();
					log.info("Exception in exceptionFileName :: file name creation ::");
					exceptionFileName = validateParam.getOutputFilePath()+File.separator+" ACK_exception_.xls";
				}
				exListExcelWriter.createExceptionExcel(errorMsglist, exceptionFileName);
			}else
				model.addAttribute("result", "Sucess");
			
			 model.addAttribute("homeAgencyMap", dbLog.getCscAgencyIdandShortNamebymap());
        return "ValidateFile";
    }
	
	@GetMapping("/Generate")
    public String Generate(Model model) {
		FileValidationParam fileValidationParam = new FileValidationParam();
		fileValidationParam.setFileDate(CommonUtil.getCurrentDateAndTime());
        model.addAttribute("fileValidationParam", fileValidationParam);
        model.addAttribute("homeAgencyMap", dbLog.getCscAgencyIdandShortNamebymap());
        return "GenerateFile";
    }
	
	@PostMapping("/GenerateFile")
    public String GenerateFile(@ModelAttribute("fileValidationParam") FileValidationParam validateParam,Model model) throws IOException {
		log.info("GenerateFile ::"+validateParam.toString());
		boolean fileValidation = false ;
		validateParam.setResponseMsg("Contact Administrator");
		 	model.addAttribute("homeAgencyMap", dbLog.getCscAgencyIdandShortNamebymap());
			cscIdTagAgencyMap =  dbLog.getCSCIdbyAgencyMap(validateParam.getFromAgency());
			if(cscIdTagAgencyMap == null) {
				model.addAttribute("result", "Invalid From Agency Code. Please refer agency list");
				return "GenerateFile";
			}
			if(!validateFileParam(validateParam,model)) {
				return "GenerateFile";
			}
			if (validateParam.getFileType().equals(IAGConstants.ITAG_FILE_TYPE)) {
				fileValidation = itagGen.itagGen(validateParam);
			} else if (validateParam.getFileType().equals(IAGConstants.ICLP_FILE_TYPE)) {
				fileValidation = iclpGen.iclpGen(validateParam);
			} else if (validateParam.getFileType().equals("ITAGandICLP")) {
				fileValidation = itagGen.itagGen(validateParam);
				String msg = validateParam.getResponseMsg();
				fileValidation = iclpGen.iclpGen(validateParam);
				validateParam.setResponseMsg(msg + " \n " + validateParam.getResponseMsg());
				log.info("msg + validateParam.getResponseMsg() ::" + msg + "\t " + validateParam.getResponseMsg());
			}else if(validateParam.getFileType().equals(IAGConstants.ICTX_FILE_TYPE)) {
				fileValidation = ictxGen.ictxGen(validateParam);
			}else if(validateParam.getFileType().equals(IAGConstants.ICRX_FILE_TYPE)) {
				fileValidation = icrxGen.icrxGen(validateParam,IAGConstants.ICRX_FILE_TYPE);
			}
				log.info("getResponseMsg ::"+validateParam.getResponseMsg() +"\t fileValidation ::"+fileValidation);
				log.info("fileValidation.getFileDate ::"+validateParam.getFileDate());
			if(!fileValidation)
				model.addAttribute("result", validateParam.getResponseMsg());
			else
				model.addAttribute("result", validateParam.getResponseMsg());
			
			return "GenerateFile";
	}
	
	private boolean validateFileParam(FileValidationParam validateParam,Model model) {
		//validate generate fiel date
		if (!CommonUtil.isDate(validateParam.getFileDate(), "yyyy-MM-dd")) {
			validateParam.setResponseMsg("Invalid File Date");
			model.addAttribute("result", "Invalid File Date.\t  Please select valid date ");
			return false;
		}

		return true;
	}

	@GetMapping("/login")
    public String login(Model model) {
		LoginParam loginParam = new LoginParam();
        model.addAttribute("loginParam", loginParam);
        return "LoginPage";
    }
	
	@PostMapping("/loginValidation")
	public String loginValidation(@ModelAttribute("loginParam") LoginParam loginParam, Model model) {

		log.info("UserName ::" + loginParam.getUsername() + "\t Password ::" + loginParam.getPassword());
		log.info("Property file :: userName ::" + userName + "\t Password ::" + password);
		if (loginParam.getUsername().equals(userName) && loginParam.getPassword().equals(password)) {
			return "HubList";
		} else {
			log.error("Login Failed");
			loginParam.setErrorMsg("Invalid UserName or Password");
			model.addAttribute("result", loginParam.getErrorMsg());
			return "LoginPage";
		}

	}
	
	@GetMapping("/hubList")
    public String hubList(Model model) {
	//	FileValidationParam fileValidationParam = new FileValidationParam();
        return "HubList";
    }
	
	@GetMapping("/Logout")
    public String logout(Model model) {
	//	FileValidationParam fileValidationParam = new FileValidationParam();
		LoginParam loginParam = new LoginParam();
		model.addAttribute("loginParam", loginParam);
		model.addAttribute("result", "Logout successfully.");
        return "LoginPage";
    }

}
