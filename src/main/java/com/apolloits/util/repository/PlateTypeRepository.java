package com.apolloits.util.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.apolloits.util.modal.PlateTypeEntity;

@Repository
public interface PlateTypeRepository extends JpaRepository<PlateTypeEntity, Integer> {

}
