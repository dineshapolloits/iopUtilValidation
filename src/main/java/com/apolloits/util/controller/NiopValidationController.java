package com.apolloits.util.controller;

import static com.apolloits.util.IAGConstants.FILE_RECORD_TYPE;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.apolloits.util.NIOPConstants;
import com.apolloits.util.modal.ErrorMsgDetail;
import com.apolloits.util.modal.FileValidationParam;
import com.apolloits.util.modal.NiopAgencyEntity;
import com.apolloits.util.reader.AgencyDataExcelReader;
import com.apolloits.util.service.DatabaseLogger;
import com.apolloits.util.utility.CommonUtil;
import com.apolloits.util.validator.niop.BTVLFileDetailValidation;
import com.apolloits.util.writer.ExceptionListExcelWriter;

import jakarta.servlet.http.HttpSession;
import jakarta.xml.bind.JAXBException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
@Setter
@Getter
public class NiopValidationController {

	@Autowired
	DatabaseLogger dbLog;
	@Autowired 
	AgencyDataExcelReader excelreader;
	public static Map<String, NiopAgencyEntity> cscIdTagNiopAgencyMap;
	
	List<ErrorMsgDetail> errorMsglist;
	
	@Autowired
	ExceptionListExcelWriter exListExcelWriter;
	
	@Autowired
	CommonUtil commonUtil;
	
	@Autowired
	BTVLFileDetailValidation btvlValidation;
	
	@GetMapping("/NiopAgencyList")
	public String loadNiopUtilPage(Model model,HttpSession session) {
		
		if(session.getAttribute("MySession") == null) {
			return "redirect:/Logout";
		}
		List<NiopAgencyEntity> agList= excelreader.getNiopAgList();
		 model.addAttribute("allAgList", agList);
		return "niop/NiopUtilityAgencyListing";
	}
	
	@GetMapping("/ValidateNiop")
    public String ValidateNiop(Model model,HttpSession session) {
		
		if(session.getAttribute("MySession") == null) {
			return "redirect:/Logout";
		}
		
		FileValidationParam fileValidationParam = new FileValidationParam();
        model.addAttribute("fileValidationParam", fileValidationParam);
        //get Map from DB
       
        model.addAttribute("homeAgencyMap", dbLog.getNiopCscAgencyIdandShortNamebymap());
        return "niop/NiopValidateFile";
    }
	
	@PostMapping("/ValidateFileNiop")
    public String ValidateFileNiop(@ModelAttribute("fileValidationParam") FileValidationParam validateParam,Model model) throws IOException, JAXBException {
		errorMsglist = new ArrayList<>();
		log.info("NIOP ValidateFile ::"+validateParam.toString());
		model.addAttribute("homeAgencyMap", dbLog.getNiopCscAgencyIdandShortNamebymap());
		cscIdTagNiopAgencyMap =  dbLog.getNiopCSCIdbyAgencyMap(validateParam.getFromAgency());
		System.out.println("Controller :: cscIdTagNiopAgencyMap ::"+cscIdTagNiopAgencyMap);
		if(!validateNiopUIField(validateParam)) {
			model.addAttribute("result", validateParam.getResponseMsg());
			return "niop/NiopValidateFile";
		}
		boolean fileValidation = false ;
		validateParam.setResponseMsg("\t Contact Administrator");
		if(validateParam.getFileType().equals(NIOPConstants.BTVL_FILE_TYPE)) {
			fileValidation = btvlValidation.btvlValidation(validateParam);
			}
		
		log.info("getResponseMsg ::"+validateParam.getResponseMsg() +"\t fileValidation ::"+fileValidation);
		
		if(!fileValidation || errorMsglist.size()>0) {
			model.addAttribute("result", "<b>Failed \t</b>"+validateParam.getResponseMsg());
			model.addAttribute("errorMsgList",errorMsglist);
		}else {
			model.addAttribute("result", "Success");
		}
		 return "niop/NiopValidateFile";
	}
	
	private boolean validateNiopUIField(FileValidationParam validateParam) {
		//validate IAG version
		if(validateParam.getFromAgency().equals("NONE")) {
			validateParam.setResponseMsg("<b>Please select From Agency Code </b>");
			return false;
		}
		if(validateParam.getToAgency().equals("NONE")) {
			validateParam.setResponseMsg("<b>Please select To Agency Code </b>");
			return false;
		}
		if(validateParam.getToAgency().equals(validateParam.getFromAgency())) {
			validateParam.setResponseMsg("<b>From agency and To agency should not be same </b>");
			return false;
		}
		if(validateParam.getInputFilePath()== null || validateParam.getInputFilePath().isEmpty() ) {
			validateParam.setResponseMsg("<b>Please give input file path </b>");
			return false;
		}else {
			File inputItagZipFile = new File(validateParam.getInputFilePath());
			if (!inputItagZipFile.exists()) {
				errorMsglist.add(new ErrorMsgDetail(FILE_RECORD_TYPE,"File","Input file not Found"));
				 //validateParam.setResponseMsg("FAILED Reason::  ZIP file not found");
				 log.error("FAILED Reason::  ZIP file not found");
				 return false;
	         }
		}
		if(validateParam.getOutputFilePath()== null || validateParam.getOutputFilePath().isEmpty() ) {
			validateParam.setResponseMsg("<b>Please give ACK output file path </b>");
			return false;
		}else if(!commonUtil.validateInfoFileGenParameter(validateParam)) {
		}
		return true;
		
	}
	
}
