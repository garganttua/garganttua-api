package com.garganttua.api.spec.definition;

import com.garganttua.core.reflection.IClass;
import com.garganttua.core.reflection.ObjectAddress;

public interface IDtoDefinition<D> {

    IClass<D> dtoClass();

    ObjectAddress uuid();

    ObjectAddress id();

    ObjectAddress tenantId();

}
