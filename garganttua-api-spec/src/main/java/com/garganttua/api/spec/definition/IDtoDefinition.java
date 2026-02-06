package com.garganttua.api.spec.definition;

import com.garganttua.core.reflection.ObjectAddress;

public interface IDtoDefinition<D> {

    Class<D> dtoClass();

    ObjectAddress uuid();

    ObjectAddress id();

    ObjectAddress tenantId();

}
