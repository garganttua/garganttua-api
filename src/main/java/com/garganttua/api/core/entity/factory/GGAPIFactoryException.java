package com.garganttua.api.core.entity.factory;

import com.garganttua.api.core.entity.exceptions.GGAPIEntityException;

import lombok.Getter;

public class GGAPIFactoryException extends Exception {
	@Getter
	private int code;
	public GGAPIFactoryException(int code, Exception e) {
		super(e);
		this.code = code;
	}
	public GGAPIFactoryException(String string) {
		super(string);
	}
	public GGAPIFactoryException(int code, String string) {
		super(string);
		this.code = code;
	}
	public GGAPIFactoryException(int code, String string, Exception e) {
		super(string, e);
		this.code = code;
	}
	public GGAPIFactoryException(Exception e) {
		super(e);
	}
	private static final long serialVersionUID = 2731911397218146961L;
	public static final int BAD_ENTITY = 0;
	public static final int ENTITY_INJECTION_ERROR = 1;

}
