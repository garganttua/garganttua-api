package com.garganttua.api.starter.mongodb;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.starter.AutoConfigurationContext;
import com.garganttua.api.commons.starter.IApiAutoConfiguration;
import com.garganttua.core.reflection.IClass;

/**
 * Test-only auto-config that disables the security gate on the {@code Widget}
 * domain so this integration test can focus purely on MongoDB persistence. It
 * registers NO DAO — the real {@link MongoAutoConfiguration} on the classpath
 * supplies the default MongoDB DAO, which is exactly what this test exercises.
 * Pre-declaring the domain lets the {@code @Entity} scan re-enter the same
 * builder.
 */
public final class AnonymousAccessAutoConfiguration implements IApiAutoConfiguration {

	@Override
	public int order() {
		// After MongoAutoConfiguration (0) but before any transport (100).
		return 1;
	}

	@Override
	public void apply(AutoConfigurationContext context) throws ApiException {
		context.apiBuilder()
				.domain(IClass.getClass(GarganttuaApplicationMongoTest.Widget.class))
					.security().disable(true).up()
				.up();
	}
}
