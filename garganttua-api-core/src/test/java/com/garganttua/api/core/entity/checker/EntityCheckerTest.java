package com.garganttua.api.core.entity.checker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.garganttua.api.core.ReflectionsAnnotationScanner;
import com.garganttua.api.core.entity.exceptions.EntityException;
import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.CoreExceptionCode;
import com.garganttua.reflection.utils.GGObjectReflectionHelper;

public class EntityCheckerTest {
	@BeforeAll
	public static void setupAnnotationScanner() {
		GGObjectReflectionHelper.annotationScanner = new ReflectionsAnnotationScanner();
	}
	
	@Test
	public void testGeolocEntity() throws CoreException {
		
		EntityException exception = assertThrows(EntityException.class, () -> {
			EntityChecker.checkEntityClass(GeolocEntity.class); 
		});
		assertEquals("Entity GeolocEntity has field test with wrong type class java.lang.String, should be class org.geojson.Point",
				exception.getMessage());
		assertEquals(CoreExceptionCode.BUILDER_CODE, exception.getCode());
	}
	
	
	
//
//	@Test
//	public void testEntityWithNoDomain() {
//
//		@Entity(domain = "")
//		class Entity {
//			@EntityUuid
//			private String tuuid;
//		}
//
//		EntityException exception = assertThrows(EntityException.class, () -> {
//			EntityChecker.checkEntityClass(Entity.class);
//		});
//
//		assertEquals("No domain provided in annotation of entity " + Entity.class.getSimpleName(),
//				exception.getMessage());
//		assertEquals(EntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
//	}
//
//	@Test
//	public void testTenantEntityWithNoAnnotatedField() {
//
//		@Entity(domain = "entity")
//		@EntityTenant
//		class Entity extends GenericEntity {
//			@EntityUuid
//			private String tuuid;
//			@EntityId
//			private String id;
//		}
//
//		EntityException exception = assertThrows(EntityException.class, () -> {
//			EntityChecker.checkEntityClass(Entity.class);
//		});
//
//		assertEquals(
//				"Entity Entity does not have any field value or field annotated with com.garganttua.api.core.entity.annotations.EntityTenantId",
//				exception.getMessage());
//		assertEquals(EntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
//	}
//
//	@Test
//	public void testTenantEntityWithAnnotatedField() {
//		assertDoesNotThrow(() -> {
//			EntityChecker.checkEntityClass(TestTenantEntityWithAnnotatedField.class);
//		});
//	}
//
//	@Test
//	public void testTenantEntityWithFieldValue() {
//		assertDoesNotThrow(() -> {
//			EntityChecker.checkEntityClass(TestTenantEntityWithFieldValue.class);
//		});
//	}
//
//	@Test
//	public void testTenantEntityWithFieldAnnotatedOfWrongType() {
//		@Entity(domain = "entity")
//		@EntityTenant()
//		class Entity extends GenericEntity {
//
//			@EntityTenantId
//			private Float uuid;
//			@EntityUuid
//			private String tuuid;
//			@EntityId
//			private String id;
//
//		}
//
//		EntityException exception = assertThrows(EntityException.class, () -> {
//			EntityChecker.checkEntityClass(Entity.class);
//		});
//
//		assertEquals(
//				"Entity " + Entity.class.getSimpleName()
//						+ " has field uuid with wrong type java.lang.Float, should be class java.lang.String",
//				exception.getMessage());
//		assertEquals(EntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
//
//	}
//
//	@Test
//	public void testTenantEntityWithFieldValueOfWrongType() {
//
//		class Dto {
//
//		}
//
//		@Entity(domain = "entity")
//		@EntityTenant(tenantId = "uuid")
//		class Entity extends GenericEntity {
//
//			private Float uuid;
//			@EntityUuid
//			private String tuuid;
//			@EntityId
//			private String id;
//
//		}
//
//		EntityException exception = assertThrows(EntityException.class, () -> {
//			EntityChecker.checkEntityClass(Entity.class);
//		});
//
//		assertEquals(
//				"Entity Entity has field uuid with wrong type class java.lang.Float, should be class java.lang.String",
//				exception.getMessage());
//		assertEquals(EntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
//	}
//
//	/***********************************************************************************/
//
//	@Test
//	public void testOwnerEntityWithNoAnnotatedField() {
//
//		class Dto {
//
//		}
//
//		@Entity(domain = "entity")
//		@EntityOwner
//		class Entity extends GenericEntity {
//			@EntityUuid
//			private String tuuid;
//			@EntityId
//			private String id;
//		}
//
//		EntityException exception = assertThrows(EntityException.class, () -> {
//			EntityChecker.checkEntityClass(Entity.class);
//		});
//
//		assertEquals(
//				"Entity Entity does not have any field value or field annotated with com.garganttua.api.core.entity.annotations.EntityOwnerId",
//				exception.getMessage());
//		assertEquals(EntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
//	}
//
//	@Test
//	public void testOwnerEntityWithAnnotatedField() {
//		assertDoesNotThrow(() -> {
//			EntityChecker.checkEntityClass(TestOwnerEntityWithAnnotatedField.class);
//		});
//	}
//
//	@Test
//	public void testOwnerEntityWithFieldValue() {
//		assertDoesNotThrow(() -> {
//			EntityChecker.checkEntityClass(TestOwnerEntityWithFieldValue.class);
//		});
//	}
//
//	@Test
//	public void testOwnerEntityWithFieldAnnotatedOfWrongType() {
//		@Entity(domain = "entity")
//		@EntityOwner()
//		class Entity extends GenericEntity {
//
//			@EntityTenantId
//			private Float uuid;
//			@EntityUuid
//			private String tuuid;
//			@EntityId
//			private String id;
//
//		}
//
//		EntityException exception = assertThrows(EntityException.class, () -> {
//			EntityChecker.checkEntityClass(Entity.class);
//		});
//
//		assertEquals(
//				"Entity Entity does not have any field value or field annotated with com.garganttua.api.core.entity.annotations.EntityOwnerId",
//				exception.getMessage());
//		assertEquals(EntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
//
//	}
//
//	@Test
//	public void testOwnerEntityWithFieldValueOfWrongType() {
//
//		class Dto {
//
//		}
//
//		@Entity(domain = "entity")
//		@EntityOwner(ownerId = "uuid")
//		class Entity extends GenericEntity {
//
//			private Float uuid;
//			@EntityUuid
//			private String tuuid;
//			@EntityId
//			private String id;
//
//		}
//
//		EntityException exception = assertThrows(EntityException.class, () -> {
//			EntityChecker.checkEntityClass(Entity.class);
//		});
//
//		assertEquals(
//				"Entity Entity has field uuid with wrong type class java.lang.Float, should be class java.lang.String",
//				exception.getMessage());
//		assertEquals(EntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
//	}
//
//	/*****************************************************************************************/
//
//	@Test
//	public void testAnnotationEntityUuidIsPresentAndFieldHasStringType() {
//		@Entity(domain = "entity")
//		class Entity {
//
//			private String uuid;
//
//		}
//
//		EntityException exception = assertThrows(EntityException.class, () -> {
//			EntityChecker.checkEntityClass(Entity.class);
//		});
//
//		assertEquals("Entity Entity does not have any field annotated with @EntityUuid", exception.getMessage());
//		assertEquals(EntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
//	}
//
//	@Test
//	public void testAnnotationEntityUuidIsPresentAndFieldHasStringType2() {
//		@Entity(domain = "entity")
//		class Entity {
//
//			@EntityUuid
//			private Float uuid;
//
//		}
//
//		EntityException exception = assertThrows(EntityException.class, () -> {
//			EntityChecker.checkEntityClass(Entity.class);
//		});
//
//		assertEquals("Entity Entity has field uuid with wrong type java.lang.Float, should be class java.lang.String",
//				exception.getMessage());
//		assertEquals(EntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
//	}
//
//	@Test
//	public void testAnnotationEntityUuidTwice() {
//		@Entity(domain = "entity")
//		class Entity {
//
//			@EntityUuid
//			private String uuid;
//
//			@EntityUuid
//			private String uuid2;
//
//		}
//
//		EntityException exception = assertThrows(EntityException.class, () -> {
//			EntityChecker.checkEntityClass(Entity.class);
//		});
//
//		assertEquals(
//				"Entity Entity has more than one field annotated with interface com.garganttua.api.core.entity.annotations.EntityUuid",
//				exception.getMessage());
//		assertEquals(EntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
//	}
//
//	@Test
//	public void testAnnotationEntityUuidFromSuperClass() {
//		assertDoesNotThrow(() -> {
//			EntityChecker.checkEntityClass(TestAnnotationEntityUuidFromSuperClass.class);
//		});
//	}
//
//	@Test
//	public void testAnnotationEntityUuid() {
//		assertDoesNotThrow(() -> {
//			EntityChecker.checkEntityClass(TestAnnotationEntityUuid.class);
//		});
//	}
//
//	/**************************************************************************/
//	@Test
//	public void testAnnotationEntityIdIsPresentAndFieldHasStringType2() {
//
//		@Entity(domain = "entity")
//		class Entity {
//
//			@EntityUuid
//			private Float uuid;
//			@EntityId
//			private String id;
//
//		}
//
//		EntityException exception = assertThrows(EntityException.class, () -> {
//			EntityChecker.checkEntityClass(Entity.class);
//		});
//
//		assertEquals("Entity Entity has field uuid with wrong type java.lang.Float, should be class java.lang.String",
//				exception.getMessage());
//		assertEquals(EntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
//	}
//
//	@Test
//	public void testAnnotationEntityIdFromSuperClass() {
//		assertDoesNotThrow(() -> {
//			EntityChecker.checkEntityClass(TestAnnotationEntityIdFromSuperClass.class);
//		});
//	}
//
//	@Test
//	public void testAnnotationEntityId() {
//		assertDoesNotThrow(() -> {
//			EntityChecker.checkEntityClass(TestAnnotationEntityId.class);
//		});
//	}
//
//	@Test
//	public void testAnnotationDeleteMethodProvider() {
//		@Entity(domain = "entity")
//		class Entity {
//			@EntityUuid
//			String uuid;
//
//			@EntityId
//			String id;
//
//			@EntitySaveMethodProvider
//			IEntitySaveMethod saveProvider;
//		}
//
//		EntityException exception = assertThrows(EntityException.class, () -> {
//			EntityChecker.checkEntityClass(Entity.class);
//		});
//
//		assertEquals("Entity Entity does not have any field annotated with @EntityDeleteMethodProvider",
//				exception.getMessage());
//		assertEquals(EntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
//	}
//
//	@Test
//	public void testAnnotationDeleteMethodProviderNotGoodType() {
//		@Entity(domain = "entity")
//		class Entity {
//			@EntityUuid
//			String uuid;
//
//			@EntityId
//			String id;
//
//			@EntitySaveMethodProvider
//			IEntitySaveMethod saveProvider;
//
//			@EntityDeleteMethodProvider
//			long deleteMethod;
//		}
//
//		EntityException exception = assertThrows(EntityException.class, () -> {
//			EntityChecker.checkEntityClass(Entity.class);
//		});
//
//		assertEquals(
//				"Entity Entity has field deleteMethod with wrong type long, should be interface com.garganttua.api.core.entity.interfaces.IEntityDeleteMethod",
//				exception.getMessage());
//		assertEquals(EntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
//	}
//
//	@Test
//	public void testAnnotationSaveMethodProvider() {
//		@Entity(domain = "entity")
//		class Entity {
//			@EntityUuid
//			String uuid;
//
//			@EntityId
//			String id;
//
//			@EntityDeleteMethodProvider
//			IEntityDeleteMethod saveProvider;
//		}
//
//		EntityException exception = assertThrows(EntityException.class, () -> {
//			EntityChecker.checkEntityClass(Entity.class);
//		});
//
//		assertEquals("Entity Entity does not have any field annotated with @EntitySaveMethodProvider",
//				exception.getMessage());
//		assertEquals(EntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
//	}
//
//	@Test
//	public void testAnnotationSaveMethodProviderNotGoodType() {
//		@Entity(domain = "entity")
//		class Entity {
//			@EntityUuid
//			String uuid;
//
//			@EntityId
//			String id;
//
//			@EntityDeleteMethodProvider
//			IEntityDeleteMethod saveProvider;
//
//			@EntitySaveMethodProvider
//			long deleteMethod;
//		}
//
//		EntityException exception = assertThrows(EntityException.class, () -> {
//			EntityChecker.checkEntityClass(Entity.class);
//		});
//
//		assertEquals(
//				"Entity Entity has field deleteMethod with wrong type long, should be interface com.garganttua.api.core.entity.interfaces.IEntitySaveMethod",
//				exception.getMessage());
//		assertEquals(EntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
//	}
//
//	@Test
//	public void testAnnotationSaveMethod() {
//		@Entity(domain = "entity")
//		class Entity {
//			@EntityUuid
//			String uuid;
//
//			@EntityId
//			String id;
//
//			@EntityDeleteMethodProvider
//			IEntityDeleteMethod deleteMethod;
//
//			@EntitySaveMethodProvider
//			IEntitySaveMethod saveMethod;
//
//			@EntityRepository
//			private IRepository repository;
//
//			public void save(ICaller caller, Map<String, String> parameters, Optional<ISecurity> security)
//					throws EntityException, EngineException {
//			}
//
//			@EntityDeleteMethod
//			public void delete(ICaller caller, Map<String, String> parameters)
//					throws EntityException, EngineException {
//			}
//		}
//
//		EntityException exception = assertThrows(EntityException.class, () -> {
//			EntityChecker.checkEntityClass(Entity.class);
//		});
//
//		assertEquals("Entity Object does not have method annotated with @EntitySaveMethod",
//				exception.getMessage());
//		assertEquals(EntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
//	}
//
//	@Test
//	public void testAnnotationSaveMethodWrongParam1() {
//		@Entity(domain = "entity")
//		class Entity {
//			@EntityUuid
//			String uuid;
//
//			@EntityId
//			String id;
//
//			@EntityDeleteMethodProvider
//			IEntityDeleteMethod deleteMethod;
//
//			@EntitySaveMethodProvider
//			IEntitySaveMethod saveMethod;
//
//			@EntityRepository
//			private IRepository repository;
//
//			@EntitySaveMethod
//			public void save(long caller, Map<String, String> parameters, Optional<ISecurity> security)
//					throws EntityException, EngineException {
//			}
//
//			@EntityDeleteMethod
//			public void delete(ICaller caller, Map<String, String> parameters)
//					throws EntityException, EngineException {
//			}
//		}
//
//		EntityException exception = assertThrows(EntityException.class, () -> {
//			EntityChecker.checkEntityClass(Entity.class);
//		});
//
//		assertEquals(
//				"Entity Entity has method save but parameter 0 is not of type interface com.garganttua.api.core.ICaller",
//				exception.getMessage());
//		assertEquals(EntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
//	}
//
//	@Test
//	public void testAnnotationSaveMethodWrongParam2() {
//
//		@Entity(domain = "entity")
//		class Entity {
//			@EntityUuid
//			String uuid;
//
//			@EntityId
//			String id;
//
//			@EntityDeleteMethodProvider
//			IEntityDeleteMethod deleteMethod;
//
//			@EntitySaveMethodProvider
//			IEntitySaveMethod saveMethod;
//
//			@EntityRepository
//			private IRepository repository;
//
//			@EntitySaveMethod
//			public void save(ICaller caller, long parameters, Optional<ISecurity> security)
//					throws EntityException, EngineException {
//			}
//
//			@EntityDeleteMethod
//			public void delete(ICaller caller, Map<String, String> parameters)
//					throws EntityException, EngineException {
//			}
//		}
//
//		EntityException exception = assertThrows(EntityException.class, () -> {
//			EntityChecker.checkEntityClass(Entity.class);
//		});
//
//		assertEquals(
//				"Entity Entity has method save but parameter 1 is not of type java.lang.Map<java.lang.String,java.lang.String>",
//				exception.getMessage());
//		assertEquals(EntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
//	}
//
//	@Test
//	public void testAnnotationSaveMethodWrongParam3() {
//
//		@Entity(domain = "entity")
//		class Entity {
//			@EntityUuid
//			String uuid;
//
//			@EntityId
//			String id;
//
//			@EntityDeleteMethodProvider
//			IEntityDeleteMethod deleteMethod;
//
//			@EntitySaveMethodProvider
//			IEntitySaveMethod saveMethod;
//
//			@EntityRepository
//			private IRepository repository;
//
//			@EntitySaveMethod
//			public void save(ICaller caller, Map<String, String> parameters, long security)
//					throws EntityException, EngineException {
//			}
//
//			@EntityDeleteMethod
//			public void delete(ICaller caller, Map<String, String> parameters)
//					throws EntityException, EngineException {
//			}
//		}
//
//		EntityException exception = assertThrows(EntityException.class, () -> {
//			EntityChecker.checkEntityClass(Entity.class);
//		});
//
//		assertEquals("Entity Entity has method save but parameter 2 is not of type Optional<ISecurity>",
//				exception.getMessage());
//		assertEquals(EntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
//	}
//
//	@Test
//	public void testAnnotationDeleteMethod() {
//
//		@Entity(domain = "entity")
//		class Entity {
//			@EntityUuid
//			String uuid;
//
//			@EntityId
//			String id;
//
//			@EntityDeleteMethodProvider
//			IEntityDeleteMethod deleteMethod;
//
//			@EntitySaveMethodProvider
//			IEntitySaveMethod saveMethod;
//
//			@EntityRepository
//			private IRepository repository;
//
//			@EntitySaveMethod
//			public void save(ICaller caller, Map<String, String> parameters, Optional<ISecurity> security)
//					throws EntityException, EngineException {
//			}
//
//			public void delete(ICaller caller, Map<String, String> parameters)
//					throws EntityException, EngineException {
//			}
//		}
//
//		EntityException exception = assertThrows(EntityException.class, () -> {
//			EntityChecker.checkEntityClass(Entity.class);
//		});
//
//		assertEquals("Entity Object does not have method annotated with @EntityDeleteMethod",
//				exception.getMessage());
//		assertEquals(EntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
//	}
//
//	@Test
//	public void testAnnotationDeleteMethodWrongParam1() {
//		@Entity(domain = "entity")
//		class Entity {
//			@EntityUuid
//			String uuid;
//
//			@EntityId
//			String id;
//
//			@EntityDeleteMethodProvider
//			IEntityDeleteMethod deleteMethod;
//
//			@EntitySaveMethodProvider
//			IEntitySaveMethod saveMethod;
//
//			@EntityRepository
//			private IRepository repository;
//
//			@EntitySaveMethod
//			public void save(ICaller caller, Map<String, String> parameters, Optional<ISecurity> security)
//					throws EntityException, EngineException {
//			}
//
//			@EntityDeleteMethod
//			public void delete(long caller, Map<String, String> parameters)
//					throws EntityException, EngineException {
//			}
//		}
//
//		EntityException exception = assertThrows(EntityException.class, () -> {
//			EntityChecker.checkEntityClass(Entity.class);
//		});
//
//		assertEquals(
//				"Entity Entity has method delete but parameter 0 is not of type interface com.garganttua.api.core.ICaller",
//				exception.getMessage());
//		assertEquals(EntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
//	}
//
//	@Test
//	public void testAnnotationDeleteMethodWrongParam2() {
//		@Entity(domain = "entity")
//		class Entity {
//
//			@EntityUuid
//			String uuid;
//
//			@EntityId
//			String id;
//
//			@EntityDeleteMethodProvider
//			IEntityDeleteMethod deleteMethod;
//
//			@EntitySaveMethodProvider
//			IEntitySaveMethod saveMethod;
//
//			@EntityRepository
//			private IRepository repository;
//
//			@EntitySaveMethod
//			public void save(ICaller caller, Map<String, String> parameters, Optional<ISecurity> security)
//					throws EntityException, EngineException {
//			}
//
//			@EntityDeleteMethod
//			public void delete(ICaller caller, long parameters) throws EntityException, EngineException {
//			}
//		}
//
//		EntityException exception = assertThrows(EntityException.class, () -> {
//			EntityChecker.checkEntityClass(Entity.class);
//		});
//
//		assertEquals(
//				"Entity Entity has method delete but parameter 1 is not of type java.lang.Map<java.lang.String,java.lang.String>",
//				exception.getMessage());
//		assertEquals(EntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
//	}
//
//	@Test
//	public void testAnnotationHiddenableFieldOrAnnotationisPresent() {
//		@Entity(domain = "entity")
//		@EntityHiddenable
//		class Entity extends GenericEntity {
//
//		}
//
//		EntityException exception = assertThrows(EntityException.class, () -> {
//			EntityChecker.checkEntityClass(Entity.class);
//		});
//
//		assertEquals(
//				"Entity Entity does not have any field value or field annotated with com.garganttua.api.core.entity.annotations.EntityHidden",
//				exception.getMessage());
//		assertEquals(EntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
//	}
//
//	@Test
//	public void testAnnotationHiddenableFieldnotPresent() {
//
//		@Entity(domain = "entity")
//		@EntityHiddenable(hidden = "hidden")
//		class Entity extends GenericEntity {
//
//		}
//
//		EntityException exception = assertThrows(EntityException.class, () -> {
//			EntityChecker.checkEntityClass(Entity.class);
//		});
//
//		assertEquals("Entity Entity does not have field hidden", exception.getMessage());
//		assertEquals(EntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
//	}
//
//	@Test
//	public void testAnnotationHiddenableFieldPresentButNotGoodType() {
//
//		@Entity(domain = "entity")
//		@EntityHiddenable(hidden = "hidden")
//		class Entity extends GenericEntity {
//
//			@EntityHidden
//			String hidden;
//		}
//
//		EntityException exception = assertThrows(EntityException.class, () -> {
//			EntityChecker.checkEntityClass(Entity.class);
//		});
//
//		assertEquals("Entity Entity has field hidden with wrong type java.lang.String, should be boolean",
//				exception.getMessage());
//		assertEquals(EntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
//	}
//
//	@Test
//	public void testAnnotationGeolocalizedFieldOrAnnotationisPresent() {
//
//		@Entity(domain = "entity")
//		@EntityGeolocalized
//		class Entity extends GenericEntity {
//
//		}
//
//		EntityException exception = assertThrows(EntityException.class, () -> {
//			EntityChecker.checkEntityClass(Entity.class);
//		});
//
//		assertEquals(
//				"Entity Entity does not have any field value or field annotated with com.garganttua.api.core.entity.annotations.EntityLocation",
//				exception.getMessage());
//		assertEquals(EntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
//	}
//
//	@Test
//	public void testAnnotationGeolocalizedFieldnotPresent() {
//		@Entity(domain = "entity")
//		@EntityGeolocalized(location = "location")
//		class Entity extends GenericEntity {
//
//		}
//
//		EntityException exception = assertThrows(EntityException.class, () -> {
//			EntityChecker.checkEntityClass(Entity.class);
//		});
//
//		assertEquals("Entity Entity does not have field location", exception.getMessage());
//		assertEquals(EntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
//	}
//
//	@Test
//	public void testAnnotationGeolocalizedFieldPresentButNotGoodType() {
//		@Entity(domain = "entity")
//		@EntityGeolocalized(location = "location")
//		class Entity extends GenericEntity {
//
//			@EntityLocation
//			String location;
//		}
//
//		EntityException exception = assertThrows(EntityException.class, () -> {
//			EntityChecker.checkEntityClass(Entity.class);
//		});
//
//		assertEquals(
//				"Entity Entity has field location with wrong type java.lang.String, should be class org.geojson.Point",
//				exception.getMessage());
//		assertEquals(EntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
//	}
//
//	@Test
//	public void testAnnotationLocationTwice() {
//		@Entity(domain = "entity")
//		@EntityGeolocalized(location = "location")
//		class Entity extends GenericEntity {
//
//			@EntityLocation
//			Point location;
//
//			@EntityLocation
//			Point location2;
//		}
//
//		EntityException exception = assertThrows(EntityException.class, () -> {
//			EntityChecker.checkEntityClass(Entity.class);
//		});
//
//		assertEquals(
//				"Entity Entity has more than one field annotated with interface com.garganttua.api.core.entity.annotations.EntityLocation",
//				exception.getMessage());
//		assertEquals(EntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
//	}
//
//	@Test
//	public void testAnnotationGeolocalized() {
//		assertDoesNotThrow(() -> {
//			EntityChecker.checkEntityClass(TestAnnotationGeolocalized.class);
//		});
//	}
//
//	@Test
//	public void testAnnotationEntityIdTwice() {
//		@Entity(domain = "entity")
//		class Entity extends GenericEntity {
//
//			@EntityUuid
//			private String uuid;
//
//			@EntityId
//			private String uuid2;
//
//			@EntityId
//			private String uuid3;
//
//			@EntitySaveMethodProvider
//			protected IEntitySaveMethod saveMethod;
//
//			@EntityDeleteMethodProvider
//			protected IEntityDeleteMethod deleteMethod;
//
//			@EntityRepository
//			protected IRepository repository;
//
//			@EntitySaveMethod
//			public void save(ICaller caller, Map<String, String> parameters, Optional<ISecurity> security)
//					throws EntityException, EngineException {
//			}
//
//			@EntityDeleteMethod
//			public void delete(ICaller caller, Map<String, String> parameters)
//					throws EntityException, EngineException {
//			}
//
//		}
//
//		EntityException exception = assertThrows(EntityException.class, () -> {
//			EntityChecker.checkEntityClass(Entity.class);
//		});
//
//		assertEquals(
//				"Entity Entity has more than one field annotated with interface com.garganttua.api.core.entity.annotations.EntityId",
//				exception.getMessage());
//		assertEquals(EntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
//	}
//
//	@Test
//	public void testAnnotationEntitySAveMethodProviderTwice() {
//		@Entity(domain = "entity")
//		class Entity extends GenericEntity {
//
//			@EntityUuid
//			private String uuid;
//
//			@EntityId
//			private String uuid2;
//
//			@EntitySaveMethodProvider
//			protected IEntitySaveMethod saveMethod;
//
//			@EntitySaveMethodProvider
//			protected IEntitySaveMethod saveMethod2;
//
//			@EntityDeleteMethodProvider
//			protected IEntityDeleteMethod deleteMethod;
//
//			@EntityRepository
//			protected IRepository repository;
//
//			@EntitySaveMethod
//			public void save(ICaller caller, Map<String, String> parameters, Optional<ISecurity> security)
//					throws EntityException, EngineException {
//			}
//
//			@EntityDeleteMethod
//			public void delete(ICaller caller, Map<String, String> parameters)
//					throws EntityException, EngineException {
//			}
//
//		}
//
//		EntityException exception = assertThrows(EntityException.class, () -> {
//			EntityChecker.checkEntityClass(Entity.class);
//		});
//
//		assertEquals(
//				"Entity Entity has more than one field annotated with interface com.garganttua.api.core.entity.annotations.EntitySaveMethodProvider",
//				exception.getMessage());
//		assertEquals(EntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
//	}
//
//	@Test
//	public void testAnnotationEntityDeleteMethodProviderTwice() {
//		@Entity(domain = "entity")
//		class Entity extends GenericEntity {
//
//			@EntityUuid
//			private String uuid;
//
//			@EntityId
//			private String uuid2;
//
//			@EntitySaveMethodProvider
//			protected IEntitySaveMethod saveMethod;
//
//			@EntityDeleteMethodProvider
//			protected IEntityDeleteMethod deleteMethod;
//
//			@EntityDeleteMethodProvider
//			protected IEntityDeleteMethod deleteMethod2;
//
//			@EntityRepository
//			protected IRepository repository;
//
//			@EntitySaveMethod
//			public void save(ICaller caller, Map<String, String> parameters, Optional<ISecurity> security)
//					throws EntityException, EngineException {
//			}
//
//			@EntityDeleteMethod
//			public void delete(ICaller caller, Map<String, String> parameters)
//					throws EntityException, EngineException {
//			}
//
//		}
//
//		EntityException exception = assertThrows(EntityException.class, () -> {
//			EntityChecker.checkEntityClass(Entity.class);
//		});
//
//		assertEquals(
//				"Entity Entity has more than one field annotated with interface com.garganttua.api.core.entity.annotations.EntityDeleteMethodProvider",
//				exception.getMessage());
//		assertEquals(EntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
//	}
//
//	@Test
//	public void testRepositoryAnnotationNotPresent() {
//		@Entity(domain = "entity")
//		class Entity {
//
//			@JsonProperty
//			@EntityUuid
//			protected String uuid;
//
//			@JsonProperty
//			@EntityId
//			protected String id;
//
//			@JsonIgnore
//			private boolean gotFromRepository;
//
//			@JsonIgnore
//			@EntitySaveMethodProvider
//			transient protected IEntitySaveMethod saveMethod;
//
//			@JsonIgnore
//			@EntityDeleteMethodProvider
//			transient protected IEntityDeleteMethod deleteMethod;
//
//			@EntitySaveMethod
//			public void save(ICaller caller, Map<String, String> parameters, Optional<ISecurity> security)
//					throws EntityException, EngineException {
//			}
//
//			@EntityDeleteMethod
//			public void delete(ICaller caller, Map<String, String> parameters)
//					throws EntityException, EngineException {
//			}
//
////			@EntityRepository
//			String location;
//		}
//
//		EntityException exception = assertThrows(EntityException.class, () -> {
//			EntityChecker.checkEntityClass(Entity.class);
//		});
//
//		assertEquals("Entity Entity does not have any field annotated with @EntityRepository",
//				exception.getMessage());
//		assertEquals(EntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
//	}
//
//	@Test
//	public void testAnnotationEntityRepositoryTwice() {
//		@Entity(domain = "entity")
//		class Entity {
//
//			@EntityUuid
//			private String uuid;
//
//			@EntityId
//			private String uuid2;
//
//			@EntitySaveMethodProvider
//			protected IEntitySaveMethod saveMethod;
//
//			@EntityDeleteMethodProvider
//			protected IEntityDeleteMethod deleteMethod;
//
//			@EntityRepository
//			protected IRepository repository;
//
//			@EntityRepository
//			protected IRepository repository2;
//
//			@EntitySaveMethod
//			public void save(ICaller caller, Map<String, String> parameters, Optional<ISecurity> security)
//					throws EntityException, EngineException {
//			}
//
//			@EntityDeleteMethod
//			public void delete(ICaller caller, Map<String, String> parameters)
//					throws EntityException, EngineException {
//			}
//
//		}
//
//		EntityException exception = assertThrows(EntityException.class, () -> {
//			EntityChecker.checkEntityClass(Entity.class);
//		});
//
//		assertEquals(
//				"Entity Entity has more than one field annotated with interface com.garganttua.api.core.entity.annotations.EntityRepository",
//				exception.getMessage());
//		assertEquals(EntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
//	}
//
//	@Test
//	public void testAnnotationEntitySaveMethodTwice() {
//
//		@Entity(domain = "entity")
//		class Entity {
//
//			@EntityUuid
//			private String uuid;
//
//			@EntityId
//			private String uuid2;
//
//			@EntitySaveMethodProvider
//			protected IEntitySaveMethod saveMethod;
//
//			@EntityDeleteMethodProvider
//			protected IEntityDeleteMethod deleteMethod;
//
//			@EntityRepository
//			protected IRepository repository;
//
//			@EntitySaveMethod
//			public void save(ICaller caller, Map<String, String> parameters, Optional<ISecurity> security)
//					throws EntityException, EngineException {
//			}
//
//			@EntitySaveMethod
//			public void save2(ICaller caller, Map<String, String> parameters, Optional<ISecurity> security)
//					throws EntityException, EngineException {
//			}
//
//			@EntityDeleteMethod
//			public void delete(ICaller caller, Map<String, String> parameters)
//					throws EntityException, EngineException {
//			}
//
//		}
//
//		EntityException exception = assertThrows(EntityException.class, () -> {
//			EntityChecker.checkEntityClass(Entity.class);
//		});
//
//		assertEquals(
//				"Entity Entity has more than one method annotated with interface com.garganttua.api.core.entity.annotations.EntitySaveMethod",
//				exception.getMessage());
//		assertEquals(EntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
//	}
//
//	@Test
//	public void testAnnotationEntityDeleteMethodTwice() {
//
//		@Entity(domain = "entity")
//		class Entity {
//
//			@EntityUuid
//			private String uuid;
//
//			@EntityId
//			private String uuid2;
//
//			@EntitySaveMethodProvider
//			protected IEntitySaveMethod saveMethod;
//
//			@EntityDeleteMethodProvider
//			protected IEntityDeleteMethod deleteMethod;
//
//			@EntityRepository
//			protected IRepository repository;
//
//			@EntitySaveMethod
//			public void save(ICaller caller, Map<String, String> parameters, Optional<ISecurity> security)
//					throws EntityException, EngineException {
//			}
//
//			@EntityDeleteMethod
//			public void delete(ICaller caller, Map<String, String> parameters)
//					throws EntityException, EngineException {
//			}
//
//			@EntityDeleteMethod
//			public void delete2(ICaller caller, Map<String, String> parameters)
//					throws EntityException, EngineException {
//			}
//
//		}
//
//		EntityException exception = assertThrows(EntityException.class, () -> {
//			EntityChecker.checkEntityClass(Entity.class);
//		});
//
//		assertEquals(
//				"Entity Entity has more than one method annotated with interface com.garganttua.api.core.entity.annotations.EntityDeleteMethod",
//				exception.getMessage());
//		assertEquals(EntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
//	}
//
//	@Test
//	public void testAnnotationOwnedAndOwner() {
//		EntityException exception = assertThrows(EntityException.class, () -> {
//			EntityChecker.checkEntityClass(TestAnnotationOwnedAndOwner.class);
//		});
//
//		assertEquals("Entity TestAnnotationOwnedAndOwner Cannot be owner and owned at the same time",
//				exception.getMessage());
//		assertEquals(EntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
//	}
//
//	@Test
//	public void testOwnedEntityWithNoAnnotatedField() {
//
//		@Entity(domain = "entity")
//		@EntityOwned
//		class Entity extends GenericEntity {
//
//		}
//
//		EntityException exception = assertThrows(EntityException.class, () -> {
//			EntityChecker.checkEntityClass(Entity.class);
//		});
//
//		assertEquals(
//				"Entity Entity does not have any field value or field annotated with com.garganttua.api.core.entity.annotations.EntityOwnerId",
//				exception.getMessage());
//		assertEquals(EntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
//	}
//
//	@Test
//	public void testOwnedEntityWithAnnotatedField() {
//
//		@Entity(domain = "entity")
//		@EntityOwned(ownerId = "uuid")
//		class Entity extends GenericEntity {
//
//		}
//		EntityException exception = assertThrows(EntityException.class, () -> {
//			EntityChecker.checkEntityClass(Entity.class);
//		});
//
//		assertEquals("Entity Entity does not have field uuid", exception.getMessage());
//		assertEquals(EntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
//	}
//
//	@Test
//	public void testOwnedEntityWithFieldValue() {
//		assertDoesNotThrow(() -> {
//			EntityChecker.checkEntityClass(TestOwnedEntityWithFieldValue.class);
//		});
//	}
//
//	@Test
//	public void testOwnedEntityWithFieldAnnotatedOfWrongType() {
//		@Entity(domain = "entity")
//		@EntityOwner
//		class Entity extends GenericEntity {
//
//			@EntityOwnerId
//			private Float uuid;
//
//		}
//
//		EntityException exception = assertThrows(EntityException.class, () -> {
//			EntityChecker.checkEntityClass(Entity.class);
//		});
//
//		assertEquals("Entity Entity has field uuid with wrong type java.lang.Float, should be class java.lang.String",
//				exception.getMessage());
//		assertEquals(EntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
//
//	}
//
//	@Test
//	public void testOwnedEntityWithFieldValueOfWrongType() {
//		@Entity(domain = "entity")
//		@EntityOwner(ownerId = "uuid")
//		class Entity extends GenericEntity {
//
//			private Float uuid;
//
//		}
//
//		EntityException exception = assertThrows(EntityException.class, () -> {
//			EntityChecker.checkEntityClass(Entity.class);
//		});
//
//		assertEquals(
//				"Entity Entity has field uuid with wrong type class java.lang.Float, should be class java.lang.String",
//				exception.getMessage());
//		assertEquals(EntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
//	}
//
//	/********************************************************************************/
//
//	@Test
//	public void testSharedEntityWithNoAnnotatedField() {
//
//		@Entity(domain = "entity")
//		@EntityShared
//		class Entity extends GenericEntity {
//
//		}
//
//		EntityException exception = assertThrows(EntityException.class, () -> {
//			EntityChecker.checkEntityClass(Entity.class);
//		});
//
//		assertEquals(
//				"Entity Entity does not have any field value or field annotated with com.garganttua.api.core.entity.annotations.EntityShare",
//				exception.getMessage());
//		assertEquals(EntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
//	}
//
//	@Test
//	public void testSharedEntityWithAnnotatedField() {
//
//		@Entity(domain = "entity")
//		@EntityShared(share = "uuid")
//		class Entity extends GenericEntity {
//
//		}
//		EntityException exception = assertThrows(EntityException.class, () -> {
//			EntityChecker.checkEntityClass(Entity.class);
//		});
//
//		assertEquals("Entity Entity does not have field uuid", exception.getMessage());
//		assertEquals(EntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
//	}
//
//	@Test
//	public void testSharedEntityWithFieldValue() {
//		assertDoesNotThrow(() -> {
//			EntityChecker.checkEntityClass(TestSharedEntityWithFieldValue.class);
//		});
//	}
//
//	@Test
//	public void testSharedEntityWithFieldAnnotatedOfWrongType() {
//
//		@Entity(domain = "entity")
//		@EntityShared
//		class Entity extends GenericEntity {
//			@EntityShare
//			private Float uuid;
//		}
//
//		EntityException exception = assertThrows(EntityException.class, () -> {
//			EntityChecker.checkEntityClass(Entity.class);
//		});
//
//		assertEquals("Entity Entity has field uuid with wrong type java.lang.Float, should be class java.lang.String",
//				exception.getMessage());
//		assertEquals(EntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
//
//	}
//
//	@Test
//	public void testSharedEntityWithFieldValueOfWrongType() {
//		@Entity(domain = "entity")
//		@EntityShared(share = "uuid")
//		class Entity extends GenericEntity {
//
//			private Float uuid;
//
//		}
//
//		EntityException exception = assertThrows(EntityException.class, () -> {
//			EntityChecker.checkEntityClass(Entity.class);
//		});
//
//		assertEquals(
//				"Entity Entity has field uuid with wrong type class java.lang.Float, should be class java.lang.String",
//				exception.getMessage());
//		assertEquals(EntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
//	}
//
//	@Test
//	public void testValidationResult() throws EntityException, ClassNotFoundException, ObjectAddressException {
//
//		EntityInfos result = EntityChecker.checkEntityClass(TestValidationResult.class);
//		EntityInfos expectedResult = new EntityInfos("tenants", new ObjectAddress("uuid"),
//				new ObjectAddress("id"), new ObjectAddress("saveMethod"),
//				new ObjectAddress("deleteMethod"), true, true, false, new ObjectAddress("uuid"),
//				new ObjectAddress("superTenant"), new ObjectAddress("uuid"),
//				new ObjectAddress("superOwner"), new ObjectAddress("save"), new ObjectAddress("delete"),
//				true, true, new ObjectAddress("enabled"), true, new ObjectAddress("location"), true,
//				new ObjectAddress("surname"), new ObjectAddress("repository"),
//				Lists.newArrayList("id", "password"), Lists.newArrayList("id", "location"), null, null, null, null,
//				null, null, null, new HashMap<String, String>(), new ObjectAddress("superTenant"));
//
//		assertEquals(expectedResult, result);
//
//	}
//
//	@Test
//	public void testValidationResult2() throws EntityException, ClassNotFoundException, ObjectAddressException {
//
//		EntityInfos result = EntityChecker.checkEntityClass(TestValidationResult2.class);
//		EntityInfos expectedResult = new EntityInfos("tenants", new ObjectAddress("uuid"),
//				new ObjectAddress("id"), new ObjectAddress("saveMethod"),
//				new ObjectAddress("deleteMethod"), true, false, true, new ObjectAddress("uuid"),
//				new ObjectAddress("superTenant"), new ObjectAddress("uuid"), null,
//				new ObjectAddress("save"), new ObjectAddress("delete"), true, true,
//				new ObjectAddress("enabled"), true, new ObjectAddress("location"), true,
//				new ObjectAddress("surname"), new ObjectAddress("repository"),
//				Lists.newArrayList("id", "password"), Lists.newArrayList("id", "location"), null, null, null, null,
//				null, null, null, new HashMap<String, String>(), new ObjectAddress("superTenant"));
//
//		assertEquals(expectedResult, result);
//
//	}
//
//	@Test
//	public void testBusinessMethodsPresence() throws EntityException, ClassNotFoundException, ObjectAddressException {
//
//		EntityInfos result = EntityChecker.checkEntityClass(TestBusinessMethodsPresence.class);
//		EntityInfos expectedResult = new EntityInfos("tenants", new ObjectAddress("uuid"),
//				new ObjectAddress("id"), new ObjectAddress("saveMethod"),
//				new ObjectAddress("deleteMethod"), true, false, true, new ObjectAddress("uuid"),
//				new ObjectAddress("superTenant"), new ObjectAddress("uuid"), null,
//				new ObjectAddress("save"), new ObjectAddress("delete"), true, true,
//				new ObjectAddress("enabled"), true, new ObjectAddress("location"), true,
//				new ObjectAddress("surname"), new ObjectAddress("repository"),
//				Lists.newArrayList("id", "password"), Lists.newArrayList("id", "location"),
//				new ObjectAddress("afterGet"), new ObjectAddress("beforeCreate"),
//				new ObjectAddress("afterCreate"), new ObjectAddress("beforeUpdate"),
//				new ObjectAddress("afterUpdate"), new ObjectAddress("beforeDelete"),
//				new ObjectAddress("afterDelete"), new HashMap<String, String>(),
//				new ObjectAddress("superTenant"));
//
//		assertEquals(expectedResult, result);
//	}
//
//	@Test
//	public void testAuthorizeUpdate() throws EntityException, ClassNotFoundException, ObjectAddressException {
//
//		EntityInfos result = EntityChecker.checkEntityClass(TestAuthorizeUpdate.class);
//		EntityInfos expectedResult = new EntityInfos("tenants", new ObjectAddress("uuid"),
//				new ObjectAddress("id"), new ObjectAddress("saveMethod"),
//				new ObjectAddress("deleteMethod"), true, false, true, new ObjectAddress("uuid"),
//				new ObjectAddress("superTenant"), new ObjectAddress("uuid"), null,
//				new ObjectAddress("save"), new ObjectAddress("delete"), true, true,
//				new ObjectAddress("enabled"), true, new ObjectAddress("location"), true,
//				new ObjectAddress("surname"), new ObjectAddress("repository"),
//				Lists.newArrayList("id", "password"), Lists.newArrayList("id", "location"), null, null, null, null,
//				null, null, null, Maps.newHashMap("superOwner", "test"), new ObjectAddress("superTenant"));
//
//		assertEquals(expectedResult, result);
//
//	}
//
//	@Test
//	public void testGenericEntity() {
//		assertDoesNotThrow(() -> {
//			EntityChecker.checkEntityClass(TestGenericEntity.class);
//		});
//	}
}
