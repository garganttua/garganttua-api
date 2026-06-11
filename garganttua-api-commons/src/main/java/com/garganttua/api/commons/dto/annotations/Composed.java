package com.garganttua.api.commons.dto.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.garganttua.core.reflection.annotations.Reflected;

/**
 * Marks a DTO field as a COMPOSITION (a reference to another stored DTO, à la Spring
 * {@code @DBRef}). The field holds the composed DTO ({@code B}) — or a {@code List<B>} for a
 * 1-N composition — but the DAO does NOT embed it: it persists only a reference
 * ({@code {$ref: <collection>, $id: <uuid>}}) and resolves it back to the full DTO on read.
 *
 * <p>{@link #collection()} is the target collection holding the referenced DTOs (explicit,
 * because collection names are chosen freely per DAO). The referenced type is the field's own
 * type (its element type for a {@code List}).
 *
 * <p>The DSL equivalent is {@code .dto(...).composed("field", "collection")}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Reflected
public @interface Composed {

	/** The target collection holding the referenced DTOs (the {@code $ref}). */
	String collection();
}
