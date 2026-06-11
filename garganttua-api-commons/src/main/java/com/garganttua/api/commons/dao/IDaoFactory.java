package com.garganttua.api.commons.dao;

import com.garganttua.api.commons.ApiException;
import com.garganttua.core.reflection.IClass;

/**
 * Produces a default {@link IDao} for a domain whose dto did not configure one
 * explicitly via {@code .db(...)}. Consulted by the dto builder at build time
 * only when no DAO is set, so an explicit {@code .db(...)} always wins.
 *
 * <p>Registered on the {@code IApiBuilder} via {@code .defaultDao(...)} — the
 * mechanism a persistence starter (e.g. the MongoDB starter) uses to make every
 * annotation-scanned domain persistable without a line of DSL.
 */
@FunctionalInterface
public interface IDaoFactory {

	/**
	 * @param domainName the plural, lower-case domain name — also the natural
	 *                   collection/table name (e.g. {@code "users"}).
	 * @param dtoClass   the dto class the DAO will persist.
	 * @return a fresh DAO for this domain, or {@code null} to decline (the
	 *         builder then falls back to its "no DAO configured" error).
	 */
	IDao create(String domainName, IClass<?> dtoClass) throws ApiException;
}
