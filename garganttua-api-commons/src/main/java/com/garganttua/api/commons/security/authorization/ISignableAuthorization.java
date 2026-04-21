package com.garganttua.api.commons.security.authorization;

import com.garganttua.api.commons.security.key.IKeyRealm;
import com.garganttua.api.commons.ApiException;

public interface ISignableAuthorization {

    void sign(IKeyRealm key) throws ApiException;

    byte[] getSignature() throws ApiException;

}
