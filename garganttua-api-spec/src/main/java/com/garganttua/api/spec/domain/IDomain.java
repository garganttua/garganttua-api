package com.garganttua.api.spec.domain;

/**
 * Runtime interface representing a domain in the API framework.
 * This interface provides access to the domain's context and configuration.
 *
 * @param <E> the entity type for this domain
 */
public interface IDomain<E> {

	/**
	 * Gets the domain context containing all configuration and metadata.
	 *
	 * @return the domain context
	 */
	IDomainContext<E> getContext();

	/**
	 * Gets the domain name.
	 *
	 * @return the domain name
	 */
	String getName();

	/**
	 * Gets the entity class for this domain.
	 *
	 * @return the entity class
	 */
	Class<E> getEntityClass();
}
