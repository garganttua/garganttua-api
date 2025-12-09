package com.garganttua.api.spec.security.authorization;

import com.garganttua.api.spec.security.key.IKeyRealm;
import com.garganttua.core.CoreException;

public interface ISignableAuthorization {

    void sign(IKeyRealm key) throws CoreException;

    byte[] getSignature() throws CoreException;

}
