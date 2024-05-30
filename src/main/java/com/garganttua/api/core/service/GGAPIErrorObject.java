/*******************************************************************************
 * Copyright (c) 2022 Jérémy COLOMBET
 *******************************************************************************/
package com.garganttua.api.core.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GGAPIErrorObject {
	
	public static int NO_ERROR_CODE = 0;

	private String message;
	
	private int code;
	
}
