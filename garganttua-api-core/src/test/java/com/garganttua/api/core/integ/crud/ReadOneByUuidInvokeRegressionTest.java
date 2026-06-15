package com.garganttua.api.core.integ.crud;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.caller.ICaller;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.context.dsl.IApiBuilder;
import com.garganttua.api.commons.dao.IDao;
import com.garganttua.api.commons.definition.IDomainDefinition;
import com.garganttua.api.commons.filter.IFilter;
import com.garganttua.api.commons.pageable.IPageable;
import com.garganttua.api.commons.service.IOperationResponse;
import com.garganttua.api.commons.service.OperationResponseCode;
import com.garganttua.api.commons.sort.ISort;
import com.garganttua.api.core.caller.Caller;
import com.garganttua.core.reflection.IClass;

/**
 * Regression: a by-uuid lookup (readOne / updateOne / deleteOne) must target the requested row,
 * not an arbitrary one. The bug: {@code Domain.doInvoke} translated {@code body→entity} but not
 * {@code entityUuid→identifier/type}, so {@code buildGetOneFilter} saw a null identifier, built no
 * uuid clause, and a by-uuid fetch degraded to match-all (returning {@code first()} of the table).
 *
 * <p>These tests drive the <strong>real</strong> {@code IDomain.readOne/updateOne/deleteOne(uuid,
 * caller)} path (which carries the key as {@code ENTITY_UUID} and never sets {@code identifier} by
 * hand) on a domain with several rows, backed by a <strong>filter-aware</strong> DAO — the shared
 * {@code StubDao} ignores the filter, which is exactly why the existing suite never caught this.
 */
@DisplayName("Lookup by uuid targets the requested row (not match-all) — via the real invoke() path")
class ReadOneByUuidInvokeRegressionTest extends AbstractCrudIntegrationTest {

	/** In-memory DAO that actually applies the IFilter (uuid lookup), unlike StubDao. */
	static class FilterAwareDao implements IDao {
		private final List<Object> storage = new ArrayList<>();

		@Override public void registerDomain(IDomainDefinition domainDefinition) { }

		@Override
		public List<Object> find(Optional<IPageable> pageable, Optional<IFilter> filter, Optional<ISort> sort) {
			List<Object> result = new ArrayList<>();
			for (Object row : storage) {
				if (filter.isEmpty() || matches(row, filter.get())) {
					result.add(row);
				}
			}
			return result;
		}

		@Override public Object save(Object object) {
			delete(object); // replace by uuid if present
			storage.add(object);
			return object;
		}

		@Override public void delete(Object object) {
			String uuid = field(object, "uuid");
			if (uuid != null) {
				storage.removeIf(row -> uuid.equals(field(row, "uuid")));
			} else {
				storage.remove(object);
			}
		}

		@Override public long count(IFilter filter) { return storage.size(); }

		List<Object> storage() { return storage; }

		/** Minimal evaluator for the shapes buildGetOneFilter emits: $and / $or / $field+$eq/$ne. */
		private static boolean matches(Object row, IFilter f) {
			if (f == null) {
				return true;
			}
			return switch (f.getName()) {
				case "$and" -> f.getFilters().stream().allMatch(sub -> matches(row, sub));
				case "$or" -> f.getFilters().stream().anyMatch(sub -> matches(row, sub));
				case "$field" -> {
					IFilter cmp = f.getFilters().get(0);
					Object actual = field(row, String.valueOf(f.getValue()));
					yield switch (cmp.getName()) {
						case "$eq" -> java.util.Objects.equals(cmp.getValue(), actual);
						case "$ne" -> !java.util.Objects.equals(cmp.getValue(), actual);
						default -> true; // unsupported comparators do not exclude
					};
				}
				default -> true;
			};
		}

		private static String field(Object obj, String name) {
			Class<?> c = obj.getClass();
			while (c != null) {
				try {
					java.lang.reflect.Field field = c.getDeclaredField(name);
					field.setAccessible(true);
					Object value = field.get(obj);
					return value != null ? value.toString() : null;
				} catch (NoSuchFieldException e) {
					c = c.getSuperclass();
				} catch (IllegalAccessException e) {
					return null;
				}
			}
			return null;
		}
	}

	private IDomain<?> users;
	private FilterAwareDao dao;
	private final ICaller caller = Caller.createSuperCaller("superTenant");

	@BeforeEach
	void setUp() throws ApiException {
		dao = new FilterAwareDao();
		IApiBuilder builder = newBuilder();
		builder.domain(IClass.getClass(User.class))
				.tenant(true)
				.superTenant("superTenant")
				.entity().id("id").uuid("uuid").tenantId("tenantId")
					.update("name")   // declare 'name' updatable (no authority) so updateOne can mutate it
				.up()
				.dto(IClass.getClass(UserDto.class))
					.id("id").uuid("uuid").tenantId("tenantId")
					.db(dao)
				.up()
				.security().disable(true).up()
			.up();
		IApi api = buildAndStart(builder);
		users = api.getDomain("users").orElseThrow();

		users.createOne(user("uuid-a", "Alice"), caller);
		users.createOne(user("uuid-b", "Bob"), caller);
		users.createOne(user("uuid-c", "Carol"), caller);
		assertEquals(3, dao.storage().size(), "three distinct rows seeded");
	}

	private static User user(String uuid, String name) {
		User u = new User();
		u.setUuid(uuid);
		u.setTenantId("superTenant");
		u.setName(name);
		return u;
	}

	private static String uuidOf(IOperationResponse response) {
		assertNotNull(response.getResponse(), "response carries the entity");
		return ((User) response.getResponse()).getUuid();
	}

	@Test
	@DisplayName("readOne(uuid) returns exactly the requested row, on a multi-row domain")
	void readOneTargetsTheRequestedRow() throws ApiException {
		assertEquals("uuid-b", uuidOf(users.readOne("uuid-b", caller)), "readOne must return the requested uuid");
		assertEquals("Bob", ((User) users.readOne("uuid-b", caller).getResponse()).getName());

		// the other rows are still individually addressable — no match-all collapse
		assertEquals("uuid-a", uuidOf(users.readOne("uuid-a", caller)));
		assertEquals("uuid-c", uuidOf(users.readOne("uuid-c", caller)));
	}

	@Test
	@DisplayName("updateOne(uuid) mutates the requested row only")
	void updateOneTargetsTheRequestedRow() throws ApiException {
		User patch = new User();
		patch.setName("Bob Updated");

		IOperationResponse updated = users.updateOne("uuid-b", patch, caller);
		assertEquals(OperationResponseCode.UPDATED, updated.getResponseCode(),
				"update should succeed. got=" + updated.getResponseCode() + " / " + updated.getResponse());
		assertEquals("uuid-b", uuidOf(updated), "the update must target uuid-b, not an arbitrary row");

		assertEquals("Bob Updated", ((User) users.readOne("uuid-b", caller).getResponse()).getName());
		assertEquals("Alice", ((User) users.readOne("uuid-a", caller).getResponse()).getName(), "uuid-a must be untouched");
	}

	@Test
	@DisplayName("deleteOne(uuid) removes the requested row only")
	void deleteOneTargetsTheRequestedRow() throws ApiException {
		IOperationResponse deleted = users.deleteOne("uuid-a", caller);
		assertEquals(OperationResponseCode.DELETED, deleted.getResponseCode(),
				"delete should succeed. got=" + deleted.getResponseCode() + " / " + deleted.getResponse());

		assertEquals(2, dao.storage().size(), "exactly one row removed");
		assertNotEquals(OperationResponseCode.OK, users.readOne("uuid-a", caller).getResponseCode(),
				"uuid-a must be gone");
		assertEquals("uuid-b", uuidOf(users.readOne("uuid-b", caller)), "uuid-b must survive");
		assertEquals("uuid-c", uuidOf(users.readOne("uuid-c", caller)), "uuid-c must survive");
	}
}
