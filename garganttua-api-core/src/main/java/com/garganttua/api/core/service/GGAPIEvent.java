package com.garganttua.api.core.service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.garganttua.api.spec.IGGAPICaller;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.event.IGGAPIEvent;
import com.garganttua.api.spec.service.GGAPIServiceMethod;
import com.garganttua.api.spec.service.GGAPIServiceResponseCode;
import com.garganttua.api.spec.service.IGGAPIServiceResponse;

import lombok.Data;

@Data
public class GGAPIEvent implements IGGAPIEvent {
	
	public GGAPIEvent() {
		this.inDate = new Date();
		this.inParams = new HashMap<String, String>();
	}
	
	private GGAPIServiceMethod method;
	
	private Date inDate;
	
	private Date outDate; 
	
	private int exceptionCode; 
	
	private Map<String, String> inParams;
	
	private Object in; 
	
	private Object out; 
	
	private IGGAPICaller caller;
	
	private String tenantId; 
	
	private String ownerId;
	
	private String userId; 
	
	private IGGAPIDomain domain;
	
	private String exceptionMessage;
	
	private GGAPIServiceResponseCode code;

	@Override
	public IGGAPIServiceResponse toServiceResponse() {
		return new GGAPIServiceResponse(out, code);
	}
}
