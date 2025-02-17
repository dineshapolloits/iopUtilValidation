package com.apolloits.util.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.apolloits.util.modal.UtilityParamEntity;

@Repository
public interface UtilParamRepository extends JpaRepository<UtilityParamEntity, String> {
	
}
