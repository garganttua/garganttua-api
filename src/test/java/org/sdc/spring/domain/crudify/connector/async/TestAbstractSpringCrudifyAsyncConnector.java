package org.sdc.spring.domain.crudify.connector.async;

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
import com.garganttua.api.connector.SpringCrudifyConnectorException;
import com.garganttua.api.connector.async.AbstractSpringCrudifyAsyncConnector;
import com.garganttua.api.connector.async.SpringCrudifyAsyncConnectorEnvelop;
import com.garganttua.api.connector.async.SpringCrudifyAsyncMessageType;
import com.garganttua.api.connector.async.SpringCrudifyAsyncResponseStatus;
import com.garganttua.api.spec.ISpringCrudifyDomain;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class TestAbstractSpringCrudifyAsyncConnector extends AbstractSpringCrudifyAsyncConnector<TestEntity, List<TestEntity>, Dto>{

	private static final ISpringCrudifyDomain<TestEntity, Dto> domainObject = new ISpringCrudifyDomain<TestEntity, Dto>() {

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

	public TestAbstractSpringCrudifyAsyncConnector(ISpringCrudifyDomain<TestEntity, Dto> domain) {
		super(domain);
	}

	static TestAbstractSpringCrudifyAsyncConnector connector = new TestAbstractSpringCrudifyAsyncConnector(domainObject);

	static long responseDelay = 150;

	@BeforeAll
	public static void setUp() {
	    final Logger logger = (Logger)LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
	    logger.setLevel(Level.ALL);
	}

	@Test
	public void testReadEntity() throws SpringCrudifyConnectorException, ExecutionException {
		System.out.println("*********************** testReadEntity");
		TestAbstractSpringCrudifyAsyncConnector.connector.executor = Executors.newFixedThreadPool(10);
		TestAbstractSpringCrudifyAsyncConnector.connector.timeout = 3;
		TestAbstractSpringCrudifyAsyncConnector.connector.unit = TimeUnit.SECONDS;

		TestAbstractSpringCrudifyAsyncConnector.responseDelay = 150;

		TestEntity entity = new TestEntity("123456789", "123456789");

		Future<TestEntity> response = TestAbstractSpringCrudifyAsyncConnector.connector.requestEntity("1", entity, SpringCrudifyConnectorOperation.READ);

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
	public void testReadEntityWithTimeout() throws SpringCrudifyConnectorException, ExecutionException {
		System.out.println("*********************** testReadEntityWithTimeout");
		TestAbstractSpringCrudifyAsyncConnector.connector.executor = Executors.newFixedThreadPool(10);
		TestAbstractSpringCrudifyAsyncConnector.connector.timeout = 5;
		TestAbstractSpringCrudifyAsyncConnector.connector.unit = TimeUnit.SECONDS;

		TestAbstractSpringCrudifyAsyncConnector.responseDelay = 150;

		TestEntity entity = new TestEntity("123456789", "123456789");

		Future<TestEntity> response = TestAbstractSpringCrudifyAsyncConnector.connector.requestEntity("1", entity, SpringCrudifyConnectorOperation.READ);

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
	public void testReadEntityWithTimeoutBiggerThanConnector() throws SpringCrudifyConnectorException, ExecutionException {
		System.out.println("*********************** testReadEntityWithTimeoutBiggerThanConnector");
		TestAbstractSpringCrudifyAsyncConnector.connector.executor = Executors.newFixedThreadPool(10);
		TestAbstractSpringCrudifyAsyncConnector.connector.timeout = 1;
		TestAbstractSpringCrudifyAsyncConnector.connector.unit = TimeUnit.SECONDS;

		TestAbstractSpringCrudifyAsyncConnector.responseDelay = 150;

		TestEntity entity = new TestEntity("123456789", "123456789");

		Future<TestEntity> response = TestAbstractSpringCrudifyAsyncConnector.connector.requestEntity("1", entity, SpringCrudifyConnectorOperation.READ);

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
	public void testReadEntityWithTimeoutBiggerThanConnectorAndResponseLate() throws SpringCrudifyConnectorException, ExecutionException, InterruptedException {
		System.out.println("*********************** testReadEntityWithTimeoutBiggerThanConnectorAndResponseLate");
		TestAbstractSpringCrudifyAsyncConnector.connector.executor = Executors.newFixedThreadPool(10);
		TestAbstractSpringCrudifyAsyncConnector.connector.timeout = 1;
		TestAbstractSpringCrudifyAsyncConnector.connector.unit = TimeUnit.SECONDS;

		TestAbstractSpringCrudifyAsyncConnector.responseDelay = 3000;

		TestEntity entity = new TestEntity("123456789", "123456789");

		Exception exception = assertThrows(ExecutionException.class, () -> {

			Future<TestEntity> response = TestAbstractSpringCrudifyAsyncConnector.connector.requestEntity("1", entity, SpringCrudifyConnectorOperation.READ);

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



	@Override
	public void publishRequest(SpringCrudifyAsyncConnectorEnvelop<?> message) throws SpringCrudifyConnectorException {

		Thread t = new Thread() {

			@Override
			public void run() {
				try {
					Thread.sleep(TestAbstractSpringCrudifyAsyncConnector.responseDelay);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				SpringCrudifyAsyncConnectorEnvelop<?> response = new SpringCrudifyAsyncConnectorEnvelop<>(
						SpringCrudifyAsyncMessageType.REQUEST, UUID.randomUUID().toString(), message.getMessageUuid(),
						message.getTenantId(), message.getDomain(), SpringCrudifyAsyncResponseStatus.OK,
						message.getOperation(), message.getEntity(), null, "Success");

				try {
					connector.onResponse(response);
				} catch (JsonProcessingException e) {
					// TODO Auto-generated catch block
//			e.printStackTrace();
				} catch (SpringCrudifyConnectorException e) {
					// TODO Auto-generated catch block
//			e.printStackTrace();
				}
			}
		};
		t.start();
	}

}
