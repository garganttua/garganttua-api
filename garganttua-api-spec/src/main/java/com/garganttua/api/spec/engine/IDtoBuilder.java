package com.garganttua.api.spec.engine;

import java.lang.reflect.Field;

import com.garganttua.api.spec.dao.IDao;
import com.garganttua.reflection.GGObjectAddress;

public interface IDtoBuilder {

    IDtoBuilder autoDetect(boolean b);

    IDtoBuilder db(IObjectSupplier<?> daoSupplier);

    IDtoBuilder db(IDao dao);

    IDtoBuilder tenantId(String string);

    IDtoBuilder tenantId(Field field);

    IDtoBuilder tenantId(GGObjectAddress fieldAddress);

    IDomainBuilder up();

}
