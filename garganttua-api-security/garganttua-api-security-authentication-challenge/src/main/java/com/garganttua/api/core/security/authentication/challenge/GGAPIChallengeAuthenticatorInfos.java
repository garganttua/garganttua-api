package com.garganttua.api.core.security.authentication.challenge;

import java.util.concurrent.TimeUnit;

import com.garganttua.api.spec.security.key.GGAPIEncryptionMode;
import com.garganttua.api.spec.security.key.GGAPIEncryptionPaddingMode;
import com.garganttua.api.spec.security.key.GGAPIKeyAlgorithm;
import com.garganttua.api.spec.security.key.GGAPISignatureAlgorithm;
import com.garganttua.reflection.GGObjectAddress;

public record GGAPIChallengeAuthenticatorInfos(
		GGObjectAddress challengeFieldAddress, 
		GGObjectAddress keyRealmFieldAddress,
		GGObjectAddress challengeExpirationFieldAddress,
		Class<?> key,
		boolean autoCreateKey,
		GGAPIKeyAlgorithm keyAlgorithm,
		int keyLifeTime,
		TimeUnit keyLifeTimeUnit,
		GGAPIEncryptionMode encryptionMode,
		GGAPIEncryptionPaddingMode encryptionPadding,
		GGAPISignatureAlgorithm signatureAlgorithm, 
		GGAPIChallengeType challengeType,
		int challengeLifeTime,
		TimeUnit challengeLifeTimeUnit) {

}
