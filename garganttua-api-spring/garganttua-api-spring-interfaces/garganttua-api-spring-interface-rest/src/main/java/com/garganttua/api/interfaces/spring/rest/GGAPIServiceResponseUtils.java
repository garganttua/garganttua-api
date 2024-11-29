package com.garganttua.api.interfaces.spring.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.garganttua.api.spec.service.GGAPIServiceResponseCode;
import com.garganttua.api.spec.service.IGGAPIServiceResponse;

public class GGAPIServiceResponseUtils {

	public static ResponseEntity<?> toResponseEntity(IGGAPIServiceResponse response) {
		GGAPIServiceResponseCode code = response.getResponseCode();
		Object serviceResponse = response.getResponse();

		switch (code) {
			case OK:
				return new ResponseEntity<>(serviceResponse, HttpStatus.OK);
			case CREATED:
				return new ResponseEntity<>(serviceResponse, HttpStatus.CREATED);
			case DELETED:
				return new ResponseEntity<>(new GGAPIResponseObject("successfully deleted", HttpStatus.OK.value()), HttpStatus.OK);
			case UPDATED:
				return new ResponseEntity<>(serviceResponse, HttpStatus.OK);
			case SERVER_ERROR:
				return new ResponseEntity<>(new GGAPIResponseObject(serviceResponse, HttpStatus.INTERNAL_SERVER_ERROR.value()), HttpStatus.INTERNAL_SERVER_ERROR);
			case NOT_FOUND:
				return new ResponseEntity<>(new GGAPIResponseObject(serviceResponse, HttpStatus.NOT_FOUND.value()), HttpStatus.NOT_FOUND);
			case CLIENT_ERROR:
				return new ResponseEntity<>(new GGAPIResponseObject(serviceResponse, HttpStatus.BAD_REQUEST.value()), HttpStatus.BAD_REQUEST);
			case NOT_AVAILABLE:
				return new ResponseEntity<>(new GGAPIResponseObject(serviceResponse, HttpStatus.NOT_IMPLEMENTED.value()), HttpStatus.NOT_IMPLEMENTED);
			case FORBIDDEN:
				return new ResponseEntity<>(new GGAPIResponseObject(serviceResponse, HttpStatus.FORBIDDEN.value()), HttpStatus.FORBIDDEN);
			case UNAUTHORIZED:
				return new ResponseEntity<>(new GGAPIResponseObject(serviceResponse, HttpStatus.FORBIDDEN.value()), HttpStatus.UNAUTHORIZED);
		}
		return new ResponseEntity<>(new GGAPIResponseObject("internal server error", HttpStatus.INTERNAL_SERVER_ERROR.value()), HttpStatus.INTERNAL_SERVER_ERROR);
	}

}
