package com.garganttua.api.connector.async;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GGAPIAsyncConnectorPair {
	
	private Object locker;

	private GGAPIAsyncConnectorEnvelop<?> entity;

}
