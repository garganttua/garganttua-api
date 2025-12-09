package com.garganttua.api.core.definition;

import com.garganttua.core.reflection.ObjectAddress;

public record DtoDefinition<D>(Class<D> dtoClass, ObjectAddress uuid, ObjectAddress id, ObjectAddress tenantId) {

}
