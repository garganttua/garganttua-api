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

import com.garganttua.api.core.entity.exceptions.GGAPIEntityException;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.caller.IGGAPICaller;
import com.garganttua.api.spec.engine.IGGAPIEngine;
import com.garganttua.api.spec.entity.GGAPIEntityInfos;
import com.garganttua.api.spec.entity.IGGAPIEntityDeleteMethod;
import com.garganttua.api.spec.entity.IGGAPIEntitySaveMethod;
import com.garganttua.api.spec.entity.annotations.GGAPIBusinessAnnotations.GGAPIEntityAfterCreate;
import com.garganttua.api.spec.entity.annotations.GGAPIBusinessAnnotations.GGAPIEntityAfterDelete;
import com.garganttua.api.spec.entity.annotations.GGAPIBusinessAnnotations.GGAPIEntityAfterGet;
import com.garganttua.api.spec.entity.annotations.GGAPIBusinessAnnotations.GGAPIEntityAfterUpdate;
import com.garganttua.api.spec.entity.annotations.GGAPIBusinessAnnotations.GGAPIEntityBeforeCreate;
import com.garganttua.api.spec.entity.annotations.GGAPIBusinessAnnotations.GGAPIEntityBeforeDelete;
import com.garganttua.api.spec.entity.annotations.GGAPIBusinessAnnotations.GGAPIEntityBeforeUpdate;
import com.garganttua.api.spec.entity.annotations.GGAPIEntity;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityAuthorizeUpdate;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityDeleteMethod;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityDeleteMethodProvider;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityEngine;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityGeolocalized;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityGotFromRepository;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityHidden;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityHiddenable;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityId;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityLocation;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityMandatories;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityMandatory;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityOwned;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityOwner;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityOwnerId;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityPublic;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityRepository;
import com.garganttua.api.spec.entity.annotations.GGAPIEntitySaveMethod;
import com.garganttua.api.spec.entity.annotations.GGAPIEntitySaveMethodProvider;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityShare;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityShared;
import com.garganttua.api.spec.entity.annotations.GGAPIEntitySuperOwner;
import com.garganttua.api.spec.entity.annotations.GGAPIEntitySuperTenant;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityTenant;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityTenantId;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityUnicities;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityUnicity;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityUuid;
import com.garganttua.api.spec.repository.IGGAPIRepository;
import com.garganttua.reflection.GGObjectAddress;
import com.garganttua.reflection.GGReflectionException;
import com.garganttua.reflection.query.GGObjectQueryFactory;
import com.garganttua.reflection.query.IGGObjectQuery;
import com.garganttua.reflection.utils.GGObjectReflectionHelper;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GGAPIEntityChecker {

	private static Map<Class<?>, GGAPIEntityInfos> infos = new HashMap<Class<?>, GGAPIEntityInfos>();

	public static GGAPIEntityInfos checkEntityClass(Class<?> entityClass) throws GGAPIException {
		if (GGAPIEntityChecker.infos.containsKey(entityClass)) {
			return GGAPIEntityChecker.infos.get(entityClass);
		}

		if (log.isDebugEnabled()) {
			log.debug("Checking entity infos from class " + entityClass.getName());
		}

		GGAPIEntity annotation = entityClass.getDeclaredAnnotation(GGAPIEntity.class);

		if (annotation == null) {
			throw new GGAPIEntityException(GGAPIExceptionCode.ENTITY_DEFINITION,
					"Entity " + entityClass.getSimpleName() + " is not annotated with @GGAPIEntity");
		} 

		//Check one constructor with no parameter
		GGAPIEntityChecker.checkConstructor(entityClass);

		//Mandatory Fields and Methods
		String domain = GGAPIEntityChecker.checkDomainInAnnotation(annotation, entityClass);
		String uuidFieldAddress = GGAPIEntityChecker.getFieldAddressAnnotatedWithAndCheckType(entityClass, GGAPIEntityUuid.class, String.class, true);
		String idFieldAddress = GGAPIEntityChecker.getFieldAddressAnnotatedWithAndCheckType(entityClass, GGAPIEntityId.class, String.class, true);
		String saveProviderFieldAddress = GGAPIEntityChecker.getFieldAddressAnnotatedWithAndCheckType(entityClass, GGAPIEntitySaveMethodProvider.class, IGGAPIEntitySaveMethod.class, true);
		String deleteProviderFieldAddress = GGAPIEntityChecker.getFieldAddressAnnotatedWithAndCheckType(entityClass, GGAPIEntityDeleteMethodProvider.class, IGGAPIEntityDeleteMethod.class, true);
		String saveMethodAddress = GGAPIEntityChecker.getMethodAnnotationAndMethodParamsHaveGoodTypes(entityClass, GGAPIEntitySaveMethod.class, true, Object.class, IGGAPICaller.class, GGObjectReflectionHelper.getParameterizedType(Map.class, String.class, String.class));
		String deleteMethodAddress = GGAPIEntityChecker.getMethodAnnotationAndMethodParamsHaveGoodTypes(entityClass, GGAPIEntityDeleteMethod.class, true, void.class, IGGAPICaller.class, GGObjectReflectionHelper.getParameterizedType(Map.class, String.class, String.class));
		String repositoryFieldAddress = GGAPIEntityChecker.getFieldAddressAnnotatedWithAndCheckType(entityClass, GGAPIEntityRepository.class, IGGAPIRepository.class, true);
		String engineFieldAddress = GGAPIEntityChecker.getFieldAddressAnnotatedWithAndCheckType(entityClass, GGAPIEntityEngine.class, IGGAPIEngine.class, true);
		String gotFromReposiotryFieldAddress = GGAPIEntityChecker.getFieldAddressAnnotatedWithAndCheckType(entityClass, GGAPIEntityGotFromRepository.class, boolean.class, true);
		String tenantIdFieldAddress = null;
		
		//Entity Optional Annotations
		Annotation tenantAnnotation = GGAPIEntityChecker.checkIfAnnotatedEntity(entityClass, GGAPIEntityTenant.class);
		Annotation ownerAnnotation = GGAPIEntityChecker.checkIfAnnotatedEntity(entityClass, GGAPIEntityOwner.class);
		Annotation ownedAnnotation = GGAPIEntityChecker.checkIfAnnotatedEntity(entityClass, GGAPIEntityOwned.class);
		Annotation publicAnnotation = GGAPIEntityChecker.checkIfAnnotatedEntity(entityClass, GGAPIEntityPublic.class);
		Annotation hiddenableAnnotation = GGAPIEntityChecker.checkIfAnnotatedEntity(entityClass, GGAPIEntityHiddenable.class);
		Annotation geolocalizedAnnotation = GGAPIEntityChecker.checkIfAnnotatedEntity(entityClass, GGAPIEntityGeolocalized.class);
		Annotation sharedAnnotation = GGAPIEntityChecker.checkIfAnnotatedEntity(entityClass, GGAPIEntityShared.class);
		
		String superTenantFieldAddress = null;
		String ownerIdFieldAddress = null;
		String superOnwerIdFieldAddress = null;
		String hiddenFieldAddress = null;
		String locationFieldAddress = null;
		String shareFieldAddress = null;
		
		String afterGetm = GGAPIEntityChecker.getMethodAnnotationAndMethodParamsHaveGoodTypes(entityClass, GGAPIEntityAfterGet.class, false, void.class, IGGAPICaller.class, GGObjectReflectionHelper.getParameterizedType(Map.class, String.class, String.class));
		String beforeCreatem = GGAPIEntityChecker.getMethodAnnotationAndMethodParamsHaveGoodTypes(entityClass, GGAPIEntityBeforeCreate.class, false, void.class, IGGAPICaller.class, GGObjectReflectionHelper.getParameterizedType(Map.class, String.class, String.class));
		String afterCreatem = GGAPIEntityChecker.getMethodAnnotationAndMethodParamsHaveGoodTypes(entityClass, GGAPIEntityAfterCreate.class, false, void.class, IGGAPICaller.class, GGObjectReflectionHelper.getParameterizedType(Map.class, String.class, String.class));
		String beforeUpdatem = GGAPIEntityChecker.getMethodAnnotationAndMethodParamsHaveGoodTypes(entityClass, GGAPIEntityBeforeUpdate.class, false, void.class, IGGAPICaller.class, GGObjectReflectionHelper.getParameterizedType(Map.class, String.class, String.class));
		String afterUpdatem = GGAPIEntityChecker.getMethodAnnotationAndMethodParamsHaveGoodTypes(entityClass, GGAPIEntityAfterUpdate.class, false, void.class, IGGAPICaller.class, GGObjectReflectionHelper.getParameterizedType(Map.class, String.class, String.class));
		String beforeDeletem = GGAPIEntityChecker.getMethodAnnotationAndMethodParamsHaveGoodTypes(entityClass, GGAPIEntityBeforeDelete.class, false, void.class, IGGAPICaller.class, GGObjectReflectionHelper.getParameterizedType(Map.class, String.class, String.class));
		String afterDeletem = GGAPIEntityChecker.getMethodAnnotationAndMethodParamsHaveGoodTypes(entityClass, GGAPIEntityAfterDelete.class, false, void.class, IGGAPICaller.class, GGObjectReflectionHelper.getParameterizedType(Map.class, String.class, String.class));
		
		Map<String, String> updateAuthorizations = GGAPIEntityChecker.getFieldAuthorizedForUpdate(entityClass, new HashMap<String, String>());
		
		List<String> mandatoryFields = GGObjectReflectionHelper.getFieldAddressesWithAnnotation(entityClass, GGAPIEntityMandatory.class, true);
		mandatoryFields.addAll(GGAPIEntityChecker.checkMandatoriesAnnotationPresent(entityClass));
		
		List<String> unicityFields = GGObjectReflectionHelper.getFieldAddressesWithAnnotation(entityClass, GGAPIEntityUnicity.class, false);
		unicityFields.addAll(GGAPIEntityChecker.checkUnicitiesAnnotationPresent(entityClass));
		
		if (ownerAnnotation != null && ownedAnnotation != null) {
			throw new GGAPIEntityException(GGAPIExceptionCode.ENTITY_DEFINITION,
					"Entity " + entityClass.getSimpleName() + " Cannot be owner and owned at the same time");
		}

		try {
			if (tenantAnnotation != null) {
				tenantIdFieldAddress = GGAPIEntityChecker.checkAnnotationOrField(entityClass,
						GGAPIEntityChecker.getFieldAddressAnnotatedWithAndCheckType(entityClass, GGAPIEntityTenantId.class,
								String.class, false),
						((GGAPIEntityTenant) tenantAnnotation).tenantId(), String.class, GGAPIEntityTenantId.class);
				superTenantFieldAddress = GGAPIEntityChecker.checkAnnotationOrField(entityClass,
						GGAPIEntityChecker.getFieldAddressAnnotatedWithAndCheckType(entityClass,
								GGAPIEntitySuperTenant.class, boolean.class, false),
						((GGAPIEntityTenant) tenantAnnotation).superTenant(), Boolean.class, GGAPIEntitySuperTenant.class);
			} else {
				tenantIdFieldAddress = GGAPIEntityChecker.getFieldAddressAnnotatedWithAndCheckType(entityClass,
						GGAPIEntityTenantId.class, String.class, true);
			}
	
			if (ownerAnnotation != null) {
				ownerIdFieldAddress = GGAPIEntityChecker.checkAnnotationOrField(entityClass,
						GGObjectReflectionHelper.getFieldAddressAnnotatedWithAndCheckType(entityClass, GGAPIEntityOwnerId.class,
								String.class),
						((GGAPIEntityOwner) ownerAnnotation).ownerId(), String.class, GGAPIEntityOwnerId.class);
				superOnwerIdFieldAddress = GGAPIEntityChecker.checkAnnotationOrField(entityClass,
						GGObjectReflectionHelper.getFieldAddressAnnotatedWithAndCheckType(entityClass,
								GGAPIEntitySuperOwner.class, boolean.class),
						((GGAPIEntityOwner) ownerAnnotation).superOwner(), boolean.class, GGAPIEntitySuperOwner.class);
			}
	
			if (ownedAnnotation != null) {
				ownerIdFieldAddress = GGAPIEntityChecker.checkAnnotationOrField(entityClass,
						GGObjectReflectionHelper.getFieldAddressAnnotatedWithAndCheckType(entityClass, GGAPIEntityOwnerId.class,
								String.class),
						((GGAPIEntityOwned) ownedAnnotation).ownerId(), String.class, GGAPIEntityOwnerId.class);
			}
	
			if (hiddenableAnnotation != null) {
				hiddenFieldAddress = GGAPIEntityChecker.checkAnnotationOrField(entityClass,
						GGObjectReflectionHelper.getFieldAddressAnnotatedWithAndCheckType(entityClass, GGAPIEntityHidden.class,
								boolean.class),
						((GGAPIEntityHiddenable) hiddenableAnnotation).hidden(), boolean.class, GGAPIEntityHidden.class);
			}
	
			if (geolocalizedAnnotation != null) {
				locationFieldAddress = GGAPIEntityChecker.checkAnnotationOrField(entityClass,
						GGObjectReflectionHelper.getFieldAddressAnnotatedWithAndCheckType(entityClass, GGAPIEntityLocation.class,
								Point.class),
						((GGAPIEntityGeolocalized) geolocalizedAnnotation).location(), Point.class,
						GGAPIEntityLocation.class);
			}
	
			if (sharedAnnotation != null) {
				shareFieldAddress = GGAPIEntityChecker.checkAnnotationOrField(entityClass,
						GGObjectReflectionHelper.getFieldAddressAnnotatedWithAndCheckType(entityClass, GGAPIEntityShare.class,
								String.class),
						((GGAPIEntityShared) sharedAnnotation).share(), String.class, GGAPIEntityShare.class);
			}
		} catch(GGReflectionException e) {
			log.atWarn().log("Error ", e);
			throw new GGAPIEntityException(e);
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

			GGAPIEntityInfos entityInfos = new GGAPIEntityInfos(domain, q.address(uuidFieldAddress),
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
					q.address(engineFieldAddress), 
					mandatoryFields.stream().map(s -> {
						try {
							return q.address(s);
						} catch (GGReflectionException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						return null;
					}).collect(Collectors.toList()),
					unicityFields.stream().map(s -> {
						try {
							return q.address(s);
						} catch (GGReflectionException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						return null;
					}).collect(Collectors.toList()),
					afterGetm == null ? null : q.address(afterGetm),
					beforeCreatem == null ? null : q.address(beforeCreatem),
					afterCreatem == null ? null : q.address(afterCreatem),
					beforeUpdatem == null ? null : q.address(beforeUpdatem),
					afterUpdatem == null ? null : q.address(afterUpdatem),
					beforeDeletem == null ? null : q.address(beforeDeletem),
					afterDeletem == null ? null : q.address(afterDeletem), 
					output,
					q.address(gotFromReposiotryFieldAddress));

			GGAPIEntityChecker.infos.put(entityClass, entityInfos);

			return entityInfos;
		} catch (GGReflectionException e) {
			log.atWarn().log("Error ", e);
			throw new GGAPIEntityException(e);
		}
	}

	private static Collection<String> checkUnicitiesAnnotationPresent(Class<?> entityClass) {
		List<String> unicities = new ArrayList<String>();
		GGAPIEntityUnicities annotation = entityClass.getDeclaredAnnotation(GGAPIEntityUnicities.class);

		if (annotation != null) {
			String[] unicities__ = annotation.unicities();
			unicities = List.of(unicities__);
		}

		return unicities;
	}

	private static Collection<String> checkMandatoriesAnnotationPresent(Class<?> entityClass) {
		List<String> mandatories = new ArrayList<String>();
		GGAPIEntityMandatories annotation = entityClass.getDeclaredAnnotation(GGAPIEntityMandatories.class);

		if (annotation != null) {
			String[] mandatories__ = annotation.mandatories();
			mandatories = List.of(mandatories__);
		}

		return mandatories;
	}

	private static void checkConstructor(Class<?> entityClass) throws GGAPIEntityException {
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
				throw new GGAPIEntityException(GGAPIExceptionCode.ENTITY_DEFINITION,
						"Entity " + entityClass.getSimpleName() + " must have at least one constructor with no args");
			}
		}
	}

	private static Map<String, String> getFieldAuthorizedForUpdate(Class<?> entityClass, Map<String, String> map) {
		for (Field field : entityClass.getDeclaredFields()) {
			if (field.isAnnotationPresent(GGAPIEntityAuthorizeUpdate.class)) {
				GGAPIEntityAuthorizeUpdate annotation = field.getAnnotation(GGAPIEntityAuthorizeUpdate.class);
				map.put(field.getName(), annotation.authority());
			}
		}
		if (entityClass.getSuperclass() != null) {
			return GGAPIEntityChecker.getFieldAuthorizedForUpdate(entityClass.getSuperclass(), map);
		} else {
			return map;
		}
	}

	public static Annotation checkIfAnnotatedEntity(Class<?> entityClass, Class<? extends Annotation> typeAnnotation) {
		Annotation annotation = entityClass.getDeclaredAnnotation(typeAnnotation);

		if (annotation == null) {
			if (entityClass.getSuperclass() != null)
				return GGAPIEntityChecker.checkIfAnnotatedEntity(entityClass.getSuperclass(), typeAnnotation);
			return null;
		}

		return annotation;
	}

	public static String getMethodAnnotationAndMethodParamsHaveGoodTypes(Class<?> entityClass, Class<? extends Annotation> annotation, boolean throwExceptionIfNotFound, Type returnedType, Type ...parameterTypes)
			throws GGAPIEntityException {
		String methodAddress = null;
		try {
			methodAddress = GGObjectReflectionHelper.getMethodAddressAnnotatedWithAndCheckMethodParamsHaveGoodTypes(entityClass, annotation, returnedType,
					parameterTypes);
			if( methodAddress == null && throwExceptionIfNotFound) {
				throw new GGAPIEntityException(GGAPIExceptionCode.ENTITY_DEFINITION, "Entity " + entityClass.getSimpleName()
				+ " does not have any method annotated with @"+annotation.getSimpleName());
			}
		} catch (GGReflectionException e) {
			throw new GGAPIEntityException(GGAPIExceptionCode.ENTITY_DEFINITION, "Entity " + entityClass.getSimpleName()
			+ " does not have any method annotated with @"+annotation.getSimpleName(), e);
		}
		return methodAddress;
	}
	
	public static String getFieldAddressAnnotatedWithAndCheckType(Class<?> entityClass, Class<? extends Annotation> annotation, Type type, boolean throwExceptionIfNotFound) throws GGAPIEntityException {
		String fieldAddress = null;
		try {
			fieldAddress = GGObjectReflectionHelper.getFieldAddressAnnotatedWithAndCheckType(entityClass,
					annotation, type);
			if( fieldAddress == null && throwExceptionIfNotFound) {
				throw new GGAPIEntityException(GGAPIExceptionCode.ENTITY_DEFINITION, "Entity " + entityClass.getSimpleName()
				+ " does not have any field annotated with @"+annotation.getSimpleName());
			}
		} catch (GGReflectionException e) {
			throw new GGAPIEntityException(GGAPIExceptionCode.ENTITY_DEFINITION, "Entity " + entityClass.getSimpleName()
					+ " does not have any field annotated with @"+annotation.getSimpleName(), e);
		}
		return fieldAddress;
	}

	private static String checkAnnotationOrField(Class<?> entityClass, String fieldAddress, String annotationFieldAddress, Class<?> fieldType, Class<? extends Annotation> annotationClass)
			throws GGAPIEntityException {
		if( annotationFieldAddress.isEmpty() && fieldAddress == null ) {
			throw new GGAPIEntityException(GGAPIExceptionCode.ENTITY_DEFINITION, "Entity "+entityClass.getSimpleName()+" does not have any field value or field annotated with "+annotationClass.getName());
		}
		if( !annotationFieldAddress.isEmpty() ) {
			GGAPIEntityChecker.checkFieldExistsAndIsOfType(entityClass, annotationFieldAddress, fieldType);
		}
		return annotationFieldAddress.isEmpty()?fieldAddress:annotationFieldAddress;
	}
	
	private static String checkFieldExistsAndIsOfType(Class<?> entityClass, String fieldAddress, Class<?> fieldType) throws GGAPIEntityException {
		try {
			
			List<Object> obj = GGObjectQueryFactory.objectQuery(entityClass).find(GGObjectQueryFactory.objectQuery(entityClass).address(fieldAddress));

			Field field = (Field) obj.get(obj.size()-1);
			if( !field.getType().equals(fieldType) ) {
				throw new GGAPIEntityException(GGAPIExceptionCode.ENTITY_DEFINITION, "Entity "+entityClass.getSimpleName()+" has field "+fieldAddress+" with wrong type "+field.getType()+", should be "+fieldType);
			} else {
				return fieldAddress;
			}
		} catch ( GGReflectionException e) {
			throw new GGAPIEntityException(GGAPIExceptionCode.ENTITY_DEFINITION, "Entity "+entityClass.getSimpleName()+" does not have field "+fieldAddress);
		}
	}

	private static String checkDomainInAnnotation(GGAPIEntity annotation, Class<?> entityClass)
			throws GGAPIEntityException {
		if (annotation.domain() == null || annotation.domain().isEmpty()) {
			throw new GGAPIEntityException(GGAPIExceptionCode.ENTITY_DEFINITION,
					"No domain provided in annotation of entity " + entityClass.getSimpleName());
		} else {
			return annotation.domain();
		}
	}

	public static GGAPIEntityInfos checkEntity(Object entity) throws GGAPIException {
		return GGAPIEntityChecker.checkEntityClass(entity.getClass());
	}
}
