package com.garganttua.api.core.dto.checker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.garganttua.api.core.dto.exceptions.GGAPIDtoException;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.dto.GGAPIDtoInfos;
import com.garganttua.api.spec.dto.annotations.GGAPIDto;
import com.garganttua.api.spec.dto.annotations.GGAPIDtoTenantId;
import com.garganttua.reflection.GGReflectionException;
import com.garganttua.reflection.query.GGObjectQueryFactory;
import com.garganttua.reflection.query.IGGObjectQuery;
import com.garganttua.reflection.utils.GGObjectReflectionHelper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GGAPIDtoChecker {
	
	private static Map<Class<?>, GGAPIDtoInfos> infos = new HashMap<Class<?>, GGAPIDtoInfos>();

	public static List<GGAPIDtoInfos> checkDtos(List<Class<?>> dtoClasss) throws GGAPIDtoException {
		List<GGAPIDtoInfos> dtoinfos = new ArrayList<GGAPIDtoInfos>();
		for (Class<?> dtoClass : dtoClasss) {
			dtoinfos.add(GGAPIDtoChecker.checkDto(dtoClass));
		}
		return dtoinfos;
	}

	public static GGAPIDtoInfos checkDto(Class<?> dtoClass) throws GGAPIDtoException {
		if( GGAPIDtoChecker.infos.containsKey(dtoClass) ) {
			return GGAPIDtoChecker.infos.get(dtoClass);  
		}
		
		if (log.isDebugEnabled()) {
			log.debug("Checking dto infos from class " + dtoClass.getName());
		}
		
		GGAPIDto annotation = dtoClass.getDeclaredAnnotation(GGAPIDto.class);
		
		if( annotation == null ) {
			throw new GGAPIDtoException(GGAPIExceptionCode.DTO_DEFINITION,
					"Dto " + dtoClass.getSimpleName() + " is not annotated with @GGAPIDto");
		}
		
		String tenantIdFieldName;
		try {
			tenantIdFieldName = GGObjectReflectionHelper.getFieldAddressAnnotatedWithAndCheckType(dtoClass,
					GGAPIDtoTenantId.class, String.class);
			if( tenantIdFieldName == null )
				throw new GGAPIDtoException(GGAPIExceptionCode.DTO_DEFINITION,
						"Dto " + dtoClass.getSimpleName() + " does not have any field annotated with @GGAPIDtoTenantId");
		} catch (GGReflectionException e) {
			throw new GGAPIDtoException(GGAPIExceptionCode.DTO_DEFINITION,
					"Dto " + dtoClass.getSimpleName() + " does not have any field annotated with @GGAPIDtoTenantId");
		}

		IGGObjectQuery q;
		try {
			q = GGObjectQueryFactory.objectQuery(dtoClass);
			GGAPIDtoInfos dtoInfos = new GGAPIDtoInfos(annotation.db(), q.address(tenantIdFieldName));
			GGAPIDtoChecker.infos.put(dtoClass, dtoInfos);
			
			return dtoInfos;
		} catch (GGReflectionException e) {
			throw new GGAPIDtoException(e);
		}
	}

	public static GGAPIDtoInfos checkDto(Object dto) throws GGAPIDtoException {
		return GGAPIDtoChecker.checkDto(dto.getClass());
	}

}
