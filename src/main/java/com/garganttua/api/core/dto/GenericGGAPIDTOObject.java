/*******************************************************************************
 * Copyright (c) 2022 Jérémy COLOMBET
 *******************************************************************************/
package com.garganttua.api.core.dto;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Field;

import com.garganttua.api.core.dto.annotations.GGAPIDtoFieldMapping;

import lombok.Data;

/**
 * 
 * @author J.Colombet
 *
 * @param <Entity>
 */
@Data
public class GenericGGAPIDTOObject {
	
	@Id
	@Indexed(unique=true)
	@GGAPIDtoFieldMapping(entityField = "uuid")
	protected String uuid;
	
	@Field
	@GGAPIDtoFieldMapping(entityField = "id")
	protected String id;
	
	@Field
	protected String tenantId;
}
