package com.garganttua.api.core.dto.exceptions;

public class GGAPIDtoException extends Exception {

	private static final long serialVersionUID = 4914275250821928797L;
	private int code;

	public GGAPIDtoException(int code, String string) {
		super(string);
		this.code = code;
	}

	public GGAPIDtoException(Exception e) {
		super(e);
	}

	public static final int DTO_DEFINITION_ERROR = 0;

}
