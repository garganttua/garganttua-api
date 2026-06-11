package com.garganttua.api.commons.definition;

import com.garganttua.core.reflection.ObjectAddress;

/**
 * A captured DTO composition: the field {@code field} references DTO(s) stored in
 * {@code collection}. The DAO persists only a reference (DBRef) for this field and resolves it
 * back on read. Whether it is 1-1 or 1-N is derived from the field's actual type by the DAO.
 *
 * @param field      the DTO field carrying the composition
 * @param collection the target collection holding the referenced DTOs (the {@code $ref})
 */
public record DtoComposition(ObjectAddress field, String collection) {
}
