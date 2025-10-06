package com.garganttua.api.core;

public class DummyAuthentication {

    Boolean authenticate(Byte[] credentials, Object principal) {
        return true;
    }

    void applySecurityOnEntity(Object entity) {

    }

}
