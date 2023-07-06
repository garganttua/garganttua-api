package com.garganttua.api.events;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatusCode;

import com.garganttua.api.spec.GGAPICrudOperation;
import com.garganttua.api.spec.IGGAPIEntity;

import lombok.Data;

@Data
public class GGAPIEvent<Entity extends IGGAPIEntity> {
	
	public GGAPIEvent() {
		this.inDate = new Date();
		this.inParams = new HashMap<String, String>();
	}
	
	private GGAPICrudOperation operation;
	
	private Date inDate;
	
	private Date outDate; 
	
	private int exceptionCode; 
	
	private Map<String, String> inParams;
	
	private Entity in; 
	
	private Entity out; 
	
	private long outCount;
	
	private List<Entity> outList;
	
	private String tenantId; 
	
	private String userId; 
	
	private Exception exception;
	
	private String exceptionMessage;
	
	private HttpStatusCode httpCode;

}
