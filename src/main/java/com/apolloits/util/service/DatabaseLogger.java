package com.apolloits.util.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apolloits.util.modal.AgencyEntity;
import com.apolloits.util.repository.AgencyRepository;

@Service
public class DatabaseLogger {

	@Autowired
	AgencyRepository agencyRepo;
	
	public void saveAgencyList(List agencyList) {
		agencyRepo.saveAll(agencyList);
	}
	public List<AgencyEntity> getAllAgencyList() {
        return agencyRepo.findAll();
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
