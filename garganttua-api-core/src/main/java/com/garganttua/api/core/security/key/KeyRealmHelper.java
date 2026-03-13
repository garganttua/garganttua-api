package com.garganttua.api.core.security.key;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;

import com.garganttua.api.core.security.exceptions.SecurityException;
import com.garganttua.api.spec.security.key.EncryptionMode;
import com.garganttua.api.spec.security.key.EncryptionPaddingMode;
import com.garganttua.api.spec.security.key.KeyAlgorithm;
import com.garganttua.api.spec.security.key.SignatureAlgorithm;
import com.garganttua.api.spec.security.key.IKeyRealm;
import com.garganttua.core.CoreException;

public class KeyRealmHelper {

	public static IKeyRealm newInstance(Class<?> type, String realmName, KeyAlgorithm algorithm,
			Date expirationDate, EncryptionMode encryptionMode, EncryptionPaddingMode paddingMode,
			SignatureAlgorithm signatureAlgorithm) throws CoreException {
		Constructor<?> ctor;
		try {
			ctor = type.getDeclaredConstructor(String.class, KeyAlgorithm.class, Date.class,
					EncryptionMode.class, EncryptionPaddingMode.class, SignatureAlgorithm.class);
			IKeyRealm keyRealm = (IKeyRealm) ctor.newInstance(realmName, algorithm, expirationDate,
					encryptionMode, paddingMode, signatureAlgorithm);
			return keyRealm;
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			throw new SecurityException(
					"Cannot instantiate new key realm of type " + type.getSimpleName(), e);
		}
	}

}
