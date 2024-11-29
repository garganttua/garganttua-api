package com.garganttua.api.core.security.key;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;

import com.garganttua.api.core.entity.exceptions.GGAPIEntityException;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.security.key.GGAPIKeyAlgorithm;
import com.garganttua.api.spec.security.key.IGGAPIKeyRealm;

public class GGAPIKeyRealmHelper {
	
	public static IGGAPIKeyRealm newInstance(Class<?> type, String realmName, GGAPIKeyAlgorithm algorithm, Date expirationDate) throws GGAPIException {
		Constructor<?> ctor;
		try {
			ctor = type.getDeclaredConstructor(String.class, GGAPIKeyAlgorithm.class, Date.class);
			IGGAPIKeyRealm keyRealm = (IGGAPIKeyRealm) ctor.newInstance(realmName, algorithm, expirationDate);
			
			return keyRealm;
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new GGAPIEntityException(GGAPIExceptionCode.INVOKE_METHOD, "Cannot instanciate new authentication of type "+type.getSimpleName(), e);
		}
	}

}
