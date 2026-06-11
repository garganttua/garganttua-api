package com.garganttua.api.starter.mongodb;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.starter.AutoConfigurationContext;
import com.garganttua.api.commons.starter.IApiAutoConfiguration;
import com.garganttua.dao.mongodb.MongoDao;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

/**
 * Auto-configures MongoDB persistence: reads {@code mongodb.uri} and
 * {@code mongodb.database} from the application config, opens a single
 * {@link MongoClient}, and registers a default DAO factory that yields a
 * {@link MongoDao} per domain (collection = domain name). Any domain whose dto
 * sets an explicit {@code .db(...)} keeps it — the factory is only consulted as
 * a fallback.
 *
 * <p>Runs at {@code order() = 0} (persistence before transport). Discovered via
 * {@link java.util.ServiceLoader}.
 */
public final class MongoAutoConfiguration implements IApiAutoConfiguration {

	@Override
	public int order() {
		return 0;
	}

	@Override
	public void apply(AutoConfigurationContext context) throws ApiException {
		String uri = context.config().getString("mongodb.uri")
				.orElseThrow(() -> new ApiException(
						"mongodb.uri is required by the MongoDB starter. Set it in application.yaml "
								+ "(mongodb.uri: mongodb://host:27017) or via GARGANTTUA_MONGODB_URI."));
		String database = context.config().getString("mongodb.database")
				.orElseThrow(() -> new ApiException(
						"mongodb.database is required by the MongoDB starter. Set it in application.yaml "
								+ "(mongodb.database: myapp) or via GARGANTTUA_MONGODB_DATABASE."));

		MongoClient client = MongoClients.create(uri);
		MongoDatabase mongoDatabase = client.getDatabase(database);
		context.registerResource(client);

		// One MongoDao per domain; the collection is the plural domain name.
		context.registerDefaultDao((domainName, dtoClass) -> new MongoDao(mongoDatabase, domainName));
	}
}
