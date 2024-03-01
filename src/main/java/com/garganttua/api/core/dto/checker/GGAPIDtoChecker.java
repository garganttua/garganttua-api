package com.garganttua.api.core.dto.checker;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.garganttua.api.core.dto.annotations.GGAPIDtoFieldMapping;
import com.garganttua.api.core.dto.annotations.GGAPIDtoObjectMapping;
import com.garganttua.api.core.dto.exceptions.GGAPIDtoException;
import com.garganttua.api.core.entity.checker.GGAPIEntityChecker;
import com.garganttua.api.core.entity.checker.GGAPIEntityChecker.EntityClassInfos;
import com.garganttua.api.core.entity.exceptions.GGAPIEntityException;
import com.garganttua.api.core.tools.GGAPIObjectReflectionHelper;

public class GGAPIDtoChecker {
	
	public static final String OBJECT_MAPPING_RULE = "OBJECT_MAPPING";

	public record DtoClassInfos(
			Map<String, DtoFieldMapping> mappings
	) {
		@Override
	    public String toString() {
	        return "DtoClassValidationResult{" +
	               "mappings=" + mappings +
	               '}';
	    }

	    @Override
	    public boolean equals(Object obj) {
	        if (this == obj) {
	            return true;
	        }
	        if (obj == null || getClass() != obj.getClass()) {
	            return false;
	        }
	        DtoClassInfos that = (DtoClassInfos) obj;
	        return Objects.equals(mappings, that.mappings);
	    }

	    @Override
	    public int hashCode() {
	        return Objects.hash(mappings);
	    }
	}
	
	public record DtoFieldMapping (
		String entityFieldName,
		String fromEntityMethod,
		String toEntityMethod
	) {
	    @Override
	    public String toString() {
	        return String.format("DtoFieldMapping{entityField='%s', fromEntityMethod='%s', toEntityMethod='%s'}",
	                             entityFieldName, fromEntityMethod, toEntityMethod);
	    }

	    @Override
	    public boolean equals(Object obj) {
	        if (this == obj) {
	            return true;
	        }
	        if (obj == null || getClass() != obj.getClass()) {
	            return false;
	        }
	        DtoFieldMapping that = (DtoFieldMapping) obj;
	        return Objects.equals(entityFieldName, that.entityFieldName) &&
	               Objects.equals(fromEntityMethod, that.fromEntityMethod) &&
	               Objects.equals(toEntityMethod, that.toEntityMethod);
	    }

	    @Override
	    public int hashCode() {
	        return Objects.hash(entityFieldName, fromEntityMethod, toEntityMethod);
	    }
	}
	
	public DtoClassInfos checkDtoClass(Class<?> dtoClass, Class<?> entityClass) throws GGAPIDtoException {
		
		EntityClassInfos entityInfos = null;
		try {
			entityInfos = new GGAPIEntityChecker().checkEntityClass(entityClass);
		} catch (GGAPIEntityException e) {
			throw new GGAPIDtoException(GGAPIDtoException.ENTITY_DEFINITION_ERROR, e);
		}
		return this.checkDtoClass(dtoClass, entityClass, entityInfos);
	}
	
	public DtoClassInfos checkDtoClass(Class<?> dtoClass, Class<?> entityClass, EntityClassInfos infos) throws GGAPIDtoException {
		Map<String, DtoFieldMapping> mappings = new HashMap<String, GGAPIDtoChecker.DtoFieldMapping>();

		if( !dtoClass.isAnnotationPresent(GGAPIDtoObjectMapping.class) ) {
			mappings = this.getMappings(mappings, dtoClass, entityClass);	
			
			this.checkMappingForField(mappings, infos.uuidFieldName(), dtoClass);
			this.checkMappingForField(mappings, infos.idFieldName(), dtoClass);
			
			if( infos.tenantEntity() ) {
				this.checkMappingForField(mappings, infos.tenantIdFieldName(), dtoClass);
				this.checkMappingForField(mappings, infos.superTenantFieldName(), dtoClass);
			}
			if( infos.ownerEntity() ) {
				this.checkMappingForField(mappings, infos.ownerIdFieldName(), dtoClass);
				this.checkMappingForField(mappings, infos.superOnwerIdFieldName(), dtoClass);
			}
			if( infos.ownedEntity() ) {
				this.checkMappingForField(mappings, infos.ownerIdFieldName(), dtoClass);
			}

	        if( infos.hiddenableEntity() ) {
	        	this.checkMappingForField(mappings, infos.hiddenFieldName(), dtoClass);
			}
	        if( infos.geolocalizedEntity() ) {
	        	this.checkMappingForField(mappings, infos.locationFieldName(), dtoClass);
			}
	        if( infos.sharedEntity() ) {
	        	this.checkMappingForField(mappings, infos.shareFieldName(), dtoClass);
			}
		} else {
			mappings = this.getObjectMapping(mappings, dtoClass, dtoClass.getDeclaredAnnotation(GGAPIDtoObjectMapping.class), entityClass);
		}

		return new DtoClassInfos(mappings);
	}

	private Map<String, DtoFieldMapping> getObjectMapping(Map<String, DtoFieldMapping> mappings, Class<?> dtoClass, GGAPIDtoObjectMapping annotation, Class<?> entityClass) throws GGAPIDtoException {
		this.checkObjectMethod(annotation.fromMethod(), entityClass, dtoClass);
		this.checkObjectMethod(annotation.toMethod(), entityClass, dtoClass);
		mappings.put(GGAPIDtoChecker.OBJECT_MAPPING_RULE, new DtoFieldMapping(null, annotation.fromMethod(), annotation.toMethod()));
		return mappings;
	}

	private void checkObjectMethod(String methodName, Class<?> entityClass, Class<?> dtoClass) throws GGAPIDtoException {
		try {
			dtoClass.getDeclaredMethod(methodName, entityClass);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new GGAPIDtoException(GGAPIDtoException.DTO_DEFINITION_ERROR,"Dto " + dtoClass + " does not have method "+methodName+"("+entityClass+")" );
		}
	}

	private void checkMappingForField(Map<String, DtoFieldMapping> mappings, String searchField, Class<?> dtoClass) throws GGAPIDtoException {
		boolean found = false;
		for(DtoFieldMapping mapping: mappings.values()) {
			if( mapping.entityFieldName().equals(searchField) ) {
				found = true;
				break;
			}
		}
		if( !found ) {
			throw new GGAPIDtoException(GGAPIDtoException.DTO_DEFINITION_ERROR,"Dto " + dtoClass + " does not have mapping rule for "+searchField+" entity field");
		}
	}

	private Map<String, DtoFieldMapping> getMappings(Map<String, DtoFieldMapping> mappings, Class<?> dtoClass, Class<?> entityClass) throws GGAPIDtoException {
		
		for( Field dtoField: dtoClass.getDeclaredFields() ) {
			if( dtoField.isAnnotationPresent(GGAPIDtoFieldMapping.class) ) {
				String fieldName = dtoField.getName();
				GGAPIDtoFieldMapping annotation = dtoField.getAnnotation(GGAPIDtoFieldMapping.class);
				
				if( annotation.entityField().isEmpty()  ) {
					throw new GGAPIDtoException(GGAPIDtoException.DTO_DEFINITION_ERROR,"Dto " + dtoClass + " field "+dtoField.getName()+ " does not have entityField defined");
				}
				
				Field entityField = GGAPIObjectReflectionHelper.getField(entityClass, annotation.entityField());
				if( !annotation.entityField().isEmpty() && (annotation.fromMethod().isEmpty() || annotation.toMethod().isEmpty())) {
					this.checkFieldsType(dtoField, entityField, dtoClass);
				}
				if( !annotation.fromMethod().isEmpty() ) {
					this.checkFieldMethod(annotation.fromMethod(), entityField, dtoField, dtoClass);
				}
				if( !annotation.toMethod().isEmpty() ) {
					this.checkFieldMethod(annotation.toMethod(), dtoField, entityField, dtoClass);
				}
				mappings.put(fieldName, new DtoFieldMapping(annotation.entityField(), annotation.fromMethod(), annotation.toMethod()));
			}
		}
		
		if( dtoClass.getSuperclass() != null ) {
			return this.getMappings(mappings, dtoClass.getSuperclass(), entityClass);
		}
		return mappings;
	}

	private void checkFieldMethod(String methodName, Field fromField, Field toField, Class<?> dtoClass) throws GGAPIDtoException {
		try {
			Method method = dtoClass.getDeclaredMethod(methodName, fromField.getType());
			if( !method.getReturnType().equals(toField.getType()) ) {
				throw new GGAPIDtoException(GGAPIDtoException.DTO_DEFINITION_ERROR,"Dto " + dtoClass + " does not have method "+toField.getGenericType()+" "+methodName+"("+fromField.getGenericType()+")" );
			}
		} catch (NoSuchMethodException | SecurityException e) {
			throw new GGAPIDtoException(GGAPIDtoException.DTO_DEFINITION_ERROR,"Dto " + dtoClass + " does not have method "+toField.getGenericType()+" "+methodName+"("+fromField.getGenericType()+")" );
		}
	}

	private void checkFieldsType(Field dtoField, Field entityField, Class<?> dtoClass) throws GGAPIDtoException {
		if( !entityField.getType().equals(dtoField.getType()) ) {
			throw new GGAPIDtoException(GGAPIDtoException.DTO_DEFINITION_ERROR,"Dto " + dtoClass + " field "+dtoField.getName()+" must be of type "+ entityField.getType());
		}
	}

}
