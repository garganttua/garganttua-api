package com.garganttua.api.security.keys;

import java.security.Key;

public interface IGGAPIKeyRealm {

	Key getCipheringKey() throws GGAPIKeyExpiredException;

	Key getUncipheringKey() throws GGAPIKeyExpiredException;

}
