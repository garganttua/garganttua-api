package com.garganttua.api.core.dto.tools;

import com.garganttua.api.core.dto.checker.GGAPIDtoChecker;
import com.garganttua.api.core.dto.exceptions.GGAPIDtoException;
import com.garganttua.api.spec.dto.GGAPIDtoInfos;
import com.garganttua.reflection.GGReflectionException;
import com.garganttua.reflection.query.GGObjectQueryFactory;

public class GGAPIDtoHelper {
	
	public static void setTenantId(Object dto, String tenantId) throws GGAPIDtoException {
		GGAPIDtoInfos infos = GGAPIDtoChecker.checkDto(dto);
		try {
			GGObjectQueryFactory.objectQuery(dto).setValue(infos.tenantIdFieldAddress(), tenantId);
		} catch (GGReflectionException e) {
			throw new GGAPIDtoException(e);
		} 
	}
	
	public static String getTenantId(Object dto) throws GGAPIDtoException {
		GGAPIDtoInfos infos = GGAPIDtoChecker.checkDto(dto);
		try {
			return (String) GGObjectQueryFactory.objectQuery(dto).getValue(infos.tenantIdFieldAddress());
		} catch (GGReflectionException e) {
			throw new GGAPIDtoException(e);
		} 
	}
}
