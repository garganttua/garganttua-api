package com.garganttua.api.core.service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.garganttua.api.spec.EntityOperation;
import com.garganttua.api.spec.caller.ICaller;
import com.garganttua.api.spec.domain.IDomain;
import com.garganttua.api.spec.event.IEvent;
import com.garganttua.api.spec.service.ServiceResponseCode;
import com.garganttua.api.spec.service.IServiceResponse;

import lombok.Data;

@Data
public class Event implements IEvent {
	
	public Event() {
		this.inDate = new Date();
		this.inParams = new HashMap<String, String>();
	}
	
	private EntityOperation operation;
	
	private Date inDate;
	
	private Date outDate; 
	
	private int exceptionCode; 
	
	private Map<String, String> inParams;
	
	private Object in; 
	
	private Object out; 
	
	private ICaller caller;
	
	private String tenantId; 
	
	private String ownerId;
	
	private String userId; 
	
	private IDomain domain;
	
	private String exceptionMessage;
	
	private ServiceResponseCode code;

	@Override
	public IServiceResponse toServiceResponse() {
		return new ServiceResponse(out, code);
	}
}
