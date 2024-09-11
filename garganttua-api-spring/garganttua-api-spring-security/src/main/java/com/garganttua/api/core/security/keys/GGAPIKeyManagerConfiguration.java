package com.garganttua.api.core.security.keys;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.garganttua.api.core.security.keys.managers.db.GGAPIDBKeyManager;
import com.garganttua.api.core.security.keys.managers.db.IGGAPIDBKeyKeeper;
import com.garganttua.api.core.security.keys.managers.inmemory.GGAPIInMemoryKeyManager;
import com.garganttua.api.spec.engine.IGGAPIEngine;
import com.garganttua.api.spring.keys.domain.GGAPIKeyRealmEntity;

@Configuration
@ConditionalOnProperty(name = "com.garganttua.api.security.key.manager", havingValue = "enabled", matchIfMissing = false)
public class GGAPIKeyManagerConfiguration {
	
	@Value("${com.garganttua.api.security:disabled}")
	private String securityEnabled = "disabled";
	
	@Value("${com.garganttua.api.security.key.manager.type}")
	private GGAPIKeyManagerType keyManagerType;
	
	@Value("${com.garganttua.api.superTenantId}")
	private String superTenantId;
	
//	@Autowired
	private Optional<IGGAPIKeyManager> customKeyManager;
	
	@Autowired
	private Optional<IGGAPIDBKeyKeeper> keyKeeper;
	
	@Autowired
	private IGGAPIEngine engine;

	@Bean
	public IGGAPIKeyManager keyManager() {
		if( this.securityEnabled.equals("enabled") ) {
			IGGAPIKeyManager manager = null;
			switch (this.keyManagerType) {
			default:
			case inmemory:
				return new GGAPIInMemoryKeyManager();
			case db:
				manager = new GGAPIDBKeyManager();
				keyKeeper.ifPresent(k -> k.setSuperTenantId(this.superTenantId));
				((GGAPIDBKeyManager) manager).setKeyKeeper(this.keyKeeper);
				return manager;
			case keystore:
				return null;
			case custom:
				return this.customKeyManager.get();
			case mongo:
				manager = new GGAPIDBKeyManager();
				IGGAPIDBKeyKeeper keeper = (IGGAPIDBKeyKeeper) engine.getRepositoriesRegistry().getRepository(GGAPIKeyRealmEntity.domain);
				keeper.setSuperTenantId(this.superTenantId);
				((GGAPIDBKeyManager) manager).setKeyKeeper(Optional.ofNullable(keeper));
				return manager;
			}
		} else {
			return null;
		}
	}

}
