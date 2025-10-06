package com.garganttua.api.core;

import java.time.Instant;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
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

    private String infoFromDto1;

    private String infoFromDto2;

    @Getter
    private String afterGetString;

    public void afterGet(String afterGetString){
        this.afterGetString = afterGetString;
    }

    public void testMethod() {

    }

    public void testMethod2(String string) {

    }

    public Byte[] toByteArray() {
        return null;
    }

    public void fromByteArray(Byte[] array) {

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
