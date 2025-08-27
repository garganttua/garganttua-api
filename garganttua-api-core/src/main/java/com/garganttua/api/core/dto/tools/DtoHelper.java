package com.garganttua.api.core.dto.tools;

import com.garganttua.api.core.InfosHelper;
import com.garganttua.api.core.dto.checker.DtoChecker;
import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.dto.DtoInfos;

public class DtoHelper {
	
	public static void setTenantId(Object dto, String tenantId) throws CoreException {
		InfosHelper.setValue(dto, DtoChecker::checkDtoClass, DtoInfos::tenantIdFieldAddress, tenantId);
	}
	
	public static String getTenantId(Object dto) throws CoreException {
		return InfosHelper.getValue(dto, DtoChecker::checkDtoClass, DtoInfos::tenantIdFieldAddress);
	}
	
}
