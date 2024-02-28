package com.garganttua.api.core.entity.checker;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import java.util.Optional;

import org.geojson.Point;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.garganttua.api.core.IGGAPICaller;
import com.garganttua.api.core.entity.AbstractGGAPIEntity;
import com.garganttua.api.core.entity.annotations.GGAPIEntity;
import com.garganttua.api.core.entity.annotations.GGAPIEntityDeleteMethod;
import com.garganttua.api.core.entity.annotations.GGAPIEntityDeleteMethodProvider;
import com.garganttua.api.core.entity.annotations.GGAPIEntityGeolocalized;
import com.garganttua.api.core.entity.annotations.GGAPIEntityHidden;
import com.garganttua.api.core.entity.annotations.GGAPIEntityHiddenable;
import com.garganttua.api.core.entity.annotations.GGAPIEntityId;
import com.garganttua.api.core.entity.annotations.GGAPIEntityLocation;
import com.garganttua.api.core.entity.annotations.GGAPIEntityOwned;
import com.garganttua.api.core.entity.annotations.GGAPIEntityOwner;
import com.garganttua.api.core.entity.annotations.GGAPIEntityOwnerId;
import com.garganttua.api.core.entity.annotations.GGAPIEntityRepository;
import com.garganttua.api.core.entity.annotations.GGAPIEntitySaveMethod;
import com.garganttua.api.core.entity.annotations.GGAPIEntitySaveMethodProvider;
import com.garganttua.api.core.entity.annotations.GGAPIEntityShare;
import com.garganttua.api.core.entity.annotations.GGAPIEntityShared;
import com.garganttua.api.core.entity.annotations.GGAPIEntityTenant;
import com.garganttua.api.core.entity.annotations.GGAPIEntityTenantId;
import com.garganttua.api.core.entity.annotations.GGAPIEntityUuid;
import com.garganttua.api.core.entity.exceptions.GGAPIEntityException;
import com.garganttua.api.core.entity.interfaces.IGGAPIEntityDeleteMethod;
import com.garganttua.api.core.entity.interfaces.IGGAPIEntitySaveMethod;
import com.garganttua.api.engine.GGAPIEngineException;
import com.garganttua.api.repository.IGGAPIRepository;
import com.garganttua.api.security.IGGAPISecurity;

public class GGAPIEntityCheckerTest {
	
	private static GGAPIEntityChecker checker;

	@BeforeAll
	public static void init() {
		GGAPIEntityCheckerTest.checker = new GGAPIEntityChecker();
	}
	
	@Test
	public void testEntityWithNoDomain() {
		
		@GGAPIEntity(
				domain = "", 
				dto = ""
		)
		class Entity {
			@GGAPIEntityUuid
			private String tuuid;
		}
		
		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityCheckerTest.checker.checkEntityClass(Entity.class);
		 });
		
		assertEquals("No domain provided in annotation of entity "+Entity.class, exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}
	
	@Test
	public void testEntityWithNoDto() {
		
		@GGAPIEntity(
				domain = "entity", 
				dto = ""
		)
		class Entity {
			@GGAPIEntityUuid
			private String tuuid;
		}
		
		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityCheckerTest.checker.checkEntityClass(Entity.class);
		 });
		
		assertEquals("No dto provided in annotation of entity "+Entity.class, exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}
	
	@Test
	public void testEntityWithInvalidDto() {
		
		@GGAPIEntity(
				domain = "entity", 
				dto = "com.dto"
		)
		class Entity {
			@GGAPIEntityUuid
			private String tuuid;
		}
		
		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityCheckerTest.checker.checkEntityClass(Entity.class);
		 });
		
		assertEquals("Provided Dto class is not found for entity "+Entity.class, exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}
	
	@Test
	public void testEntityWithValidDto() {
		
		class Dto {
			
		}

		@GGAPIEntity(
				domain = "entity", 
				dto = "com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$1Dto"
		)
		class Entity  extends AbstractGGAPIEntity{
			@GGAPIEntityUuid
			private String tuuid;
			@GGAPIEntityId
			private String id;
		}
		
		assertDoesNotThrow(() -> {
			GGAPIEntityCheckerTest.checker.checkEntityClass(Entity.class);
		});
	}
	
	@Test
	public void testTenantEntityWithNoAnnotatedField() {
		
		class Dto {
			
		}

		@GGAPIEntity(
				domain = "entity", 
				dto = "com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$1Dto"
		)
		@GGAPIEntityTenant
		class Entity  extends AbstractGGAPIEntity{
			@GGAPIEntityUuid
			private String tuuid;
			@GGAPIEntityId
			private String id;
		}
		
		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityCheckerTest.checker.checkEntityClass(Entity.class);
		 });
		
		assertEquals("Entity class com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$5Entity does not have any field value or field annotated with com.garganttua.api.core.entity.annotations.GGAPIEntityTenantId", exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}
	
	@Test
	public void testTenantEntityWithAnnotatedField() {
		
		class Dto {
			
		}

		@GGAPIEntity(
				domain = "entity", 
				dto = "com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$1Dto"
		)
		@GGAPIEntityTenant(superTenant = "superTenant")
		class Entity  extends AbstractGGAPIEntity{
			
			@GGAPIEntityTenantId
			private String uuid;
			
			private boolean superTenant;
			@GGAPIEntityUuid
			private String tuuid;
			@GGAPIEntityId
			private String id;
			
		}
		
		assertDoesNotThrow(() -> {
			GGAPIEntityCheckerTest.checker.checkEntityClass(Entity.class);
		});
	}
	
	@Test
	public void testTenantEntityWithFieldValue() {
		
		class Dto {
			
		}

		@GGAPIEntity(
				domain = "entity", 
				dto = "com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$1Dto"
		)
		@GGAPIEntityTenant(tenantId = "uuid", superTenant = "superTenant")
		class Entity  extends AbstractGGAPIEntity{
			
			private String uuid;
			
			private boolean superTenant;
			@GGAPIEntityUuid
			private String tuuid;
			@GGAPIEntityId
			private String id;
		}
		
		assertDoesNotThrow(() -> {
			GGAPIEntityCheckerTest.checker.checkEntityClass(Entity.class);
		});
	}
	
	@Test
	public void testTenantEntityWithFieldAnnotatedOfWrongType() {
		
		class Dto {
			
		}

		@GGAPIEntity(
				domain = "entity", 
				dto = "com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$1Dto"
		)
		@GGAPIEntityTenant()
		class Entity  extends AbstractGGAPIEntity{
			
			@GGAPIEntityTenantId
			private Float uuid;
			@GGAPIEntityUuid
			private String tuuid;
			@GGAPIEntityId
			private String id;
			
		}
		
		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityCheckerTest.checker.checkEntityClass(Entity.class);
		 });
		
		assertEquals("Entity "+Entity.class+" has field uuid with wrong type java.lang.Float, should be class java.lang.String", exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());

	}
	
	@Test
	public void testTenantEntityWithFieldValueOfWrongType() {
		
		class Dto {
			
		}

		@GGAPIEntity(
				domain = "entity", 
				dto = "com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$1Dto"
		)
		@GGAPIEntityTenant(tenantId = "uuid")
		class Entity  extends AbstractGGAPIEntity{
			
			private Float uuid;
			@GGAPIEntityUuid
			private String tuuid;
			@GGAPIEntityId
			private String id;
			
		}
		
		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityCheckerTest.checker.checkEntityClass(Entity.class);
		 });
		
		assertEquals("Entity class com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$9Entity has field uuid with wrong type class java.lang.Float, should be class java.lang.String", exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}
	
	
	/***********************************************************************************/
	
	
	@Test
	public void testOwnerEntityWithNoAnnotatedField() {
		
		class Dto {
			
		}

		@GGAPIEntity(
				domain = "entity", 
				dto = "com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$1Dto"
		)
		@GGAPIEntityOwner
		class Entity  extends AbstractGGAPIEntity{
			@GGAPIEntityUuid
			private String tuuid;
			@GGAPIEntityId
			private String id;
		}
		
		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityCheckerTest.checker.checkEntityClass(Entity.class);
		 });
		
		assertEquals("Entity class com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$10Entity does not have any field value or field annotated with com.garganttua.api.core.entity.annotations.GGAPIEntityOwnerId", exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}
	
	@Test
	public void testOwnerEntityWithAnnotatedField() {
		
		class Dto {
			
		}

		@GGAPIEntity(
				domain = "entity", 
				dto = "com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$1Dto"
		)
		@GGAPIEntityOwner(superOwner = "superOwner")
		class Entity  extends AbstractGGAPIEntity{
			
			@GGAPIEntityOwnerId
			private String uuid;
			
			private boolean superOwner;
			
			@GGAPIEntityUuid
			private String tuuid;
			@GGAPIEntityId
			private String id;
			
		}
		
		assertDoesNotThrow(() -> {
			GGAPIEntityCheckerTest.checker.checkEntityClass(Entity.class);
		});
	}
	
	@Test
	public void testOwnerEntityWithFieldValue() {
		
		class Dto {
			
		}

		@GGAPIEntity(
				domain = "entity", 
				dto = "com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$1Dto"
		)
		@GGAPIEntityOwner(ownerId = "uuid", superOwner = "superOwner")
		class Entity  extends AbstractGGAPIEntity {
			
			private String uuid;
			
			private boolean superOwner;
			
			@GGAPIEntityUuid
			private String tuuid;
			
			@GGAPIEntityId
			private String id;
		}
		
		assertDoesNotThrow(() -> {
			GGAPIEntityCheckerTest.checker.checkEntityClass(Entity.class);
		});
	}
	
	@Test
	public void testOwnerEntityWithFieldAnnotatedOfWrongType() {
		
		class Dto {
			
		}

		@GGAPIEntity(
				domain = "entity", 
				dto = "com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$1Dto"
		)
		@GGAPIEntityOwner()
		class Entity  extends AbstractGGAPIEntity{
			
			@GGAPIEntityTenantId
			private Float uuid;
			@GGAPIEntityUuid
			private String tuuid;
			@GGAPIEntityId
			private String id;
			
		}
		
		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityCheckerTest.checker.checkEntityClass(Entity.class);
		 });
		
		assertEquals("Entity class com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$13Entity does not have any field value or field annotated with com.garganttua.api.core.entity.annotations.GGAPIEntityOwnerId", exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());

	}
	
	@Test
	public void testOwnerEntityWithFieldValueOfWrongType() {
		
		class Dto {
			
		}

		@GGAPIEntity(
				domain = "entity", 
				dto = "com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$1Dto"
		)
		@GGAPIEntityOwner(ownerId = "uuid")
		class Entity  extends AbstractGGAPIEntity{
			
			private Float uuid;
			@GGAPIEntityUuid
			private String tuuid;
			@GGAPIEntityId
			private String id;
			
		}
		
		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityCheckerTest.checker.checkEntityClass(Entity.class);
		 });
		
		assertEquals("Entity class com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$14Entity has field uuid with wrong type class java.lang.Float, should be class java.lang.String", exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}
	
	/*****************************************************************************************/
	
	@Test
	public void testAnnotationEntityUuidIsPresentAndFieldHasStringType() {
		
		class Dto {
			
		}

		@GGAPIEntity(
				domain = "entity", 
				dto = "com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$1Dto"
		)
		class Entity {

			private String uuid;
			
		}
		
		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityCheckerTest.checker.checkEntityClass(Entity.class);
		 });
		
		assertEquals("Entity class com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$15Entity does not have any field annotated with @GGAPIEntityUuid", exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}
	
	@Test
	public void testAnnotationEntityUuidIsPresentAndFieldHasStringType2() {
		
		class Dto {
			
		}

		@GGAPIEntity(
				domain = "entity", 
				dto = "com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$1Dto"
		)
		class Entity {

			@GGAPIEntityUuid
			private Float uuid;
			
		}
		
		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityCheckerTest.checker.checkEntityClass(Entity.class);
		 });
		
		assertEquals("Entity class com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$16Entity has field uuid with wrong type java.lang.Float, should be class java.lang.String", exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}
	
	@Test
	public void testAnnotationEntityUuidTwice() {
		
		class Dto {
			
		}

		@GGAPIEntity(
				domain = "entity", 
				dto = "com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$1Dto"
		)
		class Entity {

			@GGAPIEntityUuid
			private String uuid;
			
			@GGAPIEntityUuid
			private String uuid2;
			
		}
		
		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityCheckerTest.checker.checkEntityClass(Entity.class);
		 });
		
		assertEquals("Entity class com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$17Entity has more than one field annotated with interface com.garganttua.api.core.entity.annotations.GGAPIEntityUuid", exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}
	
	@Test
	public void testAnnotationEntityUuidFromSuperClass() {
		
		class Dto {
			
		}

		@GGAPIEntity(
				domain = "entity", 
				dto = "com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$1Dto"
		)
		class Entity extends AbstractGGAPIEntity {

			private String uuid;
			
		}
		
		assertDoesNotThrow(() -> {
			GGAPIEntityCheckerTest.checker.checkEntityClass(Entity.class);
		});
	}
	
	@Test
	public void testAnnotationEntityUuid() {
		
		class Dto {
			
		}

		@GGAPIEntity(
				domain = "entity", 
				dto = "com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$1Dto"
		)
		class Entity extends AbstractGGAPIEntity {

			@GGAPIEntityUuid
			private String uuid;
			@GGAPIEntityId
			private String id;
			
		}
		
		assertDoesNotThrow(() -> {
			GGAPIEntityCheckerTest.checker.checkEntityClass(Entity.class);
		});
	}
	
	/**************************************************************************/
	@Test
	public void testAnnotationEntityIdIsPresentAndFieldHasStringType2() {
		
		class Dto {
			
		}

		@GGAPIEntity(
				domain = "entity", 
				dto = "com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$1Dto"
		)
		class Entity {

			@GGAPIEntityUuid
			private Float uuid;
			@GGAPIEntityId
			private String id;
			
		}
		
		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityCheckerTest.checker.checkEntityClass(Entity.class);
		 });
		
		assertEquals("Entity class com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$20Entity has field uuid with wrong type java.lang.Float, should be class java.lang.String", exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}
	
	@Test
	public void testAnnotationEntityIdFromSuperClass() {
		
		class Dto {
			
		}

		@GGAPIEntity(
				domain = "entity", 
				dto = "com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$1Dto"
		)
		class Entity extends AbstractGGAPIEntity {

			private String uuid;
			
		}
		
		assertDoesNotThrow(() -> {
			GGAPIEntityCheckerTest.checker.checkEntityClass(Entity.class);
		});
	}
	
	@Test
	public void testAnnotationEntityId() {
		
		class Dto {
			
		}

		@GGAPIEntity(
				domain = "entity", 
				dto = "com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$1Dto"
		)
		class Entity extends AbstractGGAPIEntity {

		}
		
		assertDoesNotThrow(() -> {
			GGAPIEntityCheckerTest.checker.checkEntityClass(Entity.class);
		});
	}
	
	
	
	@Test
	public void testAnnotationDeleteMethodProvider() {
		
		class Dto {
			
		}

		@GGAPIEntity(
				domain = "entity", 
				dto = "com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$1Dto"
		)
		class Entity {
			@GGAPIEntityUuid
			String uuid;
			
			@GGAPIEntityId
			String id;
			
			@GGAPIEntitySaveMethodProvider
			IGGAPIEntitySaveMethod saveProvider;	
		}
		
		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityCheckerTest.checker.checkEntityClass(Entity.class);
		 });
		
		assertEquals("Entity class com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$23Entity does not have any field annotated with @GGAPIEntityDeleteMethodProvider", exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}
	
	@Test
	public void testAnnotationDeleteMethodProviderNotGoodType() {
		
		class Dto {
			
		}

		@GGAPIEntity(
				domain = "entity", 
				dto = "com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$1Dto"
		)
		class Entity {
			@GGAPIEntityUuid
			String uuid;
			
			@GGAPIEntityId
			String id;
			
			@GGAPIEntitySaveMethodProvider
			IGGAPIEntitySaveMethod saveProvider;	
			
			@GGAPIEntityDeleteMethodProvider
			long deleteMethod;
		}
		
		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityCheckerTest.checker.checkEntityClass(Entity.class);
		 });
		
		assertEquals("Entity class com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$24Entity has field deleteMethod with wrong type long, should be interface com.garganttua.api.core.entity.interfaces.IGGAPIEntityDeleteMethod", exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}
	
	@Test
	public void testAnnotationSaveMethodProvider() {
		
		class Dto {
			
		}

		@GGAPIEntity(
				domain = "entity", 
				dto = "com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$1Dto"
		)
		class Entity {
			@GGAPIEntityUuid
			String uuid;
			
			@GGAPIEntityId
			String id;
			
			@GGAPIEntityDeleteMethodProvider
			IGGAPIEntityDeleteMethod saveProvider;	
		}
		
		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityCheckerTest.checker.checkEntityClass(Entity.class);
		 });
		
		assertEquals("Entity class com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$25Entity does not have any field annotated with @GGAPIEntitySaveMethodProvider", exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}
	
	@Test
	public void testAnnotationSaveMethodProviderNotGoodType() {
		
		class Dto {
			
		}

		@GGAPIEntity(
				domain = "entity", 
				dto = "com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$1Dto"
		)
		class Entity {
			@GGAPIEntityUuid
			String uuid;
			
			@GGAPIEntityId
			String id;
			
			@GGAPIEntityDeleteMethodProvider
			IGGAPIEntityDeleteMethod saveProvider;	
			
			@GGAPIEntitySaveMethodProvider
			long deleteMethod;
		}
		
		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityCheckerTest.checker.checkEntityClass(Entity.class);
		 });
		
		assertEquals("Entity class com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$26Entity has field deleteMethod with wrong type long, should be interface com.garganttua.api.core.entity.interfaces.IGGAPIEntitySaveMethod", exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}
	
	
	
	@Test
	public void testAnnotationSaveMethod() {
		
		class Dto {
			
		}

		@GGAPIEntity(
				domain = "entity", 
				dto = "com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$1Dto"
		)
		class Entity {
			@GGAPIEntityUuid
			String uuid;
			
			@GGAPIEntityId
			String id;
			
			@GGAPIEntityDeleteMethodProvider
			IGGAPIEntityDeleteMethod deleteMethod;	
			
			@GGAPIEntitySaveMethodProvider
			IGGAPIEntitySaveMethod saveMethod;

			@GGAPIEntityRepository
			private IGGAPIRepository repository;
			
			public void save(IGGAPICaller caller, Map<String, String> parameters, Optional<IGGAPISecurity> security) throws GGAPIEntityException, GGAPIEngineException {
			}

			@GGAPIEntityDeleteMethod
			public void delete(IGGAPICaller caller, Map<String, String> parameters) throws GGAPIEntityException, GGAPIEngineException {
			}
		}
		
		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityCheckerTest.checker.checkEntityClass(Entity.class);
		 });
		
		assertEquals("Entity class java.lang.Object does not have method annotated with @GGAPIEntitySaveMethod", exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}
	
	@Test
	public void testAnnotationSaveMethodWrongParam1() {
		
		class Dto {
			
		}

		@GGAPIEntity(
				domain = "entity", 
				dto = "com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$1Dto"
		)
		class Entity {
			@GGAPIEntityUuid
			String uuid;
			
			@GGAPIEntityId
			String id;
			
			@GGAPIEntityDeleteMethodProvider
			IGGAPIEntityDeleteMethod deleteMethod;	
			
			@GGAPIEntitySaveMethodProvider
			IGGAPIEntitySaveMethod saveMethod;

			@GGAPIEntityRepository
			private IGGAPIRepository repository;
			
			@GGAPIEntitySaveMethod
			public void save(long caller, Map<String, String> parameters, Optional<IGGAPISecurity> security) throws GGAPIEntityException, GGAPIEngineException {
			}

			@GGAPIEntityDeleteMethod
			public void delete(IGGAPICaller caller, Map<String, String> parameters) throws GGAPIEntityException, GGAPIEngineException {
			}
		}
		
		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityCheckerTest.checker.checkEntityClass(Entity.class);
		 });
		
		assertEquals("Entity class com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$28Entity has method save but parameter 0 is not of type interface com.garganttua.api.core.IGGAPICaller", exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}
	
	@Test
	public void testAnnotationSaveMethodWrongParam2() {
		
		class Dto {
			
		}

		@GGAPIEntity(
				domain = "entity", 
				dto = "com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$1Dto"
		)
		class Entity {
			@GGAPIEntityUuid
			String uuid;
			
			@GGAPIEntityId
			String id;
			
			@GGAPIEntityDeleteMethodProvider
			IGGAPIEntityDeleteMethod deleteMethod;	
			
			@GGAPIEntitySaveMethodProvider
			IGGAPIEntitySaveMethod saveMethod;

			@GGAPIEntityRepository
			private IGGAPIRepository repository;
			
			@GGAPIEntitySaveMethod
			public void save(IGGAPICaller caller, long parameters, Optional<IGGAPISecurity> security) throws GGAPIEntityException, GGAPIEngineException {
			}

			@GGAPIEntityDeleteMethod
			public void delete(IGGAPICaller caller, Map<String, String> parameters) throws GGAPIEntityException, GGAPIEngineException {
			}
		}
		
		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityCheckerTest.checker.checkEntityClass(Entity.class);
		 });
		
		assertEquals("Entity class com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$29Entity has method save but parameter 1 is not of type java.lang.Map<java.lang.String,java.lang.String>", exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}
	
	@Test
	public void testAnnotationSaveMethodWrongParam3() {
		
		class Dto {
			
		}

		@GGAPIEntity(
				domain = "entity", 
				dto = "com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$1Dto"
		)
		class Entity {
			@GGAPIEntityUuid
			String uuid;
			
			@GGAPIEntityId
			String id;
			
			@GGAPIEntityDeleteMethodProvider
			IGGAPIEntityDeleteMethod deleteMethod;	
			
			@GGAPIEntitySaveMethodProvider
			IGGAPIEntitySaveMethod saveMethod;

			@GGAPIEntityRepository
			private IGGAPIRepository repository;
			
			@GGAPIEntitySaveMethod
			public void save(IGGAPICaller caller, Map<String, String>  parameters, long security) throws GGAPIEntityException, GGAPIEngineException {
			}

			@GGAPIEntityDeleteMethod
			public void delete(IGGAPICaller caller, Map<String, String> parameters) throws GGAPIEntityException, GGAPIEngineException {
			}
		}
		
		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityCheckerTest.checker.checkEntityClass(Entity.class);
		 });
		
		assertEquals("Entity class com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$30Entity has method save but parameter 2 is not of type Optional<IGGAPISecurity>", exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}
	
	@Test
	public void testAnnotationDeleteMethod() {
		
		class Dto {
			
		}

		@GGAPIEntity(
				domain = "entity", 
				dto = "com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$1Dto"
		)
		class Entity {
			@GGAPIEntityUuid
			String uuid;
			
			@GGAPIEntityId
			String id;
			
			@GGAPIEntityDeleteMethodProvider
			IGGAPIEntityDeleteMethod deleteMethod;	
			
			@GGAPIEntitySaveMethodProvider
			IGGAPIEntitySaveMethod saveMethod;

			@GGAPIEntityRepository
			private IGGAPIRepository repository;
			
			@GGAPIEntitySaveMethod
			public void save(IGGAPICaller caller, Map<String, String> parameters, Optional<IGGAPISecurity> security) throws GGAPIEntityException, GGAPIEngineException {
			}

			public void delete(IGGAPICaller caller, Map<String, String> parameters) throws GGAPIEntityException, GGAPIEngineException {
			}
		}
		
		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityCheckerTest.checker.checkEntityClass(Entity.class);
		 });
		
		assertEquals("Entity class java.lang.Object does not have method annotated with @GGAPIEntitySaveMethod", exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}
	
	@Test
	public void testAnnotationDeleteMethodWrongParam1() {
		
		class Dto {
			
		}

		@GGAPIEntity(
				domain = "entity", 
				dto = "com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$1Dto"
		)
		class Entity {
			@GGAPIEntityUuid
			String uuid;
			
			@GGAPIEntityId
			String id;
			
			@GGAPIEntityDeleteMethodProvider
			IGGAPIEntityDeleteMethod deleteMethod;	
			
			@GGAPIEntitySaveMethodProvider
			IGGAPIEntitySaveMethod saveMethod;

			@GGAPIEntityRepository
			private IGGAPIRepository repository;
			
			@GGAPIEntitySaveMethod
			public void save(IGGAPICaller caller, Map<String, String> parameters, Optional<IGGAPISecurity> security) throws GGAPIEntityException, GGAPIEngineException {
			}

			@GGAPIEntityDeleteMethod
			public void delete(long caller, Map<String, String> parameters) throws GGAPIEntityException, GGAPIEngineException {
			}
		}
		
		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityCheckerTest.checker.checkEntityClass(Entity.class);
		 });
		
		assertEquals("Entity class com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$32Entity has method delete but parameter 0 is not of type interface com.garganttua.api.core.IGGAPICaller", exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}
	
	@Test
	public void testAnnotationDeleteMethodWrongParam2() {
		
		class Dto {
			
		}

		@GGAPIEntity(
				domain = "entity", 
				dto = "com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$1Dto"
		)
		class Entity {
			
			@GGAPIEntityUuid
			String uuid;
			
			@GGAPIEntityId
			String id;
			
			@GGAPIEntityDeleteMethodProvider
			IGGAPIEntityDeleteMethod deleteMethod;	
			
			@GGAPIEntitySaveMethodProvider
			IGGAPIEntitySaveMethod saveMethod;

			@GGAPIEntityRepository
			private IGGAPIRepository repository;
			
			@GGAPIEntitySaveMethod
			public void save(IGGAPICaller caller, Map<String, String> parameters, Optional<IGGAPISecurity> security) throws GGAPIEntityException, GGAPIEngineException {
			}

			@GGAPIEntityDeleteMethod
			public void delete(IGGAPICaller caller, long parameters) throws GGAPIEntityException, GGAPIEngineException {
			}
		}
		
		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityCheckerTest.checker.checkEntityClass(Entity.class);
		 });
		
		assertEquals("Entity class com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$33Entity has method delete but parameter 1 is not of type java.lang.Map<java.lang.String,java.lang.String>", exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}
	
	
	@Test
	public void testAnnotationHiddenableFieldOrAnnotationisPresent() {
		
		class Dto {
			
		}

		@GGAPIEntity(
				domain = "entity", 
				dto = "com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$1Dto"
		)
		@GGAPIEntityHiddenable
		class Entity extends AbstractGGAPIEntity {
			

		}
		
		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityCheckerTest.checker.checkEntityClass(Entity.class);
		 });
		
		assertEquals("Entity class com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$34Entity does not have any field value or field annotated with com.garganttua.api.core.entity.annotations.GGAPIEntityHidden", exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}
	
	@Test
	public void testAnnotationHiddenableFieldnotPresent() {
		
		class Dto {
			
		}

		@GGAPIEntity(
				domain = "entity", 
				dto = "com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$1Dto"
		)
		@GGAPIEntityHiddenable(hidden = "hidden")
		class Entity extends AbstractGGAPIEntity {
			

		}
		
		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityCheckerTest.checker.checkEntityClass(Entity.class);
		 });
		
		assertEquals("Entity class com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$35Entity does not have field hidden", exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}
	
	@Test
	public void testAnnotationHiddenableFieldPresentButNotGoodType() {
		
		class Dto {
			
		}

		@GGAPIEntity(
				domain = "entity", 
				dto = "com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$1Dto"
		)
		@GGAPIEntityHiddenable(hidden = "hidden")
		class Entity extends AbstractGGAPIEntity {
			
			@GGAPIEntityHidden
			String hidden;
		}
		
		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityCheckerTest.checker.checkEntityClass(Entity.class);
		 });
		
		assertEquals("Entity class com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$36Entity has field hidden with wrong type java.lang.String, should be boolean", exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}
	
	@Test
	public void testAnnotationHiddenable() {
		
		class Dto {
			
		}

		@GGAPIEntity(
				domain = "entity", 
				dto = "com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$1Dto"
		)
		@GGAPIEntityHiddenable(hidden = "hidden")
		class Entity extends AbstractGGAPIEntity {
			
			boolean hidden;
		}
		assertDoesNotThrow(() -> {
			GGAPIEntityCheckerTest.checker.checkEntityClass(Entity.class);
		});
	}
	
	@Test
	public void testAnnotationGeolocalizedFieldOrAnnotationisPresent() {
		
		class Dto {
			
		}

		@GGAPIEntity(
				domain = "entity", 
				dto = "com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$1Dto"
		)
		@GGAPIEntityGeolocalized
		class Entity extends AbstractGGAPIEntity {
			

		}
		
		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityCheckerTest.checker.checkEntityClass(Entity.class);
		 });
		
		assertEquals("Entity class com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$38Entity does not have any field value or field annotated with com.garganttua.api.core.entity.annotations.GGAPIEntityLocation", exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}
	
	@Test
	public void testAnnotationGeolocalizedFieldnotPresent() {
		
		class Dto {
			
		}

		@GGAPIEntity(
				domain = "entity", 
				dto = "com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$1Dto"
		)
		@GGAPIEntityGeolocalized(location = "location")
		class Entity extends AbstractGGAPIEntity {
			

		}
		
		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityCheckerTest.checker.checkEntityClass(Entity.class);
		 });
		
		assertEquals("Entity class com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$39Entity does not have field location", exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}
	
	@Test
	public void testAnnotationGeolocalizedFieldPresentButNotGoodType() {
		
		class Dto {
			
		}

		@GGAPIEntity(
				domain = "entity", 
				dto = "com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$1Dto"
		)
		@GGAPIEntityGeolocalized(location = "location")
		class Entity extends AbstractGGAPIEntity {
			
			@GGAPIEntityLocation
			String location;
		}
		
		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityCheckerTest.checker.checkEntityClass(Entity.class);
		 });
		
		assertEquals("Entity class com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$40Entity has field location with wrong type java.lang.String, should be class org.geojson.Point", exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}
	
	@Test
	public void testAnnotationLocationTwice() {
		
		class Dto {
			
		}

		@GGAPIEntity(
				domain = "entity", 
				dto = "com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$1Dto"
		)
		@GGAPIEntityGeolocalized(location = "location")
		class Entity extends AbstractGGAPIEntity {
			
			@GGAPIEntityLocation
			Point location;
			
			@GGAPIEntityLocation
			Point location2;
		}
		
		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityCheckerTest.checker.checkEntityClass(Entity.class);
		 });
		
		assertEquals("Entity class com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$41Entity has more than one field annotated with interface com.garganttua.api.core.entity.annotations.GGAPIEntityLocation", exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}
	
	@Test
	public void testAnnotationGeolocalized() {
		
		class Dto {
			
		}

		@GGAPIEntity(
				domain = "entity", 
				dto = "com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$1Dto"
		)
		@GGAPIEntityGeolocalized(location = "location")
		class Entity extends AbstractGGAPIEntity {
			
			Point location;
		}
		assertDoesNotThrow(() -> {
			GGAPIEntityCheckerTest.checker.checkEntityClass(Entity.class);
		});
	}
	
	@Test
	public void testAnnotationEntityIdTwice() {
		
		class Dto {
			
		}

		@GGAPIEntity(
				domain = "entity", 
				dto = "com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$1Dto"
		)
		class Entity extends AbstractGGAPIEntity {

			@GGAPIEntityUuid
			private String uuid;
			
			@GGAPIEntityId
			private String uuid2;
			
			@GGAPIEntityId
			private String uuid3;
			
			@GGAPIEntitySaveMethodProvider
			protected IGGAPIEntitySaveMethod saveMethod;

			@GGAPIEntityDeleteMethodProvider
			protected IGGAPIEntityDeleteMethod deleteMethod;
			
			@GGAPIEntityRepository
			protected IGGAPIRepository repository;
			
			@GGAPIEntitySaveMethod
			public void save(IGGAPICaller caller, Map<String, String> parameters, Optional<IGGAPISecurity> security) throws GGAPIEntityException, GGAPIEngineException {
			}

			@GGAPIEntityDeleteMethod
			public void delete(IGGAPICaller caller, Map<String, String> parameters) throws GGAPIEntityException, GGAPIEngineException {
			}

			
		}
		
		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityCheckerTest.checker.checkEntityClass(Entity.class);
		 });
		
		assertEquals("Entity class com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$43Entity has more than one field annotated with interface com.garganttua.api.core.entity.annotations.GGAPIEntityId", exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}
	
	@Test
	public void testAnnotationEntitySAveMethodProviderTwice() {
		
		class Dto {
			
		}

		@GGAPIEntity(
				domain = "entity", 
				dto = "com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$1Dto"
		)
		class Entity extends AbstractGGAPIEntity {

			@GGAPIEntityUuid
			private String uuid;
			
			@GGAPIEntityId
			private String uuid2;
			
			@GGAPIEntitySaveMethodProvider
			protected IGGAPIEntitySaveMethod saveMethod;
			
			@GGAPIEntitySaveMethodProvider
			protected IGGAPIEntitySaveMethod saveMethod2;

			@GGAPIEntityDeleteMethodProvider
			protected IGGAPIEntityDeleteMethod deleteMethod;
			
			@GGAPIEntityRepository
			protected IGGAPIRepository repository;
			
			@GGAPIEntitySaveMethod
			public void save(IGGAPICaller caller, Map<String, String> parameters, Optional<IGGAPISecurity> security) throws GGAPIEntityException, GGAPIEngineException {
			}

			@GGAPIEntityDeleteMethod
			public void delete(IGGAPICaller caller, Map<String, String> parameters) throws GGAPIEntityException, GGAPIEngineException {
			}

			
		}
		
		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityCheckerTest.checker.checkEntityClass(Entity.class);
		 });
		
		assertEquals("Entity class com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$44Entity has more than one field annotated with interface com.garganttua.api.core.entity.annotations.GGAPIEntitySaveMethodProvider", exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}
	
	@Test
	public void testAnnotationEntityDeleteMethodProviderTwice() {
		
		class Dto {
			
		}

		@GGAPIEntity(
				domain = "entity", 
				dto = "com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$1Dto"
		)
		class Entity extends AbstractGGAPIEntity {

			@GGAPIEntityUuid
			private String uuid;
			
			@GGAPIEntityId
			private String uuid2;
			
			@GGAPIEntitySaveMethodProvider
			protected IGGAPIEntitySaveMethod saveMethod;

			@GGAPIEntityDeleteMethodProvider
			protected IGGAPIEntityDeleteMethod deleteMethod;
			
			@GGAPIEntityDeleteMethodProvider
			protected IGGAPIEntityDeleteMethod deleteMethod2;
			
			@GGAPIEntityRepository
			protected IGGAPIRepository repository;
			
			@GGAPIEntitySaveMethod
			public void save(IGGAPICaller caller, Map<String, String> parameters, Optional<IGGAPISecurity> security) throws GGAPIEntityException, GGAPIEngineException {
			}

			@GGAPIEntityDeleteMethod
			public void delete(IGGAPICaller caller, Map<String, String> parameters) throws GGAPIEntityException, GGAPIEngineException {
			}
			
		}
		
		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityCheckerTest.checker.checkEntityClass(Entity.class);
		 });
		
		assertEquals("Entity class com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$45Entity has more than one field annotated with interface com.garganttua.api.core.entity.annotations.GGAPIEntityDeleteMethodProvider", exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}
	
	@Test
	public void testRepositoryAnnotationNotPresent() {
		
		class Dto {
			
		}

		@GGAPIEntity(
				domain = "entity", 
				dto = "com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$1Dto"
		)
		class Entity {
			
			@JsonProperty
			@GGAPIEntityUuid
			protected String uuid;
			
			@JsonProperty
			@GGAPIEntityId
			protected String id;
			
			@JsonIgnore
			private boolean gotFromRepository;

			@JsonIgnore
			@GGAPIEntitySaveMethodProvider
			transient protected IGGAPIEntitySaveMethod saveMethod;

			@JsonIgnore
			@GGAPIEntityDeleteMethodProvider
			transient protected IGGAPIEntityDeleteMethod deleteMethod;
			
			@GGAPIEntitySaveMethod
			public void save(IGGAPICaller caller, Map<String, String> parameters, Optional<IGGAPISecurity> security) throws GGAPIEntityException, GGAPIEngineException {
			}

			@GGAPIEntityDeleteMethod
			public void delete(IGGAPICaller caller, Map<String, String> parameters) throws GGAPIEntityException, GGAPIEngineException {
			}
			
//			@GGAPIEntityRepository
			String location;
		}
		
		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityCheckerTest.checker.checkEntityClass(Entity.class);
		 });
		
		assertEquals("Entity class com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$46Entity does not have any field annotated with @GGAPIEntityRepository", exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}
	
	@Test
	public void testAnnotationEntityRepositoryTwice() {
		
		class Dto {
			
		}

		@GGAPIEntity(
				domain = "entity", 
				dto = "com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$1Dto"
		)
		class Entity {

			@GGAPIEntityUuid
			private String uuid;
			
			@GGAPIEntityId
			private String uuid2;
			
			@GGAPIEntitySaveMethodProvider
			protected IGGAPIEntitySaveMethod saveMethod;

			@GGAPIEntityDeleteMethodProvider
			protected IGGAPIEntityDeleteMethod deleteMethod;
			
			@GGAPIEntityRepository
			protected IGGAPIRepository repository;
			
			@GGAPIEntityRepository
			protected IGGAPIRepository repository2;
			
			@GGAPIEntitySaveMethod
			public void save(IGGAPICaller caller, Map<String, String> parameters, Optional<IGGAPISecurity> security) throws GGAPIEntityException, GGAPIEngineException {
			}

			@GGAPIEntityDeleteMethod
			public void delete(IGGAPICaller caller, Map<String, String> parameters) throws GGAPIEntityException, GGAPIEngineException {
			}
			
		}
		
		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityCheckerTest.checker.checkEntityClass(Entity.class);
		 });
		
		assertEquals("Entity class com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$47Entity has more than one field annotated with interface com.garganttua.api.core.entity.annotations.GGAPIEntityRepository", exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}
	
	@Test
	public void testAnnotationEntitySaveMethodTwice() {
		
		class Dto {
			
		}

		@GGAPIEntity(
				domain = "entity", 
				dto = "com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$1Dto"
		)
		class Entity {

			@GGAPIEntityUuid
			private String uuid;
			
			@GGAPIEntityId
			private String uuid2;
			
			@GGAPIEntitySaveMethodProvider
			protected IGGAPIEntitySaveMethod saveMethod;

			@GGAPIEntityDeleteMethodProvider
			protected IGGAPIEntityDeleteMethod deleteMethod;
			
			@GGAPIEntityRepository
			protected IGGAPIRepository repository;

			
			@GGAPIEntitySaveMethod
			public void save(IGGAPICaller caller, Map<String, String> parameters, Optional<IGGAPISecurity> security) throws GGAPIEntityException, GGAPIEngineException {
			}
			
			@GGAPIEntitySaveMethod
			public void save2(IGGAPICaller caller, Map<String, String> parameters, Optional<IGGAPISecurity> security) throws GGAPIEntityException, GGAPIEngineException {
			}

			@GGAPIEntityDeleteMethod
			public void delete(IGGAPICaller caller, Map<String, String> parameters) throws GGAPIEntityException, GGAPIEngineException {
			}
			
		}
		
		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityCheckerTest.checker.checkEntityClass(Entity.class);
		 });
		
		assertEquals("Entity class com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$48Entity has more than one method annotated with interface com.garganttua.api.core.entity.annotations.GGAPIEntitySaveMethod", exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}
	
	@Test
	public void testAnnotationEntityDeleteMethodTwice() {
		
		class Dto {
			
		}

		@GGAPIEntity(
				domain = "entity", 
				dto = "com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$1Dto"
		)
		class Entity {

			@GGAPIEntityUuid
			private String uuid;
			
			@GGAPIEntityId
			private String uuid2;
			
			@GGAPIEntitySaveMethodProvider
			protected IGGAPIEntitySaveMethod saveMethod;

			@GGAPIEntityDeleteMethodProvider
			protected IGGAPIEntityDeleteMethod deleteMethod;
			
			@GGAPIEntityRepository
			protected IGGAPIRepository repository;

			@GGAPIEntitySaveMethod
			public void save(IGGAPICaller caller, Map<String, String> parameters, Optional<IGGAPISecurity> security) throws GGAPIEntityException, GGAPIEngineException {
			}

			@GGAPIEntityDeleteMethod
			public void delete(IGGAPICaller caller, Map<String, String> parameters) throws GGAPIEntityException, GGAPIEngineException {
			}
			
			@GGAPIEntityDeleteMethod
			public void delete2(IGGAPICaller caller, Map<String, String> parameters) throws GGAPIEntityException, GGAPIEngineException {
			}
			
		}
		
		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityCheckerTest.checker.checkEntityClass(Entity.class);
		 });
		
		assertEquals("Entity class com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$49Entity has more than one method annotated with interface com.garganttua.api.core.entity.annotations.GGAPIEntityDeleteMethod", exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}
	
	@Test
	public void testAnnotationOwnedAndOwner() {
		
		class Dto {
			
		}

		@GGAPIEntity(
				domain = "entity", 
				dto = "com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$1Dto"
		)
		@GGAPIEntityOwner
		@GGAPIEntityOwned
		class Entity extends AbstractGGAPIEntity{
			
		}
		
		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityCheckerTest.checker.checkEntityClass(Entity.class);
		 });
		
		assertEquals("Entity class com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$50Entity Cannot be owner and owned at the same time", exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}
		
	@Test
	public void testOwnedEntityWithNoAnnotatedField() {
		
		class Dto {
			
		}

		@GGAPIEntity(
				domain = "entity", 
				dto = "com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$1Dto"
		)
		@GGAPIEntityOwned
		class Entity  extends AbstractGGAPIEntity{

		}
		
		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityCheckerTest.checker.checkEntityClass(Entity.class);
		 });
		
		assertEquals("Entity class com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$51Entity does not have any field value or field annotated with com.garganttua.api.core.entity.annotations.GGAPIEntityOwnerId", exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}
	
	@Test
	public void testOwnedEntityWithAnnotatedField() {
		
		class Dto {
			
		}

		@GGAPIEntity(
				domain = "entity", 
				dto = "com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$1Dto"
		)
		@GGAPIEntityOwned(ownerId = "uuid")
		class Entity  extends AbstractGGAPIEntity {
			

			
		}
		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityCheckerTest.checker.checkEntityClass(Entity.class);
		 });
		
		assertEquals("Entity class com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$52Entity does not have field uuid", exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}
	
	@Test
	public void testOwnedEntityWithFieldValue() {
		
		class Dto {
			
		}

		@GGAPIEntity(
				domain = "entity", 
				dto = "com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$1Dto"
		)
		@GGAPIEntityOwned(ownerId = "uuid")
		class Entity  extends AbstractGGAPIEntity {
			
			private String uuid;
		}
		
		assertDoesNotThrow(() -> {
			GGAPIEntityCheckerTest.checker.checkEntityClass(Entity.class);
		});
	}
	
	@Test
	public void testOwnedEntityWithFieldAnnotatedOfWrongType() {
		
		class Dto {
			
		}

		@GGAPIEntity(
				domain = "entity", 
				dto = "com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$1Dto"
		)
		@GGAPIEntityOwner
		class Entity  extends AbstractGGAPIEntity{

			@GGAPIEntityOwnerId
			private Float uuid;

			
		}
		
		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityCheckerTest.checker.checkEntityClass(Entity.class);
		 });
		
		assertEquals("Entity class com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$54Entity has field uuid with wrong type java.lang.Float, should be class java.lang.String", exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());

	}
	
	@Test
	public void testOwnedEntityWithFieldValueOfWrongType() {
		
		class Dto {
			
		}

		@GGAPIEntity(
				domain = "entity", 
				dto = "com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$1Dto"
		)
		@GGAPIEntityOwner(ownerId = "uuid")
		class Entity  extends AbstractGGAPIEntity{
			
			private Float uuid;
			
		}
		
		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityCheckerTest.checker.checkEntityClass(Entity.class);
		 });
		
		assertEquals("Entity class com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$55Entity has field uuid with wrong type class java.lang.Float, should be class java.lang.String", exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}
	
	/********************************************************************************/
	
	@Test
	public void testSharedEntityWithNoAnnotatedField() {
		
		class Dto {
			
		}

		@GGAPIEntity(
				domain = "entity", 
				dto = "com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$1Dto"
		)
		@GGAPIEntityShared
		class Entity  extends AbstractGGAPIEntity{

		}
		
		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityCheckerTest.checker.checkEntityClass(Entity.class);
		 });
		
		assertEquals("Entity class com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$56Entity does not have any field value or field annotated with com.garganttua.api.core.entity.annotations.GGAPIEntityShare", exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}
	
	@Test
	public void testSharedEntityWithAnnotatedField() {
		
		class Dto {
			
		}

		@GGAPIEntity(
				domain = "entity", 
				dto = "com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$1Dto"
		)
		@GGAPIEntityShared(share = "uuid")
		class Entity  extends AbstractGGAPIEntity {
			

			
		}
		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityCheckerTest.checker.checkEntityClass(Entity.class);
		 });
		
		assertEquals("Entity class com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$57Entity does not have field uuid", exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}
	
	@Test
	public void testSharedEntityWithFieldValue() {
		
		class Dto {
			
		}

		@GGAPIEntity(
				domain = "entity", 
				dto = "com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$1Dto"
		)
		@GGAPIEntityShared(share = "uuid")
		class Entity  extends AbstractGGAPIEntity {
			
			private String uuid;
		}
		
		assertDoesNotThrow(() -> {
			GGAPIEntityCheckerTest.checker.checkEntityClass(Entity.class);
		});
	}
	
	@Test
	public void testSharedEntityWithFieldAnnotatedOfWrongType() {
		
		class Dto {
			
		}

		@GGAPIEntity(
				domain = "entity", 
				dto = "com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$1Dto"
		)
		@GGAPIEntityShared
		class Entity  extends AbstractGGAPIEntity{

			@GGAPIEntityShare
			private Float uuid;

			
		}
		
		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityCheckerTest.checker.checkEntityClass(Entity.class);
		 });
		
		assertEquals("Entity class com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$59Entity has field uuid with wrong type java.lang.Float, should be class java.lang.String", exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());

	}
	
	@Test
	public void testSharedEntityWithFieldValueOfWrongType() {
		
		class Dto {
			
		}

		@GGAPIEntity(
				domain = "entity", 
				dto = "com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$1Dto"
		)
		@GGAPIEntityShared(share = "uuid")
		class Entity  extends AbstractGGAPIEntity{
			
			private Float uuid;
			
		}
		
		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityCheckerTest.checker.checkEntityClass(Entity.class);
		 });
		
		assertEquals("Entity class com.garganttua.api.core.entity.checker.GGAPIEntityCheckerTest$60Entity has field uuid with wrong type class java.lang.Float, should be class java.lang.String", exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}
}
