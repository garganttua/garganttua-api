package com.garganttua.api.core.unit.context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.javatuples.Pair;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.garganttua.api.commons.caller.ICaller;
import com.garganttua.api.core.entity.EntityCreator;
import com.garganttua.core.reflection.ObjectAddress;

/**
 * The CREATE-time field whitelist ({@code entity().create(field[, authority])}). When a whitelist is
 * declared, only declared fields the caller is authorized for survive on the inbound entity; every
 * other client-supplied field is stripped. With no whitelist, the entity is untouched. Mirrors
 * {@code EntityUpdaterTest} (and shares its no-super-bypass contract).
 */
@DisplayName("EntityCreator — create-time field whitelist")
class EntityCreatorTest {

	public static class Product {
		private String name;
		private double price;     // primitive — cannot hold null, never stripped
		private String category;
		private String secret;

		public Product(String name, double price, String category, String secret) {
			this.name = name;
			this.price = price;
			this.category = category;
			this.secret = secret;
		}

		public String getName() { return name; }
		public double getPrice() { return price; }
		public String getCategory() { return category; }
		public String getSecret() { return secret; }
	}

	private static ICaller callerWith(List<String> authorities) {
		return callerWith(authorities, false, false);
	}

	private static ICaller callerWith(List<String> authorities, boolean superTenant, boolean superOwner) {
		return new ICaller() {
			@Override public String tenantId() { return "T1"; }
			@Override public String requestedTenantId() { return "T1"; }
			@Override public String ownerId() { return "O1"; }
			@Override public String callerId() { return "C1"; }
			@Override public boolean superTenant() { return superTenant; }
			@Override public boolean superOwner() { return superOwner; }
			@Override public List<String> authorities() { return authorities; }
		};
	}

	private final EntityCreator creator = new EntityCreator();

	@Test
	@DisplayName("no whitelist declared → the entity is returned untouched")
	void emptyWhitelistKeepsEverything() throws Exception {
		Product p = new Product("N", 9.0, "C", "S");

		creator.create(callerWith(List.of()), p, List.of());

		assertEquals("N", p.getName());
		assertEquals("C", p.getCategory());
		assertEquals("S", p.getSecret());
	}

	@Test
	@DisplayName("whitelist keeps declared+authorized fields and strips everything else")
	void whitelistStripsUndeclared() throws Exception {
		Product p = new Product("N", 9.0, "C", "S");
		List<Pair<ObjectAddress, String>> creates = List.of(
				Pair.with(new ObjectAddress("name"), null),               // free
				Pair.with(new ObjectAddress("category"), "ROLE_CAT"));    // guarded

		creator.create(callerWith(List.of("ROLE_CAT")), p, creates);

		assertEquals("N", p.getName(), "declared no-auth field kept");
		assertEquals("C", p.getCategory(), "declared field, caller authorized → kept");
		assertNull(p.getSecret(), "undeclared field must be stripped");
		assertEquals(9.0, p.getPrice(), "primitive field is preserved (cannot hold null)");
	}

	@Test
	@DisplayName("a declared field the caller is not authorized for is stripped")
	void unauthorizedDeclaredFieldStripped() throws Exception {
		Product p = new Product("N", 9.0, "C", "S");
		List<Pair<ObjectAddress, String>> creates = List.of(
				Pair.with(new ObjectAddress("name"), null),
				Pair.with(new ObjectAddress("category"), "ROLE_CAT"));

		creator.create(callerWith(List.of("OTHER")), p, creates);

		assertEquals("N", p.getName());
		assertNull(p.getCategory(), "declared but caller lacks 'ROLE_CAT' → stripped");
		assertNull(p.getSecret());
	}

	@Test
	@DisplayName("super-tenant/super-owner does NOT bypass — a guarded field it can't authorize is stripped")
	void superDoesNotBypass() throws Exception {
		Product p = new Product("N", 9.0, "C", "S");
		List<Pair<ObjectAddress, String>> creates = List.of(
				Pair.with(new ObjectAddress("name"), null),
				Pair.with(new ObjectAddress("category"), "ROLE_CAT"));

		creator.create(callerWith(null, true, true), p, creates); // super, no authorities

		assertEquals("N", p.getName(), "the no-auth declared field is still kept");
		assertNull(p.getCategory(), "super grants reach, not the authority — category stripped");
	}

	@Test
	@DisplayName("null caller authorities cannot satisfy a guarded field")
	void nullAuthoritiesStripGuarded() throws Exception {
		Product p = new Product("N", 9.0, "C", "S");
		List<Pair<ObjectAddress, String>> creates = List.of(
				Pair.with(new ObjectAddress("name"), null),
				Pair.with(new ObjectAddress("category"), "ROLE_CAT"));

		creator.create(callerWith(null), p, creates);

		assertEquals("N", p.getName(), "no-auth declared field still kept");
		assertNull(p.getCategory(), "null authorities fail a guarded field");
	}
}
