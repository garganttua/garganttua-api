package com.garganttua.api.core.entity.checker;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Optional;

import org.geojson.Point;

import com.garganttua.api.core.IGGAPICaller;
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
import com.garganttua.api.core.entity.annotations.GGAPIEntityUuid;
import com.garganttua.api.core.entity.exceptions.GGAPIEntityException;
import com.garganttua.api.core.entity.interfaces.IGGAPIEntityDeleteMethod;
import com.garganttua.api.core.entity.interfaces.IGGAPIEntitySaveMethod;
import com.garganttua.api.repository.IGGAPIRepository;
import com.garganttua.api.security.IGGAPISecurity;

public class GGAPIEntityChecker {

	public void checkEntityClass(Class<?> entityClass) throws GGAPIEntityException {
		GGAPIEntity annotation = entityClass.getDeclaredAnnotation(GGAPIEntity.class);
		this.checkDomainInAnnotation(annotation, entityClass);
		this.checkDtoInAnnotation(annotation, entityClass);
		String uuidFieldName = this.checkUuidAnnotationPresentAndFieldHasGoodType(entityClass);
		String idFieldName = this.checkIdAnnotationPresentAndFieldHasGoodType(entityClass);
		String saveProviderFieldName = this.checkSaveProviderAnnotationPresentAndFieldHasGoodType(entityClass);
		String deleteProviderFieldName = this.checkDeleteProviderAnnotationPresentAndFieldHasGoodType(entityClass);
		boolean tenantEntity = this.checkIfAnnotatedEntity(entityClass, GGAPIEntityTenant.class);
		if( tenantEntity ) {
			String tenantIdFieldName = this.checkAnnotationOrField(entityClass, this.getFieldNameAnnotatedWithAndCheckType(entityClass, GGAPIEntityTenantId.class, String.class), entityClass.getDeclaredAnnotation(GGAPIEntityTenant.class).tenantId(), String.class, GGAPIEntityTenantId.class);
			String superTenantFieldName = this.checkAnnotationOrField(entityClass, this.getFieldNameAnnotatedWithAndCheckType(entityClass, GGAPIEntitySuperTenant.class, boolean.class), entityClass.getDeclaredAnnotation(GGAPIEntityTenant.class).superTenant(), boolean.class, GGAPIEntitySuperTenant.class);
		}
		
		boolean ownerEntity = this.checkIfAnnotatedEntity(entityClass, GGAPIEntityOwner.class);
		boolean ownedEntity = this.checkIfAnnotatedEntity(entityClass, GGAPIEntityOwned.class);
		
		if(ownerEntity &&  ownedEntity) {
			throw new GGAPIEntityException(GGAPIEntityException.ENTITY_DEFINITION_ERROR, "Entity "+entityClass+" Cannot be owner and owned at the same time");
		}
		
		if( ownerEntity ) {
			String ownerIdFieldName = this.checkAnnotationOrField(entityClass, this.getFieldNameAnnotatedWithAndCheckType(entityClass, GGAPIEntityOwnerId.class, String.class), entityClass.getDeclaredAnnotation(GGAPIEntityOwner.class).ownerId(), String.class, GGAPIEntityOwnerId.class);
			String superOnwerIdFieldName = this.checkAnnotationOrField(entityClass, this.getFieldNameAnnotatedWithAndCheckType(entityClass, GGAPIEntitySuperOwner.class, boolean.class), entityClass.getDeclaredAnnotation(GGAPIEntityOwner.class).superOwner(), boolean.class, GGAPIEntitySuperOwner.class);	
		}
		
		if( ownedEntity ) {
			String ownerIdFieldName = this.checkAnnotationOrField(entityClass, this.getFieldNameAnnotatedWithAndCheckType(entityClass, GGAPIEntityOwnerId.class, String.class), entityClass.getDeclaredAnnotation(GGAPIEntityOwned.class).ownerId(), String.class, GGAPIEntityOwnerId.class);
		}
		
		String saveMethodName = this.checkSaveMethodAnnotationAndMethodParamsHaveGoodTypes(entityClass);
		String deleteMethodName = this.checkDeleteMethodAnnotationAndMethodParamsHaveGoodTypes(entityClass);
		
		boolean publicEntity = this.checkIfAnnotatedEntity(entityClass, GGAPIEntityPublic.class);
		
		boolean hiddenableEntity = this.checkIfAnnotatedEntity(entityClass, GGAPIEntityHiddenable.class);
		if( hiddenableEntity ) {
			String hiddenFieldName = this.checkAnnotationOrField(entityClass, this.getFieldNameAnnotatedWithAndCheckType(entityClass, GGAPIEntityHidden.class, boolean.class), entityClass.getDeclaredAnnotation(GGAPIEntityHiddenable.class).hidden(), boolean.class, GGAPIEntityHidden.class);
		}
		
		boolean geolocalizedEntity = this.checkIfAnnotatedEntity(entityClass, GGAPIEntityGeolocalized.class);
		if( geolocalizedEntity ) {
			String locationFieldName = this.checkAnnotationOrField(entityClass, this.getFieldNameAnnotatedWithAndCheckType(entityClass, GGAPIEntityLocation.class, Point.class), entityClass.getDeclaredAnnotation(GGAPIEntityGeolocalized.class).location(), Point.class, GGAPIEntityLocation.class);
		}
		
		boolean sharedEntity = this.checkIfAnnotatedEntity(entityClass, GGAPIEntityShared.class);
		if( sharedEntity ) {
			String shareFieldName = this.checkAnnotationOrField(entityClass, this.getFieldNameAnnotatedWithAndCheckType(entityClass, GGAPIEntityShare.class, String.class), entityClass.getDeclaredAnnotation(GGAPIEntityShared.class).share(), String.class, GGAPIEntityShare.class);
		}
		
		String repositoryFieldName = this.checkRepositoryAnnotationPresentAndFieldHasGoodType(entityClass);
	}

	private boolean checkIfAnnotatedEntity(Class<?> entityClass, Class<? extends Annotation> typeAnnotation) {
		 Annotation annotation = entityClass.getDeclaredAnnotation(typeAnnotation);

		if( annotation == null ) {
			return false;
		}

		return true;
	}

	private String checkDeleteMethodAnnotationAndMethodParamsHaveGoodTypes(Class<?> entityClass) throws GGAPIEntityException {
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
			methodName = this.checkSaveMethodAnnotationAndMethodParamsHaveGoodTypes( entityClass.getSuperclass() );
		}
		
		if( methodName == null || methodName.isEmpty() ) {
			throw new GGAPIEntityException(GGAPIEntityException.ENTITY_DEFINITION_ERROR, "Entity "+entityClass+" does not have method annotated with @GGAPIEntityDeleteMethod");
		}
		
		return methodName;
	}

	private String checkSaveMethodAnnotationAndMethodParamsHaveGoodTypes(Class<?> entityClass) throws GGAPIEntityException {
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
			methodName = this.checkSaveMethodAnnotationAndMethodParamsHaveGoodTypes( entityClass.getSuperclass() );
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

	private String checkDeleteProviderAnnotationPresentAndFieldHasGoodType(Class<?> entityClass) throws GGAPIEntityException {
		String fieldName = this.getFieldNameAnnotatedWithAndCheckType(entityClass, GGAPIEntityDeleteMethodProvider.class, IGGAPIEntityDeleteMethod.class);
		if( fieldName == null ) {
			throw new GGAPIEntityException(GGAPIEntityException.ENTITY_DEFINITION_ERROR, "Entity "+entityClass+" does not have any field annotated with @GGAPIEntityDeleteMethodProvider");
		}
		return fieldName; 
	}

	private String checkSaveProviderAnnotationPresentAndFieldHasGoodType(Class<?> entityClass) throws GGAPIEntityException {
		String fieldName = this.getFieldNameAnnotatedWithAndCheckType(entityClass, GGAPIEntitySaveMethodProvider.class, IGGAPIEntitySaveMethod.class);
		if( fieldName == null ) {
			throw new GGAPIEntityException(GGAPIEntityException.ENTITY_DEFINITION_ERROR, "Entity "+entityClass+" does not have any field annotated with @GGAPIEntitySaveMethodProvider");
		}
		return fieldName;
	}

	private String checkIdAnnotationPresentAndFieldHasGoodType(Class<?> entityClass) throws GGAPIEntityException {
		String fieldName = this.getFieldNameAnnotatedWithAndCheckType(entityClass, GGAPIEntityId.class, String.class);
		if( fieldName == null ) {
			throw new GGAPIEntityException(GGAPIEntityException.ENTITY_DEFINITION_ERROR, "Entity "+entityClass+" does not have any field annotated with @GGAPIEntityId");
		}
		return fieldName; 
	} 
	
	private String checkRepositoryAnnotationPresentAndFieldHasGoodType(Class<?> entityClass) throws GGAPIEntityException {
		String fieldName = this.getFieldNameAnnotatedWithAndCheckType(entityClass, GGAPIEntityRepository.class, IGGAPIRepository.class);
		if( fieldName == null ) {
			throw new GGAPIEntityException(GGAPIEntityException.ENTITY_DEFINITION_ERROR, "Entity "+entityClass+" does not have any field annotated with @GGAPIEntityRepository");
		}
		return fieldName; 
	} 
	
	private String checkUuidAnnotationPresentAndFieldHasGoodType(Class<?> entityClass) throws GGAPIEntityException {
		String fieldName = this.getFieldNameAnnotatedWithAndCheckType(entityClass, GGAPIEntityUuid.class, String.class);
		if( fieldName == null ) {
			throw new GGAPIEntityException(GGAPIEntityException.ENTITY_DEFINITION_ERROR, "Entity "+entityClass+" does not have any field annotated with @GGAPIEntityUuid");
		}
		return fieldName; 
	} 

	private String checkAnnotationOrField(Class<?> entityClass, String fieldName, String annotationFieldName, Class<?> fieldType, Class<? extends Annotation> annotationClass)
			throws GGAPIEntityException {
		if( annotationFieldName.isEmpty() && fieldName == null ) {
			throw new GGAPIEntityException(GGAPIEntityException.ENTITY_DEFINITION_ERROR, "Entity "+entityClass+" does not have any field value or field annotated with "+annotationClass.getName());
		}
		if( !annotationFieldName.isEmpty() ) {
			this.checkFieldExistsAndIsOfType(entityClass, annotationFieldName, fieldType);
		}
		return annotationFieldName==null?fieldName:annotationFieldName;
	}

	private String checkFieldExistsAndIsOfType(Class<?> entityClass, String fieldName, Class<?> fieldType) throws GGAPIEntityException {
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

	private String getFieldNameAnnotatedWithAndCheckType(Class<?> entityClass, Class<? extends Annotation> annotationClass, Class<?> fieldClass) throws GGAPIEntityException {
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
			return this.getFieldNameAnnotatedWithAndCheckType(entityClass.getSuperclass(), annotationClass, fieldClass);
		} else {
			return fieldName;
		}
	}

	private void checkDtoInAnnotation(GGAPIEntity annotation, Class<?> entityClass) throws GGAPIEntityException {
		if( annotation.dto() == null || annotation.dto().isEmpty() ) {
			throw new GGAPIEntityException(GGAPIEntityException.ENTITY_DEFINITION_ERROR, "No dto provided in annotation of entity "+entityClass);
		} else
			try {
				if( Class.forName(annotation.dto()) == null ) {
					throw new GGAPIEntityException(GGAPIEntityException.ENTITY_DEFINITION_ERROR, "No dto provided in annotation of entity "+entityClass);
				}
			} catch (ClassNotFoundException e) {
				throw new GGAPIEntityException(GGAPIEntityException.ENTITY_DEFINITION_ERROR, "Provided Dto class is not found for entity "+entityClass, e);
			}
	}

	private void checkDomainInAnnotation(GGAPIEntity annotation, Class<?> entityClass) throws GGAPIEntityException {
		if( annotation.domain() == null || annotation.domain().isEmpty() ) {
			throw new GGAPIEntityException(GGAPIEntityException.ENTITY_DEFINITION_ERROR, "No domain provided in annotation of entity "+entityClass);
		}
	}

}
