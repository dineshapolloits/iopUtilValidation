package com.apolloits.util.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apolloits.util.modal.AgencyEntity;
import com.apolloits.util.modal.UtilityParamEntity;
import com.apolloits.util.repository.AgencyRepository;
import com.apolloits.util.repository.UtilParamRepository;

@Service
public class DatabaseLogger {

	@Autowired
	AgencyRepository agencyRepo;
	
	@Autowired
	UtilParamRepository utilParamRepo;
	
	public void saveAgencyList(List<AgencyEntity> agencyList) {
		agencyRepo.saveAll(agencyList);
	}
	public List<AgencyEntity> getAllAgencyList() {
        return agencyRepo.findAll();
    }
	
	public void saveUtilParamList(List<UtilityParamEntity> utilParamList) {
		utilParamRepo.saveAll(utilParamList);
	}
	
	public Map<String, AgencyEntity> getCSCIdbyAgencyMap(String AgencyCode) {
		List<AgencyEntity> cscIdAgencyList =agencyRepo.findByCSCIDforAgency(AgencyCode);
		
		Map<String, AgencyEntity> cscIdTagAgencyMap = cscIdAgencyList.stream()
	            .collect(Collectors.toMap(AgencyEntity::getTagAgencyID, agency -> agency));

		cscIdTagAgencyMap.forEach((tag, agency) -> 
	            System.out.println("TagAgencyID: " + tag + ", Tag Start: " + agency.getTagSequenceStart() +"\t END ::"+agency.getTagSequenceEnd()));
	        
		return cscIdTagAgencyMap;
	}
	
}
