package com.garganttua.api.core.mapper;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.garganttua.api.core.dto.checker.GGAPIDtoChecker;
import com.garganttua.api.core.dto.checker.GGAPIDtoChecker.DtoClassInfos;
import com.garganttua.api.core.dto.checker.GGAPIDtoChecker.DtoFieldMapping;
import com.garganttua.api.core.dto.exceptions.GGAPIDtoException;
import com.garganttua.api.core.entity.annotations.GGAPIEntity;
import com.garganttua.api.core.entity.checker.GGAPIEntityChecker;
import com.garganttua.api.core.entity.checker.GGAPIEntityChecker.EntityClassInfos;
import com.garganttua.api.core.entity.exceptions.GGAPIEntityException;
import com.garganttua.api.core.tools.GGAPIConstructorAccessManager;
import com.garganttua.api.core.tools.GGAPIFieldAccessManager;
import com.garganttua.api.core.tools.GGAPIMethodAccessManager;
import com.garganttua.api.core.tools.GGAPIObjectReflectionHelper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GGAPIMapper {
	
	public Object map(Object object) throws GGAPIMapperException {
		if( object.getClass().isAnnotationPresent(GGAPIEntity.class) ) {
			return this.mapToDto(object);
		} else {
			return null;
		}
	}

	public Object mapToDto(Object entity) throws GGAPIMapperException {
		try {
			EntityClassInfos entityInfos = new GGAPIEntityChecker().checkEntityClass(entity.getClass());
			DtoClassInfos dtoInfos = new GGAPIDtoChecker().checkDtoClass(entityInfos.dtoClass(), entity.getClass(), entityInfos);
			return this.mapToDto(entity, entityInfos, dtoInfos);
		} catch (GGAPIEntityException e) {
			throw new GGAPIMapperException(GGAPIMapperException.ENTITY_DEFINITION_ERROR, e);
		} catch (GGAPIDtoException e) {
			throw new GGAPIMapperException(GGAPIMapperException.DTO_DEFINITION_ERROR, e);
		}
	}

	public Object mapToDto(Object entity, EntityClassInfos entityInfos, DtoClassInfos dtoInfos) throws GGAPIMapperException {
		
		Class<?> dtoClass = entityInfos.dtoClass();
		Constructor<?> constructorWithNoParams = GGAPIObjectReflectionHelper.getConstructorWithNoParams(dtoClass);
		
		try( GGAPIConstructorAccessManager manager = new GGAPIConstructorAccessManager(constructorWithNoParams) ){
			Object dto = constructorWithNoParams.newInstance();
				
			for( String dtoFieldName: dtoInfos.mappings().keySet() ) {
				if( dtoFieldName.equals(GGAPIDtoChecker.OBJECT_MAPPING_RULE) ) {
					this.doObjectMappingToDto(dto, entity, dtoInfos.mappings().get(dtoFieldName));
				} else {
					this.doMappingToDto(dto, entity, dtoFieldName, dtoInfos.mappings().get(dtoFieldName));
				}
			}
			
			return dto;
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			throw new GGAPIMapperException(GGAPIMapperException.DTO_INSTANCIATION_ERROR, "Unable to instanciate dto object of type "+dtoClass, e);
		} 

	}

	private void doObjectMappingToDto(Object dto, Object entity, DtoFieldMapping dtoFieldMapping) throws GGAPIMapperException {
		if( log.isDebugEnabled() )
			log.debug("Object mapping : method "+dtoFieldMapping.fromEntityMethod()+" of object "+dto.getClass());

		Method method = GGAPIObjectReflectionHelper.getMethod(dto.getClass(), dtoFieldMapping.fromEntityMethod());
		try( GGAPIMethodAccessManager manager = new GGAPIMethodAccessManager(method) ){
			method.invoke(dto, entity);
		} catch (IllegalAccessException | InvocationTargetException e) {
			if( log.isDebugEnabled() )
				log.warn("Cannot map with object method "+dtoFieldMapping.fromEntityMethod()+" of object "+dto.getClass(), e);
			throw new GGAPIMapperException(GGAPIMapperException.DIRECT_FIELD_MAPPING, "Cannot map with object method "+dtoFieldMapping.fromEntityMethod()+" of object "+dto.getClass(), e);
		} 
	}

	private void doMappingToDto(Object dto, Object entity, String dtoFieldName, DtoFieldMapping dtoFieldMapping) throws GGAPIMapperException {
		if( dtoFieldMapping.toEntityMethod().isEmpty() ) {
			this.doDirectMappingToDto(dto, entity, dtoFieldName, dtoFieldMapping.entityFieldName());
		} else {
			this.doMethodMappingToDto(dto, entity, dtoFieldName, dtoFieldMapping.entityFieldName(), dto, dtoFieldMapping.fromEntityMethod());
		}
	}

	private void doMethodMappingToDto(Object toObject, Object fromObject, String toFieldName, String fromFieldName, Object methodObject, String methodName) throws GGAPIMapperException {
		if( log.isDebugEnabled() )
			log.debug("Method mapping : method "+methodName+" of object "+methodObject.getClass()+" from field "+fromFieldName+" of object "+fromObject.getClass()+" to field "+toFieldName+" of object "+toObject.getClass().getName());
	
		Field toField = GGAPIObjectReflectionHelper.getField(toObject.getClass(), toFieldName);
		Field fromField = GGAPIObjectReflectionHelper.getField(fromObject.getClass(), fromFieldName);
		Method method = GGAPIObjectReflectionHelper.getMethod(methodObject.getClass(), methodName);	

		try( GGAPIMethodAccessManager manager = new GGAPIMethodAccessManager(method) ){
			try( GGAPIFieldAccessManager manager2 = new GGAPIFieldAccessManager(fromField) ){
				try( GGAPIFieldAccessManager manager3 = new GGAPIFieldAccessManager(toField) ){
					Object value = method.invoke(methodObject, fromField.get(fromObject));
					toField.set(toObject, value);
				} catch (IllegalAccessException | InvocationTargetException e) {
					if( log.isDebugEnabled() )
						log.warn("Cannot map with method "+methodName+" to field "+toFieldName+" of object "+toObject.getClass().getName(), e);
					throw new GGAPIMapperException(GGAPIMapperException.DIRECT_FIELD_MAPPING, "Cannot map with method "+methodName+" to field "+toFieldName+" of object "+toObject.getClass().getName(), e);
				} 
			}
		}
		
	}

	private void doDirectMappingToDto(Object toObject, Object fromObject, String toFieldName, String fromFieldName) throws GGAPIMapperException {
		if( log.isDebugEnabled() )
			log.debug("Direct mapping : from field "+fromFieldName+" of object "+fromObject.getClass().getName()+" to field "+toFieldName+" of object "+toObject.getClass().getName());
	
		Field dtoField = GGAPIObjectReflectionHelper.getField(toObject.getClass(), toFieldName);
		Field entityField = GGAPIObjectReflectionHelper.getField(fromObject.getClass(), fromFieldName);
		
		try( GGAPIFieldAccessManager manager = new GGAPIFieldAccessManager(entityField) ){
			try( GGAPIFieldAccessManager manager2 = new GGAPIFieldAccessManager(dtoField) ){
				dtoField.set(toObject, entityField.get(fromObject));
			} catch (IllegalArgumentException | IllegalAccessException e) {
				if( log.isDebugEnabled() )
					log.warn("Cannot map field "+fromFieldName+" of object "+fromObject.getClass().getName()+" to field "+toFieldName+" of object "+toObject.getClass().getName(), e);
				throw new GGAPIMapperException(GGAPIMapperException.DIRECT_FIELD_MAPPING, "Cannot map field "+fromFieldName+" of object "+fromObject.getClass().getName()+" to field "+toFieldName+" of object "+toObject.getClass().getName(), e);
			} 
		}
	}

}
