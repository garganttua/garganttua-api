package com.garganttua.dao.mongodb.aot;

import com.garganttua.core.aot.commons.IAOTInfrastructureSeed;
import com.garganttua.core.aot.commons.IAOTSeedContext;

import com.garganttua.dao.mongodb.MongoDao;

/**
 * Pre-registers {@code garganttua-api-dao-mongodb}'s framework-public concrete
 * types in the {@code AOTRegistry} on cold-start, so they resolve at runtime in
 * pure-AOT mode (when {@code garganttua-runtime-reflection} is absent and
 * {@code AOTReflectionProvider} is the only provider).
 *
 * <p>Coverage: {@link MongoDao} — the {@code IDao} implementation users wire via
 * {@code .db(new MongoDao(...))}. It carries {@code @Reflected} so the AOT
 * processor emits its reflection descriptor; this seed makes the class
 * resolvable in the registry. {@code MongoFilterConverter} is a static-only
 * utility (no instantiation, no reflective access) and intentionally not
 * registered.
 *
 * <p>Discovered via {@link java.util.ServiceLoader} from
 * {@code META-INF/services/com.garganttua.core.aot.commons.IAOTInfrastructureSeed}.
 *
 * @since 3.0.0-ALPHA01
 */
public class MongoDaoInfrastructureSeed implements IAOTInfrastructureSeed {

    @Override
    public void seed(IAOTSeedContext context) {
        context.registerClass(MongoDao.class);
    }
}
