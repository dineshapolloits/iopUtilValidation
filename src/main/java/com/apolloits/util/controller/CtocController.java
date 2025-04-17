package com.apolloits.util.controller;

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
import org.springframework.web.bind.annotation.RequestParam;

import com.apolloits.util.IAGConstants;
import com.apolloits.util.generate.CORFileGenerator;
import com.apolloits.util.generate.CRCFileGenerator;
import com.apolloits.util.generate.PBPFileGenerator;
import com.apolloits.util.generate.PLTFileGenerator;
import com.apolloits.util.generate.PRCFileGenerator;
import com.apolloits.util.generate.TAGFileGenerator;
import com.apolloits.util.generate.TOLFileGenerator;
import com.apolloits.util.generate.TRCFileGenerator;
import com.apolloits.util.modal.AgencyEntity;
import com.apolloits.util.modal.ErrorMsgDetail;
import com.apolloits.util.modal.FileValidationParam;
import com.apolloits.util.reader.AgencyDataExcelReader;
import com.apolloits.util.service.DatabaseLogger;
import com.apolloits.util.utility.CommonUtil;
import com.apolloits.util.validator.CORDetailValidation;
import com.apolloits.util.validator.CRCDetailValidation;
import com.apolloits.util.validator.CtocAckDetailValidation;
import com.apolloits.util.validator.PBPDetailValidation;
import com.apolloits.util.validator.PLTDetailValidation;
import com.apolloits.util.validator.PRCDetailValidation;
import com.apolloits.util.validator.TAGDetailValidation;
import com.apolloits.util.validator.TOLLDetailValidation;
import com.apolloits.util.validator.TRCDetailValidation;
import com.apolloits.util.writer.ExceptionListExcelWriter;

import jakarta.servlet.http.HttpSession;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
@Setter
@Getter
public class CtocController {
	@Autowired
	DatabaseLogger dbLog;
	@Autowired
	AgencyDataExcelReader excelreader;
	@Autowired
	TAGDetailValidation fdValidation;
	@Autowired
	PLTDetailValidation pltValidation;
	@Autowired
	TOLLDetailValidation tolValidation;
	@Autowired
	TRCDetailValidation trcValidation;
	@Autowired
	PBPDetailValidation pbpValidation;
	@Autowired
	PRCDetailValidation prcValidation;
	@Autowired
	CORDetailValidation corValidation;
	@Autowired
	CRCDetailValidation crcValidation;
	@Autowired
	CtocAckDetailValidation ackValidation;
	@Autowired	
	TAGFileGenerator tagGenerator;
	@Autowired	
	PLTFileGenerator pltGenerator;
	@Autowired	
	TOLFileGenerator tolGenerator;
	@Autowired	
	PBPFileGenerator pbpGenerator;
	@Autowired	
	TRCFileGenerator trcGenerator;
	@Autowired	
	PRCFileGenerator prcGenerator;
	@Autowired	
	CORFileGenerator corGenerator;
	@Autowired	
	CRCFileGenerator crcGenerator;
	
	List<ErrorMsgDetail> errorMsglist;

	@Autowired
	ExceptionListExcelWriter exListExcelWriter;

	public static Map<String, AgencyEntity> cscIdTagAgencyMap;

	@GetMapping("/CTOCAgencyList")
	public String greeting(@RequestParam(name = "name", required = false, defaultValue = "World") String name,
			Model model, HttpSession session) {

		if (session.getAttribute("MySession") == null) {
			return "redirect:/Logout";
		}
		model.addAttribute("name", name);
		log.info("Welcome to ValidationController name:: " + name);
		// List<AgencyEntity> agList= AgencyDataExcelReader.excelToStuList();
		// List<AgencyEntity> agList= dbLog.getAllAgencyList();
		List<AgencyEntity> agList = excelreader.getCtocAgList();
		System.out.println(":: agList ::" + agList);
		// System.out.println(" ::excelreader ::"+excelreader.getAgList());
		// dbLog.saveAgencyList(agList);
		model.addAttribute("allAgList", agList);
		return "CtocUtilityAgencyListing";
	}

	@GetMapping("/GenerateCtoc")
	public String Generate(Model model, HttpSession session) {

		if (session.getAttribute("MySession") == null) {
			return "redirect:/Logout";
		}

		FileValidationParam fileValidationParam = new FileValidationParam();
		fileValidationParam.setFileDate(CommonUtil.getCurrentDateAndTime());
		model.addAttribute("fileValidationParam", fileValidationParam);
		model.addAttribute("homeAgencyMap", dbLog.getCscAgencyIdandShortNamebymap("CTOC"));
		return "GenerateCtocFile";
	}

	@GetMapping("/ValidateCtoc")
	public String Validate(Model model, HttpSession session) {

		if (session.getAttribute("MySession") == null) {
			return "redirect:/Logout";
		}

		FileValidationParam fileValidationParam = new FileValidationParam();
		model.addAttribute("fileValidationParam", fileValidationParam);
		// get Map from DB

		model.addAttribute("homeAgencyMap", dbLog.getCscAgencyIdandShortNamebymap("CTOC"));
		return "CtocValidateFile";
	}

	@PostMapping("/CtocValidateFile")
	public String CtocValidateFile(@ModelAttribute("fileValidationParam") FileValidationParam validateParam,
			Model model) throws IOException {
		errorMsglist = new ArrayList<>();
		log.info("ValidateFile ::" + validateParam.toString());
		log.info("From ::" + validateParam.getFromAgency());
		log.info("To ::" + validateParam.getToAgency());
		log.info("input file path ::" + validateParam.getInputFilePath());
		log.info("input file path ::" + validateParam.getOutputFilePath());
		// UI validation
		// validateUIField(validateParam);
		boolean fileValidation = false;
		validateParam.setResponseMsg("\t Contact Administrator");
		cscIdTagAgencyMap = dbLog.getCSCIdbyAgencyMap(validateParam.getFromAgency().substring(0, 4));
		log.info("After cscIdTagAgencyMap" + validateParam.getFromAgency().substring(0, 4));
		if (validateParam.getFileType().equals(IAGConstants.TAG_FILE_TYPE)) {
			fileValidation = fdValidation.tagValidation(validateParam);
		} else if (validateParam.getFileType().equals(IAGConstants.PLT_FILE_TYPE)) {
			fileValidation = pltValidation.pltValidation(validateParam);
		} else if (validateParam.getFileType().equals(IAGConstants.TOLL_FILE_TYPE)) {
			fileValidation = tolValidation.tolValidation(validateParam);
		} else if (validateParam.getFileType().equals(IAGConstants.TOLL_RECON_FILE_TYPE)) {
			fileValidation = trcValidation.trcValidation(validateParam);
		} else if (validateParam.getFileType().equals(IAGConstants.PAY_BY_PLATE_FILE_TYPE)) {
			fileValidation = pbpValidation.pbpValidation(validateParam);
		} else if (validateParam.getFileType().equals(IAGConstants.PLATE_RECON_FILE_TYPE)) {
			fileValidation = prcValidation.prcValidation(validateParam);
		}else if (validateParam.getFileType().equals(IAGConstants.COR_FILE_TYPE)) {
			fileValidation = corValidation.corValidation(validateParam);
		}else if (validateParam.getFileType().equals(IAGConstants.ACK_FILETYPE)) {
			fileValidation = ackValidation.ackValidation(validateParam);
		}else if (validateParam.getFileType().equals(IAGConstants.COR_RECON_FILE_TYPE)) {
			fileValidation = crcValidation.crcValidation(validateParam);
		}


		log.info("getResponseMsg ::" + validateParam.getResponseMsg() + "\t fileValidation ::" + fileValidation);
		if (!fileValidation || errorMsglist.size() > 0) {
			model.addAttribute("result", "Failed " + validateParam.getResponseMsg());
			model.addAttribute("errorMsgList", errorMsglist);
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
			exListExcelWriter.createExceptionExcel(errorMsglist, exceptionFileName, "CTOC");
		} else
			model.addAttribute("result", "Success");

		model.addAttribute("homeAgencyMap", dbLog.getCscAgencyIdandShortNamebymap("CTOC"));

		return "CtocValidateFile";
	}
	
	@PostMapping("/GenerateCtocFile")
	 public String GenerateCtocFile(@ModelAttribute("fileValidationParam") FileValidationParam validateParam,Model model) throws IOException {
		boolean fileValidation = false ;
		errorMsglist = new ArrayList<>();
		validateParam.setResponseMsg("Contact Administrator");
		 	model.addAttribute("homeAgencyMap", dbLog.getCscAgencyIdandShortNamebymap("CTOC"));
			cscIdTagAgencyMap =  dbLog.getCSCIdbyAgencyMap(validateParam.getFromAgency().substring(0, 4));
			if(cscIdTagAgencyMap == null) {
				model.addAttribute("result", "Invalid From Agency Code. Please refer agency list");
				return "GenerateCtocFile";
			}
			if(!validateFileParam(validateParam,model)) {
				return "GenerateCtocFile";
			}
			if (validateParam.getFileType().equals(IAGConstants.TAG_FILE_TYPE)) {
				fileValidation = tagGenerator.tagGenerate(validateParam);
			} else if (validateParam.getFileType().equals(IAGConstants.PLT_FILE_TYPE)) {
				fileValidation = pltGenerator.pltGenerate(validateParam);
			}	else if(validateParam.getFileType().equals(IAGConstants.TOLL_FILE_TYPE)) {
				fileValidation = tolGenerator.tolGenenerate(validateParam);
			}else if(validateParam.getFileType().equals(IAGConstants.PAY_BY_PLATE_FILE_TYPE)) {
				fileValidation = pbpGenerator.pbpGenenerate(validateParam);
			}else if(validateParam.getFileType().equals(IAGConstants.TOLL_RECON_FILE_TYPE)) {
				fileValidation = trcGenerator.trcGenenerate(validateParam);
			}else if(validateParam.getFileType().equals(IAGConstants.PLATE_RECON_FILE_TYPE)) {
				fileValidation = prcGenerator.prcGenenerate(validateParam);
			}else if(validateParam.getFileType().equals(IAGConstants.COR_FILE_TYPE)) {
				fileValidation = corGenerator.corGenenerate(validateParam);
			}else if(validateParam.getFileType().equals(IAGConstants.COR_RECON_FILE_TYPE)) {
				fileValidation = crcGenerator.crcGenenerate(validateParam);
			}
		
			log.info("getResponseMsg ::"+validateParam.getResponseMsg() +"\t fileValidation ::"+fileValidation);
			log.info("fileValidation.getFileDate ::"+validateParam.getFileDate());
		if(!fileValidation) {
			model.addAttribute("result", validateParam.getResponseMsg());
			//model.addAttribute("errorMsgList", errorMsglist);
		}else
			model.addAttribute("result", validateParam.getResponseMsg());
		
		return "GenerateCtocFile";
		
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
}
