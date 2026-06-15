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

    /**
     * CREATE-time field whitelist: each pair binds a field a caller may valorize at creation to the
     * authority it requires (null/empty = no authority). When EMPTY, creation is unrestricted (the
     * client body is kept as-is). When non-empty, only these fields are kept — every other
     * client-supplied field is stripped. Declared via {@code entity().create(field[, authority])}.
     */
    default List<Pair<ObjectAddress, String>> creates() {
        return List.of();
    }

    List<Pair<ObjectAddress, String>> updates();

    List<Pair<ObjectAddress, IClass<? extends Annotation>>> annotatedFields();

    List<Pair<ObjectAddress, IClass<? extends Annotation>>> annotatedMethods();

}
