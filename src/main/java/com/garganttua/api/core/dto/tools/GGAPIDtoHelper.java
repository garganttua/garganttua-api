package com.garganttua.api.core.dto.tools;

import com.garganttua.api.core.dto.checker.GGAPIDtoChecker;
import com.garganttua.api.core.dto.checker.GGAPIDtoChecker.GGAPIDtoInfos;
import com.garganttua.api.core.dto.exceptions.GGAPIDtoException;
import com.garganttua.api.core.objects.query.GGAPIObjectQueryException;
import com.garganttua.api.core.objects.query.GGAPIObjectQueryFactory;

public class GGAPIDtoHelper {
	
	public static void setTenantId(Object dto, String tenantId) throws GGAPIDtoException {
		GGAPIDtoInfos infos = GGAPIDtoChecker.checkDto(dto);
		try {
			GGAPIObjectQueryFactory.objectQuery(dto).setValue(infos.tenantIdFieldAddress(), tenantId);
		} catch (GGAPIObjectQueryException e) {
			throw new GGAPIDtoException(e);
		} 
	}
	
	public static String getTenantId(Object dto) throws GGAPIDtoException {
		GGAPIDtoInfos infos = GGAPIDtoChecker.checkDto(dto);
		try {
			return (String) GGAPIObjectQueryFactory.objectQuery(dto).getValue(infos.tenantIdFieldAddress());
		} catch (GGAPIObjectQueryException e) {
			throw new GGAPIDtoException(e);
		} 
	}
}
