package com.garganttua.api.spec.security.authorization;

import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.security.key.IKeyRealm;

public interface ISignableAuthorization {

    void sign(IKeyRealm key) throws CoreException;

    byte[] getSignature() throws CoreException;

}
