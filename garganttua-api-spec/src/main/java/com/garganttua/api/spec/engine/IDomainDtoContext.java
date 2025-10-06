package com.garganttua.api.spec.engine;

import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.dao.IDao;

public interface IDomainDtoContext {

    IDao getDao() throws CoreException;

    String getUuid(Object object) throws CoreException;

}
