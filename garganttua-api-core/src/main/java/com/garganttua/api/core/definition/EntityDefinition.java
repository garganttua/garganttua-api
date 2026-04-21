package com.garganttua.api.core.definition;

import java.lang.annotation.Annotation;
import java.util.List;

import org.javatuples.Pair;

import com.garganttua.api.commons.definition.IEntityDefinition;
import com.garganttua.api.commons.entity.annotations.UnicityScope;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.reflection.ObjectAddress;
import com.garganttua.core.reflection.binders.IMethodBinder;

public record EntityDefinition<E>(
    IClass<E> entityClass,
    ObjectAddress id,
    ObjectAddress uuid,
    ObjectAddress tenantId,
    List<ObjectAddress> mandatories,
    List<Pair<ObjectAddress, UnicityScope>> unicities,
    List<Pair<ObjectAddress, String>> updates,
    List<Pair<ObjectAddress, IClass<? extends Annotation>>> annotatedFields,
    List<Pair<ObjectAddress, IClass<? extends Annotation>>> annotatedMethods,
    List<IMethodBinder<Void>> afterGetMethodBuilders,
    List<IMethodBinder<Void>>  beforeCreateMethodBuilders,
    List<IMethodBinder<Void>>  afterCreateMethodBuilders,
    List<IMethodBinder<Void>>  beforeUpdateMethodBuilders,
    List<IMethodBinder<Void>>  afterUpdateMethodBuilders,
    List<IMethodBinder<Void>>  beforeDeleteMethodBuilders,
    List<IMethodBinder<Void>>  afterDeleteMethodBuilders) implements IEntityDefinition<E> {

}
