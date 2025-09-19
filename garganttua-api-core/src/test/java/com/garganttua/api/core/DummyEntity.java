package com.garganttua.api.core;

import java.time.Instant;
import java.util.List;

public class DummyEntity {

    private String tenantId;

    private String uuid;

    private String id;

    private Boolean hidden;

    private List<String> authorities;

    private Instant creation;

    private Instant expiration;

    private Boolean revoked;

    private Instant refreshExpiration;

    private Boolean refreshRevoked;

    private Boolean active;

    public void testMethod() {

    }

    public void testMethod2(String string) {

    }

    public Byte[] toByteArray() {
        return null;
    }

    public Boolean validate(DummyKey key) {
        return false;
    }

    public Boolean validateAgainst(DummyEntity test, DummyKey key) {
        return false;
    }

    public void sign(DummyKey key) {

    }

    public Byte[] refreshToByteArray() {
        return null;
    }

    public Boolean refreshValidate(DummyKey key) {
        return false;
    }

    public Boolean refreshValidateAgainst(DummyEntity test, DummyKey key) {
        return false;
    }

}
