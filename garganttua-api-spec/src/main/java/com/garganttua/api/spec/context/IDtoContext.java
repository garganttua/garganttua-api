package com.garganttua.api.spec.context;

import com.garganttua.api.spec.dao.IDao;
import com.garganttua.core.CoreException;

public interface IDtoContext<D> {

    IDao getDao() throws CoreException;

    String getUuid(Object object) throws CoreException;

}
