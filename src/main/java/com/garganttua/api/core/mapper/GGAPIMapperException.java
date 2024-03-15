package com.garganttua.api.core.mapper;

import com.garganttua.api.core.mapper.fieldFinder.GGAPIFieldFinderException;

import lombok.Getter;

public class GGAPIMapperException extends Exception {

	private static final long serialVersionUID = 3629256996026750672L;
	public static final int ENTITY_DEFINITION_ERROR = 0;
	public static final int DTO_DEFINITION_ERROR = 1;
	public static final int DTO_INSTANCIATION_ERROR = 2;
	public static final int DIRECT_FIELD_MAPPING = 3;
	public static final int MAPPING = 4;
	public static final int ENTITY_INSTANCIATION_ERROR = 5;
	public static final int OBJECT_MAPPING = 6;
	public static final int METHOD_FIELD_MAPPING = 7;
	
	@Getter
	private int code;

	public GGAPIMapperException(int code, Exception e) {
		super(e);
		this.code = code;
	}

	public GGAPIMapperException(int code, String string, Exception e) {
		super(string, e);
		this.code = code;
	}

	public GGAPIMapperException(int code, String string) {
		super(string);
		this.code = code;
	}

	public GGAPIMapperException(String message, GGAPIFieldFinderException e) {
		super(message, e);
	}


}
