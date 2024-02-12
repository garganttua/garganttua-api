package com.garganttua.api.security.keys.managers.mongo;

import com.garganttua.api.core.GGAPICaller;
import com.garganttua.api.repository.GGAPIRepository;
import com.garganttua.api.security.keys.GGAPIKeyExpiredException;
import com.garganttua.api.security.keys.GGAPIKeyRealms;
import com.garganttua.api.security.keys.IGGAPIKeyRealm;
import com.garganttua.api.security.keys.managers.db.IGGAPIDBKeyKeeper;

import lombok.Setter;

public class GGAPIKeyRepository extends GGAPIRepository<GGAPIKeyRealmEntity, GGAPIKeyRealmDTO> implements IGGAPIDBKeyKeeper {

	@Setter
	private String superTenantId;

	@Override
	public IGGAPIKeyRealm getRealm(String realmStr) {

		GGAPICaller caller = new GGAPICaller();
		caller.setTenantId(this.superTenantId);
		caller.setRequestedTenantId(this.superTenantId);
		caller.setSuperTenant(true);
		GGAPIKeyRealmEntity realm = this.getOneByUuid(caller, realmStr);
		if( realm != null) {
			return GGAPIKeyRealms.createRealm(realmStr, realm.getAlgorithm(), realm.getCipheringKey(), realm.getUncipheringKey());
		}
		return null;
	}

	@Override
	public void createRealm(IGGAPIKeyRealm realm) throws GGAPIKeyExpiredException {
		GGAPIKeyRealmEntity entity = new GGAPIKeyRealmEntity(realm.getName(), realm.getName(), realm.getAlgo(), realm.getCipheringKey(), realm.getUncipheringKey());
		GGAPICaller caller = new GGAPICaller();
		caller.setTenantId(this.superTenantId);
		caller.setRequestedTenantId(this.superTenantId);
		caller.setSuperTenant(true);
		this.save(caller, entity);
	}

	@Override
	public void update(IGGAPIKeyRealm realm) throws GGAPIKeyExpiredException {
		GGAPIKeyRealmEntity entity = new GGAPIKeyRealmEntity(realm.getName(), realm.getName(), realm.getAlgo(), realm.getCipheringKey(), realm.getUncipheringKey());
		GGAPICaller caller = new GGAPICaller();
		caller.setTenantId(this.superTenantId);
		caller.setRequestedTenantId(this.superTenantId);
		caller.setSuperTenant(true);
		this.update(caller, entity);
	}

}
