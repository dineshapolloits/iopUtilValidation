package com.apolloits.util.modal;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "UtilityParam")
@Data
public class UtilityParamEntity {
	@Id
	private String Type;
	private String SubType;
	private String TypeValue;
}
