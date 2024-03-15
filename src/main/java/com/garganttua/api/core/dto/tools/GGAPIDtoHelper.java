package com.garganttua.api.core.dto.tools;

import com.garganttua.api.core.dto.checker.GGAPIDtoChecker;
import com.garganttua.api.core.dto.checker.GGAPIDtoChecker.GGAPIDtoInfos;
import com.garganttua.api.core.dto.exceptions.GGAPIDtoException;
import com.garganttua.api.core.tools.GGAPIObjectReflectionHelper;
import com.garganttua.api.core.tools.GGAPIObjectReflectionHelperExcpetion;

public class GGAPIDtoHelper {
	
	public static void setTenantId(Object dto, String tenantId) throws GGAPIDtoException {
		GGAPIDtoInfos infos = GGAPIDtoChecker.checkDto(dto);
		try {
			GGAPIObjectReflectionHelper.setObjectFieldValue(dto, infos.tenantIdFieldName(), tenantId);
		} catch (GGAPIObjectReflectionHelperExcpetion e) {
			throw new GGAPIDtoException(e);
		} 
	}
	
	public static String getTenantId(Object dto) throws GGAPIDtoException {
		GGAPIDtoInfos infos = GGAPIDtoChecker.checkDto(dto);
		try {
			return (String) GGAPIObjectReflectionHelper.getObjectFieldValue(dto, infos.tenantIdFieldName());
		} catch (GGAPIObjectReflectionHelperExcpetion e) {
			throw new GGAPIDtoException(e);
		} 
	}
}
