package com.apolloits.util.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.apolloits.util.modal.AgencyEntity;
import com.apolloits.util.modal.NiopAgencyEntity;


@Repository
public interface NiopAgencyRepository extends JpaRepository<NiopAgencyEntity, Integer> {
	@Query("SELECT ag FROM NiopAgencyEntity ag WHERE ag.CSCID = :CSCID")
	List<AgencyEntity> findByCSCIDforAgency(@Param("CSCID") String CSCID);
}
