package com.garganttua.api.core.security.authentication.challenge;

import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.security.authentication.GGAPIChallengeAuthenticatorInfos;
import com.garganttua.api.spec.security.key.IGGAPIKeyRealm;
import com.garganttua.reflection.GGReflectionException;
import com.garganttua.reflection.query.GGObjectQueryFactory;

public class GGAPIChallengeEntityAuthenticatorHelper {

	public static byte[] getChallenge(Object entity) throws GGAPIException {
		GGAPIChallengeAuthenticatorInfos infos = GGAPIChallengeEntityAuthenticatorChecker.checkEntityAuthenticator(entity.getClass());
		if( infos.challengeFieldAddress() != null ) {
			try {
				return (byte[]) GGObjectQueryFactory.objectQuery(entity).getValue(infos.challengeFieldAddress());
			} catch (GGReflectionException e) {
				GGAPIException.processException(e);
			}
		}
		return null;
	}

	public static IGGAPIKeyRealm getKeyRealm(Object entity) throws GGAPIException {
		GGAPIChallengeAuthenticatorInfos infos = GGAPIChallengeEntityAuthenticatorChecker.checkEntityAuthenticator(entity.getClass());
		if( infos.keyRealmFieldAddress() != null ) {
			try {
				return (IGGAPIKeyRealm) GGObjectQueryFactory.objectQuery(entity).getValue(infos.keyRealmFieldAddress());
			} catch (GGReflectionException e) {
				GGAPIException.processException(e);
			}
		}
		return null;
	}

}
