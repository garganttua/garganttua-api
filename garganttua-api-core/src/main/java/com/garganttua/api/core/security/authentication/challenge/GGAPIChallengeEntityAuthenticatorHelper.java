package com.garganttua.api.core.security.authentication.challenge;

import com.garganttua.api.core.GGAPIInfosHelper;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.security.authentication.GGAPIChallengeAuthenticatorInfos;
import com.garganttua.api.spec.security.key.IGGAPIKeyRealm;

public class GGAPIChallengeEntityAuthenticatorHelper {

	public static byte[] getChallenge(Object entity) throws GGAPIException {
		return GGAPIInfosHelper.getValue(entity, GGAPIChallengeEntityAuthenticatorChecker::checkEntityAuthenticatorClass, GGAPIChallengeAuthenticatorInfos::challengeFieldAddress);
	}

	public static IGGAPIKeyRealm getKeyRealm(Object entity) throws GGAPIException {
		return GGAPIInfosHelper.getValue(entity, GGAPIChallengeEntityAuthenticatorChecker::checkEntityAuthenticatorClass, GGAPIChallengeAuthenticatorInfos::keyRealmFieldAddress);
	}

	public static void setkeyRealm(Object entity, IGGAPIKeyRealm key) throws GGAPIException {
		GGAPIInfosHelper.setValue(entity, GGAPIChallengeEntityAuthenticatorChecker::checkEntityAuthenticatorClass, GGAPIChallengeAuthenticatorInfos::keyRealmFieldAddress, key);
	}

	public static void setChallenge(Object entity, byte[] challenge) throws GGAPIException {
		GGAPIInfosHelper.setValue(entity, GGAPIChallengeEntityAuthenticatorChecker::checkEntityAuthenticatorClass, GGAPIChallengeAuthenticatorInfos::challengeFieldAddress, challenge);		
	}


}
