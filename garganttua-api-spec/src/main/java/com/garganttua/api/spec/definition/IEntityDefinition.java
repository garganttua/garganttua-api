package com.garganttua.api.spec.definition;

import java.lang.annotation.Annotation;
import java.util.List;

import org.javatuples.Pair;

import com.garganttua.api.spec.entity.annotations.UnicityScope;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.reflection.ObjectAddress;

public interface IEntityDefinition<E> {

    IClass<E> entityClass();

    ObjectAddress id();

    ObjectAddress uuid();

    ObjectAddress tenantId();

    List<ObjectAddress> mandatories();

    List<Pair<ObjectAddress, UnicityScope>> unicities();

    List<Pair<ObjectAddress, String>> updates();

    List<Pair<ObjectAddress, IClass<? extends Annotation>>> annotatedFields();

    List<Pair<ObjectAddress, IClass<? extends Annotation>>> annotatedMethods();

}
