package com.garganttua.api.core.dto.tools;

import com.garganttua.api.core.dto.checker.GGAPIDtoChecker;
import com.garganttua.api.core.dto.exceptions.GGAPIDtoException;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.dto.GGAPIDtoInfos;
import com.garganttua.reflection.GGReflectionException;
import com.garganttua.reflection.query.GGObjectQueryFactory;

public class GGAPIDtoHelper {
	
	public static void setTenantId(Object dto, String tenantId) throws GGAPIException {
		GGAPIDtoInfos infos = GGAPIDtoChecker.checkDto(dto);
		try {
			GGObjectQueryFactory.objectQuery(dto).setValue(infos.tenantIdFieldAddress(), tenantId);
		} catch (GGReflectionException e) {
			GGAPIDtoHelper.processException(e);
		} 
	}
	
	public static String getTenantId(Object dto) throws GGAPIException {
		GGAPIDtoInfos infos = GGAPIDtoChecker.checkDto(dto);
		try {
			return (String) GGObjectQueryFactory.objectQuery(dto).getValue(infos.tenantIdFieldAddress());
		} catch (GGReflectionException e) {
			GGAPIDtoHelper.processException(e);
		} 
		//Should never be reached
		return null;
	}
	
	private static void processException(GGReflectionException e) throws GGAPIDtoException, GGAPIException {
		GGAPIException apiException = GGAPIException.findFirstInException(e);
		if( apiException != null ) {
			throw apiException;
		} else {
			throw new GGAPIDtoException(e);
		}
	}
}
