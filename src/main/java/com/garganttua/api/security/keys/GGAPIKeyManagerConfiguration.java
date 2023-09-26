package com.garganttua.api.security.keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "com.garganttua.api.security.key.manager", havingValue = "enabled", matchIfMissing = false)
public class GGAPIKeyManagerConfiguration {
	
	@Value("${com.garganttua.api.security.key.manager.type}")
	private GGAPIKeyManagerType keyManagerType;

	@Bean
	public IGGAPIKeyManager keyManager() {
		switch (this.keyManagerType) {
		default:
		case inmemory:
			return new GGAPIInMemoryKeyManager();
		case db:
			return new GGAPIDBKeyManager();
		case keystore:
			return null;
		}
	}

}
