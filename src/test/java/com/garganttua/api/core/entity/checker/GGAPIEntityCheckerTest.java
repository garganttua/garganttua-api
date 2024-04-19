package com.garganttua.api.core.entity.checker;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.assertj.core.util.Lists;
import org.assertj.core.util.Maps;
import org.geojson.Point;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.garganttua.api.core.IGGAPICaller;
import com.garganttua.api.core.engine.GGAPIEngineException;
import com.garganttua.api.core.entity.GenericGGAPIEntity;
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
import com.garganttua.api.core.entity.checker.GGAPIEntityChecker.GGAPIEntityInfos;
import com.garganttua.api.core.entity.exceptions.GGAPIEntityException;
import com.garganttua.api.core.entity.interfaces.IGGAPIEntityDeleteMethod;
import com.garganttua.api.core.entity.interfaces.IGGAPIEntitySaveMethod;
import com.garganttua.api.core.objects.GGAPIObjectAddress;
import com.garganttua.api.core.objects.GGAPIObjectAddressException;
import com.garganttua.api.core.repository.IGGAPIRepository;
import com.garganttua.api.core.security.IGGAPISecurity;

public class GGAPIEntityCheckerTest {

	@Test
	public void testEntityWithNoDomain() {

		@GGAPIEntity(domain = "")
		class Entity {
			@GGAPIEntityUuid
			private String tuuid;
		}

		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityChecker.checkEntityClass(Entity.class);
		});

		assertEquals("No domain provided in annotation of entity " + Entity.class.getSimpleName(),
				exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}

	@Test
	public void testTenantEntityWithNoAnnotatedField() {

		@GGAPIEntity(domain = "entity")
		@GGAPIEntityTenant
		class Entity extends GenericGGAPIEntity {
			@GGAPIEntityUuid
			private String tuuid;
			@GGAPIEntityId
			private String id;
		}

		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityChecker.checkEntityClass(Entity.class);
		});

		assertEquals(
				"Entity Entity does not have any field value or field annotated with com.garganttua.api.core.entity.annotations.GGAPIEntityTenantId",
				exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}

	@Test
	public void testTenantEntityWithAnnotatedField() {
		assertDoesNotThrow(() -> {
			GGAPIEntityChecker.checkEntityClass(TestTenantEntityWithAnnotatedField.class);
		});
	}

	@Test
	public void testTenantEntityWithFieldValue() {
		assertDoesNotThrow(() -> {
			GGAPIEntityChecker.checkEntityClass(TestTenantEntityWithFieldValue.class);
		});
	}

	@Test
	public void testTenantEntityWithFieldAnnotatedOfWrongType() {
		@GGAPIEntity(domain = "entity")
		@GGAPIEntityTenant()
		class Entity extends GenericGGAPIEntity {

			@GGAPIEntityTenantId
			private Float uuid;
			@GGAPIEntityUuid
			private String tuuid;
			@GGAPIEntityId
			private String id;

		}

		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityChecker.checkEntityClass(Entity.class);
		});

		assertEquals(
				"Entity " + Entity.class.getSimpleName()
						+ " has field uuid with wrong type java.lang.Float, should be class java.lang.String",
				exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());

	}

	@Test
	public void testTenantEntityWithFieldValueOfWrongType() {

		class Dto {

		}

		@GGAPIEntity(domain = "entity")
		@GGAPIEntityTenant(tenantId = "uuid")
		class Entity extends GenericGGAPIEntity {

			private Float uuid;
			@GGAPIEntityUuid
			private String tuuid;
			@GGAPIEntityId
			private String id;

		}

		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityChecker.checkEntityClass(Entity.class);
		});

		assertEquals(
				"Entity Entity has field uuid with wrong type class java.lang.Float, should be class java.lang.String",
				exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}

	/***********************************************************************************/

	@Test
	public void testOwnerEntityWithNoAnnotatedField() {

		class Dto {

		}

		@GGAPIEntity(domain = "entity")
		@GGAPIEntityOwner
		class Entity extends GenericGGAPIEntity {
			@GGAPIEntityUuid
			private String tuuid;
			@GGAPIEntityId
			private String id;
		}

		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityChecker.checkEntityClass(Entity.class);
		});

		assertEquals(
				"Entity Entity does not have any field value or field annotated with com.garganttua.api.core.entity.annotations.GGAPIEntityOwnerId",
				exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}

	@Test
	public void testOwnerEntityWithAnnotatedField() {
		assertDoesNotThrow(() -> {
			GGAPIEntityChecker.checkEntityClass(TestOwnerEntityWithAnnotatedField.class);
		});
	}

	@Test
	public void testOwnerEntityWithFieldValue() {
		assertDoesNotThrow(() -> {
			GGAPIEntityChecker.checkEntityClass(TestOwnerEntityWithFieldValue.class);
		});
	}

	@Test
	public void testOwnerEntityWithFieldAnnotatedOfWrongType() {
		@GGAPIEntity(domain = "entity")
		@GGAPIEntityOwner()
		class Entity extends GenericGGAPIEntity {

			@GGAPIEntityTenantId
			private Float uuid;
			@GGAPIEntityUuid
			private String tuuid;
			@GGAPIEntityId
			private String id;

		}

		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityChecker.checkEntityClass(Entity.class);
		});

		assertEquals(
				"Entity Entity does not have any field value or field annotated with com.garganttua.api.core.entity.annotations.GGAPIEntityOwnerId",
				exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());

	}

	@Test
	public void testOwnerEntityWithFieldValueOfWrongType() {

		class Dto {

		}

		@GGAPIEntity(domain = "entity")
		@GGAPIEntityOwner(ownerId = "uuid")
		class Entity extends GenericGGAPIEntity {

			private Float uuid;
			@GGAPIEntityUuid
			private String tuuid;
			@GGAPIEntityId
			private String id;

		}

		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityChecker.checkEntityClass(Entity.class);
		});

		assertEquals(
				"Entity Entity has field uuid with wrong type class java.lang.Float, should be class java.lang.String",
				exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}

	/*****************************************************************************************/

	@Test
	public void testAnnotationEntityUuidIsPresentAndFieldHasStringType() {
		@GGAPIEntity(domain = "entity")
		class Entity {

			private String uuid;

		}

		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityChecker.checkEntityClass(Entity.class);
		});

		assertEquals("Entity Entity does not have any field annotated with @GGAPIEntityUuid", exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}

	@Test
	public void testAnnotationEntityUuidIsPresentAndFieldHasStringType2() {
		@GGAPIEntity(domain = "entity")
		class Entity {

			@GGAPIEntityUuid
			private Float uuid;

		}

		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityChecker.checkEntityClass(Entity.class);
		});

		assertEquals("Entity Entity has field uuid with wrong type java.lang.Float, should be class java.lang.String",
				exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}

	@Test
	public void testAnnotationEntityUuidTwice() {
		@GGAPIEntity(domain = "entity")
		class Entity {

			@GGAPIEntityUuid
			private String uuid;

			@GGAPIEntityUuid
			private String uuid2;

		}

		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityChecker.checkEntityClass(Entity.class);
		});

		assertEquals(
				"Entity Entity has more than one field annotated with interface com.garganttua.api.core.entity.annotations.GGAPIEntityUuid",
				exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}

	@Test
	public void testAnnotationEntityUuidFromSuperClass() {
		assertDoesNotThrow(() -> {
			GGAPIEntityChecker.checkEntityClass(TestAnnotationEntityUuidFromSuperClass.class);
		});
	}

	@Test
	public void testAnnotationEntityUuid() {
		assertDoesNotThrow(() -> {
			GGAPIEntityChecker.checkEntityClass(TestAnnotationEntityUuid.class);
		});
	}

	/**************************************************************************/
	@Test
	public void testAnnotationEntityIdIsPresentAndFieldHasStringType2() {

		@GGAPIEntity(domain = "entity")
		class Entity {

			@GGAPIEntityUuid
			private Float uuid;
			@GGAPIEntityId
			private String id;

		}

		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityChecker.checkEntityClass(Entity.class);
		});

		assertEquals("Entity Entity has field uuid with wrong type java.lang.Float, should be class java.lang.String",
				exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}

	@Test
	public void testAnnotationEntityIdFromSuperClass() {
		assertDoesNotThrow(() -> {
			GGAPIEntityChecker.checkEntityClass(TestAnnotationEntityIdFromSuperClass.class);
		});
	}

	@Test
	public void testAnnotationEntityId() {
		assertDoesNotThrow(() -> {
			GGAPIEntityChecker.checkEntityClass(TestAnnotationEntityId.class);
		});
	}

	@Test
	public void testAnnotationDeleteMethodProvider() {
		@GGAPIEntity(domain = "entity")
		class Entity {
			@GGAPIEntityUuid
			String uuid;

			@GGAPIEntityId
			String id;

			@GGAPIEntitySaveMethodProvider
			IGGAPIEntitySaveMethod saveProvider;
		}

		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityChecker.checkEntityClass(Entity.class);
		});

		assertEquals("Entity Entity does not have any field annotated with @GGAPIEntityDeleteMethodProvider",
				exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}

	@Test
	public void testAnnotationDeleteMethodProviderNotGoodType() {
		@GGAPIEntity(domain = "entity")
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
			GGAPIEntityChecker.checkEntityClass(Entity.class);
		});

		assertEquals(
				"Entity Entity has field deleteMethod with wrong type long, should be interface com.garganttua.api.core.entity.interfaces.IGGAPIEntityDeleteMethod",
				exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}

	@Test
	public void testAnnotationSaveMethodProvider() {
		@GGAPIEntity(domain = "entity")
		class Entity {
			@GGAPIEntityUuid
			String uuid;

			@GGAPIEntityId
			String id;

			@GGAPIEntityDeleteMethodProvider
			IGGAPIEntityDeleteMethod saveProvider;
		}

		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityChecker.checkEntityClass(Entity.class);
		});

		assertEquals("Entity Entity does not have any field annotated with @GGAPIEntitySaveMethodProvider",
				exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}

	@Test
	public void testAnnotationSaveMethodProviderNotGoodType() {
		@GGAPIEntity(domain = "entity")
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
			GGAPIEntityChecker.checkEntityClass(Entity.class);
		});

		assertEquals(
				"Entity Entity has field deleteMethod with wrong type long, should be interface com.garganttua.api.core.entity.interfaces.IGGAPIEntitySaveMethod",
				exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}

	@Test
	public void testAnnotationSaveMethod() {
		@GGAPIEntity(domain = "entity")
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

			public void save(IGGAPICaller caller, Map<String, String> parameters, Optional<IGGAPISecurity> security)
					throws GGAPIEntityException, GGAPIEngineException {
			}

			@GGAPIEntityDeleteMethod
			public void delete(IGGAPICaller caller, Map<String, String> parameters)
					throws GGAPIEntityException, GGAPIEngineException {
			}
		}

		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityChecker.checkEntityClass(Entity.class);
		});

		assertEquals("Entity Object does not have method annotated with @GGAPIEntitySaveMethod",
				exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}

	@Test
	public void testAnnotationSaveMethodWrongParam1() {
		@GGAPIEntity(domain = "entity")
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
			public void save(long caller, Map<String, String> parameters, Optional<IGGAPISecurity> security)
					throws GGAPIEntityException, GGAPIEngineException {
			}

			@GGAPIEntityDeleteMethod
			public void delete(IGGAPICaller caller, Map<String, String> parameters)
					throws GGAPIEntityException, GGAPIEngineException {
			}
		}

		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityChecker.checkEntityClass(Entity.class);
		});

		assertEquals(
				"Entity Entity has method save but parameter 0 is not of type interface com.garganttua.api.core.IGGAPICaller",
				exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}

	@Test
	public void testAnnotationSaveMethodWrongParam2() {

		@GGAPIEntity(domain = "entity")
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
			public void save(IGGAPICaller caller, long parameters, Optional<IGGAPISecurity> security)
					throws GGAPIEntityException, GGAPIEngineException {
			}

			@GGAPIEntityDeleteMethod
			public void delete(IGGAPICaller caller, Map<String, String> parameters)
					throws GGAPIEntityException, GGAPIEngineException {
			}
		}

		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityChecker.checkEntityClass(Entity.class);
		});

		assertEquals(
				"Entity Entity has method save but parameter 1 is not of type java.lang.Map<java.lang.String,java.lang.String>",
				exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}

	@Test
	public void testAnnotationSaveMethodWrongParam3() {

		@GGAPIEntity(domain = "entity")
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
			public void save(IGGAPICaller caller, Map<String, String> parameters, long security)
					throws GGAPIEntityException, GGAPIEngineException {
			}

			@GGAPIEntityDeleteMethod
			public void delete(IGGAPICaller caller, Map<String, String> parameters)
					throws GGAPIEntityException, GGAPIEngineException {
			}
		}

		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityChecker.checkEntityClass(Entity.class);
		});

		assertEquals("Entity Entity has method save but parameter 2 is not of type Optional<IGGAPISecurity>",
				exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}

	@Test
	public void testAnnotationDeleteMethod() {

		@GGAPIEntity(domain = "entity")
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
			public void save(IGGAPICaller caller, Map<String, String> parameters, Optional<IGGAPISecurity> security)
					throws GGAPIEntityException, GGAPIEngineException {
			}

			public void delete(IGGAPICaller caller, Map<String, String> parameters)
					throws GGAPIEntityException, GGAPIEngineException {
			}
		}

		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityChecker.checkEntityClass(Entity.class);
		});

		assertEquals("Entity Object does not have method annotated with @GGAPIEntityDeleteMethod",
				exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}

	@Test
	public void testAnnotationDeleteMethodWrongParam1() {
		@GGAPIEntity(domain = "entity")
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
			public void save(IGGAPICaller caller, Map<String, String> parameters, Optional<IGGAPISecurity> security)
					throws GGAPIEntityException, GGAPIEngineException {
			}

			@GGAPIEntityDeleteMethod
			public void delete(long caller, Map<String, String> parameters)
					throws GGAPIEntityException, GGAPIEngineException {
			}
		}

		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityChecker.checkEntityClass(Entity.class);
		});

		assertEquals(
				"Entity Entity has method delete but parameter 0 is not of type interface com.garganttua.api.core.IGGAPICaller",
				exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}

	@Test
	public void testAnnotationDeleteMethodWrongParam2() {
		@GGAPIEntity(domain = "entity")
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
			public void save(IGGAPICaller caller, Map<String, String> parameters, Optional<IGGAPISecurity> security)
					throws GGAPIEntityException, GGAPIEngineException {
			}

			@GGAPIEntityDeleteMethod
			public void delete(IGGAPICaller caller, long parameters) throws GGAPIEntityException, GGAPIEngineException {
			}
		}

		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityChecker.checkEntityClass(Entity.class);
		});

		assertEquals(
				"Entity Entity has method delete but parameter 1 is not of type java.lang.Map<java.lang.String,java.lang.String>",
				exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}

	@Test
	public void testAnnotationHiddenableFieldOrAnnotationisPresent() {
		@GGAPIEntity(domain = "entity")
		@GGAPIEntityHiddenable
		class Entity extends GenericGGAPIEntity {

		}

		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityChecker.checkEntityClass(Entity.class);
		});

		assertEquals(
				"Entity Entity does not have any field value or field annotated with com.garganttua.api.core.entity.annotations.GGAPIEntityHidden",
				exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}

	@Test
	public void testAnnotationHiddenableFieldnotPresent() {

		@GGAPIEntity(domain = "entity")
		@GGAPIEntityHiddenable(hidden = "hidden")
		class Entity extends GenericGGAPIEntity {

		}

		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityChecker.checkEntityClass(Entity.class);
		});

		assertEquals("Entity Entity does not have field hidden", exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}

	@Test
	public void testAnnotationHiddenableFieldPresentButNotGoodType() {

		@GGAPIEntity(domain = "entity")
		@GGAPIEntityHiddenable(hidden = "hidden")
		class Entity extends GenericGGAPIEntity {

			@GGAPIEntityHidden
			String hidden;
		}

		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityChecker.checkEntityClass(Entity.class);
		});

		assertEquals("Entity Entity has field hidden with wrong type java.lang.String, should be boolean",
				exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}

	@Test
	public void testAnnotationGeolocalizedFieldOrAnnotationisPresent() {

		@GGAPIEntity(domain = "entity")
		@GGAPIEntityGeolocalized
		class Entity extends GenericGGAPIEntity {

		}

		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityChecker.checkEntityClass(Entity.class);
		});

		assertEquals(
				"Entity Entity does not have any field value or field annotated with com.garganttua.api.core.entity.annotations.GGAPIEntityLocation",
				exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}

	@Test
	public void testAnnotationGeolocalizedFieldnotPresent() {
		@GGAPIEntity(domain = "entity")
		@GGAPIEntityGeolocalized(location = "location")
		class Entity extends GenericGGAPIEntity {

		}

		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityChecker.checkEntityClass(Entity.class);
		});

		assertEquals("Entity Entity does not have field location", exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}

	@Test
	public void testAnnotationGeolocalizedFieldPresentButNotGoodType() {
		@GGAPIEntity(domain = "entity")
		@GGAPIEntityGeolocalized(location = "location")
		class Entity extends GenericGGAPIEntity {

			@GGAPIEntityLocation
			String location;
		}

		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityChecker.checkEntityClass(Entity.class);
		});

		assertEquals(
				"Entity Entity has field location with wrong type java.lang.String, should be class org.geojson.Point",
				exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}

	@Test
	public void testAnnotationLocationTwice() {
		@GGAPIEntity(domain = "entity")
		@GGAPIEntityGeolocalized(location = "location")
		class Entity extends GenericGGAPIEntity {

			@GGAPIEntityLocation
			Point location;

			@GGAPIEntityLocation
			Point location2;
		}

		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityChecker.checkEntityClass(Entity.class);
		});

		assertEquals(
				"Entity Entity has more than one field annotated with interface com.garganttua.api.core.entity.annotations.GGAPIEntityLocation",
				exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}

	@Test
	public void testAnnotationGeolocalized() {
		assertDoesNotThrow(() -> {
			GGAPIEntityChecker.checkEntityClass(TestAnnotationGeolocalized.class);
		});
	}

	@Test
	public void testAnnotationEntityIdTwice() {
		@GGAPIEntity(domain = "entity")
		class Entity extends GenericGGAPIEntity {

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
			public void save(IGGAPICaller caller, Map<String, String> parameters, Optional<IGGAPISecurity> security)
					throws GGAPIEntityException, GGAPIEngineException {
			}

			@GGAPIEntityDeleteMethod
			public void delete(IGGAPICaller caller, Map<String, String> parameters)
					throws GGAPIEntityException, GGAPIEngineException {
			}

		}

		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityChecker.checkEntityClass(Entity.class);
		});

		assertEquals(
				"Entity Entity has more than one field annotated with interface com.garganttua.api.core.entity.annotations.GGAPIEntityId",
				exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}

	@Test
	public void testAnnotationEntitySAveMethodProviderTwice() {
		@GGAPIEntity(domain = "entity")
		class Entity extends GenericGGAPIEntity {

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
			public void save(IGGAPICaller caller, Map<String, String> parameters, Optional<IGGAPISecurity> security)
					throws GGAPIEntityException, GGAPIEngineException {
			}

			@GGAPIEntityDeleteMethod
			public void delete(IGGAPICaller caller, Map<String, String> parameters)
					throws GGAPIEntityException, GGAPIEngineException {
			}

		}

		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityChecker.checkEntityClass(Entity.class);
		});

		assertEquals(
				"Entity Entity has more than one field annotated with interface com.garganttua.api.core.entity.annotations.GGAPIEntitySaveMethodProvider",
				exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}

	@Test
	public void testAnnotationEntityDeleteMethodProviderTwice() {
		@GGAPIEntity(domain = "entity")
		class Entity extends GenericGGAPIEntity {

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
			public void save(IGGAPICaller caller, Map<String, String> parameters, Optional<IGGAPISecurity> security)
					throws GGAPIEntityException, GGAPIEngineException {
			}

			@GGAPIEntityDeleteMethod
			public void delete(IGGAPICaller caller, Map<String, String> parameters)
					throws GGAPIEntityException, GGAPIEngineException {
			}

		}

		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityChecker.checkEntityClass(Entity.class);
		});

		assertEquals(
				"Entity Entity has more than one field annotated with interface com.garganttua.api.core.entity.annotations.GGAPIEntityDeleteMethodProvider",
				exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}

	@Test
	public void testRepositoryAnnotationNotPresent() {
		@GGAPIEntity(domain = "entity")
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
			public void save(IGGAPICaller caller, Map<String, String> parameters, Optional<IGGAPISecurity> security)
					throws GGAPIEntityException, GGAPIEngineException {
			}

			@GGAPIEntityDeleteMethod
			public void delete(IGGAPICaller caller, Map<String, String> parameters)
					throws GGAPIEntityException, GGAPIEngineException {
			}

//			@GGAPIEntityRepository
			String location;
		}

		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityChecker.checkEntityClass(Entity.class);
		});

		assertEquals("Entity Entity does not have any field annotated with @GGAPIEntityRepository",
				exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}

	@Test
	public void testAnnotationEntityRepositoryTwice() {
		@GGAPIEntity(domain = "entity")
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
			public void save(IGGAPICaller caller, Map<String, String> parameters, Optional<IGGAPISecurity> security)
					throws GGAPIEntityException, GGAPIEngineException {
			}

			@GGAPIEntityDeleteMethod
			public void delete(IGGAPICaller caller, Map<String, String> parameters)
					throws GGAPIEntityException, GGAPIEngineException {
			}

		}

		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityChecker.checkEntityClass(Entity.class);
		});

		assertEquals(
				"Entity Entity has more than one field annotated with interface com.garganttua.api.core.entity.annotations.GGAPIEntityRepository",
				exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}

	@Test
	public void testAnnotationEntitySaveMethodTwice() {

		@GGAPIEntity(domain = "entity")
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
			public void save(IGGAPICaller caller, Map<String, String> parameters, Optional<IGGAPISecurity> security)
					throws GGAPIEntityException, GGAPIEngineException {
			}

			@GGAPIEntitySaveMethod
			public void save2(IGGAPICaller caller, Map<String, String> parameters, Optional<IGGAPISecurity> security)
					throws GGAPIEntityException, GGAPIEngineException {
			}

			@GGAPIEntityDeleteMethod
			public void delete(IGGAPICaller caller, Map<String, String> parameters)
					throws GGAPIEntityException, GGAPIEngineException {
			}

		}

		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityChecker.checkEntityClass(Entity.class);
		});

		assertEquals(
				"Entity Entity has more than one method annotated with interface com.garganttua.api.core.entity.annotations.GGAPIEntitySaveMethod",
				exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}

	@Test
	public void testAnnotationEntityDeleteMethodTwice() {

		@GGAPIEntity(domain = "entity")
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
			public void save(IGGAPICaller caller, Map<String, String> parameters, Optional<IGGAPISecurity> security)
					throws GGAPIEntityException, GGAPIEngineException {
			}

			@GGAPIEntityDeleteMethod
			public void delete(IGGAPICaller caller, Map<String, String> parameters)
					throws GGAPIEntityException, GGAPIEngineException {
			}

			@GGAPIEntityDeleteMethod
			public void delete2(IGGAPICaller caller, Map<String, String> parameters)
					throws GGAPIEntityException, GGAPIEngineException {
			}

		}

		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityChecker.checkEntityClass(Entity.class);
		});

		assertEquals(
				"Entity Entity has more than one method annotated with interface com.garganttua.api.core.entity.annotations.GGAPIEntityDeleteMethod",
				exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}

	@Test
	public void testAnnotationOwnedAndOwner() {
		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityChecker.checkEntityClass(TestAnnotationOwnedAndOwner.class);
		});

		assertEquals("Entity TestAnnotationOwnedAndOwner Cannot be owner and owned at the same time",
				exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}

	@Test
	public void testOwnedEntityWithNoAnnotatedField() {

		@GGAPIEntity(domain = "entity")
		@GGAPIEntityOwned
		class Entity extends GenericGGAPIEntity {

		}

		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityChecker.checkEntityClass(Entity.class);
		});

		assertEquals(
				"Entity Entity does not have any field value or field annotated with com.garganttua.api.core.entity.annotations.GGAPIEntityOwnerId",
				exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}

	@Test
	public void testOwnedEntityWithAnnotatedField() {

		@GGAPIEntity(domain = "entity")
		@GGAPIEntityOwned(ownerId = "uuid")
		class Entity extends GenericGGAPIEntity {

		}
		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityChecker.checkEntityClass(Entity.class);
		});

		assertEquals("Entity Entity does not have field uuid", exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}

	@Test
	public void testOwnedEntityWithFieldValue() {
		assertDoesNotThrow(() -> {
			GGAPIEntityChecker.checkEntityClass(TestOwnedEntityWithFieldValue.class);
		});
	}

	@Test
	public void testOwnedEntityWithFieldAnnotatedOfWrongType() {
		@GGAPIEntity(domain = "entity")
		@GGAPIEntityOwner
		class Entity extends GenericGGAPIEntity {

			@GGAPIEntityOwnerId
			private Float uuid;

		}

		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityChecker.checkEntityClass(Entity.class);
		});

		assertEquals("Entity Entity has field uuid with wrong type java.lang.Float, should be class java.lang.String",
				exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());

	}

	@Test
	public void testOwnedEntityWithFieldValueOfWrongType() {
		@GGAPIEntity(domain = "entity")
		@GGAPIEntityOwner(ownerId = "uuid")
		class Entity extends GenericGGAPIEntity {

			private Float uuid;

		}

		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityChecker.checkEntityClass(Entity.class);
		});

		assertEquals(
				"Entity Entity has field uuid with wrong type class java.lang.Float, should be class java.lang.String",
				exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}

	/********************************************************************************/

	@Test
	public void testSharedEntityWithNoAnnotatedField() {

		@GGAPIEntity(domain = "entity")
		@GGAPIEntityShared
		class Entity extends GenericGGAPIEntity {

		}

		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityChecker.checkEntityClass(Entity.class);
		});

		assertEquals(
				"Entity Entity does not have any field value or field annotated with com.garganttua.api.core.entity.annotations.GGAPIEntityShare",
				exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}

	@Test
	public void testSharedEntityWithAnnotatedField() {

		@GGAPIEntity(domain = "entity")
		@GGAPIEntityShared(share = "uuid")
		class Entity extends GenericGGAPIEntity {

		}
		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityChecker.checkEntityClass(Entity.class);
		});

		assertEquals("Entity Entity does not have field uuid", exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}

	@Test
	public void testSharedEntityWithFieldValue() {
		assertDoesNotThrow(() -> {
			GGAPIEntityChecker.checkEntityClass(TestSharedEntityWithFieldValue.class);
		});
	}

	@Test
	public void testSharedEntityWithFieldAnnotatedOfWrongType() {

		@GGAPIEntity(domain = "entity")
		@GGAPIEntityShared
		class Entity extends GenericGGAPIEntity {
			@GGAPIEntityShare
			private Float uuid;
		}

		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityChecker.checkEntityClass(Entity.class);
		});

		assertEquals("Entity Entity has field uuid with wrong type java.lang.Float, should be class java.lang.String",
				exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());

	}

	@Test
	public void testSharedEntityWithFieldValueOfWrongType() {
		@GGAPIEntity(domain = "entity")
		@GGAPIEntityShared(share = "uuid")
		class Entity extends GenericGGAPIEntity {

			private Float uuid;

		}

		GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			GGAPIEntityChecker.checkEntityClass(Entity.class);
		});

		assertEquals(
				"Entity Entity has field uuid with wrong type class java.lang.Float, should be class java.lang.String",
				exception.getMessage());
		assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}

	@Test
	public void testValidationResult() throws GGAPIEntityException, ClassNotFoundException, GGAPIObjectAddressException {

		GGAPIEntityInfos result = GGAPIEntityChecker.checkEntityClass(TestValidationResult.class);
		GGAPIEntityInfos expectedResult = new GGAPIEntityInfos("tenants", new GGAPIObjectAddress("uuid"),
				new GGAPIObjectAddress("id"), new GGAPIObjectAddress("saveMethod"),
				new GGAPIObjectAddress("deleteMethod"), true, true, false, new GGAPIObjectAddress("uuid"),
				new GGAPIObjectAddress("superTenant"), new GGAPIObjectAddress("uuid"),
				new GGAPIObjectAddress("superOwner"), new GGAPIObjectAddress("save"), new GGAPIObjectAddress("delete"),
				true, true, new GGAPIObjectAddress("enabled"), true, new GGAPIObjectAddress("location"), true,
				new GGAPIObjectAddress("surname"), new GGAPIObjectAddress("repository"),
				Lists.newArrayList("id", "password"), Lists.newArrayList("id", "location"), null, null, null, null,
				null, null, null, new HashMap<String, String>(), new GGAPIObjectAddress("superTenant"));

		assertEquals(expectedResult, result);

	}

	@Test
	public void testValidationResult2() throws GGAPIEntityException, ClassNotFoundException, GGAPIObjectAddressException {

		GGAPIEntityInfos result = GGAPIEntityChecker.checkEntityClass(TestValidationResult2.class);
		GGAPIEntityInfos expectedResult = new GGAPIEntityInfos("tenants", new GGAPIObjectAddress("uuid"),
				new GGAPIObjectAddress("id"), new GGAPIObjectAddress("saveMethod"),
				new GGAPIObjectAddress("deleteMethod"), true, false, true, new GGAPIObjectAddress("uuid"),
				new GGAPIObjectAddress("superTenant"), new GGAPIObjectAddress("uuid"), null,
				new GGAPIObjectAddress("save"), new GGAPIObjectAddress("delete"), true, true,
				new GGAPIObjectAddress("enabled"), true, new GGAPIObjectAddress("location"), true,
				new GGAPIObjectAddress("surname"), new GGAPIObjectAddress("repository"),
				Lists.newArrayList("id", "password"), Lists.newArrayList("id", "location"), null, null, null, null,
				null, null, null, new HashMap<String, String>(), new GGAPIObjectAddress("superTenant"));

		assertEquals(expectedResult, result);

	}

	@Test
	public void testBusinessMethodsPresence() throws GGAPIEntityException, ClassNotFoundException, GGAPIObjectAddressException {

		GGAPIEntityInfos result = GGAPIEntityChecker.checkEntityClass(TestBusinessMethodsPresence.class);
		GGAPIEntityInfos expectedResult = new GGAPIEntityInfos("tenants", new GGAPIObjectAddress("uuid"),
				new GGAPIObjectAddress("id"), new GGAPIObjectAddress("saveMethod"),
				new GGAPIObjectAddress("deleteMethod"), true, false, true, new GGAPIObjectAddress("uuid"),
				new GGAPIObjectAddress("superTenant"), new GGAPIObjectAddress("uuid"), null,
				new GGAPIObjectAddress("save"), new GGAPIObjectAddress("delete"), true, true,
				new GGAPIObjectAddress("enabled"), true, new GGAPIObjectAddress("location"), true,
				new GGAPIObjectAddress("surname"), new GGAPIObjectAddress("repository"),
				Lists.newArrayList("id", "password"), Lists.newArrayList("id", "location"),
				new GGAPIObjectAddress("afterGet"), new GGAPIObjectAddress("beforeCreate"),
				new GGAPIObjectAddress("afterCreate"), new GGAPIObjectAddress("beforeUpdate"),
				new GGAPIObjectAddress("afterUpdate"), new GGAPIObjectAddress("beforeDelete"),
				new GGAPIObjectAddress("afterDelete"), new HashMap<String, String>(),
				new GGAPIObjectAddress("superTenant"));

		assertEquals(expectedResult, result);
	}

	@Test
	public void testAuthorizeUpdate() throws GGAPIEntityException, ClassNotFoundException, GGAPIObjectAddressException {

		GGAPIEntityInfos result = GGAPIEntityChecker.checkEntityClass(TestAuthorizeUpdate.class);
		GGAPIEntityInfos expectedResult = new GGAPIEntityInfos("tenants", new GGAPIObjectAddress("uuid"),
				new GGAPIObjectAddress("id"), new GGAPIObjectAddress("saveMethod"),
				new GGAPIObjectAddress("deleteMethod"), true, false, true, new GGAPIObjectAddress("uuid"),
				new GGAPIObjectAddress("superTenant"), new GGAPIObjectAddress("uuid"), null,
				new GGAPIObjectAddress("save"), new GGAPIObjectAddress("delete"), true, true,
				new GGAPIObjectAddress("enabled"), true, new GGAPIObjectAddress("location"), true,
				new GGAPIObjectAddress("surname"), new GGAPIObjectAddress("repository"),
				Lists.newArrayList("id", "password"), Lists.newArrayList("id", "location"), null, null, null, null,
				null, null, null, Maps.newHashMap("superOwner", "test"), new GGAPIObjectAddress("superTenant"));

		assertEquals(expectedResult, result);

	}

	@Test
	public void testGenericEntity() {
		assertDoesNotThrow(() -> {
			GGAPIEntityChecker.checkEntityClass(TestGenericEntity.class);
		});
	}
}
