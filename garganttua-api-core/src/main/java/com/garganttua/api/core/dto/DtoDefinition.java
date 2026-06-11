package com.garganttua.api.core.dto;

import java.util.List;

import com.garganttua.api.commons.definition.DtoComposition;
import com.garganttua.api.commons.definition.IDtoDefinition;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.reflection.ObjectAddress;

public record DtoDefinition<D>(IClass<D> dtoClass, ObjectAddress uuid, ObjectAddress id, ObjectAddress tenantId,
		List<DtoComposition> compositions) implements IDtoDefinition<D> {

}
