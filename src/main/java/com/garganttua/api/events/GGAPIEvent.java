package com.garganttua.api.events;

import java.util.Date;
import java.util.List;

import org.springframework.http.HttpStatusCode;

import com.garganttua.api.spec.GGAPICrudOperation;
import com.garganttua.api.spec.IGGAPIEntity;

import lombok.Data;

@Data
public class GGAPIEvent<Entity extends IGGAPIEntity> {
	
	public GGAPIEvent() {
		this.date = new Date();
	}
	
	private GGAPICrudOperation operation;
	
	private Date date; 
	
	private int exceptionCode; 
	
	private List<String> inParams;
	
	private Entity in; 
	
	private Entity out; 
	
	private List<Entity> outList;
	
	private String tenantId; 
	
	private String userId; 
	
	private Exception exception;
	
	private HttpStatusCode httpCode;

}
