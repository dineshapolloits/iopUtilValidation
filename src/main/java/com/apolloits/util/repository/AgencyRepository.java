package com.apolloits.util.repository;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.apolloits.util.modal.AgencyEntity;


@Repository
public interface AgencyRepository extends JpaRepository<AgencyEntity, Integer> {
	@Query("SELECT ag FROM AgencyEntity ag WHERE ag.CSCID = :CSCID ")
	List<AgencyEntity> findByCSCIDforAgency(@Param("CSCID") String CSCID);
	
	@Query("SELECT a FROM AgencyEntity a WHERE a.HubName = :HubName")
	List<AgencyEntity> findByHubName(@Param("HubName")String name, Sort sort);
}
