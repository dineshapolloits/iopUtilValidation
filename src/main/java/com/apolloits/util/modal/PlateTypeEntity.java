package com.apolloits.util.modal;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "PlateType")
@Data
public class PlateTypeEntity {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer plateTppeId;
	private String licState;
	private String licType;
	private String plateDesc;
	
}
