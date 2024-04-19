package com.garganttua.api.core.exceptions;

import lombok.Getter;

public enum GGAPICoreExceptionCode {
	
	/*
	 * Core API error codes 
	 */
	CORE_GENERIC_CODE				(000),
	UNKNOWN_ERROR					(001), 
	
	
	//Entities error codes
	ENTITY_DEFINITION				(100),
	ENTITY_INSTANCIATION			(101),
	SET_FIELD_VALUE					(102),
	DELETION_ERROR					(103),
	ENTITY_ALREADY_EXISTS			(104),
	BAD_REQUEST						(105),
	
	//Dto error codes
	DTO_DEFINITION					(110),
	DTO_INSTANCIATION				(111),
	NO_DTO_FOUND					(112),
	
	//Mapping error codes
	DIRECT_FIELD_MAPPING			(120),
	OBJECT_MAPPING					(121),
	METHOD_FIELD_MAPPING			(122),
	OBJECT_ADDRESS_LOOP_DETECTED	(123),
	FIELD_METHOD_SAME_NAME          (125),
	VALUE_OR_KEY_NOT_IN_ADDRESS		(126),
	OBJECT_NOT_FOUND				(127),
	INSTANCIATION_ERROR 			(128),
	
	//Entity factory codes
	GENERIC_FACTORY_EXCEPTION		(180),
	
	/*
	 * Security API error codes 
	 */
	GENERIC_SECURITY_ERROR			(200), 
	
	//Token error codes
	TOKEN_NOT_FOUND					(210), 
	TOKEN_EXPIRED					(211), 
	
	//Key error codes
	KEY_EXPIRED						(220),
	KEY_ERROR						(220),
	
	
	;
	
	@Getter
	private int code;
	
	GGAPICoreExceptionCode(int code) {
		this.code = code;
	}

}
