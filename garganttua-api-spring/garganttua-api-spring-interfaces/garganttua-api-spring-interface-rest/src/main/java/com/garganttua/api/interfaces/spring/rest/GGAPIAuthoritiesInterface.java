package com.garganttua.api.interfaces.spring.rest;

import javax.inject.Inject;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.garganttua.api.spec.engine.IGGAPIEngine;

@RestController
public class GGAPIAuthoritiesInterface {

	@Inject
	private IGGAPIEngine engine;
	
	@RequestMapping(method = RequestMethod.GET, path = "/authorities")
	public ResponseEntity<?> getAuthorities(){
		return new ResponseEntity<>(this.engine.getAuthorities(),  HttpStatus.OK);
	}
}
