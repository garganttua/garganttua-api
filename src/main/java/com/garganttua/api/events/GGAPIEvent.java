package com.garganttua.api.events;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.garganttua.api.core.IGGAPICaller;
import com.garganttua.api.service.GGAPIErrorObject;
import com.garganttua.api.service.GGAPIServiceMethod;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GGAPIEvent<Entity> {

	private GGAPIServiceMethod method;
	
	private IGGAPICaller caller;
	
	private String endPoint;
	
	private Date inDate = new Date();
	
	private Date outDate; 
	
	private int exceptionCode; 
	
	private Map<String, String> inParams = new HashMap<String, String>();
	
	private Entity in; 
	
	private Entity out; 
	
	private String entityClass;
	
	private long outCount;
	
	private List<Entity> outList;
	
	private GGAPIErrorObject errorObject;
	
	private String tenantId; 
	
	private String ownerId; 
	
	private Exception exception;
	
	private String exceptionMessage;
	
	private int httpReturnedCode;
	
}
