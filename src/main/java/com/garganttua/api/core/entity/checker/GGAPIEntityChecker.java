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
import com.garganttua.api.core.objects.GGAPIObjectAddress;
import com.garganttua.api.core.objects.query.GGAPIObjectQueryException;
import com.garganttua.api.core.objects.query.GGAPIObjectQueryFactory;
import com.garganttua.api.core.objects.query.IGGAPIObjectQuery;
import com.garganttua.api.repository.IGGAPIRepository;
import com.garganttua.api.security.IGGAPISecurity;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GGAPIEntityChecker {
	
	public record GGAPIEntityInfos (
			String domain,
	        GGAPIObjectAddress uuidFieldAddress,
	        GGAPIObjectAddress idFieldAddress,
	        GGAPIObjectAddress saveProviderFieldAddress,
	        GGAPIObjectAddress deleteProviderFieldAddress,
	        boolean tenantEntity,
	        boolean ownerEntity,
	        boolean ownedEntity,
	        GGAPIObjectAddress tenantIdFieldAddress,
	        GGAPIObjectAddress superTenantFieldAddress,
	        GGAPIObjectAddress ownerIdFieldAddress,
	        GGAPIObjectAddress superOnwerIdFieldAddress,
	        GGAPIObjectAddress saveMethodAddress,
	        GGAPIObjectAddress deleteMethodAddress,
	        boolean publicEntity,
	        boolean hiddenableEntity,
	        GGAPIObjectAddress hiddenFieldAddress,
	        boolean geolocalizedEntity,
	        GGAPIObjectAddress locationFieldAddress,
	        boolean sharedEntity,
	        GGAPIObjectAddress shareFieldAddress,
	        GGAPIObjectAddress repositoryFieldAddress,
	        List<String> mandatoryFields,
	        List<String> unicityFields,
	        GGAPIObjectAddress afterGetMethodAddress,
	        GGAPIObjectAddress beforeCreateMethodAddress, 
	        GGAPIObjectAddress afterCreateMethodAddress, 
	        GGAPIObjectAddress beforeUpdateMethodAddress, 
	        GGAPIObjectAddress afterUpdateMethodAddress, 
	        GGAPIObjectAddress beforeDeleteMethodAddress, 
	        GGAPIObjectAddress afterDeleteMethodAddress, 
	        Map<String, String> updateAuthorizations,
	        GGAPIObjectAddress gotFromRepositoryFieldAddress
	) {
		
		@Override
		public boolean equals(Object obj) {
		    if (this == obj) return true;
		    if (obj == null || getClass() != obj.getClass()) return false;

		    GGAPIEntityInfos other = (GGAPIEntityInfos) obj;

		    return Objects.equals(uuidFieldAddress, other.uuidFieldAddress) &&
		    		Objects.equals(domain, other.domain) &&
		            Objects.equals(idFieldAddress, other.idFieldAddress) &&
		            Objects.equals(saveProviderFieldAddress, other.saveProviderFieldAddress) &&
		            Objects.equals(deleteProviderFieldAddress, other.deleteProviderFieldAddress) &&
		            tenantEntity == other.tenantEntity &&
		            ownerEntity == other.ownerEntity &&
		            ownedEntity == other.ownedEntity &&
		            Objects.equals(tenantIdFieldAddress, other.tenantIdFieldAddress) &&
		            Objects.equals(superTenantFieldAddress, other.superTenantFieldAddress) &&
		            Objects.equals(ownerIdFieldAddress, other.ownerIdFieldAddress) &&
		            Objects.equals(superOnwerIdFieldAddress, other.superOnwerIdFieldAddress) &&
		            Objects.equals(saveMethodAddress, other.saveMethodAddress) &&
		            Objects.equals(deleteMethodAddress, other.deleteMethodAddress) &&
		            publicEntity == other.publicEntity &&
		            hiddenableEntity == other.hiddenableEntity &&
		            Objects.equals(hiddenFieldAddress, other.hiddenFieldAddress) &&
		            geolocalizedEntity == other.geolocalizedEntity &&
		            Objects.equals(locationFieldAddress, other.locationFieldAddress) &&
		            sharedEntity == other.sharedEntity &&
		            Objects.equals(shareFieldAddress, other.shareFieldAddress) &&
		            Objects.equals(repositoryFieldAddress, other.repositoryFieldAddress) &&
		            Objects.equals(mandatoryFields, other.mandatoryFields) &&
		            Objects.equals(unicityFields, other.unicityFields) &&
		            Objects.equals(afterGetMethodAddress, other.afterGetMethodAddress) &&
		            Objects.equals(beforeCreateMethodAddress, other.beforeCreateMethodAddress) &&
		            Objects.equals(afterCreateMethodAddress, other.afterCreateMethodAddress) &&
		            Objects.equals(beforeUpdateMethodAddress, other.beforeUpdateMethodAddress) &&
		            Objects.equals(afterUpdateMethodAddress, other.afterUpdateMethodAddress) &&
		            Objects.equals(beforeDeleteMethodAddress, other.beforeDeleteMethodAddress) &&
		            Objects.equals(afterDeleteMethodAddress, other.afterDeleteMethodAddress) &&
		            Objects.equals(updateAuthorizations, other.updateAuthorizations) &&
		            Objects.equals(gotFromRepositoryFieldAddress, other.gotFromRepositoryFieldAddress);
		}
	}

	public static GGAPIEntityInfos checkEntityClass(Class<?> entityClass) throws GGAPIEntityException {
		if (log.isDebugEnabled()) {
			log.debug("Checking entity infos from class " + entityClass.getName());
		}
		GGAPIEntity annotation = entityClass.getDeclaredAnnotation(GGAPIEntity.class);
		String domain = GGAPIEntityChecker.checkDomainInAnnotation(annotation, entityClass);
		String uuidFieldAddress = GGAPIEntityChecker.checkUuidAnnotationPresentAndFieldHasGoodType(entityClass);
		String idFieldAddress = GGAPIEntityChecker.checkIdAnnotationPresentAndFieldHasGoodType(entityClass);
		String saveProviderFieldAddress = GGAPIEntityChecker.checkSaveProviderAnnotationPresentAndFieldHasGoodType(entityClass);
		String deleteProviderFieldAddress = GGAPIEntityChecker.checkDeleteProviderAnnotationPresentAndFieldHasGoodType(entityClass);
		boolean tenantEntity = GGAPIEntityChecker.checkIfAnnotatedEntity(entityClass, GGAPIEntityTenant.class);
		String tenantIdFieldAddress = null;
		String superTenantFieldAddress = null;
		
		if( tenantEntity ) {
			tenantIdFieldAddress = GGAPIEntityChecker.checkAnnotationOrField(entityClass, GGAPIEntityChecker.getFieldAddressAnnotatedWithAndCheckType(entityClass, GGAPIEntityTenantId.class, String.class), entityClass.getDeclaredAnnotation(GGAPIEntityTenant.class).tenantId(), String.class, GGAPIEntityTenantId.class);
			superTenantFieldAddress = GGAPIEntityChecker.checkAnnotationOrField(entityClass, GGAPIEntityChecker.getFieldAddressAnnotatedWithAndCheckType(entityClass, GGAPIEntitySuperTenant.class, boolean.class), entityClass.getDeclaredAnnotation(GGAPIEntityTenant.class).superTenant(), boolean.class, GGAPIEntitySuperTenant.class);
		}
		
		boolean ownerEntity = GGAPIEntityChecker.checkIfAnnotatedEntity(entityClass, GGAPIEntityOwner.class);
		boolean ownedEntity = GGAPIEntityChecker.checkIfAnnotatedEntity(entityClass, GGAPIEntityOwned.class);
		
		if(ownerEntity &&  ownedEntity) {
			throw new GGAPIEntityException(GGAPIEntityException.ENTITY_DEFINITION_ERROR, "Entity "+entityClass.getSimpleName()+" Cannot be owner and owned at the same time");
		}
		
		String ownerIdFieldAddress = null;
		String superOnwerIdFieldAddress = null;
		if( ownerEntity ) {
			ownerIdFieldAddress = GGAPIEntityChecker.checkAnnotationOrField(entityClass, GGAPIEntityChecker.getFieldAddressAnnotatedWithAndCheckType(entityClass, GGAPIEntityOwnerId.class, String.class), entityClass.getDeclaredAnnotation(GGAPIEntityOwner.class).ownerId(), String.class, GGAPIEntityOwnerId.class);
			superOnwerIdFieldAddress = GGAPIEntityChecker.checkAnnotationOrField(entityClass, GGAPIEntityChecker.getFieldAddressAnnotatedWithAndCheckType(entityClass, GGAPIEntitySuperOwner.class, boolean.class), entityClass.getDeclaredAnnotation(GGAPIEntityOwner.class).superOwner(), boolean.class, GGAPIEntitySuperOwner.class);	
		}
		
		if( ownedEntity ) {
			ownerIdFieldAddress = GGAPIEntityChecker.checkAnnotationOrField(entityClass, GGAPIEntityChecker.getFieldAddressAnnotatedWithAndCheckType(entityClass, GGAPIEntityOwnerId.class, String.class), entityClass.getDeclaredAnnotation(GGAPIEntityOwned.class).ownerId(), String.class, GGAPIEntityOwnerId.class);
		}
		
		String saveMethodAddress = GGAPIEntityChecker.checkSaveMethodAnnotationAndMethodParamsHaveGoodTypes(entityClass);
		String deleteMethodAddress = GGAPIEntityChecker.checkDeleteMethodAnnotationAndMethodParamsHaveGoodTypes(entityClass);
		
		boolean publicEntity = GGAPIEntityChecker.checkIfAnnotatedEntity(entityClass, GGAPIEntityPublic.class);
		
		boolean hiddenableEntity = GGAPIEntityChecker.checkIfAnnotatedEntity(entityClass, GGAPIEntityHiddenable.class);
		String hiddenFieldAddress = null;
		if( hiddenableEntity ) {
			hiddenFieldAddress = GGAPIEntityChecker.checkAnnotationOrField(entityClass, GGAPIEntityChecker.getFieldAddressAnnotatedWithAndCheckType(entityClass, GGAPIEntityHidden.class, boolean.class), entityClass.getDeclaredAnnotation(GGAPIEntityHiddenable.class).hidden(), boolean.class, GGAPIEntityHidden.class);
		}
		
		boolean geolocalizedEntity = GGAPIEntityChecker.checkIfAnnotatedEntity(entityClass, GGAPIEntityGeolocalized.class);
		String locationFieldAddress = null;
		if( geolocalizedEntity ) {
			locationFieldAddress = GGAPIEntityChecker.checkAnnotationOrField(entityClass, GGAPIEntityChecker.getFieldAddressAnnotatedWithAndCheckType(entityClass, GGAPIEntityLocation.class, Point.class), entityClass.getDeclaredAnnotation(GGAPIEntityGeolocalized.class).location(), Point.class, GGAPIEntityLocation.class);
		}
		
		boolean sharedEntity = GGAPIEntityChecker.checkIfAnnotatedEntity(entityClass, GGAPIEntityShared.class);
		String shareFieldAddress = null;
		if( sharedEntity ) {
			shareFieldAddress = GGAPIEntityChecker.checkAnnotationOrField(entityClass, GGAPIEntityChecker.getFieldAddressAnnotatedWithAndCheckType(entityClass, GGAPIEntityShare.class, String.class), entityClass.getDeclaredAnnotation(GGAPIEntityShared.class).share(), String.class, GGAPIEntityShare.class);
		}
		
		String repositoryFieldAddress = GGAPIEntityChecker.checkRepositoryAnnotationPresentAndFieldHasGoodType(entityClass);
		
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
		
		String gotFromReposiotryFieldAddress = GGAPIEntityChecker.checkGotFromRepositoryAnnotationPresentAndFieldHasGoodType(entityClass);;
		
		GGAPIEntityChecker.checkConstructor(entityClass);

		try {
			IGGAPIObjectQuery q = GGAPIObjectQueryFactory.objectQuery(entityClass);
			return new GGAPIEntityInfos (
					domain, 
					q.address(uuidFieldAddress), 
					q.address(idFieldAddress), 
					q.address(saveProviderFieldAddress), 
					q.address(deleteProviderFieldAddress), 
					tenantEntity, 
					ownerEntity, 
					ownedEntity, 
					tenantIdFieldAddress==null?null:q.address(tenantIdFieldAddress),
					superTenantFieldAddress==null?null:q.address(superTenantFieldAddress),
					ownerIdFieldAddress==null?null:q.address(ownerIdFieldAddress),
					superOnwerIdFieldAddress==null?null:q.address(superOnwerIdFieldAddress),
					q.address(saveMethodAddress), 
					q.address(deleteMethodAddress), 
					publicEntity, 
					hiddenableEntity, 
					hiddenFieldAddress==null?null:q.address(hiddenFieldAddress), 
					geolocalizedEntity, 
					locationFieldAddress==null?null:q.address(locationFieldAddress), 
					sharedEntity, 
					shareFieldAddress==null?null:q.address(shareFieldAddress), 
					q.address(repositoryFieldAddress), 
					mandatoryFields, 
					unicityFields,
					afterGetm==null?null:q.address(afterGetm.getName()),
					beforeCreatem==null?null:q.address(beforeCreatem.getName()),
					afterCreatem==null?null:q.address(afterCreatem.getName()),
					beforeUpdatem==null?null:q.address(beforeUpdatem.getName()),
					afterUpdatem==null?null:q.address(afterUpdatem.getName()),
					beforeDeletem==null?null:q.address(beforeDeletem.getName()),
					afterDeletem==null?null:q.address(afterDeletem.getName()),
					updateAuthorizations,
					q.address(gotFromReposiotryFieldAddress));
		} catch (GGAPIObjectQueryException e) {
			throw new GGAPIEntityException(e);
		}
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
		String fieldAddress = GGAPIEntityChecker.getFieldAddressAnnotatedWithAndCheckType(entityClass, GGAPIEntityGotFromRepository.class, boolean.class);
		if( fieldAddress == null ) {
			throw new GGAPIEntityException(GGAPIEntityException.ENTITY_DEFINITION_ERROR, "Entity "+entityClass.getSimpleName()+" does not have any field annotated with @GGAPIEntityGotFromRepository");
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
		        throw new GGAPIEntityException(GGAPIEntityException.ENTITY_DEFINITION_ERROR, "Entity " + entityClass.getSimpleName() + " must have at least one constructor with no args");
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

	private static boolean checkIfAnnotatedEntity(Class<?> entityClass, Class<? extends Annotation> typeAnnotation) {
		 Annotation annotation = entityClass.getDeclaredAnnotation(typeAnnotation);

		if( annotation == null ) {
			return false;
		}

		return true;
	}

	private static String checkDeleteMethodAnnotationAndMethodParamsHaveGoodTypes(Class<?> entityClass) throws GGAPIEntityException {
		String methodAddress = null;
		for( Method method : entityClass.getDeclaredMethods() ) {
			if( method.isAnnotationPresent(GGAPIEntityDeleteMethod.class)) {
				if( methodAddress != null && !methodAddress.isEmpty() ) {
					throw new GGAPIEntityException(GGAPIEntityException.ENTITY_DEFINITION_ERROR, "Entity "+entityClass.getSimpleName()+" has more than one method annotated with "+GGAPIEntityDeleteMethod.class);
				}
				methodAddress = method.getName();
				Type[] parameters = method.getGenericParameterTypes();
				if( !parameters[0].equals(IGGAPICaller.class) ) {
					throw new GGAPIEntityException(GGAPIEntityException.ENTITY_DEFINITION_ERROR, "Entity "+entityClass.getSimpleName()+" has method "+methodAddress+" but parameter 0 is not of type "+IGGAPICaller.class);
				}
				if( !isMapOfString(parameters[1]) ) {
					throw new GGAPIEntityException(GGAPIEntityException.ENTITY_DEFINITION_ERROR, "Entity "+entityClass.getSimpleName()+" has method "+methodAddress+" but parameter 1 is not of type java.lang.Map<java.lang.String,java.lang.String>");
				}
			}
		}
		
		if( methodAddress == null && entityClass.getSuperclass() != null ) {
			methodAddress = GGAPIEntityChecker.checkDeleteMethodAnnotationAndMethodParamsHaveGoodTypes( entityClass.getSuperclass() );
		}
		
		if( methodAddress == null || methodAddress.isEmpty() ) {
			throw new GGAPIEntityException(GGAPIEntityException.ENTITY_DEFINITION_ERROR, "Entity "+entityClass.getSimpleName()+" does not have method annotated with @GGAPIEntityDeleteMethod");
		}
		
		return methodAddress;
	}

	private static String checkSaveMethodAnnotationAndMethodParamsHaveGoodTypes(Class<?> entityClass) throws GGAPIEntityException {
		String methodAddress = null;
		for( Method method : entityClass.getDeclaredMethods() ) {
			if( method.isAnnotationPresent(GGAPIEntitySaveMethod.class)) {
				
				if( methodAddress != null && !methodAddress.isEmpty() ) {
					throw new GGAPIEntityException(GGAPIEntityException.ENTITY_DEFINITION_ERROR, "Entity "+entityClass.getSimpleName()+" has more than one method annotated with "+GGAPIEntitySaveMethod.class);
				}
				methodAddress = method.getName();
				Type[] parameters = method.getGenericParameterTypes();
				if( !parameters[0].equals(IGGAPICaller.class) ) {
					throw new GGAPIEntityException(GGAPIEntityException.ENTITY_DEFINITION_ERROR, "Entity "+entityClass.getSimpleName()+" has method "+methodAddress+" but parameter 0 is not of type "+IGGAPICaller.class);
				}
				if( !isMapOfString(parameters[1]) ) {
					throw new GGAPIEntityException(GGAPIEntityException.ENTITY_DEFINITION_ERROR, "Entity "+entityClass.getSimpleName()+" has method "+methodAddress+" but parameter 1 is not of type java.lang.Map<java.lang.String,java.lang.String>");
				}
				if( !isOptionalIGGAPISecurity(parameters[2]) ) {
					throw new GGAPIEntityException(GGAPIEntityException.ENTITY_DEFINITION_ERROR, "Entity "+entityClass.getSimpleName()+" has method "+methodAddress+" but parameter 2 is not of type Optional<IGGAPISecurity>");
				}
			}
		}
		
		if( methodAddress == null && entityClass.getSuperclass() != null ) {
			methodAddress = GGAPIEntityChecker.checkSaveMethodAnnotationAndMethodParamsHaveGoodTypes( entityClass.getSuperclass() );
		}
		
		if( methodAddress == null || methodAddress.isEmpty() ) {
			throw new GGAPIEntityException(GGAPIEntityException.ENTITY_DEFINITION_ERROR, "Entity "+entityClass.getSimpleName()+" does not have method annotated with @GGAPIEntitySaveMethod");
		}
		
		return methodAddress;
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
		String fieldAddress = GGAPIEntityChecker.getFieldAddressAnnotatedWithAndCheckType(entityClass, GGAPIEntityDeleteMethodProvider.class, IGGAPIEntityDeleteMethod.class);
		if( fieldAddress == null ) {
			throw new GGAPIEntityException(GGAPIEntityException.ENTITY_DEFINITION_ERROR, "Entity "+entityClass.getSimpleName()+" does not have any field annotated with @GGAPIEntityDeleteMethodProvider");
		}
		return fieldAddress; 
	}

	private static String checkSaveProviderAnnotationPresentAndFieldHasGoodType(Class<?> entityClass) throws GGAPIEntityException {
		String fieldAddress = GGAPIEntityChecker.getFieldAddressAnnotatedWithAndCheckType(entityClass, GGAPIEntitySaveMethodProvider.class, IGGAPIEntitySaveMethod.class);
		if( fieldAddress == null ) {
			throw new GGAPIEntityException(GGAPIEntityException.ENTITY_DEFINITION_ERROR, "Entity "+entityClass.getSimpleName()+" does not have any field annotated with @GGAPIEntitySaveMethodProvider");
		}
		return fieldAddress;
	}

	private static String checkIdAnnotationPresentAndFieldHasGoodType(Class<?> entityClass) throws GGAPIEntityException {
		String fieldAddress = GGAPIEntityChecker.getFieldAddressAnnotatedWithAndCheckType(entityClass, GGAPIEntityId.class, String.class);
		if( fieldAddress == null ) {
			throw new GGAPIEntityException(GGAPIEntityException.ENTITY_DEFINITION_ERROR, "Entity "+entityClass.getSimpleName()+" does not have any field annotated with @GGAPIEntityId");
		}
		return fieldAddress; 
	} 
	
	private static String checkRepositoryAnnotationPresentAndFieldHasGoodType(Class<?> entityClass) throws GGAPIEntityException {
		String fieldAddress = GGAPIEntityChecker.getFieldAddressAnnotatedWithAndCheckType(entityClass, GGAPIEntityRepository.class, IGGAPIRepository.class);
		if( fieldAddress == null ) {
			throw new GGAPIEntityException(GGAPIEntityException.ENTITY_DEFINITION_ERROR, "Entity "+entityClass.getSimpleName()+" does not have any field annotated with @GGAPIEntityRepository");
		}
		return fieldAddress; 
	} 
	
	private static String checkUuidAnnotationPresentAndFieldHasGoodType(Class<?> entityClass) throws GGAPIEntityException {
		String fieldAddress = GGAPIEntityChecker.getFieldAddressAnnotatedWithAndCheckType(entityClass, GGAPIEntityUuid.class, String.class);
		if( fieldAddress == null ) {
			throw new GGAPIEntityException(GGAPIEntityException.ENTITY_DEFINITION_ERROR, "Entity "+entityClass.getSimpleName()+" does not have any field annotated with @GGAPIEntityUuid");
		}
		return fieldAddress; 
	} 

	private static String checkAnnotationOrField(Class<?> entityClass, String fieldAddress, String annotationFieldAddress, Class<?> fieldType, Class<? extends Annotation> annotationClass)
			throws GGAPIEntityException {
		if( annotationFieldAddress.isEmpty() && fieldAddress == null ) {
			throw new GGAPIEntityException(GGAPIEntityException.ENTITY_DEFINITION_ERROR, "Entity "+entityClass.getSimpleName()+" does not have any field value or field annotated with "+annotationClass.getName());
		}
		if( !annotationFieldAddress.isEmpty() ) {
			GGAPIEntityChecker.checkFieldExistsAndIsOfType(entityClass, annotationFieldAddress, fieldType);
		}
		return annotationFieldAddress.isEmpty()?fieldAddress:annotationFieldAddress;
	}

	private static String checkFieldExistsAndIsOfType(Class<?> entityClass, String fieldAddress, Class<?> fieldType) throws GGAPIEntityException {
		try {
			Field field = entityClass.getDeclaredField(fieldAddress);
			if( !field.getType().equals(fieldType) ) {
				throw new GGAPIEntityException(GGAPIEntityException.ENTITY_DEFINITION_ERROR, "Entity "+entityClass.getSimpleName()+" has field "+fieldAddress+" with wrong type "+field.getType()+", should be "+fieldType);
			} else {
				return fieldAddress;
			}
		} catch (NoSuchFieldException | SecurityException e) {
			throw new GGAPIEntityException(GGAPIEntityException.ENTITY_DEFINITION_ERROR, "Entity "+entityClass.getSimpleName()+" does not have field "+fieldAddress);
		}
	}

	private static String getFieldAddressAnnotatedWithAndCheckType(Class<?> entityClass, Class<? extends Annotation> annotationClass, Class<?> fieldClass) throws GGAPIEntityException {
		String fieldAddress = null;
		for( Field field: entityClass.getDeclaredFields() ) {
			if( field.isAnnotationPresent(annotationClass) ) {
				if( fieldAddress != null && !fieldAddress.isEmpty() ) {
					throw new GGAPIEntityException(GGAPIEntityException.ENTITY_DEFINITION_ERROR, "Entity "+entityClass.getSimpleName()+" has more than one field annotated with "+annotationClass);
				}
				if( field.getType().equals(fieldClass) ) {
					fieldAddress = field.getName();
				} else {
					throw new GGAPIEntityException(GGAPIEntityException.ENTITY_DEFINITION_ERROR, "Entity "+entityClass.getSimpleName()+" has field "+field.getName()+" with wrong type "+field.getType().getName()+", should be "+fieldClass);
				}
			}
		}

		if( entityClass.getSuperclass() != null && fieldAddress == null ) {
			return GGAPIEntityChecker.getFieldAddressAnnotatedWithAndCheckType(entityClass.getSuperclass(), annotationClass, fieldClass);
		} else {
			return fieldAddress;
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
			throw new GGAPIEntityException(GGAPIEntityException.ENTITY_DEFINITION_ERROR, "No domain provided in annotation of entity "+entityClass.getSimpleName());
		} else {
			return annotation.domain();
		}
	}

	public static GGAPIEntityInfos checkEntity(Object entity) throws GGAPIEntityException {
		return GGAPIEntityChecker.checkEntityClass(entity.getClass());
	}

}
