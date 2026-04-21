package com.garganttua.api.core.security.authentication.challenge;

import java.util.concurrent.TimeUnit;

import com.garganttua.api.commons.security.key.EncryptionMode;
import com.garganttua.api.commons.security.key.EncryptionPaddingMode;
import com.garganttua.api.commons.security.key.KeyAlgorithm;
import com.garganttua.api.commons.security.key.SignatureAlgorithm;
import com.garganttua.core.reflection.ObjectAddress;

public record ChallengeAuthenticatorInfos(
		ObjectAddress challengeFieldAddress, 
		ObjectAddress keyRealmFieldAddress,
		ObjectAddress challengeExpirationFieldAddress,
		Class<?> key,
		boolean autoCreateKey,
		KeyAlgorithm keyAlgorithm,
		int keyLifeTime,
		TimeUnit keyLifeTimeUnit,
		EncryptionMode encryptionMode,
		EncryptionPaddingMode encryptionPadding,
		SignatureAlgorithm signatureAlgorithm, 
		ChallengeType challengeType,
		int challengeLifeTime,
		TimeUnit challengeLifeTimeUnit) {

}
