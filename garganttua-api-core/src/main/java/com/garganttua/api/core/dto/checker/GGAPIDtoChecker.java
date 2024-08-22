package com.garganttua.api.core.dto.checker;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.garganttua.api.core.dto.exceptions.GGAPIDtoException;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.dto.GGAPIDtoInfos;
import com.garganttua.api.spec.dto.annotations.GGAPIDto;
import com.garganttua.api.spec.dto.annotations.GGAPIDtoTenantId;
import com.garganttua.reflection.GGReflectionException;
import com.garganttua.reflection.query.GGObjectQueryFactory;
import com.garganttua.reflection.query.IGGObjectQuery;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GGAPIDtoChecker {

	public static List<GGAPIDtoInfos> checkDtos(List<Class<?>> dtoClasss) throws GGAPIDtoException {
		List<GGAPIDtoInfos> dtoinfos = new ArrayList<GGAPIDtoInfos>();
		for (Class<?> dtoClass : dtoClasss) {
			dtoinfos.add(GGAPIDtoChecker.checkDto(dtoClass));
		}
		return dtoinfos;
	}

	public static GGAPIDtoInfos checkDto(Class<?> dtoClass) throws GGAPIDtoException {
		if (log.isDebugEnabled()) {
			log.debug("Checking dto infos from class " + dtoClass.getName());
		}
		
		GGAPIDto annotation = dtoClass.getDeclaredAnnotation(GGAPIDto.class);
		
		if( annotation == null ) {
			throw new GGAPIDtoException(GGAPIExceptionCode.DTO_DEFINITION,
					"Dto " + dtoClass.getSimpleName() + " is not annotated with @GGAPIDto");
		}
		
		String tenantIdFieldName = GGAPIDtoChecker.getFieldNameAnnotatedWithAndCheckType(dtoClass,
				GGAPIDtoTenantId.class, String.class);

		if (tenantIdFieldName == null || tenantIdFieldName.isEmpty()) {
			throw new GGAPIDtoException(GGAPIExceptionCode.DTO_DEFINITION,
					"Dto " + dtoClass.getSimpleName() + " does not have any field annotated with @GGAPIDtoTenantId");
		}
		IGGObjectQuery q;
		try {
			q = GGObjectQueryFactory.objectQuery(dtoClass);
			return new GGAPIDtoInfos(annotation.db(), q.address(tenantIdFieldName));
		} catch (GGReflectionException e) {
			throw new GGAPIDtoException(e);
		}
	}

	private static String getFieldNameAnnotatedWithAndCheckType(Class<?> dtoClass,
			Class<? extends Annotation> annotationClass, Class<?> fieldClass) throws GGAPIDtoException {
		String fieldName = null;
		for (Field field : dtoClass.getDeclaredFields()) {
			if (field.isAnnotationPresent(annotationClass)) {
				if (fieldName != null && !fieldName.isEmpty()) {
					throw new GGAPIDtoException(GGAPIExceptionCode.DTO_DEFINITION,
							"Dto " + dtoClass.getSimpleName() + " has more than one field annotated with " + annotationClass);
				}
				if (field.getType().equals(fieldClass)) {
					fieldName = field.getName();
				} else {
					throw new GGAPIDtoException(GGAPIExceptionCode.DTO_DEFINITION,
							"Dto " + dtoClass.getSimpleName() + " has field " + field.getName() + " with wrong type "
									+ field.getType().getName() + ", should be " + fieldClass);
				}
			}
		}

		if (dtoClass.getSuperclass() != null && fieldName == null) {
			return GGAPIDtoChecker.getFieldNameAnnotatedWithAndCheckType(dtoClass.getSuperclass(), annotationClass,
					fieldClass);
		} else {
			return fieldName;
		}
	}

	public static GGAPIDtoInfos checkDto(Object dto) throws GGAPIDtoException {
		return GGAPIDtoChecker.checkDto(dto.getClass());
	}

}