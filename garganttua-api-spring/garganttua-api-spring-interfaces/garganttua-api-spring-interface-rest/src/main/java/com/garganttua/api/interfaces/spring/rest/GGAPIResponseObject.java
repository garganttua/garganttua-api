package com.garganttua.api.interfaces.spring.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GGAPIResponseObject {
	
	public static final int UNEXPECTED_ERROR = 2;
	public static int NO_ERROR_CODE = 0;
	public static int BAD_REQUEST = 1;
	
	private Object message;
	
	private int code;
	
	@Override
	public String toString() {
		try {
			return new ObjectMapper().writeValueAsString(this);
		} catch (JsonProcessingException e) {
			return this.message.toString();
		}
	}
	
}