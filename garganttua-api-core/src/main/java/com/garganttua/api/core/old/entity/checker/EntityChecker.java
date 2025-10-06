package com.garganttua.api.core.entity.checker;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.geojson.Point;
import org.javatuples.Pair;

import com.garganttua.api.core.entity.exceptions.EntityException;
import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.CoreExceptionCode;
import com.garganttua.api.spec.caller.ICaller;
import com.garganttua.api.spec.engine.IEngine;
import com.garganttua.api.spec.entity.EntityInfos;
import com.garganttua.api.spec.entity.IEntityDeleteMethod;
import com.garganttua.api.spec.entity.IEntitySaveMethod;
import com.garganttua.api.spec.entity.annotations.BusinessAnnotations.EntityAfterCreate;
import com.garganttua.api.spec.entity.annotations.BusinessAnnotations.EntityAfterDelete;
import com.garganttua.api.spec.entity.annotations.BusinessAnnotations.EntityAfterGet;
import com.garganttua.api.spec.entity.annotations.BusinessAnnotations.EntityAfterUpdate;
import com.garganttua.api.spec.entity.annotations.BusinessAnnotations.EntityBeforeCreate;
import com.garganttua.api.spec.entity.annotations.BusinessAnnotations.EntityBeforeDelete;
import com.garganttua.api.spec.entity.annotations.BusinessAnnotations.EntityBeforeUpdate;
import com.garganttua.api.spec.entity.annotations.Entity;
import com.garganttua.api.spec.entity.annotations.EntityAuthorizeUpdate;
import com.garganttua.api.spec.entity.annotations.EntityDeleteMethod;
import com.garganttua.api.spec.entity.annotations.EntityDeleteMethodProvider;
import com.garganttua.api.spec.entity.annotations.EntityEngine;
import com.garganttua.api.spec.entity.annotations.EntityGeolocalized;
import com.garganttua.api.spec.entity.annotations.EntityGotFromRepository;
import com.garganttua.api.spec.entity.annotations.EntityHidden;
import com.garganttua.api.spec.entity.annotations.EntityHiddenable;
import com.garganttua.api.spec.entity.annotations.EntityId;
import com.garganttua.api.spec.entity.annotations.EntityLocation;
import com.garganttua.api.spec.entity.annotations.EntityMandatories;
import com.garganttua.api.spec.entity.annotations.EntityMandatory;
import com.garganttua.api.spec.entity.annotations.EntityOwned;
import com.garganttua.api.spec.entity.annotations.EntityOwner;
import com.garganttua.api.spec.entity.annotations.EntityOwnerId;
import com.garganttua.api.spec.entity.annotations.EntityPublic;
import com.garganttua.api.spec.entity.annotations.EntityRepository;
import com.garganttua.api.spec.entity.annotations.EntitySaveMethod;
import com.garganttua.api.spec.entity.annotations.EntitySaveMethodProvider;
import com.garganttua.api.spec.entity.annotations.EntityShare;
import com.garganttua.api.spec.entity.annotations.EntityShared;
import com.garganttua.api.spec.entity.annotations.EntitySuperOwner;
import com.garganttua.api.spec.entity.annotations.EntitySuperTenant;
import com.garganttua.api.spec.entity.annotations.EntityTenant;
import com.garganttua.api.spec.entity.annotations.EntityTenantId;
import com.garganttua.api.spec.entity.annotations.EntityUnicities;
import com.garganttua.api.spec.entity.annotations.EntityUnicity;
import com.garganttua.api.spec.entity.annotations.EntityUuid;
import com.garganttua.api.spec.entity.annotations.UnicityScope;
import com.garganttua.api.spec.repository.IRepository;
import com.garganttua.reflection.GGObjectAddress;
import com.garganttua.reflection.GGReflectionException;
import com.garganttua.reflection.query.GGObjectQueryFactory;
import com.garganttua.reflection.query.IGGObjectQuery;
import com.garganttua.reflection.utils.GGObjectReflectionHelper;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EntityChecker {

	private static Map<Class<?>, EntityInfos> infos = new HashMap<Class<?>, EntityInfos>();

	public static EntityInfos checkEntityClass(Class<?> entityClass) throws CoreException {
		if (EntityChecker.infos.containsKey(entityClass)) {
			return EntityChecker.infos.get(entityClass);
		}

		if (log.isDebugEnabled()) {
			log.debug("Checking entity infos from class " + entityClass.getName());
		}

		Entity annotation = entityClass.getDeclaredAnnotation(Entity.class);

		if (annotation == null) {
			throw new EntityException(CoreExceptionCode.ENTITY_DEFINITION,
					"Entity " + entityClass.getSimpleName() + " is not annotated with @Entity");
		}

		// Check one constructor with no parameter
		EntityChecker.checkConstructor(entityClass);

		// Mandatory Fields and Methods
		String domain = EntityChecker.checkDomainInAnnotation(annotation, entityClass);
		String uuidFieldAddress = EntityChecker.getFieldAddressAnnotatedWithAndCheckType(entityClass,
				EntityUuid.class, String.class, true);
		String idFieldAddress = EntityChecker.getFieldAddressAnnotatedWithAndCheckType(entityClass,
				EntityId.class, String.class, true);
		String saveProviderFieldAddress = EntityChecker.getFieldAddressAnnotatedWithAndCheckType(entityClass,
				EntitySaveMethodProvider.class, IEntitySaveMethod.class, true);
		String deleteProviderFieldAddress = EntityChecker.getFieldAddressAnnotatedWithAndCheckType(entityClass,
				EntityDeleteMethodProvider.class, IEntityDeleteMethod.class, true);
		String saveMethodAddress = EntityChecker.getMethodAnnotationAndMethodParamsHaveGoodTypes(entityClass,
				EntitySaveMethod.class, true, Object.class, ICaller.class,
				GGObjectReflectionHelper.getParameterizedType(Map.class, String.class, String.class));
		String deleteMethodAddress = EntityChecker.getMethodAnnotationAndMethodParamsHaveGoodTypes(entityClass,
				EntityDeleteMethod.class, true, void.class, ICaller.class,
				GGObjectReflectionHelper.getParameterizedType(Map.class, String.class, String.class));
		String repositoryFieldAddress = EntityChecker.getFieldAddressAnnotatedWithAndCheckType(entityClass,
				EntityRepository.class, IRepository.class, true);
		String engineFieldAddress = EntityChecker.getFieldAddressAnnotatedWithAndCheckType(entityClass,
				EntityEngine.class, IEngine.class, true);
		String gotFromReposiotryFieldAddress = EntityChecker.getFieldAddressAnnotatedWithAndCheckType(entityClass,
				EntityGotFromRepository.class, boolean.class, true);
		String tenantIdFieldAddress = null;

		// Entity Optional Annotations
		Annotation tenantAnnotation = EntityChecker.checkIfAnnotatedEntity(entityClass, EntityTenant.class);
		Annotation ownerAnnotation = EntityChecker.checkIfAnnotatedEntity(entityClass, EntityOwner.class);
		Annotation ownedAnnotation = EntityChecker.checkIfAnnotatedEntity(entityClass, EntityOwned.class);
		Annotation publicAnnotation = EntityChecker.checkIfAnnotatedEntity(entityClass, EntityPublic.class);
		Annotation hiddenableAnnotation = EntityChecker.checkIfAnnotatedEntity(entityClass,
				EntityHiddenable.class);
		Annotation geolocalizedAnnotation = EntityChecker.checkIfAnnotatedEntity(entityClass,
				EntityGeolocalized.class);
		Annotation sharedAnnotation = EntityChecker.checkIfAnnotatedEntity(entityClass, EntityShared.class);

		String superTenantFieldAddress = null;
		String ownerIdFieldAddress = null;
		String superOnwerIdFieldAddress = null;
		String hiddenFieldAddress = null;
		String locationFieldAddress = null;
		String shareFieldAddress = null;

		String afterGetm = EntityChecker.getMethodAnnotationAndMethodParamsHaveGoodTypes(entityClass,
				EntityAfterGet.class, false, void.class, ICaller.class,
				GGObjectReflectionHelper.getParameterizedType(Map.class, String.class, String.class));
		String beforeCreatem = EntityChecker.getMethodAnnotationAndMethodParamsHaveGoodTypes(entityClass,
				EntityBeforeCreate.class, false, void.class, ICaller.class,
				GGObjectReflectionHelper.getParameterizedType(Map.class, String.class, String.class));
		String afterCreatem = EntityChecker.getMethodAnnotationAndMethodParamsHaveGoodTypes(entityClass,
				EntityAfterCreate.class, false, void.class, ICaller.class,
				GGObjectReflectionHelper.getParameterizedType(Map.class, String.class, String.class));
		String beforeUpdatem = EntityChecker.getMethodAnnotationAndMethodParamsHaveGoodTypes(entityClass,
				EntityBeforeUpdate.class, false, void.class, ICaller.class,
				GGObjectReflectionHelper.getParameterizedType(Map.class, String.class, String.class));
		String afterUpdatem = EntityChecker.getMethodAnnotationAndMethodParamsHaveGoodTypes(entityClass,
				EntityAfterUpdate.class, false, void.class, ICaller.class,
				GGObjectReflectionHelper.getParameterizedType(Map.class, String.class, String.class));
		String beforeDeletem = EntityChecker.getMethodAnnotationAndMethodParamsHaveGoodTypes(entityClass,
				EntityBeforeDelete.class, false, void.class, ICaller.class,
				GGObjectReflectionHelper.getParameterizedType(Map.class, String.class, String.class));
		String afterDeletem = EntityChecker.getMethodAnnotationAndMethodParamsHaveGoodTypes(entityClass,
				EntityAfterDelete.class, false, void.class, ICaller.class,
				GGObjectReflectionHelper.getParameterizedType(Map.class, String.class, String.class));

		Map<String, String> updateAuthorizations = EntityChecker.getFieldAuthorizedForUpdate(entityClass,
				new HashMap<String, String>());

		List<String> mandatoryFields = GGObjectReflectionHelper.getFieldAddressesWithAnnotation(entityClass,
				EntityMandatory.class, true);
		mandatoryFields.addAll(EntityChecker.checkMandatoriesAnnotationPresent(entityClass));

		List<String> unicityFieldsTemp = GGObjectReflectionHelper.getFieldAddressesWithAnnotation(entityClass,
				EntityUnicity.class, false);

		if (ownerAnnotation != null && ownedAnnotation != null) {
			throw new EntityException(CoreExceptionCode.ENTITY_DEFINITION,
					"Entity " + entityClass.getSimpleName() + " Cannot be owner and owned at the same time");
		}

		try {
			if (tenantAnnotation != null) {
				tenantIdFieldAddress = EntityChecker.checkAnnotationOrField(entityClass,
						EntityChecker.getFieldAddressAnnotatedWithAndCheckType(entityClass,
								EntityTenantId.class, String.class, false),
						((EntityTenant) tenantAnnotation).tenantId(), String.class, EntityTenantId.class);
				superTenantFieldAddress = EntityChecker.checkAnnotationOrField(entityClass,
						EntityChecker.getFieldAddressAnnotatedWithAndCheckType(entityClass,
								EntitySuperTenant.class, boolean.class, false),
						((EntityTenant) tenantAnnotation).superTenant(), Boolean.class,
						EntitySuperTenant.class);
			} else {
				tenantIdFieldAddress = EntityChecker.getFieldAddressAnnotatedWithAndCheckType(entityClass,
						EntityTenantId.class, String.class, true);
			}

			if (ownerAnnotation != null) {
				ownerIdFieldAddress = EntityChecker.checkAnnotationOrField(entityClass,
						GGObjectReflectionHelper.getFieldAddressAnnotatedWithAndCheckType(entityClass,
								EntityOwnerId.class, String.class),
						((EntityOwner) ownerAnnotation).ownerId(), String.class, EntityOwnerId.class);
				superOnwerIdFieldAddress = EntityChecker.checkAnnotationOrField(entityClass,
						GGObjectReflectionHelper.getFieldAddressAnnotatedWithAndCheckType(entityClass,
								EntitySuperOwner.class, boolean.class),
						((EntityOwner) ownerAnnotation).superOwner(), boolean.class, EntitySuperOwner.class);
			}

			if (ownedAnnotation != null) {
				ownerIdFieldAddress = EntityChecker.checkAnnotationOrField(entityClass,
						GGObjectReflectionHelper.getFieldAddressAnnotatedWithAndCheckType(entityClass,
								EntityOwnerId.class, String.class),
						((EntityOwned) ownedAnnotation).ownerId(), String.class, EntityOwnerId.class);
			}

			if (hiddenableAnnotation != null) {
				hiddenFieldAddress = EntityChecker.checkAnnotationOrField(entityClass,
						GGObjectReflectionHelper.getFieldAddressAnnotatedWithAndCheckType(entityClass,
								EntityHidden.class, boolean.class),
						((EntityHiddenable) hiddenableAnnotation).hidden(), boolean.class,
						EntityHidden.class);
			}

			if (geolocalizedAnnotation != null) {
				locationFieldAddress = EntityChecker.checkAnnotationOrField(entityClass,
						GGObjectReflectionHelper.getFieldAddressAnnotatedWithAndCheckType(entityClass,
								EntityLocation.class, Point.class),
						((EntityGeolocalized) geolocalizedAnnotation).location(), Point.class,
						EntityLocation.class);
			}

			if (sharedAnnotation != null) {
				shareFieldAddress = EntityChecker.checkAnnotationOrField(entityClass,
						GGObjectReflectionHelper.getFieldAddressAnnotatedWithAndCheckType(entityClass,
								EntityShare.class, String.class),
						((EntityShared) sharedAnnotation).share(), String.class, EntityShare.class);
			}
		} catch (GGReflectionException e) {
			log.atWarn().log("Error ", e);
			throw new EntityException(e);
		}

		try {
			IGGObjectQuery q = GGObjectQueryFactory.objectQuery(entityClass);
			Map<GGObjectAddress, String> output = updateAuthorizations.entrySet().stream()
					.collect(Collectors.toMap(e -> {
						try {
							return q.address(e.getKey());
						} catch (GGReflectionException e1) {
							e1.printStackTrace();
						}
						return null;
					}, Map.Entry::getValue));

			List<Pair<String, UnicityScope>> unicityFields = unicityFieldsTemp.stream().map(str -> {
				GGObjectAddress address;
				try {
					address = q.address(str);
					List<Object> struct = q.find(address);
					Field f = (Field) struct.get(struct.size() - 1);
					EntityUnicity annot = f.getAnnotation(EntityUnicity.class);
					UnicityScope scope = annot.scope();
					return new Pair<String, UnicityScope>(str, scope);
				} catch (GGReflectionException e1) {
					e1.printStackTrace();
				}
				return null;
			}).collect(Collectors.toList());

			unicityFields.addAll(EntityChecker.checkUnicitiesAnnotationPresent(entityClass));

			EntityInfos entityInfos = new EntityInfos(domain, q.address(uuidFieldAddress),
					q.address(idFieldAddress), q.address(saveProviderFieldAddress),
					q.address(deleteProviderFieldAddress), tenantAnnotation == null ? false : true,
					ownerAnnotation == null ? false : true, ownedAnnotation == null ? false : true,
					tenantIdFieldAddress == null ? null : q.address(tenantIdFieldAddress),
					superTenantFieldAddress == null ? null : q.address(superTenantFieldAddress),
					ownerIdFieldAddress == null ? null : q.address(ownerIdFieldAddress),
					superOnwerIdFieldAddress == null ? null : q.address(superOnwerIdFieldAddress),
					q.address(saveMethodAddress), q.address(deleteMethodAddress),
					publicAnnotation == null ? false : true, hiddenableAnnotation == null ? false : true,
					hiddenFieldAddress == null ? null : q.address(hiddenFieldAddress),
					geolocalizedAnnotation == null ? false : true,
					locationFieldAddress == null ? null : q.address(locationFieldAddress),
					sharedAnnotation == null ? false : true,
					shareFieldAddress == null ? null : q.address(shareFieldAddress), q.address(repositoryFieldAddress),
					q.address(engineFieldAddress), mandatoryFields.stream().map(s -> {
						try {
							return q.address(s);
						} catch (GGReflectionException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						return null;
					}).collect(Collectors.toList()), unicityFields.stream().map(p -> {
						try {
							return new Pair<GGObjectAddress, UnicityScope>(q.address(p.getValue0()),
									p.getValue1());
						} catch (GGReflectionException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						return null;
					}).collect(Collectors.toList()), afterGetm == null ? null : q.address(afterGetm),
					beforeCreatem == null ? null : q.address(beforeCreatem),
					afterCreatem == null ? null : q.address(afterCreatem),
					beforeUpdatem == null ? null : q.address(beforeUpdatem),
					afterUpdatem == null ? null : q.address(afterUpdatem),
					beforeDeletem == null ? null : q.address(beforeDeletem),
					afterDeletem == null ? null : q.address(afterDeletem), output,
					q.address(gotFromReposiotryFieldAddress));

			EntityChecker.infos.put(entityClass, entityInfos);

			return entityInfos;
		} catch (GGReflectionException e) {
			log.atWarn().log("Error ", e);
			throw new EntityException(e);
		}
	}

	private static Collection<Pair<String, UnicityScope>> checkUnicitiesAnnotationPresent(Class<?> entityClass) {
		List<Pair<String, UnicityScope>> unicities = new ArrayList<Pair<String, UnicityScope>>();
		EntityUnicities annotation = entityClass.getDeclaredAnnotation(EntityUnicities.class);

		if (annotation != null) {
			for (String unicity : annotation.unicities()) {
				unicities.add(new Pair<String, UnicityScope>(unicity, UnicityScope.tenant));
			}
		}

		return unicities;
	}

	private static Collection<String> checkMandatoriesAnnotationPresent(Class<?> entityClass) {
		List<String> mandatories = new ArrayList<String>();
		EntityMandatories annotation = entityClass.getDeclaredAnnotation(EntityMandatories.class);

		if (annotation != null) {
			String[] mandatories__ = annotation.mandatories();
			mandatories = List.of(mandatories__);
		}

		return mandatories;
	}

	private static void checkConstructor(Class<?> entityClass) throws EntityException {
		if (!entityClass.isAnnotationPresent(NoArgsConstructor.class)) {
			Constructor<?>[] constructors = entityClass.getDeclaredConstructors();

			boolean noArgsConstructorFound = false;
			for (Constructor<?> constructor : constructors) {
				if (constructor.getParameterCount() == 0
						&& !(entityClass.isLocalClass() || entityClass.isAnonymousClass())) {
					noArgsConstructorFound = true;
					break;
				}
				if (constructor.getParameterCount() == 1
						&& (entityClass.isLocalClass() || entityClass.isAnonymousClass())) {
					noArgsConstructorFound = true;
					break;
				}
			}

			if (!noArgsConstructorFound) {
				throw new EntityException(CoreExceptionCode.ENTITY_DEFINITION,
						"Entity " + entityClass.getSimpleName() + " must have at least one constructor with no args");
			}
		}
	}

	private static Map<String, String> getFieldAuthorizedForUpdate(Class<?> entityClass, Map<String, String> map) {
		for (Field field : entityClass.getDeclaredFields()) {
			if (field.isAnnotationPresent(EntityAuthorizeUpdate.class)) {
				EntityAuthorizeUpdate annotation = field.getAnnotation(EntityAuthorizeUpdate.class);
				map.put(field.getName(), annotation.authority());
			}
		}
		if (entityClass.getSuperclass() != null) {
			return EntityChecker.getFieldAuthorizedForUpdate(entityClass.getSuperclass(), map);
		} else {
			return map;
		}
	}

	public static Annotation checkIfAnnotatedEntity(Class<?> entityClass, Class<? extends Annotation> typeAnnotation) {
		Annotation annotation = entityClass.getDeclaredAnnotation(typeAnnotation);

		if (annotation == null) {
			if (entityClass.getSuperclass() != null)
				return EntityChecker.checkIfAnnotatedEntity(entityClass.getSuperclass(), typeAnnotation);
			return null;
		}

		return annotation;
	}

	public static String getMethodAnnotationAndMethodParamsHaveGoodTypes(Class<?> entityClass,
			Class<? extends Annotation> annotation, boolean throwExceptionIfNotFound, Type returnedType,
			Type... parameterTypes) throws EntityException {
		String methodAddress = null;
		try {
			methodAddress = GGObjectReflectionHelper.getMethodAddressAnnotatedWithAndCheckMethodParamsHaveGoodTypes(
					entityClass, annotation, returnedType, parameterTypes);
			if (methodAddress == null && throwExceptionIfNotFound) {
				throw new EntityException(CoreExceptionCode.ENTITY_DEFINITION,
						"Entity " + entityClass.getSimpleName() + " does not have any method annotated with @"
								+ annotation.getSimpleName());
			}
		} catch (GGReflectionException e) {
			throw new EntityException(CoreExceptionCode.ENTITY_DEFINITION, "Entity " + entityClass.getSimpleName()
					+ " does not have any method annotated with @" + annotation.getSimpleName(), e);
		}
		return methodAddress;
	}

	public static String getFieldAddressAnnotatedWithAndCheckType(Class<?> entityClass,
			Class<? extends Annotation> annotation, Type type, boolean throwExceptionIfNotFound)
			throws EntityException {
		String fieldAddress = null;
		try {
			fieldAddress = GGObjectReflectionHelper.getFieldAddressAnnotatedWithAndCheckType(entityClass, annotation,
					type);
			if (fieldAddress == null && throwExceptionIfNotFound) {
				throw new EntityException(CoreExceptionCode.ENTITY_DEFINITION,
						"Entity " + entityClass.getSimpleName() + " does not have any field annotated with @"
								+ annotation.getSimpleName());
			}
		} catch (GGReflectionException e) {
			throw new EntityException(CoreExceptionCode.ENTITY_DEFINITION, "Entity " + entityClass.getSimpleName()
					+ " does not have any field annotated with @" + annotation.getSimpleName(), e);
		}
		return fieldAddress;
	}

	private static String checkAnnotationOrField(Class<?> entityClass, String fieldAddress,
			String annotationFieldAddress, Class<?> fieldType, Class<? extends Annotation> annotationClass)
			throws EntityException {
		if (annotationFieldAddress.isEmpty() && fieldAddress == null) {
			throw new EntityException(CoreExceptionCode.ENTITY_DEFINITION, "Entity " + entityClass.getSimpleName()
					+ " does not have any field value or field annotated with " + annotationClass.getName());
		}
		if (!annotationFieldAddress.isEmpty()) {
			EntityChecker.checkFieldExistsAndIsOfType(entityClass, annotationFieldAddress, fieldType);
		}
		return annotationFieldAddress.isEmpty() ? fieldAddress : annotationFieldAddress;
	}

	private static String checkFieldExistsAndIsOfType(Class<?> entityClass, String fieldAddress, Class<?> fieldType)
			throws EntityException {
		try {

			List<Object> obj = GGObjectQueryFactory.objectQuery(entityClass)
					.find(GGObjectQueryFactory.objectQuery(entityClass).address(fieldAddress));

			Field field = (Field) obj.get(obj.size() - 1);
			if (!field.getType().equals(fieldType)) {
				throw new EntityException(CoreExceptionCode.ENTITY_DEFINITION,
						"Entity " + entityClass.getSimpleName() + " has field " + fieldAddress + " with wrong type "
								+ field.getType() + ", should be " + fieldType);
			} else {
				return fieldAddress;
			}
		} catch (GGReflectionException e) {
			throw new EntityException(CoreExceptionCode.ENTITY_DEFINITION,
					"Entity " + entityClass.getSimpleName() + " does not have field " + fieldAddress);
		}
	}

	private static String checkDomainInAnnotation(Entity annotation, Class<?> entityClass)
			throws EntityException {
		if (annotation.domain() == null || annotation.domain().isEmpty()) {
			throw new EntityException(CoreExceptionCode.ENTITY_DEFINITION,
					"No domain provided in annotation of entity " + entityClass.getSimpleName());
		} else {
			return annotation.domain();
		}
	}

	public static EntityInfos checkEntity(Object entity) throws CoreException {
		return EntityChecker.checkEntityClass(entity.getClass());
	}
}
