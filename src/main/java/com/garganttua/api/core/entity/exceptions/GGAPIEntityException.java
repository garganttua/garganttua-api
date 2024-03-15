/*******************************************************************************
 * Copyright (c) 2022 Jérémy COLOMBET
 *******************************************************************************/
package com.garganttua.api.core.entity.exceptions;

import lombok.Getter;

@Getter
public class GGAPIEntityException extends Exception {

	private int code;
	
	public GGAPIEntityException(String string) {
		super(string);
	}

	public GGAPIEntityException(int code, String string) {
		super(string);
		this.code = code;
	}
	
	public GGAPIEntityException(int code, String string, Exception e) {
		super(string, e);
		this.code = code;
	}

	public GGAPIEntityException(Exception e) {
		super(e);
	}

	public GGAPIEntityException(int code, Exception e) {
		super(e);
		this.code = code;
	}

	public GGAPIEntityException(String string, Exception e) {
		super(string, e);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 8581388689485492204L;
	public static final int ENTITY_DEFINITION_ERROR = 1;
	public static final int SET_FIELD_VALUE = 0;
	public static final int DELETION_ERROR = 2;
	public static final int ENTITY_ALREADY_EXISTS = 3;
	public static final int BAD_REQUEST = 4;
	public static final int UNKNOWN_ERROR = 6;
}
