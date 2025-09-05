package com.garganttua.api.spec.engine;

import java.lang.reflect.Field;

import com.garganttua.reflection.GGObjectAddress;

public interface IDtoBuilder {

    IDtoBuilder autoDetect(boolean b);

    IDtoBuilder db(String string);

    IDtoBuilder tenantId(String string);

    IDtoBuilder tenantId(Field field);

    IDtoBuilder tenantId(GGObjectAddress fieldAddress);

    IDomainBuilder up();

}
