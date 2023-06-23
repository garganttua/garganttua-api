package com.garganttua.api.connector.async;

import com.garganttua.api.connector.IGGAPIConnector.GGAPIConnectorOperation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class GGAPIAsyncConnectorEnvelop <T> {
	
	private GGAPIAsyncMessageType type;
	
	private String messageUuid; 
	
	private String transactionUuid;

	private String tenantId;

	private String domain;
	
	private GGAPIAsyncResponseStatus status;

	private GGAPIConnectorOperation operation;
	
	private T entity;
	
	private String responseDirective;
	
	private String responseMessage;
	
}
