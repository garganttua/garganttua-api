package com.garganttua.api.core.entity.checker;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.geojson.Point;
import org.javatuples.Pair;

import com.garganttua.api.core.IGGAPICaller;
import com.garganttua.api.core.dto.checker.GGAPIDtoChecker;
import com.garganttua.api.core.dto.checker.GGAPIDtoChecker.GGAPIDtoInfos;
import com.garganttua.api.core.dto.exceptions.GGAPIDtoException;
import com.garganttua.api.core.entity.annotations.GGAPIBusinessAnnotations;
import com.garganttua.api.core.entity.annotations.GGAPIBusinessAnnotations.GGAPIEntityAfterCreate;
import com.garganttua.api.core.entity.annotations.GGAPIBusinessAnnotations.GGAPIEntityAfterDelete;
import com.garganttua.api.core.entity.annotations.GGAPIBusinessAnnotations.GGAPIEntityAfterGet;
import com.garganttua.api.core.entity.annotations.GGAPIBusinessAnnotations.GGAPIEntityAfterUpdate;
import com.garganttua.api.core.entity.annotations.GGAPIBusinessAnnotations.GGAPIEntityBeforeCreate;
import com.garganttua.api.core.entity.annotations.GGAPIBusinessAnnotations.GGAPIEntityBeforeDelete;
import com.garganttua.api.core.entity.annotations.GGAPIBusinessAnnotations.GGAPIEntityBeforeUpdate;
import com.garganttua.api.core.entity.annotations.GGAPIEntity;
import com.garganttua.api.core.entity.annotations.GGAPIEntityAuthorizeUpdate;
import com.garganttua.api.core.entity.annotations.GGAPIEntityDeleteMethod;
import com.garganttua.api.core.entity.annotations.GGAPIEntityDeleteMethodProvider;
import com.garganttua.api.core.entity.annotations.GGAPIEntityGeolocalized;
import com.garganttua.api.core.entity.annotations.GGAPIEntityGotFromRepository;
import com.garganttua.api.core.entity.annotations.GGAPIEntityHidden;
import com.garganttua.api.core.entity.annotations.GGAPIEntityHiddenable;
import com.garganttua.api.core.entity.annotations.GGAPIEntityId;
import com.garganttua.api.core.entity.annotations.GGAPIEntityLocation;
import com.garganttua.api.core.entity.annotations.GGAPIEntityMandatory;
import com.garganttua.api.core.entity.annotations.GGAPIEntityOwned;
import com.garganttua.api.core.entity.annotations.GGAPIEntityOwner;
import com.garganttua.api.core.entity.annotations.GGAPIEntityOwnerId;
import com.garganttua.api.core.entity.annotations.GGAPIEntityPublic;
import com.garganttua.api.core.entity.annotations.GGAPIEntityRepository;
import com.garganttua.api.core.entity.annotations.GGAPIEntitySaveMethod;
import com.garganttua.api.core.entity.annotations.GGAPIEntitySaveMethodProvider;
import com.garganttua.api.core.entity.annotations.GGAPIEntityShare;
import com.garganttua.api.core.entity.annotations.GGAPIEntityShared;
import com.garganttua.api.core.entity.annotations.GGAPIEntitySuperOwner;
import com.garganttua.api.core.entity.annotations.GGAPIEntitySuperTenant;
import com.garganttua.api.core.entity.annotations.GGAPIEntityTenant;
import com.garganttua.api.core.entity.annotations.GGAPIEntityTenantId;
import com.garganttua.api.core.entity.annotations.GGAPIEntityUnicity;
import com.garganttua.api.core.entity.annotations.GGAPIEntityUuid;
import com.garganttua.api.core.entity.exceptions.GGAPIEntityException;
import com.garganttua.api.core.entity.interfaces.IGGAPIEntityDeleteMethod;
import com.garganttua.api.core.entity.interfaces.IGGAPIEntitySaveMethod;
import com.garganttua.api.repository.IGGAPIRepository;
import com.garganttua.api.security.IGGAPISecurity;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GGAPIEntityChecker {
	
	public record GGAPIEntityInfos (
			String domain,
	        String uuidFieldName,
	        String idFieldName,
	        String saveProviderFieldName,
	        String deleteProviderFieldName,
	        boolean tenantEntity,
	        boolean ownerEntity,
	        boolean ownedEntity,
	        String tenantIdFieldName,
	        String superTenantFieldName,
	        String ownerIdFieldName,
	        String superOnwerIdFieldName,
	        String saveMethodName,
	        String deleteMethodName,
	        boolean publicEntity,
	        boolean hiddenableEntity,
	        String hiddenFieldName,
	        boolean geolocalizedEntity,
	        String locationFieldName,
	        boolean sharedEntity,
	        String shareFieldName,
	        String repositoryFieldName,
	        List<String> mandatoryFields,
	        List<String> unicityFields,
	        String afterGetMethodName,
	        String beforeCreateMethodName, 
	        String afterCreateMethodName, 
	        String beforeUpdateMethodName, 
	        String afterUpdateMethodName, 
	        String beforeDeleteMethodName, 
	        String afterDeleteMethodName, 
	        Map<String, String> updateAuthorizations,
	        String gotFromRepositoryFieldName
	) {
		@Override
		public boolean equals(Object obj) {
		    if (this == obj) return true;
		    if (obj == null || getClass() != obj.getClass()) return false;

		    GGAPIEntityInfos other = (GGAPIEntityInfos) obj;

		    return Objects.equals(uuidFieldName, other.uuidFieldName) &&
		    		Objects.equals(domain, other.domain) &&
		            Objects.equals(idFieldName, other.idFieldName) &&
		            Objects.equals(saveProviderFieldName, other.saveProviderFieldName) &&
		            Objects.equals(deleteProviderFieldName, other.deleteProviderFieldName) &&
		            tenantEntity == other.tenantEntity &&
		            ownerEntity == other.ownerEntity &&
		            ownedEntity == other.ownedEntity &&
		            Objects.equals(tenantIdFieldName, other.tenantIdFieldName) &&
		            Objects.equals(superTenantFieldName, other.superTenantFieldName) &&
		            Objects.equals(ownerIdFieldName, other.ownerIdFieldName) &&
		            Objects.equals(superOnwerIdFieldName, other.superOnwerIdFieldName) &&
		            Objects.equals(saveMethodName, other.saveMethodName) &&
		            Objects.equals(deleteMethodName, other.deleteMethodName) &&
		            publicEntity == other.publicEntity &&
		            hiddenableEntity == other.hiddenableEntity &&
		            Objects.equals(hiddenFieldName, other.hiddenFieldName) &&
		            geolocalizedEntity == other.geolocalizedEntity &&
		            Objects.equals(locationFieldName, other.locationFieldName) &&
		            sharedEntity == other.sharedEntity &&
		            Objects.equals(shareFieldName, other.shareFieldName) &&
		            Objects.equals(repositoryFieldName, other.repositoryFieldName) &&
		            Objects.equals(mandatoryFields, other.mandatoryFields) &&
		            Objects.equals(unicityFields, other.unicityFields) &&
		            Objects.equals(afterGetMethodName, other.afterGetMethodName) &&
		            Objects.equals(beforeCreateMethodName, other.beforeCreateMethodName) &&
		            Objects.equals(afterCreateMethodName, other.afterCreateMethodName) &&
		            Objects.equals(beforeUpdateMethodName, other.beforeUpdateMethodName) &&
		            Objects.equals(afterUpdateMethodName, other.afterUpdateMethodName) &&
		            Objects.equals(beforeDeleteMethodName, other.beforeDeleteMethodName) &&
		            Objects.equals(afterDeleteMethodName, other.afterDeleteMethodName) &&
		            Objects.equals(updateAuthorizations, other.updateAuthorizations) &&
		            Objects.equals(gotFromRepositoryFieldName, other.gotFromRepositoryFieldName);
		}
	}

	public static GGAPIEntityInfos checkEntityClass(Class<?> entityClass) throws GGAPIEntityException {
		if (log.isDebugEnabled()) {
			log.debug("Checking entity infos from class " + entityClass.getName());
		}
		GGAPIEntity annotation = entityClass.getDeclaredAnnotation(GGAPIEntity.class);
		String domain = GGAPIEntityChecker.checkDomainInAnnotation(annotation, entityClass);
		String uuidFieldName = GGAPIEntityChecker.checkUuidAnnotationPresentAndFieldHasGoodType(entityClass);
		String idFieldName = GGAPIEntityChecker.checkIdAnnotationPresentAndFieldHasGoodType(entityClass);
		String saveProviderFieldName = GGAPIEntityChecker.checkSaveProviderAnnotationPresentAndFieldHasGoodType(entityClass);
		String deleteProviderFieldName = GGAPIEntityChecker.checkDeleteProviderAnnotationPresentAndFieldHasGoodType(entityClass);
		boolean tenantEntity = GGAPIEntityChecker.checkIfAnnotatedEntity(entityClass, GGAPIEntityTenant.class);
		String tenantIdFieldName = null;
		String superTenantFieldName = null;
		
		if( tenantEntity ) {
			tenantIdFieldName = GGAPIEntityChecker.checkAnnotationOrField(entityClass, GGAPIEntityChecker.getFieldNameAnnotatedWithAndCheckType(entityClass, GGAPIEntityTenantId.class, String.class), entityClass.getDeclaredAnnotation(GGAPIEntityTenant.class).tenantId(), String.class, GGAPIEntityTenantId.class);
			superTenantFieldName = GGAPIEntityChecker.checkAnnotationOrField(entityClass, GGAPIEntityChecker.getFieldNameAnnotatedWithAndCheckType(entityClass, GGAPIEntitySuperTenant.class, boolean.class), entityClass.getDeclaredAnnotation(GGAPIEntityTenant.class).superTenant(), boolean.class, GGAPIEntitySuperTenant.class);
		}
		
		boolean ownerEntity = GGAPIEntityChecker.checkIfAnnotatedEntity(entityClass, GGAPIEntityOwner.class);
		boolean ownedEntity = GGAPIEntityChecker.checkIfAnnotatedEntity(entityClass, GGAPIEntityOwned.class);
		
		if(ownerEntity &&  ownedEntity) {
			throw new GGAPIEntityException(GGAPIEntityException.ENTITY_DEFINITION_ERROR, "Entity "+entityClass+" Cannot be owner and owned at the same time");
		}
		
		String ownerIdFieldName = null;
		String superOnwerIdFieldName = null;
		if( ownerEntity ) {
			ownerIdFieldName = GGAPIEntityChecker.checkAnnotationOrField(entityClass, GGAPIEntityChecker.getFieldNameAnnotatedWithAndCheckType(entityClass, GGAPIEntityOwnerId.class, String.class), entityClass.getDeclaredAnnotation(GGAPIEntityOwner.class).ownerId(), String.class, GGAPIEntityOwnerId.class);
			superOnwerIdFieldName = GGAPIEntityChecker.checkAnnotationOrField(entityClass, GGAPIEntityChecker.getFieldNameAnnotatedWithAndCheckType(entityClass, GGAPIEntitySuperOwner.class, boolean.class), entityClass.getDeclaredAnnotation(GGAPIEntityOwner.class).superOwner(), boolean.class, GGAPIEntitySuperOwner.class);	
		}
		
		if( ownedEntity ) {
			ownerIdFieldName = GGAPIEntityChecker.checkAnnotationOrField(entityClass, GGAPIEntityChecker.getFieldNameAnnotatedWithAndCheckType(entityClass, GGAPIEntityOwnerId.class, String.class), entityClass.getDeclaredAnnotation(GGAPIEntityOwned.class).ownerId(), String.class, GGAPIEntityOwnerId.class);
		}
		
		String saveMethodName = GGAPIEntityChecker.checkSaveMethodAnnotationAndMethodParamsHaveGoodTypes(entityClass);
		String deleteMethodName = GGAPIEntityChecker.checkDeleteMethodAnnotationAndMethodParamsHaveGoodTypes(entityClass);
		
		boolean publicEntity = GGAPIEntityChecker.checkIfAnnotatedEntity(entityClass, GGAPIEntityPublic.class);
		
		boolean hiddenableEntity = GGAPIEntityChecker.checkIfAnnotatedEntity(entityClass, GGAPIEntityHiddenable.class);
		String hiddenFieldName = null;
		if( hiddenableEntity ) {
			hiddenFieldName = GGAPIEntityChecker.checkAnnotationOrField(entityClass, GGAPIEntityChecker.getFieldNameAnnotatedWithAndCheckType(entityClass, GGAPIEntityHidden.class, boolean.class), entityClass.getDeclaredAnnotation(GGAPIEntityHiddenable.class).hidden(), boolean.class, GGAPIEntityHidden.class);
		}
		
		boolean geolocalizedEntity = GGAPIEntityChecker.checkIfAnnotatedEntity(entityClass, GGAPIEntityGeolocalized.class);
		String locationFieldName = null;
		if( geolocalizedEntity ) {
			locationFieldName = GGAPIEntityChecker.checkAnnotationOrField(entityClass, GGAPIEntityChecker.getFieldNameAnnotatedWithAndCheckType(entityClass, GGAPIEntityLocation.class, Point.class), entityClass.getDeclaredAnnotation(GGAPIEntityGeolocalized.class).location(), Point.class, GGAPIEntityLocation.class);
		}
		
		boolean sharedEntity = GGAPIEntityChecker.checkIfAnnotatedEntity(entityClass, GGAPIEntityShared.class);
		String shareFieldName = null;
		if( sharedEntity ) {
			shareFieldName = GGAPIEntityChecker.checkAnnotationOrField(entityClass, GGAPIEntityChecker.getFieldNameAnnotatedWithAndCheckType(entityClass, GGAPIEntityShare.class, String.class), entityClass.getDeclaredAnnotation(GGAPIEntityShared.class).share(), String.class, GGAPIEntityShare.class);
		}
		
		String repositoryFieldName = GGAPIEntityChecker.checkRepositoryAnnotationPresentAndFieldHasGoodType(entityClass);
		
		List<String> mandatoryFields = new ArrayList<String>();
		mandatoryFields = GGAPIEntityChecker.getFieldsWithAnnotation(mandatoryFields, entityClass, GGAPIEntityMandatory.class);
		
		List<String> unicityFields = new ArrayList<String>();
		unicityFields = GGAPIEntityChecker.getFieldsWithAnnotation(unicityFields, entityClass, GGAPIEntityUnicity.class);
		
		Method afterGetm = GGAPIBusinessAnnotations.hasAnnotation(entityClass, GGAPIEntityAfterGet.class);
		Method beforeCreatem = GGAPIBusinessAnnotations.hasAnnotation(entityClass, GGAPIEntityBeforeCreate.class);
		Method afterCreatem = GGAPIBusinessAnnotations.hasAnnotation(entityClass, GGAPIEntityAfterCreate.class);
		Method beforeUpdatem = GGAPIBusinessAnnotations.hasAnnotation(entityClass, GGAPIEntityBeforeUpdate.class);
		Method afterUpdatem = GGAPIBusinessAnnotations.hasAnnotation(entityClass, GGAPIEntityAfterUpdate.class);
		Method beforeDeletem = GGAPIBusinessAnnotations.hasAnnotation(entityClass, GGAPIEntityBeforeDelete.class);
		Method afterDeletem = GGAPIBusinessAnnotations.hasAnnotation(entityClass, GGAPIEntityAfterDelete.class);

		Map<String, String> updateAuthorizations = new HashMap<String, String>();
				
		updateAuthorizations = GGAPIEntityChecker.getFieldAuthorizedForUpdate(entityClass, updateAuthorizations);
		
		String gotFromReposiotryFieldName = GGAPIEntityChecker.checkGotFromRepositoryAnnotationPresentAndFieldHasGoodType(entityClass);;
		
		GGAPIEntityChecker.checkConstructor(entityClass);
		
		return new GGAPIEntityInfos(
				domain, 
				uuidFieldName, 
				idFieldName, 
				saveProviderFieldName, 
				deleteProviderFieldName, 
				tenantEntity, 
				ownerEntity, 
				ownedEntity, 
		        tenantIdFieldName,
		        superTenantFieldName,
		        ownerIdFieldName,
		        superOnwerIdFieldName,
				saveMethodName, 
				deleteMethodName, 
				publicEntity, 
				hiddenableEntity, 
				hiddenFieldName, 
				geolocalizedEntity, 
				locationFieldName, 
				sharedEntity, 
				shareFieldName, 
				repositoryFieldName, 
				mandatoryFields, 
				unicityFields,
				afterGetm==null?null:afterGetm.getName(),
				beforeCreatem==null?null:beforeCreatem.getName(),
				afterCreatem==null?null:afterCreatem.getName(),
				beforeUpdatem==null?null:beforeUpdatem.getName(),
				afterUpdatem==null?null:afterUpdatem.getName(),
				beforeDeletem==null?null:beforeDeletem.getName(),
				afterDeletem==null?null:afterDeletem.getName(),
				updateAuthorizations,
				gotFromReposiotryFieldName);
	}

	private static List<Pair<Class<?>, GGAPIDtoInfos>> checkDtos(List<Class<?>> dtoClasss) throws GGAPIEntityException {
		try {
			List<Pair<Class<?>, GGAPIDtoInfos>> infos = new ArrayList<Pair<Class<?>,GGAPIDtoInfos>>();
			for( Class<?> dtoClass: dtoClasss ) {
				infos.add( new Pair<Class<?>, GGAPIDtoInfos>(dtoClass, GGAPIDtoChecker.checkDto(dtoClass)));
			}
			return infos;
		} catch (GGAPIDtoException e) {
			throw new GGAPIEntityException(e);
		}
	}

	private static String checkGotFromRepositoryAnnotationPresentAndFieldHasGoodType(Class<?> entityClass) throws GGAPIEntityException {
		String fieldName = GGAPIEntityChecker.getFieldNameAnnotatedWithAndCheckType(entityClass, GGAPIEntityGotFromRepository.class, boolean.class);
		if( fieldName == null ) {
			throw new GGAPIEntityException(GGAPIEntityException.ENTITY_DEFINITION_ERROR, "Entity "+entityClass+" does not have any field annotated with @GGAPIEntityGotFromRepository");
		}
		return fieldName; 
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
		        throw new GGAPIEntityException(GGAPIEntityException.ENTITY_DEFINITION_ERROR, "Entity " + entityClass + " must have at least one constructor with no args");
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
				String name = field.getName();
				if( !fields.contains(name) ) {
					fields.add(name);
				}
			};
		}
			
		if( entityClass.getSuperclass() != null ) {
			return GGAPIEntityChecker.getFieldsWithAnnotation(fields, entityClass.getSuperclass(), annotation);
		} else {
			return fields;
		}
	}

	private static boolean checkIfAnnotatedEntity(Class<?> entityClass, Class<? extends Annotation> typeAnnotation) {
		 Annotation annotation = entityClass.getDeclaredAnnotation(typeAnnotation);

		if( annotation == null ) {
			return false;
		}

		return true;
	}

	private static String checkDeleteMethodAnnotationAndMethodParamsHaveGoodTypes(Class<?> entityClass) throws GGAPIEntityException {
		String methodName = null;
		for( Method method : entityClass.getDeclaredMethods() ) {
			if( method.isAnnotationPresent(GGAPIEntityDeleteMethod.class)) {
				if( methodName != null && !methodName.isEmpty() ) {
					throw new GGAPIEntityException(GGAPIEntityException.ENTITY_DEFINITION_ERROR, "Entity "+entityClass+" has more than one method annotated with "+GGAPIEntityDeleteMethod.class);
				}
				methodName = method.getName();
				Type[] parameters = method.getGenericParameterTypes();
				if( !parameters[0].equals(IGGAPICaller.class) ) {
					throw new GGAPIEntityException(GGAPIEntityException.ENTITY_DEFINITION_ERROR, "Entity "+entityClass+" has method "+methodName+" but parameter 0 is not of type "+IGGAPICaller.class);
				}
				if( !isMapOfString(parameters[1]) ) {
					throw new GGAPIEntityException(GGAPIEntityException.ENTITY_DEFINITION_ERROR, "Entity "+entityClass+" has method "+methodName+" but parameter 1 is not of type java.lang.Map<java.lang.String,java.lang.String>");
				}
			}
		}
		
		if( methodName == null && entityClass.getSuperclass() != null ) {
			methodName = GGAPIEntityChecker.checkDeleteMethodAnnotationAndMethodParamsHaveGoodTypes( entityClass.getSuperclass() );
		}
		
		if( methodName == null || methodName.isEmpty() ) {
			throw new GGAPIEntityException(GGAPIEntityException.ENTITY_DEFINITION_ERROR, "Entity "+entityClass+" does not have method annotated with @GGAPIEntityDeleteMethod");
		}
		
		return methodName;
	}

	private static String checkSaveMethodAnnotationAndMethodParamsHaveGoodTypes(Class<?> entityClass) throws GGAPIEntityException {
		String methodName = null;
		for( Method method : entityClass.getDeclaredMethods() ) {
			if( method.isAnnotationPresent(GGAPIEntitySaveMethod.class)) {
				
				if( methodName != null && !methodName.isEmpty() ) {
					throw new GGAPIEntityException(GGAPIEntityException.ENTITY_DEFINITION_ERROR, "Entity "+entityClass+" has more than one method annotated with "+GGAPIEntitySaveMethod.class);
				}
				methodName = method.getName();
				Type[] parameters = method.getGenericParameterTypes();
				if( !parameters[0].equals(IGGAPICaller.class) ) {
					throw new GGAPIEntityException(GGAPIEntityException.ENTITY_DEFINITION_ERROR, "Entity "+entityClass+" has method "+methodName+" but parameter 0 is not of type "+IGGAPICaller.class);
				}
				if( !isMapOfString(parameters[1]) ) {
					throw new GGAPIEntityException(GGAPIEntityException.ENTITY_DEFINITION_ERROR, "Entity "+entityClass+" has method "+methodName+" but parameter 1 is not of type java.lang.Map<java.lang.String,java.lang.String>");
				}
				if( !isOptionalIGGAPISecurity(parameters[2]) ) {
					throw new GGAPIEntityException(GGAPIEntityException.ENTITY_DEFINITION_ERROR, "Entity "+entityClass+" has method "+methodName+" but parameter 2 is not of type Optional<IGGAPISecurity>");
				}
			}
		}
		
		if( methodName == null && entityClass.getSuperclass() != null ) {
			methodName = GGAPIEntityChecker.checkSaveMethodAnnotationAndMethodParamsHaveGoodTypes( entityClass.getSuperclass() );
		}
		
		if( methodName == null || methodName.isEmpty() ) {
			throw new GGAPIEntityException(GGAPIEntityException.ENTITY_DEFINITION_ERROR, "Entity "+entityClass+" does not have method annotated with @GGAPIEntitySaveMethod");
		}
		
		return methodName;
	}
	
	private static boolean isOptionalIGGAPISecurity(Type type) {
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type rawType = parameterizedType.getRawType();
            Type[] typeArguments = parameterizedType.getActualTypeArguments();

            if (rawType instanceof Class<?>) {
                Class<?> rawClass = (Class<?>) rawType;
                if (rawClass == Optional.class && typeArguments.length == 1) {
                    Type typeArgument = typeArguments[0];
                    if (typeArgument instanceof Class<?>) {
                        Class<?> classArgument = (Class<?>) typeArgument;
                        if (classArgument == IGGAPISecurity.class) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
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
		String fieldName = GGAPIEntityChecker.getFieldNameAnnotatedWithAndCheckType(entityClass, GGAPIEntityDeleteMethodProvider.class, IGGAPIEntityDeleteMethod.class);
		if( fieldName == null ) {
			throw new GGAPIEntityException(GGAPIEntityException.ENTITY_DEFINITION_ERROR, "Entity "+entityClass+" does not have any field annotated with @GGAPIEntityDeleteMethodProvider");
		}
		return fieldName; 
	}

	private static String checkSaveProviderAnnotationPresentAndFieldHasGoodType(Class<?> entityClass) throws GGAPIEntityException {
		String fieldName = GGAPIEntityChecker.getFieldNameAnnotatedWithAndCheckType(entityClass, GGAPIEntitySaveMethodProvider.class, IGGAPIEntitySaveMethod.class);
		if( fieldName == null ) {
			throw new GGAPIEntityException(GGAPIEntityException.ENTITY_DEFINITION_ERROR, "Entity "+entityClass+" does not have any field annotated with @GGAPIEntitySaveMethodProvider");
		}
		return fieldName;
	}

	private static String checkIdAnnotationPresentAndFieldHasGoodType(Class<?> entityClass) throws GGAPIEntityException {
		String fieldName = GGAPIEntityChecker.getFieldNameAnnotatedWithAndCheckType(entityClass, GGAPIEntityId.class, String.class);
		if( fieldName == null ) {
			throw new GGAPIEntityException(GGAPIEntityException.ENTITY_DEFINITION_ERROR, "Entity "+entityClass+" does not have any field annotated with @GGAPIEntityId");
		}
		return fieldName; 
	} 
	
	private static String checkRepositoryAnnotationPresentAndFieldHasGoodType(Class<?> entityClass) throws GGAPIEntityException {
		String fieldName = GGAPIEntityChecker.getFieldNameAnnotatedWithAndCheckType(entityClass, GGAPIEntityRepository.class, IGGAPIRepository.class);
		if( fieldName == null ) {
			throw new GGAPIEntityException(GGAPIEntityException.ENTITY_DEFINITION_ERROR, "Entity "+entityClass+" does not have any field annotated with @GGAPIEntityRepository");
		}
		return fieldName; 
	} 
	
	private static String checkUuidAnnotationPresentAndFieldHasGoodType(Class<?> entityClass) throws GGAPIEntityException {
		String fieldName = GGAPIEntityChecker.getFieldNameAnnotatedWithAndCheckType(entityClass, GGAPIEntityUuid.class, String.class);
		if( fieldName == null ) {
			throw new GGAPIEntityException(GGAPIEntityException.ENTITY_DEFINITION_ERROR, "Entity "+entityClass+" does not have any field annotated with @GGAPIEntityUuid");
		}
		return fieldName; 
	} 

	private static String checkAnnotationOrField(Class<?> entityClass, String fieldName, String annotationFieldName, Class<?> fieldType, Class<? extends Annotation> annotationClass)
			throws GGAPIEntityException {
		if( annotationFieldName.isEmpty() && fieldName == null ) {
			throw new GGAPIEntityException(GGAPIEntityException.ENTITY_DEFINITION_ERROR, "Entity "+entityClass+" does not have any field value or field annotated with "+annotationClass.getName());
		}
		if( !annotationFieldName.isEmpty() ) {
			GGAPIEntityChecker.checkFieldExistsAndIsOfType(entityClass, annotationFieldName, fieldType);
		}
		return annotationFieldName.isEmpty()?fieldName:annotationFieldName;
	}

	private static String checkFieldExistsAndIsOfType(Class<?> entityClass, String fieldName, Class<?> fieldType) throws GGAPIEntityException {
		try {
			Field field = entityClass.getDeclaredField(fieldName);
			if( !field.getType().equals(fieldType) ) {
				throw new GGAPIEntityException(GGAPIEntityException.ENTITY_DEFINITION_ERROR, "Entity "+entityClass+" has field "+fieldName+" with wrong type "+field.getType()+", should be "+fieldType);
			} else {
				return fieldName;
			}
		} catch (NoSuchFieldException | SecurityException e) {
			throw new GGAPIEntityException(GGAPIEntityException.ENTITY_DEFINITION_ERROR, "Entity "+entityClass+" does not have field "+fieldName);
		}
	}

	private static String getFieldNameAnnotatedWithAndCheckType(Class<?> entityClass, Class<? extends Annotation> annotationClass, Class<?> fieldClass) throws GGAPIEntityException {
		String fieldName = null;
		for( Field field: entityClass.getDeclaredFields() ) {
			if( field.isAnnotationPresent(annotationClass) ) {
				if( fieldName != null && !fieldName.isEmpty() ) {
					throw new GGAPIEntityException(GGAPIEntityException.ENTITY_DEFINITION_ERROR, "Entity "+entityClass+" has more than one field annotated with "+annotationClass);
				}
				if( field.getType().equals(fieldClass) ) {
					fieldName = field.getName();
				} else {
					throw new GGAPIEntityException(GGAPIEntityException.ENTITY_DEFINITION_ERROR, "Entity "+entityClass+" has field "+field.getName()+" with wrong type "+field.getType().getName()+", should be "+fieldClass);
				}
			}
		}

		if( entityClass.getSuperclass() != null && fieldName == null ) {
			return GGAPIEntityChecker.getFieldNameAnnotatedWithAndCheckType(entityClass.getSuperclass(), annotationClass, fieldClass);
		} else {
			return fieldName;
		}
	}
	
//	private static List<Class<?>> checkDtosInAnnotation(GGAPIEntity annotation, Class<?> entityClass) throws GGAPIEntityException {
//		List<Class<?>> dtoClasses = new ArrayList<Class<?>>();
//		if( annotation.dto() == null || annotation.dto().length == 0 ) {
//			throw new GGAPIEntityException(GGAPIEntityException.ENTITY_DEFINITION_ERROR, "No dto provided in annotation of entity "+entityClass);
//		}
//		for( Class<?> dto: annotation.dto() ) {
//			dtoClasses.add(dto);
//		}
//		return dtoClasses;
//	}

	private static String checkDomainInAnnotation(GGAPIEntity annotation, Class<?> entityClass) throws GGAPIEntityException {
		if( annotation.domain() == null || annotation.domain().isEmpty() ) {
			throw new GGAPIEntityException(GGAPIEntityException.ENTITY_DEFINITION_ERROR, "No domain provided in annotation of entity "+entityClass);
		} else {
			return annotation.domain();
		}
	}

	public static GGAPIEntityInfos checkEntity(Object entity) throws GGAPIEntityException {
		return GGAPIEntityChecker.checkEntityClass(entity.getClass());
	}

}
