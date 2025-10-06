package com.garganttua.api.core.definition;

import java.lang.annotation.Annotation;
import java.util.List;

import org.javatuples.Pair;

import com.garganttua.api.spec.engine.IMethodBinderBuilder;
import com.garganttua.api.spec.entity.annotations.UnicityScope;
import com.garganttua.reflection.GGObjectAddress;

public record DomainEntityDefinition(
    Class<?> entityClass,
    GGObjectAddress id,
    GGObjectAddress uuid,
    GGObjectAddress tenantId,
    List<GGObjectAddress> mandatories,
    List<Pair<GGObjectAddress, UnicityScope>> unicities,
    List<Pair<GGObjectAddress, String>> updates,
    List<Pair<GGObjectAddress, Class<? extends Annotation>>> annotatedFields,
    List<Pair<GGObjectAddress, Class<? extends Annotation>>> annotatedMethods,
    List<IMethodBinderBuilder<?, ?>> afterGetMethodBuilders,
    List<IMethodBinderBuilder<?, ?>> beforeCreateMethodBuilders,
    List<IMethodBinderBuilder<?, ?>> afterCreateMethodBuilders,
    List<IMethodBinderBuilder<?, ?>> beforeUpdateMethodBuilders,
    List<IMethodBinderBuilder<?, ?>> afterUpdateMethodBuilders,
    List<IMethodBinderBuilder<?, ?>> beforeDeleteMethodBuilders,
    List<IMethodBinderBuilder<?, ?>> afterDeleteMethodBuilders) {

}
