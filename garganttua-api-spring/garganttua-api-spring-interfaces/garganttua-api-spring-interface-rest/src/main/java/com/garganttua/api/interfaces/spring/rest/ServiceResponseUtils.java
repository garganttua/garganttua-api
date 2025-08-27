package com.garganttua.api.interfaces.spring.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.garganttua.api.spec.service.ServiceResponseCode;
import com.garganttua.api.spec.service.IServiceResponse;

public class ServiceResponseUtils {

	public static ResponseEntity<?> toResponseEntity(IServiceResponse response) {
		ServiceResponseCode code = response.getResponseCode();
		Object serviceResponse = response.getResponse();

		switch (code) {
			case OK:
				return new ResponseEntity<>(serviceResponse, HttpStatus.OK);
			case CREATED:
				return new ResponseEntity<>(serviceResponse, HttpStatus.CREATED);
			case DELETED:
				return new ResponseEntity<>(new ResponseObject("successfully deleted", HttpStatus.OK.value()), HttpStatus.OK);
			case UPDATED:
				return new ResponseEntity<>(serviceResponse, HttpStatus.OK);
			case SERVER_ERROR:
				return new ResponseEntity<>(new ResponseObject(serviceResponse, HttpStatus.INTERNAL_SERVER_ERROR.value()), HttpStatus.INTERNAL_SERVER_ERROR);
			case NOT_FOUND:
				return new ResponseEntity<>(new ResponseObject(serviceResponse, HttpStatus.NOT_FOUND.value()), HttpStatus.NOT_FOUND);
			case CLIENT_ERROR:
				return new ResponseEntity<>(new ResponseObject(serviceResponse, HttpStatus.BAD_REQUEST.value()), HttpStatus.BAD_REQUEST);
			case NOT_AVAILABLE:
				return new ResponseEntity<>(new ResponseObject(serviceResponse, HttpStatus.NOT_IMPLEMENTED.value()), HttpStatus.NOT_IMPLEMENTED);
			case FORBIDDEN:
				return new ResponseEntity<>(new ResponseObject(serviceResponse, HttpStatus.FORBIDDEN.value()), HttpStatus.FORBIDDEN);
			case UNAUTHORIZED:
				return new ResponseEntity<>(new ResponseObject(serviceResponse, HttpStatus.FORBIDDEN.value()), HttpStatus.UNAUTHORIZED);
		}
		return new ResponseEntity<>(new ResponseObject("internal server error", HttpStatus.INTERNAL_SERVER_ERROR.value()), HttpStatus.INTERNAL_SERVER_ERROR);
	}

}
