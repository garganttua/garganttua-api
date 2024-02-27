/*******************************************************************************
 * Copyright (c) 2022 Jérémy COLOMBET
 *******************************************************************************/
package com.garganttua.api.core;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class GGAPIEntityException extends Exception {

	private int code = BAD_REQUEST;
	
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
	public static final int ENTITY_NOT_FOUND = 1;
	public static final int BAD_REQUEST = 2;
	public static final int UNKNOWN_ERROR = 3;
	public static final int CONNECTOR_ERROR = 4;
	public static final int ENTITY_ALREADY_EXISTS = 5;
	public static final int INTERNAL_ERROR = 6;

	/**
	 * 
	 * @param e
	 * @return
	 */
	public HttpStatus getHttpErrorCode() {
		switch (code) {
		default:
			return HttpStatus.INTERNAL_SERVER_ERROR;
		case GGAPIEntityException.BAD_REQUEST:
			return HttpStatus.BAD_REQUEST;
		case GGAPIEntityException.ENTITY_NOT_FOUND:
			return HttpStatus.NOT_FOUND;
		}
	}

}
