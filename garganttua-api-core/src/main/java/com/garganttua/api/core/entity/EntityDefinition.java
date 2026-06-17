package com.garganttua.api.core.entity;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

import org.javatuples.Pair;

import com.garganttua.api.commons.definition.IEntityDefinition;
import com.garganttua.api.commons.entity.IUuidGenerator;
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
    List<Pair<ObjectAddress, String>> creates,
    List<Pair<ObjectAddress, String>> updates,
    List<Pair<ObjectAddress, IClass<? extends Annotation>>> annotatedFields,
    List<Pair<ObjectAddress, IClass<? extends Annotation>>> annotatedMethods,
    List<IMethodBinder<Void>> afterGetMethodBuilders,
    List<IMethodBinder<Void>>  beforeCreateMethodBuilders,
    List<IMethodBinder<Void>>  afterCreateMethodBuilders,
    List<IMethodBinder<Void>>  beforeUpdateMethodBuilders,
    List<IMethodBinder<Void>>  afterUpdateMethodBuilders,
    List<IMethodBinder<Void>>  beforeDeleteMethodBuilders,
    List<IMethodBinder<Void>>  afterDeleteMethodBuilders,
    boolean overwriteUuid,
    IUuidGenerator uuidGenerator,
    /**
     * Free lifecycle-hook binders keyed by hook name ("beforeCreate" / "afterGet" / …). Unlike the
     * {@code *MethodBuilders} above (instance methods invoked ON the entity via invokeDeep), these are
     * fully-wired binders bound to an EXACT method (possibly on an external class, static or instance),
     * fed the current entity + injected framework context, and EXECUTED. Declared via the
     * {@code entity().beforeCreate(IMethod)} overloads.
     */
    Map<String, List<IMethodBinder<?>>> freeHookBinders) implements IEntityDefinition<E> {

}
