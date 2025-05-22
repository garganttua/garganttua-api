package com.garganttua.api.spec.security.authorization;

import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.security.key.IGGAPIKeyRealm;

public interface IGGAPISignableAuthorization {

    void sign(IGGAPIKeyRealm key) throws GGAPIException;

    byte[] getSignature() throws GGAPIException;

}
