package com.apolloits.util.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.apolloits.util.modal.AgencyEntity;
import com.apolloits.util.modal.NiopAgencyEntity;
import com.apolloits.util.modal.PlateTypeEntity;
import com.apolloits.util.modal.UtilityParamEntity;
import com.apolloits.util.repository.AgencyRepository;
import com.apolloits.util.repository.NiopAgencyRepository;
import com.apolloits.util.repository.PlateTypeRepository;
import com.apolloits.util.repository.UtilParamRepository;

@Service
public class DatabaseLogger {

	@Autowired
	AgencyRepository agencyRepo;
	
	@Autowired
	UtilParamRepository utilParamRepo;
	
	@Autowired
	PlateTypeRepository plateTypeRepo;
	
	@Autowired
	NiopAgencyRepository niopAgencyRepo;
	
	public void saveAgencyList(List<AgencyEntity> agencyList) {
		agencyRepo.saveAll(agencyList);
	}
	
	public void saveNiopAgencyList(List<NiopAgencyEntity> agencyList) {
		niopAgencyRepo.saveAll(agencyList);
	}
	public List<AgencyEntity> getAllAgencyList() {
        return agencyRepo.findAll();
    }
	
	public void saveUtilParamList(List<UtilityParamEntity> utilParamList) {
		utilParamRepo.saveAll(utilParamList);
	}
	
	public void savePlateTypeList(List<PlateTypeEntity> plateTypeList) {
		plateTypeRepo.saveAll(plateTypeList);
	}
	
	public Map<String, AgencyEntity> getCSCIdbyAgencyMap(String AgencyCode) {
		List<AgencyEntity> cscIdAgencyList =agencyRepo.findByCSCIDforAgency(AgencyCode);
		
		Map<String, AgencyEntity> cscIdTagAgencyMap = cscIdAgencyList.stream()
	            .collect(Collectors.toMap(AgencyEntity::getTagAgencyID, agency -> agency));

		cscIdTagAgencyMap.forEach((tag, agency) -> 
	            System.out.println("TagAgencyID: " + tag + ", Tag Start: " + agency.getTagSequenceStart() +"\t END ::"+agency.getTagSequenceEnd()));
	        
		return cscIdTagAgencyMap;
	}
	
	public Map<String, String> getCscAgencyIdandShortNamebymap() {
		List<AgencyEntity> agencyList = agencyRepo.findAll(Sort.by(Sort.Direction.ASC, "CSCID"));
		 Map<String, String> map = new LinkedHashMap<>();
		 for (AgencyEntity agencyEntity : agencyList) {
			 map.put(agencyEntity.getCSCID(), agencyEntity.getCSCID()+"-"+agencyEntity.getCSCAgencyShortName());
		}
		return map;
	}
	
	public Map<String, NiopAgencyEntity> getAllNiopCSCIdbyAgencyMap() {
		List<NiopAgencyEntity> cscIdAgencyList =niopAgencyRepo.findAll();
		//(r1, r2) -> r1
		Map<String, NiopAgencyEntity> cscIdTagAgencyMap = cscIdAgencyList.stream()
	            .collect(Collectors.toMap(NiopAgencyEntity::getHomeAgencyID, Function.identity(), (first, second) -> first));
		cscIdTagAgencyMap.forEach((tag, agency) -> 
        System.out.println("getHomeAgencyID: " + tag + ", Tag Start: " + agency.getTagSequenceStart() +"\t END ::"+agency.getTagSequenceEnd()));

		return cscIdTagAgencyMap;
	}
	
	public Map<String, NiopAgencyEntity> getNiopCSCIdbyAgencyMap(String AgencyCode) {
		List<NiopAgencyEntity> cscIdAgencyList =niopAgencyRepo.findByCSCIDforAgency(AgencyCode);
		
		Map<String, NiopAgencyEntity> cscIdTagAgencyMap = cscIdAgencyList.stream()
	            .collect(Collectors.toMap(NiopAgencyEntity::getTagAgencyID, agency -> agency));

		cscIdTagAgencyMap.forEach((tag, agency) -> 
	            System.out.println("TagAgencyID: " + tag + ", Tag Start: " + agency.getTagSequenceStart() +"\t END ::"+agency.getTagSequenceEnd()));
	        
		return cscIdTagAgencyMap;
	}
	
	
	public Map<String, String> getNiopCscAgencyIdandShortNamebymap() {
		List<NiopAgencyEntity> agencyList = niopAgencyRepo.findAll(Sort.by(Sort.Direction.ASC, "CSCID"));
		 Map<String, String> map = new LinkedHashMap<>();
		 for (NiopAgencyEntity agencyEntity : agencyList) {
			 map.put(agencyEntity.getCSCID(), agencyEntity.getCSCID()+"-"+agencyEntity.getCSCAgencyShortName());
		}
		return map;
	}
}
