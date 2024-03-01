package com.garganttua.api.core.dto.exceptions;

import com.garganttua.api.core.entity.exceptions.GGAPIEntityException;

import lombok.Getter;

public class GGAPIDtoException extends Exception {

	@Getter
	private int code;

	public GGAPIDtoException(int dtoDefinitionError, String string) {
		super(string);
		this.code = dtoDefinitionError;
	}

	public GGAPIDtoException(int entityDefinitionError, GGAPIEntityException e) {
		super(e);
		this.code = entityDefinitionError;
	}

	private static final long serialVersionUID = -1362599450835541877L;

	public static final int DTO_DEFINITION_ERROR = 0;

	public static final int ENTITY_DEFINITION_ERROR = 1;
}
