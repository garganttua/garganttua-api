package com.garganttua.api.core.dto.tools;

import com.garganttua.api.core.GGAPIInfosHelper;
import com.garganttua.api.core.dto.checker.GGAPIDtoChecker;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.dto.GGAPIDtoInfos;

public class GGAPIDtoHelper {
	
	public static void setTenantId(Object dto, String tenantId) throws GGAPIException {
		GGAPIInfosHelper.setValue(dto, GGAPIDtoChecker::checkDtoClass, GGAPIDtoInfos::tenantIdFieldAddress, tenantId);
	}
	
	public static String getTenantId(Object dto) throws GGAPIException {
		return GGAPIInfosHelper.getValue(dto, GGAPIDtoChecker::checkDtoClass, GGAPIDtoInfos::tenantIdFieldAddress);
	}
	
}
