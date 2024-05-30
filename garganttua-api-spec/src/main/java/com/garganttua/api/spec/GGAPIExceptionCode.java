package com.garganttua.api.spec;

import lombok.Getter;

public enum GGAPIExceptionCode {
	
	/*
	 * API error codes 
	 */
	CORE_GENERIC_CODE				(000),
	UNKNOWN_ERROR					(001), 
	
	
	//Entities error codes
	ENTITY_DEFINITION				(100),
	ENTITY_INSTANCIATION			(101),
	SET_FIELD_VALUE					(102),
	GET_FIELD_VALUE					(103),
	DELETION_ERROR					(104),
	ENTITY_ALREADY_EXISTS			(105),
	BAD_REQUEST						(106),
	INVOKE_METHOD					(107),
	
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
	
	//Reflection error codes
	NO_DEFAULT_CTOR					(130),
	
	//Entity factory codes
	GENERIC_FACTORY_EXCEPTION		(180),
	INJECTION_ERROR					(181),
	
	/*
	 * Security API error codes 
	 */
	GENERIC_SECURITY_ERROR			(200), 
	
	//Token error codes
	TOKEN_NOT_FOUND					(210), 
	TOKEN_EXPIRED					(211), 
	
	//Key error codes
	KEY_EXPIRED						(220),
	KEY_ERROR						(221), 
	
	// Authentication error codes
	FAILED_AUTHENTICATION			(230), 
	
	
	;
	
	@Getter
	private int code;
	
	GGAPIExceptionCode(int code) {
		this.code = code;
	}

}
