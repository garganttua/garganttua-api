package com.garganttua.api.core.dto.checker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.garganttua.api.core.dto.exceptions.DtoException;
import com.garganttua.api.spec.CoreExceptionCode;
import com.garganttua.api.spec.dto.DtoInfos;
import com.garganttua.api.spec.dto.annotations.Dto;
import com.garganttua.api.spec.dto.annotations.DtoTenantId;
import com.garganttua.reflection.GGReflectionException;
import com.garganttua.reflection.query.GGObjectQueryFactory;
import com.garganttua.reflection.query.IGGObjectQuery;
import com.garganttua.reflection.utils.GGObjectReflectionHelper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DtoChecker {
	
	private static Map<Class<?>, DtoInfos> infos = new HashMap<Class<?>, DtoInfos>();

	public static List<DtoInfos> checkDtosClasses(List<Class<?>> dtoClasss) throws DtoException {
		List<DtoInfos> dtoinfos = new ArrayList<DtoInfos>();
		for (Class<?> dtoClass : dtoClasss) {
			dtoinfos.add(DtoChecker.checkDtoClass(dtoClass));
		}
		return dtoinfos;
	}

	public static DtoInfos checkDtoClass(Class<?> dtoClass) throws DtoException {
		if( DtoChecker.infos.containsKey(dtoClass) ) {
			return DtoChecker.infos.get(dtoClass);  
		}
		
		if (log.isDebugEnabled()) {
			log.debug("Checking dto infos from class " + dtoClass.getName());
		}
		
		Dto annotation = dtoClass.getDeclaredAnnotation(Dto.class);
		
		if( annotation == null ) {
			throw new DtoException(CoreExceptionCode.DTO_DEFINITION,
					"Dto " + dtoClass.getSimpleName() + " is not annotated with @Dto");
		}
		
		String tenantIdFieldName;
		try {
			tenantIdFieldName = GGObjectReflectionHelper.getFieldAddressAnnotatedWithAndCheckType(dtoClass,
					DtoTenantId.class, String.class);
			if( tenantIdFieldName == null )
				throw new DtoException(CoreExceptionCode.DTO_DEFINITION,
						"Dto " + dtoClass.getSimpleName() + " does not have any field annotated with @DtoTenantId");
		} catch (GGReflectionException e) {
			throw new DtoException(CoreExceptionCode.DTO_DEFINITION,
					"Dto " + dtoClass.getSimpleName() + " does not have any field annotated with @DtoTenantId");
		}

		IGGObjectQuery q;
		try {
			q = GGObjectQueryFactory.objectQuery(dtoClass);
			DtoInfos dtoInfos = new DtoInfos(annotation.db(), q.address(tenantIdFieldName));
			DtoChecker.infos.put(dtoClass, dtoInfos);
			
			return dtoInfos;
		} catch (GGReflectionException e) {
			throw new DtoException(e);
		}
	}

	public static DtoInfos checkDto(Object dto) throws DtoException {
		return DtoChecker.checkDtoClass(dto.getClass());
	}

}
