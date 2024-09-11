package com.garganttua.api.core.security.keys.managers.mongo;

import com.garganttua.api.core.caller.GGAPICallerICallerICaller;
import com.garganttua.api.core.repository.GGAPIRepositoryException;
import com.garganttua.api.core.repository.GGAPISimpleRepository;
import com.garganttua.api.core.security.keys.managers.db.GGAPIDBKeyKeeperException;
import com.garganttua.api.core.security.keys.managers.db.IGGAPIDBKeyKeeper;
import com.garganttua.api.spring.keys.domain.GGAPIKeyExpiredException;
import com.garganttua.api.spring.keys.domain.GGAPIKeyRealmEntity;
import com.garganttua.api.spring.keys.domain.GGAPIKeyRealms;
import com.garganttua.api.spring.keys.domain.IGGAPIKeyRealm;

import lombok.Setter;

public class GGAPIKeyMongoRepository extends GGAPISimpleRepository implements IGGAPIDBKeyKeeper {

	@Setter
	private String superTenantId;

	@Override
	public IGGAPIKeyRealm getRealm(String realmStr) throws GGAPIDBKeyKeeperException {

		GGAPICaller caller = new GGAPICaller();
		caller.setTenantId(this.superTenantId);
		caller.setRequestedTenantId(this.superTenantId);
		caller.setSuperTenant(true);
		GGAPIKeyRealmEntity realm;
		try {
			realm = (GGAPIKeyRealmEntity) this.getOneByUuid(caller, realmStr);
			if( realm != null) {
				return GGAPIKeyRealms.createRealm(realmStr, realm.getAlgorithm(), realm.getCipheringKey(), realm.getUncipheringKey());
			}
			return null;
		} catch (GGAPIRepositoryException e) {
			throw new GGAPIDBKeyKeeperException(e);
		}
	}

	@Override
	public void createRealm(IGGAPIKeyRealm realm) throws GGAPIDBKeyKeeperException {
		try {
			GGAPIKeyRealmEntity entity = new GGAPIKeyRealmEntity(realm.getName(), realm.getName(), realm.getAlgo(), realm.getCipheringKey(), realm.getUncipheringKey());
			GGAPICaller caller = new GGAPICaller();
			caller.setTenantId(this.superTenantId);
			caller.setRequestedTenantId(this.superTenantId);
			caller.setSuperTenant(true);
			this.save(caller, entity);
		} catch (GGAPIRepositoryException | GGAPIKeyExpiredException e) {
			throw new GGAPIDBKeyKeeperException(e);
		}
	}

	@Override
	public void update(IGGAPIKeyRealm realm) throws GGAPIDBKeyKeeperException {
		try {
			GGAPIKeyRealmEntity entity = new GGAPIKeyRealmEntity(realm.getName(), realm.getName(), realm.getAlgo(), realm.getCipheringKey(), realm.getUncipheringKey());
			GGAPICaller caller = new GGAPICaller();
			caller.setTenantId(this.superTenantId);
			caller.setRequestedTenantId(this.superTenantId);
			caller.setSuperTenant(true);
			this.update(caller, entity);
		} catch (GGAPIRepositoryException | GGAPIKeyExpiredException e) {
			throw new GGAPIDBKeyKeeperException(e);
		}
	}

}
