package com.apolloits.util.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apolloits.util.modal.UtilityParamEntity;


public interface UtilParamRepository extends JpaRepository<UtilityParamEntity, String> {
	
}
