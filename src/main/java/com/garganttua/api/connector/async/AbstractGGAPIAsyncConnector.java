package com.garganttua.api.connector.async;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Value;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.garganttua.api.connector.IGGAPIConnector;
import com.garganttua.api.connector.GGAPIConnectorException;
import com.garganttua.api.repository.dto.IGGAPIDTOObject;
import com.garganttua.api.spec.IGGAPIDomain;
import com.garganttua.api.spec.IGGAPIEntity;
import com.garganttua.api.spec.GGAPIDomainable;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractGGAPIAsyncConnector<T extends IGGAPIEntity, S extends List<T>, U extends IGGAPIDTOObject<T>> extends GGAPIDomainable<T, U> implements IGGAPIConnector<T, S, U> {

	public AbstractGGAPIAsyncConnector(IGGAPIDomain<T, U> domain) {
		super(domain);
	}

	@Inject
	protected ExecutorService executor;
	
	@Value("${spring.domain.crudify.connector.timeout}")
	protected int timeout;
	
	@Value("${spring.domain.crudify.connector.timeout.timeUnit}")
	protected TimeUnit unit;
	
	private ObjectMapper mapper = new ObjectMapper();
	
	private Object mapLocker = new Object(); 

	private Map<String, GGAPIAsyncConnectorPair> receivedMessages = new HashMap<String, GGAPIAsyncConnectorPair>();

	
	/*
	 * This method drives me feeling sick, it has to be refactored ASAP !!!! The filosophy is to merge it with the requestEntity method.  
	 */
	@Override
	public Future<S> requestList(String tenantId, S list, GGAPIConnectorOperation operation) throws GGAPIConnectorException {
		log.info("[Tenant {}] [Domain {}] Processing request entity list operation {} ", tenantId, this.domain, operation);
		
		return this.executor.submit(new Callable<S>() {
			
			@SuppressWarnings("unchecked")
			public S call() throws GGAPIConnectorException {
				mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, true);
				
				String uuid = UUID.randomUUID().toString();
				String transactionUuid = uuid;
				
				Object locker = new Object();

				GGAPIAsyncConnectorEnvelop<S> message = new GGAPIAsyncConnectorEnvelop<S>(
						GGAPIAsyncMessageType.REQUEST, uuid, transactionUuid, tenantId, domain, null, operation,
						list, null, null);
				
				try {
					log.info("[Tenant {}] [Domain {}] Sending request {}", tenantId, domain, mapper.writeValueAsString(message));
				} catch (JsonProcessingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				synchronized (mapLocker) {
					receivedMessages.put(uuid, new GGAPIAsyncConnectorPair(locker, null));
				}
				
				publishRequest(message);
				
				if( log.isDebugEnabled() ) log.debug("Thread sleeping for {} {}", timeout, unit.toString());
				
				long millis = TimeUnit.MILLISECONDS.convert(timeout, unit);
				
				if( log.isDebugEnabled() ) log.debug("Thread sleeping for {} ms", millis);
				
				try {
					synchronized (locker) {
						locker.wait(millis);
					}
					
				} catch(InterruptedException e) {
					//Message is received
					if( log.isDebugEnabled() ) log.debug("Thread awake due to interruption exception");
				}
				
				if( log.isDebugEnabled() ) log.debug("Thread awake");
				
				GGAPIAsyncConnectorPair pair = null;
				synchronized (mapLocker) {
					pair = receivedMessages.remove(uuid);
				}
				
				if( pair.getEntity() == null ) {
					try {
						log.warn("[Tenant {}] [Domain {}] no message received for message {}", tenantId, domain, mapper.writeValueAsString(message));
					} catch (JsonProcessingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					throw new GGAPIConnectorException("No message received from connector");
				}
				
				if( log.isDebugEnabled() ) log.debug("map size "+receivedMessages.size());
				
				return (S) pair.getEntity().getEntity();
				
			}
		});
	}
	
	
	@Override
	public Future<T> requestEntity(String tenantId, T object, GGAPIConnectorOperation operation) throws GGAPIConnectorException {
		log.info("[Tenant {}] [Domain {}] Processing request entity operation {} ", tenantId, this.domain, operation);
		
		return this.executor.submit(new Callable<T>() {
			
			@SuppressWarnings("unchecked")
			public T call() throws GGAPIConnectorException {
				
				if( object == null ) {
					mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
				} else {
					mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, true);
				}
				
				String uuid = UUID.randomUUID().toString();
				String transactionUuid = uuid;
				
				Object locker = new Object();

				GGAPIAsyncConnectorEnvelop<T> message = new GGAPIAsyncConnectorEnvelop<T>(
						GGAPIAsyncMessageType.REQUEST, uuid, transactionUuid, tenantId, domain, null, operation,
						object, null, null);
				
				try {
					log.info("[Tenant {}] [Domain {}] Sending request {}", tenantId, domain, mapper.writeValueAsString(message));
				} catch (JsonProcessingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				synchronized (mapLocker) {
					receivedMessages.put(uuid, new GGAPIAsyncConnectorPair(locker, null));
				}
				
				publishRequest(message);
				
				if( log.isDebugEnabled() ) log.debug("Thread sleeping for {} {}", timeout, unit.toString());
				
				long millis = TimeUnit.MILLISECONDS.convert(timeout, unit);
				
				if( log.isDebugEnabled() ) log.debug("Thread sleeping for {} ms", millis);
				
				try {
					synchronized (locker) {
						locker.wait(millis);
					}
					
				} catch(InterruptedException e) {
					//Message is received
					if( log.isDebugEnabled() ) log.debug("Thread awake due to interruption exception");
				}
				
				if( log.isDebugEnabled() ) log.debug("Thread awake");
				
				GGAPIAsyncConnectorPair pair = null;
				synchronized (mapLocker) {
					pair = receivedMessages.remove(uuid);
				}
				
				if( pair.getEntity() == null ) {
					try {
						log.warn("[Tenant {}] [Domain {}] no message received for message {}", tenantId, domain, mapper.writeValueAsString(message));
					} catch (JsonProcessingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					throw new GGAPIConnectorException("No message received from connector");
				}
				
				if( log.isDebugEnabled() ) log.debug("map size "+receivedMessages.size());
				return (T) pair.getEntity().getEntity();
			}
		});

	}

	protected void onResponse(GGAPIAsyncConnectorEnvelop<?> message) throws GGAPIConnectorException, JsonProcessingException {
		log.info("[Tenant {}] [Domain {}] Response received {}", message.getTenantId(), domain, this.mapper.writeValueAsString(message));
		
		String transactionUuid = message.getTransactionUuid();
		
		GGAPIAsyncConnectorPair pair = null;
		synchronized (this.mapLocker) {
			pair = this.receivedMessages.get(transactionUuid);
		}
		
		if( pair != null ) {
			pair.setEntity(message);
			synchronized (pair.getLocker()) {
				pair.getLocker().notifyAll();
			}
		} else {
			log.warn("[Tenant {}] [Domain {}] Request with transaction id {} not found, dropping message", message.getTenantId(), this.domain, message.getTransactionUuid());
		}
		
	}

	// -----------------------------------------------------------//
	// Abstract methods below to be implemented by sub classes    //
	// -----------------------------------------------------------//

	abstract public void publishRequest(GGAPIAsyncConnectorEnvelop<?> message) throws GGAPIConnectorException;
	
}
