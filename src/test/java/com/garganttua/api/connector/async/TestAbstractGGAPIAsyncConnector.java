package com.garganttua.api.connector.async;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.garganttua.api.connector.GGAPIConnectorException;
import com.garganttua.api.connector.IGGAPIConnector.GGAPIConnectorOperation;
import com.garganttua.api.engine.IGGAPIEngine;
import com.garganttua.api.spec.IGGAPIDomain;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class TestAbstractGGAPIAsyncConnector{

	private static final IGGAPIDomain<TestEntity, Dto> domainObject = new IGGAPIDomain<TestEntity, Dto>() {

		@Override
		public Class<TestEntity> getEntityClass() {
			return TestEntity.class;
		}

		@Override
		public Class<Dto> getDtoClass() {
			return Dto.class;
		}

		@Override
		public String getDomain() {
			return "tests";
		}
	};
	
	private static Connector connector = new Connector(domainObject);
	
	static private class Connector extends AbstractGGAPIAsyncConnector<TestEntity, List<TestEntity>, Dto>{

		public Connector(IGGAPIDomain<TestEntity, Dto> domain) {
			super(domain);
		}

		@Override
		public void publishRequest(GGAPIAsyncConnectorEnvelop<?> message) throws GGAPIConnectorException {
			Thread t = new Thread() {

				@Override
				public void run() {
					try {
						Thread.sleep(TestAbstractGGAPIAsyncConnector.responseDelay);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					GGAPIAsyncConnectorEnvelop<?> response = new GGAPIAsyncConnectorEnvelop<>(
							GGAPIAsyncMessageType.REQUEST, UUID.randomUUID().toString(), message.getMessageUuid(),
							message.getTenantId(), message.getDomain(), GGAPIAsyncResponseStatus.OK,
							message.getOperation(), message.getEntity(), null, "Success");

					try {
						connector.onResponse(response);
					} catch (JsonProcessingException e) {
						// TODO Auto-generated catch block
//				e.printStackTrace();
					} catch (GGAPIConnectorException e) {
						// TODO Auto-generated catch block
//				e.printStackTrace();
					}
				}
			};
			t.start();
			
		}

		@Override
		public void setEngine(IGGAPIEngine engine) {
			// TODO Auto-generated method stub
			
		}
	}

	static long responseDelay = 150;

	@BeforeAll
	public static void setUp() {
	    final Logger logger = (Logger)LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
	    logger.setLevel(Level.ALL);
	}

	@Test
	public void testReadEntity() throws GGAPIConnectorException, ExecutionException {
		System.out.println("*********************** testReadEntity");
		TestAbstractGGAPIAsyncConnector.connector.executor = Executors.newFixedThreadPool(10);
		TestAbstractGGAPIAsyncConnector.connector.timeout = 3;
		TestAbstractGGAPIAsyncConnector.connector.unit = TimeUnit.SECONDS;

		TestAbstractGGAPIAsyncConnector.responseDelay = 150;

		TestEntity entity = new TestEntity("123456789", "123456789");

		Future<TestEntity> response = TestAbstractGGAPIAsyncConnector.connector.requestEntity("1", entity, GGAPIConnectorOperation.READ);

		try {

			while(!response.isDone()) {
			    Thread.sleep(300);
			}


		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
		}
		TestEntity entityReponse = null;
		try {
			entityReponse = response.get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
		}

		assertNotNull(entityReponse);
	}

	@Test
	public void testReadEntityWithTimeout() throws GGAPIConnectorException, ExecutionException {
		System.out.println("*********************** testReadEntityWithTimeout");
		TestAbstractGGAPIAsyncConnector.connector.executor = Executors.newFixedThreadPool(10);
		TestAbstractGGAPIAsyncConnector.connector.timeout = 5;
		TestAbstractGGAPIAsyncConnector.connector.unit = TimeUnit.SECONDS;

		TestAbstractGGAPIAsyncConnector.responseDelay = 150;

		TestEntity entity = new TestEntity("123456789", "123456789");

		Future<TestEntity> response = TestAbstractGGAPIAsyncConnector.connector.requestEntity("1", entity, GGAPIConnectorOperation.READ);

		TestEntity entityReponse = null;
		try {

			response.get(1, TimeUnit.SECONDS);

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
		}

		assertEquals(null, entityReponse);
	}

	@Test
	public void testReadEntityWithTimeoutBiggerThanConnector() throws GGAPIConnectorException, ExecutionException {
		System.out.println("*********************** testReadEntityWithTimeoutBiggerThanConnector");
		TestAbstractGGAPIAsyncConnector.connector.executor = Executors.newFixedThreadPool(10);
		TestAbstractGGAPIAsyncConnector.connector.timeout = 1;
		TestAbstractGGAPIAsyncConnector.connector.unit = TimeUnit.SECONDS;

		TestAbstractGGAPIAsyncConnector.responseDelay = 150;

		TestEntity entity = new TestEntity("123456789", "123456789");

		Future<TestEntity> response = TestAbstractGGAPIAsyncConnector.connector.requestEntity("1", entity, GGAPIConnectorOperation.READ);

		TestEntity entityReponse = null;
		try {

			entityReponse = response.get(3, TimeUnit.SECONDS);


		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
		}

		assertNotNull(entityReponse);
	}

	@Test
	public void testReadEntityWithTimeoutBiggerThanConnectorAndResponseLate() throws GGAPIConnectorException, ExecutionException, InterruptedException {
		System.out.println("*********************** testReadEntityWithTimeoutBiggerThanConnectorAndResponseLate");
		TestAbstractGGAPIAsyncConnector.connector.executor = Executors.newFixedThreadPool(10);
		TestAbstractGGAPIAsyncConnector.connector.timeout = 1;
		TestAbstractGGAPIAsyncConnector.connector.unit = TimeUnit.SECONDS;

		TestAbstractGGAPIAsyncConnector.responseDelay = 3000;

		TestEntity entity = new TestEntity("123456789", "123456789");

		Exception exception = assertThrows(ExecutionException.class, () -> {

			Future<TestEntity> response = TestAbstractGGAPIAsyncConnector.connector.requestEntity("1", entity, GGAPIConnectorOperation.READ);

			TestEntity entityReponse = null;

			try {

				entityReponse = response.get(2, TimeUnit.SECONDS);

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
//			e.printStackTrace();
			} catch (TimeoutException e) {
				// TODO Auto-generated catch block
//			e.printStackTrace();
			}

		 });


	}


}
