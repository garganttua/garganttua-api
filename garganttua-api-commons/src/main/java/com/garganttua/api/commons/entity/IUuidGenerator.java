package com.garganttua.api.commons.entity;

/**
 * Computes the UUID assigned to an entity at creation.
 * <p>
 * The framework default is a time-ordered UUID v7. A domain may declare a custom
 * generator via {@code .entity().uuidGenerator(...)} on the DSL — for example a
 * namespaced/deterministic UUID derived from the entity's own fields.
 * <p>
 * {@link #generate(Object)} is invoked only when the framework assigns the uuid:
 * either the client sent none, or the entity declares {@code overwriteUuid(true)}.
 */
@FunctionalInterface
public interface IUuidGenerator {

	/**
	 * Returns the UUID string to set on {@code entity}. The entity is supplied so the
	 * computation may derive the id from its fields; a stateless generator simply
	 * ignores it.
	 */
	String generate(Object entity);
}
