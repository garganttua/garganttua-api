package com.garganttua.api.core.entity.checker;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
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

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GGAPIEntityChecker {
	
	private static Map<Class<?>, GGAPIEntityInfos> infos = new HashMap<Class<?>, GGAPIEntityInfos>();
	
	public static GGAPIEntityInfos checkEntityClass(Class<?> entityClass) throws GGAPIException {
		if( GGAPIEntityChecker.infos.containsKey(entityClass) ) {
			return GGAPIEntityChecker.infos.get(entityClass);  
		}

		if (log.isDebugEnabled()) {
			log.debug("Checking entity infos from class " + entityClass.getName());
		}
			
		GGAPIEntity annotation = entityClass.getDeclaredAnnotation(GGAPIEntity.class);
		
		if( annotation == null ) {
			throw new GGAPIEntityException(GGAPIExceptionCode.ENTITY_DEFINITION,
					"Entity " + entityClass.getSimpleName() + " is not annotated with @GGAPIEntity");
		}
		
		String domain = GGAPIEntityChecker.checkDomainInAnnotation(annotation, entityClass);
		String uuidFieldAddress = GGAPIEntityChecker.checkUuidAnnotationPresentAndFieldHasGoodType(entityClass);
		String idFieldAddress = GGAPIEntityChecker.checkIdAnnotationPresentAndFieldHasGoodType(entityClass);
		String saveProviderFieldAddress = GGAPIEntityChecker.checkSaveProviderAnnotationPresentAndFieldHasGoodType(entityClass);
		String deleteProviderFieldAddress = GGAPIEntityChecker.checkDeleteProviderAnnotationPresentAndFieldHasGoodType(entityClass);
		Annotation tenantAnnotation = GGAPIEntityChecker.checkIfAnnotatedEntity(entityClass, GGAPIEntityTenant.class);
		String tenantIdFieldAddress = null;
		String superTenantFieldAddress = null;
		
		if( tenantAnnotation != null ) {
			tenantIdFieldAddress = GGAPIEntityChecker.checkAnnotationOrField(entityClass, GGAPIEntityChecker.getFieldAddressAnnotatedWithAndCheckType(entityClass, GGAPIEntityTenantId.class, String.class), ((GGAPIEntityTenant) tenantAnnotation).tenantId(), String.class, GGAPIEntityTenantId.class);
			superTenantFieldAddress = GGAPIEntityChecker.checkAnnotationOrField(entityClass, GGAPIEntityChecker.getFieldAddressAnnotatedWithAndCheckType(entityClass, GGAPIEntitySuperTenant.class, boolean.class), ((GGAPIEntityTenant) tenantAnnotation).superTenant(), Boolean.class, GGAPIEntitySuperTenant.class);
		} else {
			tenantIdFieldAddress = GGAPIEntityChecker.getFieldAddressAnnotatedWithAndCheckType(entityClass, GGAPIEntityTenantId.class, String.class);
		}
		
		Annotation ownerAnnotation = GGAPIEntityChecker.checkIfAnnotatedEntity(entityClass, GGAPIEntityOwner.class);
		Annotation ownedAnnotation = GGAPIEntityChecker.checkIfAnnotatedEntity(entityClass, GGAPIEntityOwned.class);
		
		if(ownerAnnotation != null && ownedAnnotation != null) {
			throw new GGAPIEntityException(GGAPIExceptionCode.ENTITY_DEFINITION, "Entity "+entityClass.getSimpleName()+" Cannot be owner and owned at the same time");
		}
		
		String ownerIdFieldAddress = null;
		String superOnwerIdFieldAddress = null;
		if( ownerAnnotation != null ) {
			ownerIdFieldAddress = GGAPIEntityChecker.checkAnnotationOrField(entityClass, GGAPIEntityChecker.getFieldAddressAnnotatedWithAndCheckType(entityClass, GGAPIEntityOwnerId.class, String.class), ((GGAPIEntityOwner) ownerAnnotation).ownerId(), String.class, GGAPIEntityOwnerId.class);
			superOnwerIdFieldAddress = GGAPIEntityChecker.checkAnnotationOrField(entityClass, GGAPIEntityChecker.getFieldAddressAnnotatedWithAndCheckType(entityClass, GGAPIEntitySuperOwner.class, boolean.class), ((GGAPIEntityOwner) ownerAnnotation).superOwner(), boolean.class, GGAPIEntitySuperOwner.class);	
		}
		
		if( ownedAnnotation != null ) {
			ownerIdFieldAddress = GGAPIEntityChecker.checkAnnotationOrField(entityClass, GGAPIEntityChecker.getFieldAddressAnnotatedWithAndCheckType(entityClass, GGAPIEntityOwnerId.class, String.class), ((GGAPIEntityOwned) ownedAnnotation).ownerId(), String.class, GGAPIEntityOwnerId.class);
		}
		
		String saveMethodAddress = GGAPIEntityChecker.checkSaveMethodAnnotationAndMethodParamsHaveGoodTypes(entityClass);
		String deleteMethodAddress = GGAPIEntityChecker.checkDeleteMethodAnnotationAndMethodParamsHaveGoodTypes(entityClass);
		
		Annotation publicAnnotation = GGAPIEntityChecker.checkIfAnnotatedEntity(entityClass, GGAPIEntityPublic.class);
		
		Annotation hiddenableAnnotation = GGAPIEntityChecker.checkIfAnnotatedEntity(entityClass, GGAPIEntityHiddenable.class);
		String hiddenFieldAddress = null;
		if( hiddenableAnnotation != null ) {
			hiddenFieldAddress = GGAPIEntityChecker.checkAnnotationOrField(entityClass, GGAPIEntityChecker.getFieldAddressAnnotatedWithAndCheckType(entityClass, GGAPIEntityHidden.class, boolean.class), ((GGAPIEntityHiddenable) hiddenableAnnotation).hidden(), boolean.class, GGAPIEntityHidden.class);
		}
		
		Annotation geolocalizedAnnotation = GGAPIEntityChecker.checkIfAnnotatedEntity(entityClass, GGAPIEntityGeolocalized.class);
		String locationFieldAddress = null;
		if( geolocalizedAnnotation != null ) {
			locationFieldAddress = GGAPIEntityChecker.checkAnnotationOrField(entityClass, GGAPIEntityChecker.getFieldAddressAnnotatedWithAndCheckType(entityClass, GGAPIEntityLocation.class, Point.class), ((GGAPIEntityGeolocalized) geolocalizedAnnotation).location(), Point.class, GGAPIEntityLocation.class);
		}
		
		Annotation sharedAnnotation = GGAPIEntityChecker.checkIfAnnotatedEntity(entityClass, GGAPIEntityShared.class);
		String shareFieldAddress = null;
		if( sharedAnnotation != null ) {
			shareFieldAddress = GGAPIEntityChecker.checkAnnotationOrField(entityClass, GGAPIEntityChecker.getFieldAddressAnnotatedWithAndCheckType(entityClass, GGAPIEntityShare.class, String.class), ((GGAPIEntityShared) sharedAnnotation).share(), String.class, GGAPIEntityShare.class);
		}
		
		String repositoryFieldAddress = GGAPIEntityChecker.checkRepositoryAnnotationPresentAndFieldHasGoodType(entityClass);
		String engineFieldAddress = GGAPIEntityChecker.checkEngineAnnotationPresentAndFieldHasGoodType(entityClass);
		
		List<String> mandatoryFields = new ArrayList<String>();
		mandatoryFields = GGAPIEntityChecker.getFieldsWithAnnotation(mandatoryFields, entityClass, GGAPIEntityMandatory.class);
		mandatoryFields.addAll(GGAPIEntityChecker.checkMandatoriesAnnotationPresent(entityClass));
		
		List<String> unicityFields = new ArrayList<String>();
		unicityFields = GGAPIEntityChecker.getFieldsWithAnnotation(unicityFields, entityClass, GGAPIEntityUnicity.class);
		unicityFields.addAll(GGAPIEntityChecker.checkUnicitiesAnnotationPresent(entityClass));
		
		Method afterGetm = GGAPIEntityChecker.hasAnnotation(entityClass, GGAPIEntityAfterGet.class);
		Method beforeCreatem = GGAPIEntityChecker.hasAnnotation(entityClass, GGAPIEntityBeforeCreate.class);
		Method afterCreatem = GGAPIEntityChecker.hasAnnotation(entityClass, GGAPIEntityAfterCreate.class);
		Method beforeUpdatem = GGAPIEntityChecker.hasAnnotation(entityClass, GGAPIEntityBeforeUpdate.class);
		Method afterUpdatem = GGAPIEntityChecker.hasAnnotation(entityClass, GGAPIEntityAfterUpdate.class);
		Method beforeDeletem = GGAPIEntityChecker.hasAnnotation(entityClass, GGAPIEntityBeforeDelete.class);
		Method afterDeletem = GGAPIEntityChecker.hasAnnotation(entityClass, GGAPIEntityAfterDelete.class);

		Map<String, String> updateAuthorizations = new HashMap<String, String>();	
		updateAuthorizations = GGAPIEntityChecker.getFieldAuthorizedForUpdate(entityClass, updateAuthorizations);

		String gotFromReposiotryFieldAddress = GGAPIEntityChecker.checkGotFromRepositoryAnnotationPresentAndFieldHasGoodType(entityClass);;
		
		GGAPIEntityChecker.checkConstructor(entityClass);

		try {
			IGGObjectQuery q = GGObjectQueryFactory.objectQuery(entityClass);
			Map<GGObjectAddress, String> output = updateAuthorizations.entrySet().stream()
	        .collect(Collectors.toMap(
	                e -> {
						try {
							return q.address(e.getKey());
						} catch (GGReflectionException e1) {
							e1.printStackTrace();
						}
						return null;
					},
	                Map.Entry::getValue)
	        );
			
			GGAPIEntityInfos entityInfos = new GGAPIEntityInfos (
					domain, 
					q.address(uuidFieldAddress), 
					q.address(idFieldAddress), 
					q.address(saveProviderFieldAddress), 
					q.address(deleteProviderFieldAddress), 
					tenantAnnotation==null?false:true, 
					ownerAnnotation==null?false:true, 
					ownedAnnotation==null?false:true, 
					tenantIdFieldAddress==null?null:q.address(tenantIdFieldAddress),
					superTenantFieldAddress==null?null:q.address(superTenantFieldAddress),
					ownerIdFieldAddress==null?null:q.address(ownerIdFieldAddress),
					superOnwerIdFieldAddress==null?null:q.address(superOnwerIdFieldAddress),
					q.address(saveMethodAddress), 
					q.address(deleteMethodAddress), 
					publicAnnotation==null?false:true, 
					hiddenableAnnotation==null?false:true, 
					hiddenFieldAddress==null?null:q.address(hiddenFieldAddress), 
					geolocalizedAnnotation==null?false:true, 
					locationFieldAddress==null?null:q.address(locationFieldAddress), 
					sharedAnnotation==null?false:true, 
					shareFieldAddress==null?null:q.address(shareFieldAddress), 
					q.address(repositoryFieldAddress), 
					q.address(engineFieldAddress), 
					mandatoryFields, 
					unicityFields,
					afterGetm==null?null:q.address(afterGetm.getName()),
					beforeCreatem==null?null:q.address(beforeCreatem.getName()),
					afterCreatem==null?null:q.address(afterCreatem.getName()),
					beforeUpdatem==null?null:q.address(beforeUpdatem.getName()),
					afterUpdatem==null?null:q.address(afterUpdatem.getName()),
					beforeDeletem==null?null:q.address(beforeDeletem.getName()),
					afterDeletem==null?null:q.address(afterDeletem.getName()),
					output,
					q.address(gotFromReposiotryFieldAddress));
			
			GGAPIEntityChecker.infos.put(entityClass, entityInfos);
			
			return entityInfos;
		} catch (GGReflectionException e) {
			e.printStackTrace();
			throw new GGAPIEntityException(e);
		}
	}

	private static Collection<String> checkUnicitiesAnnotationPresent(Class<?> entityClass) {
		List<String> unicities = new ArrayList<String>();
		GGAPIEntityUnicities annotation = entityClass.getDeclaredAnnotation(GGAPIEntityUnicities.class);
		
		if( annotation != null ) {
			String[] unicities__ = annotation.unicities();
			unicities = List.of(unicities__);
		}
		
		return unicities;
	}

	private static Collection<String> checkMandatoriesAnnotationPresent(Class<?> entityClass) {
		List<String> mandatories = new ArrayList<String>();
		GGAPIEntityMandatories annotation = entityClass.getDeclaredAnnotation(GGAPIEntityMandatories.class);
		
		if( annotation != null ) {
			String[] mandatories__ = annotation.mandatories();
			mandatories = List.of(mandatories__);
		}
		
		return mandatories;
	}

	private static String checkGotFromRepositoryAnnotationPresentAndFieldHasGoodType(Class<?> entityClass) throws GGAPIEntityException {
		String fieldAddress = GGAPIEntityChecker.getFieldAddressAnnotatedWithAndCheckType(entityClass, GGAPIEntityGotFromRepository.class, boolean.class);
		if( fieldAddress == null ) {
			throw new GGAPIEntityException(GGAPIExceptionCode.ENTITY_DEFINITION, "Entity "+entityClass.getSimpleName()+" does not have any field annotated with @GGAPIEntityGotFromRepository");
		}
		return fieldAddress; 
	}

	private static void checkConstructor(Class<?> entityClass) throws GGAPIEntityException {
		if( !entityClass.isAnnotationPresent(NoArgsConstructor.class) ) {
		    Constructor<?>[] constructors = entityClass.getDeclaredConstructors();

		    boolean noArgsConstructorFound = false;
		    for (Constructor<?> constructor : constructors) {
		        if (constructor.getParameterCount() == 0  && !(entityClass.isLocalClass() || entityClass.isAnonymousClass())) {
		            noArgsConstructorFound = true;
		            break;
		        }
		        if (constructor.getParameterCount() == 1  && (entityClass.isLocalClass() || entityClass.isAnonymousClass())) {
		            noArgsConstructorFound = true;
		            break;
		        }
		    }

		    if (!noArgsConstructorFound) {
		        throw new GGAPIEntityException(GGAPIExceptionCode.ENTITY_DEFINITION, "Entity " + entityClass.getSimpleName() + " must have at least one constructor with no args");
		    }
		}
	}

	private static Map<String, String> getFieldAuthorizedForUpdate(Class<?> entityClass, Map<String, String> map) {
		for( Field field: entityClass.getDeclaredFields() ) {
			if( field.isAnnotationPresent(GGAPIEntityAuthorizeUpdate.class) ) {
				GGAPIEntityAuthorizeUpdate annotation = field.getAnnotation(GGAPIEntityAuthorizeUpdate.class);
				map.put(field.getName(), annotation.authority());
			}
		}
		if( entityClass.getSuperclass() != null ) {
			return GGAPIEntityChecker.getFieldAuthorizedForUpdate(entityClass.getSuperclass(), map);
		} else {
			return map;
		}
	}

	private static List<String> getFieldsWithAnnotation(List<String> fields, Class<?> entityClass, Class<? extends Annotation> annotation) {
		
		for( Field field: entityClass.getDeclaredFields() ) {
			if( field.isAnnotationPresent(annotation) ) {
				String Address = field.getName();
				if( !fields.contains(Address) ) {
					fields.add(Address);
				}
			};
		}
			
		if( entityClass.getSuperclass() != null ) {
			return GGAPIEntityChecker.getFieldsWithAnnotation(fields, entityClass.getSuperclass(), annotation);
		} else {
			return fields;
		}
	}

	private static Annotation checkIfAnnotatedEntity(Class<?> entityClass, Class<? extends Annotation> typeAnnotation) {
		 Annotation annotation = entityClass.getDeclaredAnnotation(typeAnnotation);

		if( annotation == null ) {
			if( entityClass.getSuperclass() != null )
				return GGAPIEntityChecker.checkIfAnnotatedEntity(entityClass.getSuperclass(), typeAnnotation);
			return null;
		}

		return annotation;
	}

	private static String checkDeleteMethodAnnotationAndMethodParamsHaveGoodTypes(Class<?> entityClass) throws GGAPIEntityException {
		String methodAddress = null;
		for( Method method : entityClass.getDeclaredMethods() ) {
			if( method.isAnnotationPresent(GGAPIEntityDeleteMethod.class)) {
				if( methodAddress != null && !methodAddress.isEmpty() ) {
					throw new GGAPIEntityException(GGAPIExceptionCode.ENTITY_DEFINITION, "Entity "+entityClass.getSimpleName()+" has more than one method annotated with "+GGAPIEntityDeleteMethod.class);
				}
				methodAddress = method.getName();
				Type[] parameters = method.getGenericParameterTypes();
				if( !parameters[0].equals(IGGAPICaller.class) ) {
					throw new GGAPIEntityException(GGAPIExceptionCode.ENTITY_DEFINITION, "Entity "+entityClass.getSimpleName()+" has method "+methodAddress+" but parameter 0 is not of type "+IGGAPICaller.class);
				}
				if( !isMapOfString(parameters[1]) ) {
					throw new GGAPIEntityException(GGAPIExceptionCode.ENTITY_DEFINITION, "Entity "+entityClass.getSimpleName()+" has method "+methodAddress+" but parameter 1 is not of type java.lang.Map<java.lang.String,java.lang.String>");
				}
			}
		}
		
		if( methodAddress == null && entityClass.getSuperclass() != null ) {
			methodAddress = GGAPIEntityChecker.checkDeleteMethodAnnotationAndMethodParamsHaveGoodTypes( entityClass.getSuperclass() );
		}
		
		if( methodAddress == null || methodAddress.isEmpty() ) {
			throw new GGAPIEntityException(GGAPIExceptionCode.ENTITY_DEFINITION, "Entity "+entityClass.getSimpleName()+" does not have method annotated with @GGAPIEntityDeleteMethod");
		}
		
		return methodAddress;
	}

	private static String checkSaveMethodAnnotationAndMethodParamsHaveGoodTypes(Class<?> entityClass) throws GGAPIEntityException {
		String methodAddress = null;
		for( Method method : entityClass.getDeclaredMethods() ) {
			if( method.isAnnotationPresent(GGAPIEntitySaveMethod.class)) {
				
				if( methodAddress != null && !methodAddress.isEmpty() ) {
					throw new GGAPIEntityException(GGAPIExceptionCode.ENTITY_DEFINITION, "Entity "+entityClass.getSimpleName()+" has more than one method annotated with "+GGAPIEntitySaveMethod.class);
				}
				methodAddress = method.getName();
				Type[] parameters = method.getGenericParameterTypes();
				if( !parameters[0].equals(IGGAPICaller.class) ) {
					throw new GGAPIEntityException(GGAPIExceptionCode.ENTITY_DEFINITION, "Entity "+entityClass.getSimpleName()+" has method "+methodAddress+" but parameter 0 is not of type "+IGGAPICaller.class);
				}
				if( !isMapOfString(parameters[1]) ) {
					throw new GGAPIEntityException(GGAPIExceptionCode.ENTITY_DEFINITION, "Entity "+entityClass.getSimpleName()+" has method "+methodAddress+" but parameter 1 is not of type java.lang.Map<java.lang.String,java.lang.String>");
				}
				if( !method.getReturnType().equals(Object.class) ) {
					throw new GGAPIEntityException(GGAPIExceptionCode.ENTITY_DEFINITION, "Entity "+entityClass.getSimpleName()+" has method "+methodAddress+" but returned type is not Object.class but is "+method.getGenericReturnType());
				}
			}
		}
		
		if( methodAddress == null && entityClass.getSuperclass() != null ) {
			methodAddress = GGAPIEntityChecker.checkSaveMethodAnnotationAndMethodParamsHaveGoodTypes( entityClass.getSuperclass() );
		}
		
		if( methodAddress == null || methodAddress.isEmpty() ) {
			throw new GGAPIEntityException(GGAPIExceptionCode.ENTITY_DEFINITION, "Entity "+entityClass.getSimpleName()+" does not have method annotated with @GGAPIEntitySaveMethod");
		}
		
		return methodAddress;
	}

	static private boolean isMapOfString(Type type) {
		if (type instanceof ParameterizedType) {
			ParameterizedType parameterizedType = (ParameterizedType) type;
			Type[] typeArguments = parameterizedType.getActualTypeArguments();
			return parameterizedType.getRawType() == Map.class && typeArguments.length == 2
					&& typeArguments[0] == String.class && typeArguments[1] == String.class;
		}
		return false;
	}

	private static String checkDeleteProviderAnnotationPresentAndFieldHasGoodType(Class<?> entityClass) throws GGAPIEntityException {
		String fieldAddress = GGAPIEntityChecker.getFieldAddressAnnotatedWithAndCheckType(entityClass, GGAPIEntityDeleteMethodProvider.class, IGGAPIEntityDeleteMethod.class);
		if( fieldAddress == null ) {
			throw new GGAPIEntityException(GGAPIExceptionCode.ENTITY_DEFINITION, "Entity "+entityClass.getSimpleName()+" does not have any field annotated with @GGAPIEntityDeleteMethodProvider");
		}
		return fieldAddress; 
	}

	private static String checkSaveProviderAnnotationPresentAndFieldHasGoodType(Class<?> entityClass) throws GGAPIEntityException {
		String fieldAddress = GGAPIEntityChecker.getFieldAddressAnnotatedWithAndCheckType(entityClass, GGAPIEntitySaveMethodProvider.class, IGGAPIEntitySaveMethod.class);
		if( fieldAddress == null ) {
			throw new GGAPIEntityException(GGAPIExceptionCode.ENTITY_DEFINITION, "Entity "+entityClass.getSimpleName()+" does not have any field annotated with @GGAPIEntitySaveMethodProvider");
		}
		return fieldAddress;
	}

	private static String checkIdAnnotationPresentAndFieldHasGoodType(Class<?> entityClass) throws GGAPIEntityException {
		String fieldAddress = GGAPIEntityChecker.getFieldAddressAnnotatedWithAndCheckType(entityClass, GGAPIEntityId.class, String.class);
		if( fieldAddress == null ) {
			throw new GGAPIEntityException(GGAPIExceptionCode.ENTITY_DEFINITION, "Entity "+entityClass.getSimpleName()+" does not have any field annotated with @GGAPIEntityId");
		}
		return fieldAddress; 
	} 
	
	private static String checkRepositoryAnnotationPresentAndFieldHasGoodType(Class<?> entityClass) throws GGAPIEntityException {
		String fieldAddress = GGAPIEntityChecker.getFieldAddressAnnotatedWithAndCheckType(entityClass, GGAPIEntityRepository.class, IGGAPIRepository.class);
		if( fieldAddress == null ) {
			throw new GGAPIEntityException(GGAPIExceptionCode.ENTITY_DEFINITION, "Entity "+entityClass.getSimpleName()+" does not have any field annotated with @GGAPIEntityRepository");
		}
		return fieldAddress; 
	} 
	
	private static String checkEngineAnnotationPresentAndFieldHasGoodType(Class<?> entityClass) throws GGAPIEntityException {
		String fieldAddress = GGAPIEntityChecker.getFieldAddressAnnotatedWithAndCheckType(entityClass, GGAPIEntityEngine.class, IGGAPIEngine.class);
		if( fieldAddress == null ) {
			throw new GGAPIEntityException(GGAPIExceptionCode.ENTITY_DEFINITION, "Entity "+entityClass.getSimpleName()+" does not have any field annotated with @GGAPIEntityEngine");
		}
		return fieldAddress; 
	} 
	
	private static String checkUuidAnnotationPresentAndFieldHasGoodType(Class<?> entityClass) throws GGAPIEntityException {
		String fieldAddress = GGAPIEntityChecker.getFieldAddressAnnotatedWithAndCheckType(entityClass, GGAPIEntityUuid.class, String.class);
		if( fieldAddress == null ) {
			throw new GGAPIEntityException(GGAPIExceptionCode.ENTITY_DEFINITION, "Entity "+entityClass.getSimpleName()+" does not have any field annotated with @GGAPIEntityUuid");
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

			Field field = (Field) obj.get(obj.size()-1);//entityClass.getDeclaredField(fieldAddress);
			if( !field.getType().equals(fieldType) ) {
				throw new GGAPIEntityException(GGAPIExceptionCode.ENTITY_DEFINITION, "Entity "+entityClass.getSimpleName()+" has field "+fieldAddress+" with wrong type "+field.getType()+", should be "+fieldType);
			} else {
				return fieldAddress;
			}
		} catch ( GGReflectionException e) {
			throw new GGAPIEntityException(GGAPIExceptionCode.ENTITY_DEFINITION, "Entity "+entityClass.getSimpleName()+" does not have field "+fieldAddress);
		}
	}

	private static String getFieldAddressAnnotatedWithAndCheckType(Class<?> entityClass, Class<? extends Annotation> annotationClass, Class<?> fieldClass) throws GGAPIEntityException {
		log.atDebug().log("Looking for annotation "+annotationClass.getSimpleName()+" on field of type "+fieldClass.getSimpleName()+" from type "+entityClass.getSimpleName());
		String fieldAddress = null;
		for( Field field: entityClass.getDeclaredFields() ) {
			if( field.isAnnotationPresent(annotationClass) ) {
				if( fieldAddress != null && !fieldAddress.isEmpty() ) {
					throw new GGAPIEntityException(GGAPIExceptionCode.ENTITY_DEFINITION, "Entity "+entityClass.getSimpleName()+" has more than one field annotated with "+annotationClass);
				}
				if( field.getType().equals(fieldClass) ) {
					fieldAddress = field.getName();
					break;
				} else {
					throw new GGAPIEntityException(GGAPIExceptionCode.ENTITY_DEFINITION, "Entity "+entityClass.getSimpleName()+" has field "+field.getName()+" with wrong type "+field.getType().getName()+", should be "+fieldClass);
				}
			} else {
				if( isNotPrimitiveOrInternal(field.getType()) && !entityClass.equals(field.getType()))
					fieldAddress = GGAPIEntityChecker.getFieldAddressAnnotatedWithAndCheckType(field.getType(), annotationClass, fieldClass);
			}
		}

		if( entityClass.getSuperclass() != null && fieldAddress == null ) {
			return GGAPIEntityChecker.getFieldAddressAnnotatedWithAndCheckType(entityClass.getSuperclass(), annotationClass, fieldClass);
		} else {
			return fieldAddress;
		}
	}

	public static boolean isNotPrimitiveOrInternal(Class<?> clazz) {
		// Vérifier si c'est un type primitif
		if (clazz.isPrimitive()) {
			return false;
		}

		// Vérifier si c'est un type interne Java (java.* ou javax.*)
		Package package1 = clazz.getPackage();
		if( package1 == null ) {
			return false;
		}
		
		String packageName = package1.getName();
		if (packageName.startsWith("java.") || packageName.startsWith("javax.")) {
			return false;
		}

		// Sinon, c'est un type valide
		return true;
	}
	
	private static String checkDomainInAnnotation(GGAPIEntity annotation, Class<?> entityClass) throws GGAPIEntityException {
		if( annotation.domain() == null || annotation.domain().isEmpty() ) {
			throw new GGAPIEntityException(GGAPIExceptionCode.ENTITY_DEFINITION, "No domain provided in annotation of entity "+entityClass.getSimpleName());
		} else {
			return annotation.domain();
		}
	}

	public static GGAPIEntityInfos checkEntity(Object entity) throws GGAPIException {
		return GGAPIEntityChecker.checkEntityClass(entity.getClass());
	}
	
	private static Method hasAnnotation(Class<?> type, Class<?> searchAnnotation) throws GGAPIException {
		for( Method method: type.getDeclaredMethods()) {	
			for( Annotation annotation: method.getAnnotations()) {
				if( annotation.annotationType().equals(searchAnnotation) ) {
					
					Type[] parameters = method.getGenericParameterTypes();
					if( parameters.length != 2 ) {
						throw new GGAPIEntityException(GGAPIExceptionCode.ENTITY_DEFINITION, "The method "+method.getName()+" of entity of type "+type.getName()+" must have two parameters (IGGAPICaller, Map<String,String>");
					}
					if( !parameters[0].equals(IGGAPICaller.class) ) {
						throw new GGAPIEntityException(GGAPIExceptionCode.ENTITY_DEFINITION, "The method "+method.getName()+" of entity of type "+type.getName()+" must have two parameters (IGGAPICaller, Map<String,String>");
					}
					if( !isMapOfString(parameters[1])) {
						throw new GGAPIEntityException(GGAPIExceptionCode.ENTITY_DEFINITION, "The method "+method.getName()+" of entity of type "+type.getName()+" must have two parameters (IGGAPICaller, Map<String,String>");
					}
					return method;
				}
			}
		}
		if( type.getSuperclass() != null ) {
			return hasAnnotation(type.getSuperclass(), searchAnnotation);
		}
		return null;		
	}
}
