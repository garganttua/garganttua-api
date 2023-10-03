package com.garganttua.api.events;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.garganttua.api.spec.GGAPICrudOperation;
import com.garganttua.api.spec.IGGAPIEntity;
import com.garganttua.api.ws.GGAPIErrorObject;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GGAPIEvent<Entity extends IGGAPIEntity> {

	private GGAPICrudOperation operation;
	
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
	
	private String userId; 
	
	private Exception exception;
	
	private String exceptionMessage;
	
	private int httpReturnedCode;
	
}
