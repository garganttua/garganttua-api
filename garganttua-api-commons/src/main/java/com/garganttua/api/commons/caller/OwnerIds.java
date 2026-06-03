package com.garganttua.api.commons.caller;

/**
 * Canonical helper for the framework-wide owner/caller naming rule
 * <strong>{@code ${domainName}:${id}}</strong>.
 *
 * <p>A qualified owner id namespaces a raw entity uuid with the domain it
 * belongs to, so an {@code ownerId} stored on an owned entity (or on an
 * authorization's {@code owned} field, or in an authorization's
 * {@code signedBy} field) is self-describing: it says <em>which domain</em>
 * the owner lives in, not just its uuid. This lets the same raw uuid space be
 * reused across domains without collision and makes ownership relations
 * readable at a glance (e.g. {@code "users:018f...c3"}).
 *
 * <p>The rule is applied at the framework's write points only — once stored,
 * the qualified value flows verbatim through caller derivation and the
 * repository owner filter, so both sides of an ownership comparison speak the
 * same qualified form.
 */
public final class OwnerIds {

	/** Separator between the domain name and the raw id. */
	public static final String SEPARATOR = ":";

	private OwnerIds() {
	}

	/**
	 * Qualifies a raw id with its domain name: {@code domainName + ":" + id}.
	 *
	 * @param domainName the owner's domain name (e.g. {@code "users"}); required
	 * @param id         the raw id (uuid); {@code null} is propagated as {@code null}
	 * @return {@code "domainName:id"}, or {@code null} when {@code id} is {@code null}
	 * @throws IllegalArgumentException when {@code id} is non-null but {@code domainName} is null/blank
	 */
	public static String qualify(String domainName, String id) {
		if (id == null) {
			return null;
		}
		if (domainName == null || domainName.isBlank()) {
			throw new IllegalArgumentException("domainName is required to qualify owner id '" + id + "'");
		}
		return domainName + SEPARATOR + id;
	}

	/**
	 * Returns the domain-name part of a qualified id, or {@code null} when the
	 * value is null or not qualified.
	 */
	public static String domainOf(String qualified) {
		if (qualified == null) {
			return null;
		}
		int i = qualified.indexOf(SEPARATOR);
		return i <= 0 ? null : qualified.substring(0, i);
	}

	/**
	 * Returns the raw id part of a qualified id (everything after the first
	 * {@code ':'}), or the input unchanged when it carries no domain prefix.
	 */
	public static String idOf(String qualified) {
		if (qualified == null) {
			return null;
		}
		int i = qualified.indexOf(SEPARATOR);
		return i < 0 ? qualified : qualified.substring(i + 1);
	}

	/** Returns {@code true} when the value already carries a {@code domainName:} prefix. */
	public static boolean isQualified(String value) {
		return value != null && value.indexOf(SEPARATOR) > 0;
	}
}
