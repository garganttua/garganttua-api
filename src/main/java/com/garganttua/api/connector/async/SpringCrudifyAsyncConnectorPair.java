package com.garganttua.api.connector.async;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SpringCrudifyAsyncConnectorPair {
	
	private Object locker;

	private SpringCrudifyAsyncConnectorEnvelop<?> entity;

}
