package com.garganttua.api.commons.definition;

import java.lang.annotation.Annotation;
import java.util.List;

import org.javatuples.Pair;

import com.garganttua.api.commons.entity.IUuidGenerator;
import com.garganttua.api.commons.entity.annotations.UnicityScope;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.reflection.ObjectAddress;

public interface IEntityDefinition<E> {

    IClass<E> entityClass();

    ObjectAddress id();

    ObjectAddress uuid();

    /** When true, the framework (re)generates the uuid at creation even if the client supplied one. */
    boolean overwriteUuid();

    /** Custom uuid generator for this domain, or null to use the framework default (time-ordered UUID v7). */
    IUuidGenerator uuidGenerator();

    ObjectAddress tenantId();

    List<ObjectAddress> mandatories();

    List<Pair<ObjectAddress, UnicityScope>> unicities();

    List<Pair<ObjectAddress, String>> updates();

    List<Pair<ObjectAddress, IClass<? extends Annotation>>> annotatedFields();

    List<Pair<ObjectAddress, IClass<? extends Annotation>>> annotatedMethods();

}
