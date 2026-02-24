package com.garganttua.api.spec.security.authorization;

import com.garganttua.api.spec.security.key.IKeyRealm;
import com.garganttua.api.spec.ApiException;

public interface ISignableAuthorization {

    void sign(IKeyRealm key) throws ApiException;

    byte[] getSignature() throws ApiException;

}
