package com.garganttua.api.core.dto.checker;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.garganttua.api.core.dto.annotations.GGAPIDto;
import com.garganttua.api.core.dto.annotations.GGAPIDtoTenantId;
import com.garganttua.api.core.dto.exceptions.GGAPIDtoException;

public class GGAPIDtoChecker {

	public record GGAPIDtoInfos(String db, String tenantIdFieldName) {
		@Override
		public String toString() {
			return "GGAPIDtoInfos{tenantIdFieldName='" + tenantIdFieldName + "', db='" + db + "'}";
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			GGAPIDtoInfos that = (GGAPIDtoInfos) o;
			return Objects.equals(tenantIdFieldName, that.tenantIdFieldName) &&
					Objects.equals(db, that.db);
		}

		@Override
		public int hashCode() {
			return Objects.hash(tenantIdFieldName)*Objects.hash(db);
		}
	}

	public static List<GGAPIDtoInfos> checkDtos(List<Class<?>> dtoClasss) throws GGAPIDtoException {
		List<GGAPIDtoInfos> dtoinfos = new ArrayList<GGAPIDtoInfos>();
		for (Class<?> dtoClass : dtoClasss) {
			dtoinfos.add(GGAPIDtoChecker.checkDto(dtoClass));
		}
		return dtoinfos;
	}

	public static GGAPIDtoInfos checkDto(Class<?> dtoClass) throws GGAPIDtoException {

		GGAPIDto annotation = dtoClass.getDeclaredAnnotation(GGAPIDto.class);
		
		if( annotation == null ) {
			throw new GGAPIDtoException(GGAPIDtoException.DTO_DEFINITION_ERROR,
					"Dto " + dtoClass + " does is not annotated with @GGAPIDto");
		}
		
		String tenantIdFieldName = GGAPIDtoChecker.getFieldNameAnnotatedWithAndCheckType(dtoClass,
				GGAPIDtoTenantId.class, String.class);

		if (tenantIdFieldName == null || tenantIdFieldName.isEmpty()) {
			throw new GGAPIDtoException(GGAPIDtoException.DTO_DEFINITION_ERROR,
					"Dto " + dtoClass + " does not have any field annotated with @GGAPIDtoTenantId");
		}

		return new GGAPIDtoInfos(annotation.db(), tenantIdFieldName);
	}

	private static String getFieldNameAnnotatedWithAndCheckType(Class<?> dtoClass,
			Class<? extends Annotation> annotationClass, Class<?> fieldClass) throws GGAPIDtoException {
		String fieldName = null;
		for (Field field : dtoClass.getDeclaredFields()) {
			if (field.isAnnotationPresent(annotationClass)) {
				if (fieldName != null && !fieldName.isEmpty()) {
					throw new GGAPIDtoException(GGAPIDtoException.DTO_DEFINITION_ERROR,
							"Dto " + dtoClass + " has more than one field annotated with " + annotationClass);
				}
				if (field.getType().equals(fieldClass)) {
					fieldName = field.getName();
				} else {
					throw new GGAPIDtoException(GGAPIDtoException.DTO_DEFINITION_ERROR,
							"Dto " + dtoClass + " has field " + field.getName() + " with wrong type "
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
