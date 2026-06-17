package com.garganttua.api.starter.javalin;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.dao.IDao;
import com.garganttua.api.commons.definition.IDomainDefinition;
import com.garganttua.api.commons.filter.IFilter;
import com.garganttua.api.commons.operation.Access;
import com.garganttua.api.commons.pageable.IPageable;
import com.garganttua.api.commons.sort.ISort;
import com.garganttua.api.commons.starter.AutoConfigurationContext;
import com.garganttua.api.commons.starter.IApiAutoConfiguration;
import com.garganttua.core.reflection.IClass;

/**
 * Test-only auto-config exercising the E2E: it registers an in-memory default
 * DAO (so the bootstrap → ServiceLoader → default-DAO-hook path runs without a
 * live MongoDB) and opens the {@code Widget} domain to anonymous CRUD — exactly
 * what a real public API would configure. Pre-declaring the domain here lets the
 * {@code @Entity} scan re-enter the same builder and add the dto + default DAO.
 * Discovered via the test classpath's {@code META-INF/services/...}. Runs at
 * order 0 (before the real Javalin transport auto-config at order 100).
 */
public final class InMemoryDaoAutoConfiguration implements IApiAutoConfiguration {

	@Override
	public int order() {
		return 0;
	}

	@Override
	public void apply(AutoConfigurationContext context) throws ApiException {
		context.registerDefaultDao((domainName, dtoClass) -> new InMemoryDao());

		context.apiBuilder()
				.domain(IClass.getClass(GarganttuaApplicationE2ETest.Widget.class))
					.security()
						.creationAccess(Access.anonymous)
						.readAllAccess(Access.anonymous)
						.readOneAccess(Access.anonymous)
					.up()
				.up();
	}

	/** Minimal list-backed {@link IDao} — stores whatever the pipeline saves. */
	static final class InMemoryDao implements IDao {
		private final List<Object> storage = new ArrayList<>();

		@Override
		public void registerDomain(IDomainDefinition<?> domainDefinition) {
		}

		@Override
		public List<Object> find(Optional<IPageable> pageable, Optional<IFilter> filter, Optional<ISort> sort) {
			List<Object> result = new ArrayList<>(this.storage);
			filter.ifPresent(f -> result.removeIf(o -> !matches(o, f)));
			return result;
		}

		/** Minimal filter matcher — enough to honour {@code $and}/{@code $or}/{@code $field:$eq} from the query filter. */
		private static boolean matches(Object o, IFilter f) {
			if (f == null || f.getName() == null) {
				return true;
			}
			switch (f.getName()) {
				case "$and":
					for (IFilter sub : f.getFilters()) {
						if (!matches(o, sub)) return false;
					}
					return true;
				case "$or":
					for (IFilter sub : f.getFilters()) {
						if (matches(o, sub)) return true;
					}
					return f.getFilters() == null || f.getFilters().isEmpty();
				case "$field": {
					IFilter op = f.getFilters().get(0);
					Object actual = readField(o, String.valueOf(f.getValue()));
					if ("$eq".equals(op.getName())) {
						return String.valueOf(actual).equals(String.valueOf(op.getValue()));
					}
					if ("$ne".equals(op.getName())) {
						return !String.valueOf(actual).equals(String.valueOf(op.getValue()));
					}
					return true; // unsupported operator → do not exclude
				}
				default:
					return true;
			}
		}

		private static Object readField(Object o, String field) {
			try {
				java.lang.reflect.Field jf = o.getClass().getDeclaredField(field);
				jf.setAccessible(true);
				return jf.get(o);
			} catch (Exception e) {
				return null;
			}
		}

		@Override
		public Object save(Object object) {
			String uuid = uuidOf(object);
			if (uuid != null) {
				this.storage.removeIf(stored -> uuid.equals(uuidOf(stored)));
			}
			this.storage.add(object);
			return object;
		}

		@Override
		public void delete(Object object) {
			String uuid = uuidOf(object);
			if (uuid != null) {
				this.storage.removeIf(stored -> uuid.equals(uuidOf(stored)));
			} else {
				this.storage.remove(object);
			}
		}

		@Override
		public long count(IFilter filter) {
			return this.storage.size();
		}

		private static String uuidOf(Object object) {
			try {
				java.lang.reflect.Field field = object.getClass().getDeclaredField("uuid");
				field.setAccessible(true);
				Object value = field.get(object);
				return value != null ? value.toString() : null;
			} catch (ReflectiveOperationException e) {
				return null;
			}
		}
	}
}
