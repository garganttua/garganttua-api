package com.garganttua.api.core.definition;

import com.garganttua.api.spec.definition.IDtoDefinition;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.reflection.ObjectAddress;

public record DtoDefinition<D>(IClass<D> dtoClass, ObjectAddress uuid, ObjectAddress id, ObjectAddress tenantId) implements IDtoDefinition<D> {

}
