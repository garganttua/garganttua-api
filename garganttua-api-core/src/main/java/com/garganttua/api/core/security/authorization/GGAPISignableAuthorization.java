package com.garganttua.api.core.security.authorization;

import java.util.Date;
import java.util.List;

import com.garganttua.api.spec.entity.annotations.GGAPIEntityMandatory;
import com.garganttua.api.spec.security.key.IGGAPIKeyRealm;

public abstract class GGAPISignableAuthorization extends GGAPIAuthorization {
	
	@GGAPIEntityMandatory
	protected IGGAPIKeyRealm key = null;
	
	public GGAPISignableAuthorization(String uuid, String id, String tenantId, String ownerUuid, List<String> authorities, Date creationDate, Date expirationDate, IGGAPIKeyRealm keyRealm) {
		super(uuid, id, tenantId, ownerUuid, authorities, creationDate, expirationDate);
		this.key = keyRealm;
	}
	
	public GGAPISignableAuthorization(byte[] raw, IGGAPIKeyRealm realm) {
		super(null, null, null, null, null, null, null);
		this.key = realm;
	}
}
