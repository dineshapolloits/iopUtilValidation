package com.apolloits.util.controller;

import static com.apolloits.util.IAGConstants.FILE_RECORD_TYPE;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.apolloits.util.IAGConstants;
import com.apolloits.util.NIOPConstants;
import com.apolloits.util.generate.niop.STRANFileGenerator;
import com.apolloits.util.generate.niop.TVLFileGenerator;
import com.apolloits.util.modal.ErrorMsgDetail;
import com.apolloits.util.modal.FileValidationParam;
import com.apolloits.util.modal.NiopAgencyEntity;
import com.apolloits.util.reader.AgencyDataExcelReader;
import com.apolloits.util.service.DatabaseLogger;
import com.apolloits.util.utility.CommonUtil;
import com.apolloits.util.validator.niop.BTVLFileDetailValidation;
import com.apolloits.util.validator.niop.SCORRFileDetailValidation;
import com.apolloits.util.validator.niop.SRECONFileDetailValidation;
import com.apolloits.util.validator.niop.STRANFileDetailValidation;
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
	public static Map<String, NiopAgencyEntity> allCscIdNiopAgencyMap;
	
	public static Map<String, NiopAgencyEntity> cscIdTagNiopAgencyMap;
	
	List<ErrorMsgDetail> errorMsglist;
	
	@Autowired
	ExceptionListExcelWriter exListExcelWriter;
	
	@Autowired
	CommonUtil commonUtil;
	
	@Autowired
	BTVLFileDetailValidation btvlValidation;
	
	@Autowired
	STRANFileDetailValidation stranValidation;
	
	@Autowired
	SRECONFileDetailValidation sreconValidation;
	
	@Autowired
	SCORRFileDetailValidation scorrValidation;
	
	@Autowired
	TVLFileGenerator tvlGen;
	
	@Autowired
	STRANFileGenerator stranGen;
	
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
		allCscIdNiopAgencyMap =  dbLog.getAllNiopCSCIdbyAgencyMap();
		System.out.println("Controller :: allCscIdNiopAgencyMap ::"+allCscIdNiopAgencyMap);
		if(!validateNiopUIField(validateParam)) {
			model.addAttribute("result", validateParam.getResponseMsg());
			return "niop/NiopValidateFile";
		}
		boolean fileValidation = false ;
		validateParam.setResponseMsg("\t Contact Administrator");
		
		
		switch (validateParam.getFileType()) {
		case NIOPConstants.BTVL_FILE_TYPE:
		case NIOPConstants.DTVL_FILE_TYPE:
			fileValidation = btvlValidation.btvlValidation(validateParam, validateParam.getFileType());
			break;
		case NIOPConstants.STRAN_FILE_TYPE:
			fileValidation = stranValidation.starnValidation(validateParam);
			break;
		case NIOPConstants.SRECON_FILE_TYPE:
			fileValidation = sreconValidation.sreconValidation(validateParam);
			break;
		case NIOPConstants.SCORR_FILE_TYPE:
			fileValidation = scorrValidation.scorrValidation(validateParam);
			break;
		default:
			validateParam.setResponseMsg("\t Please select correct file type");
		}
		
		log.info("getResponseMsg ::"+validateParam.getResponseMsg() +"\t fileValidation ::"+fileValidation);
		
		if(!fileValidation || errorMsglist.size()>0) {
			model.addAttribute("result", "<b>Failed \t</b>"+validateParam.getResponseMsg());
			model.addAttribute("errorMsgList",errorMsglist);
			if(errorMsglist.size()>0) {
			File testObj = new File(validateParam.getInputFilePath());
			String exceptionFileName;
			String fileNameWithOutExt = FilenameUtils.removeExtension(testObj.getName());
			try {
				exceptionFileName = validateParam.getOutputFilePath() + File.separator + fileNameWithOutExt
						+ "_Exception.xls";
			} catch (Exception e) {
				e.printStackTrace();
				log.info("Exception in exceptionFileName :: file name creation ::");
				exceptionFileName = validateParam.getOutputFilePath() + File.separator + " ACK_exception_.xls";
			}
			exListExcelWriter.createExceptionExcel(errorMsglist, exceptionFileName);
		}
		}else {
			model.addAttribute("result", "Success");
		}
		 return "niop/NiopValidateFile";
	}
	
	@GetMapping("/GenerateNiop")
    public String Generate(Model model,HttpSession session) {
		
		if(session.getAttribute("MySession") == null) {
			return "redirect:/Logout";
		}
		
		FileValidationParam fileValidationParam = new FileValidationParam();
		fileValidationParam.setFileDate(CommonUtil.getCurrentDateAndTime());
        model.addAttribute("fileValidationParam", fileValidationParam);
        model.addAttribute("homeAgencyMap", dbLog.getNiopCscAgencyIdandShortNamebymap());
        return "niop/NiopGenerateFile";
    }
	
	@PostMapping("/NiopGenerateFile")
    public String GenerateFile(@ModelAttribute("fileValidationParam") FileValidationParam validateParam,Model model) throws IOException {
		log.info("GenerateFile ::"+validateParam.toString());
		boolean fileValidation = false ;
		validateParam.setResponseMsg("Contact Administrator");
		 	model.addAttribute("homeAgencyMap", dbLog.getNiopCscAgencyIdandShortNamebymap());
			
		 	cscIdTagNiopAgencyMap =  dbLog.getNiopCSCIdbyAgencyMap(validateParam.getFromAgency());
			allCscIdNiopAgencyMap =  dbLog.getAllNiopCSCIdbyAgencyMap();
			
			switch (validateParam.getFileType()) {
			case NIOPConstants.BTVL_FILE_TYPE:
			case NIOPConstants.DTVL_FILE_TYPE:
				fileValidation = tvlGen.tvlGen(validateParam);
			break;
			case NIOPConstants.STRAN_FILE_TYPE:
				
				if(!validateNiopUIField(validateParam)) {
					model.addAttribute("result", validateParam.getResponseMsg());
					return "niop/NiopGenerateFile";
				}
				
			fileValidation =stranGen.stranGen(validateParam);
			break;
			default:
				validateParam.setResponseMsg("\t Please select correct file type");
			}
				log.info("getResponseMsg ::"+validateParam.getResponseMsg() +"\t fileValidation ::"+fileValidation);
				log.info("fileValidation.getFileDate ::"+validateParam.getFileDate());
			if(!fileValidation)
				model.addAttribute("result", validateParam.getResponseMsg());
			else
				model.addAttribute("result", validateParam.getResponseMsg());
			
			return "niop/NiopGenerateFile";
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
				 validateParam.setResponseMsg("FAILED Reason::  ZIP file not found");
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
