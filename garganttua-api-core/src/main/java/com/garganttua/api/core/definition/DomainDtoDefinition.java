package com.garganttua.api.core.definition;

import com.garganttua.reflection.GGObjectAddress;

public record DomainDtoDefinition(Class<?> dtoClass, GGObjectAddress uuid, GGObjectAddress id,GGObjectAddress tenantId ) {


}
