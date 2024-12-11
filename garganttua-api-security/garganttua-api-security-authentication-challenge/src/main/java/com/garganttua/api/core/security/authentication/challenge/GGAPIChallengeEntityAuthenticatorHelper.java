package com.garganttua.api.core.security.authentication.challenge;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.garganttua.api.core.GGAPIInfosHelper;
import com.garganttua.api.core.entity.tools.GGAPIEntityHelper;
import com.garganttua.api.core.security.GGAPIExpirationTools;
import com.garganttua.api.core.security.entity.tools.GGAPIEntityAuthenticatorHelper;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.caller.IGGAPICaller;
import com.garganttua.api.spec.security.key.IGGAPIKeyRealm;

public class GGAPIChallengeEntityAuthenticatorHelper {
	
	public static GGAPIChallenge getChallenge(Object entity) throws GGAPIException {
		GGAPIChallengeAuthenticatorInfos infos = GGAPIChallengeEntityAuthenticatorChecker.checkEntityAuthenticatorClass(entity.getClass());
		
		final byte[] rawChallenge = GGAPIInfosHelper.getValue(entity, GGAPIChallengeEntityAuthenticatorChecker::checkEntityAuthenticatorClass, GGAPIChallengeAuthenticatorInfos::challengeFieldAddress);
		Date challengeExpiration = GGAPIInfosHelper.getValue(entity, GGAPIChallengeEntityAuthenticatorChecker::checkEntityAuthenticatorClass, GGAPIChallengeAuthenticatorInfos::challengeExpirationFieldAddress);
		GGAPIChallengeType type = infos.challengeType();
		return new GGAPIChallenge(rawChallenge, type, challengeExpiration);
	}

	public static GGAPIChallenge getOrCreateChallengeAndSave(IGGAPICaller caller, Object entity) throws GGAPIException {
		GGAPIChallengeAuthenticatorInfos infos = GGAPIChallengeEntityAuthenticatorChecker.checkEntityAuthenticatorClass(entity.getClass());
		GGAPIChallengeType type = infos.challengeType();
		int lifeTime = infos.challengeLifeTime();
		TimeUnit unit = infos.challengeLifeTimeUnit();
		
		GGAPIChallenge challenge = getChallenge(entity);
		
		String newChallenge = UUID.randomUUID().toString();

		switch (type) {
		case TIME_LIMITED:
			if( challenge.getChallenge() == null ) {
				challenge.setChallenge(newChallenge.getBytes());
			}
			if( challenge.getExpiration() == null || Instant.now().isAfter(challenge.getExpiration().toInstant()) 
					|| !GGAPIEntityAuthenticatorHelper.isCredentialsNonExpired(entity) ) {
				Date expiration = GGAPIExpirationTools.getExpirationDateFromNow(lifeTime, unit);
				challenge.setExpiration(expiration);
				challenge.setChallenge(newChallenge.getBytes());
				GGAPIEntityAuthenticatorHelper.setCredentialsNonExpired(entity, true);
			}
			break;
		case ONE_TIME:
			challenge.setChallenge(newChallenge.getBytes());

			if( challenge.getExpiration() == null || Instant.now().isAfter(challenge.getExpiration().toInstant())
					|| !GGAPIEntityAuthenticatorHelper.isCredentialsNonExpired(entity) ) {
				Date expiration = GGAPIExpirationTools.getExpirationDateFromNow(lifeTime, unit);
				challenge.setExpiration(expiration);
				challenge.setChallenge(newChallenge.getBytes());
				GGAPIEntityAuthenticatorHelper.setCredentialsNonExpired(entity, true);
			}
			break;
		case UNLIMITED:
			if( challenge.getChallenge() == null ) {
				challenge.setChallenge(newChallenge.getBytes());
			}
			challenge.setExpiration(null);
			GGAPIEntityAuthenticatorHelper.setCredentialsNonExpired(entity, true);
			break;
		}
		setChallenge(entity, challenge);
		GGAPIEntityHelper.save(entity, caller, new HashMap<String, String>());		
		return challenge;
	}

	public static IGGAPIKeyRealm getKeyRealm(Object entity) throws GGAPIException {
		return GGAPIInfosHelper.getValue(entity, GGAPIChallengeEntityAuthenticatorChecker::checkEntityAuthenticatorClass, GGAPIChallengeAuthenticatorInfos::keyRealmFieldAddress);
	}

	public static void setkeyRealm(Object entity, IGGAPIKeyRealm key) throws GGAPIException {
		GGAPIInfosHelper.setValue(entity, GGAPIChallengeEntityAuthenticatorChecker::checkEntityAuthenticatorClass, GGAPIChallengeAuthenticatorInfos::keyRealmFieldAddress, key);
	}

	public static void setChallenge(Object entity, GGAPIChallenge challenge) throws GGAPIException {
		GGAPIInfosHelper.setValue(entity, GGAPIChallengeEntityAuthenticatorChecker::checkEntityAuthenticatorClass, GGAPIChallengeAuthenticatorInfos::challengeFieldAddress, challenge.getChallenge());
		GGAPIInfosHelper.setValue(entity, GGAPIChallengeEntityAuthenticatorChecker::checkEntityAuthenticatorClass, GGAPIChallengeAuthenticatorInfos::challengeExpirationFieldAddress, challenge.getExpiration());		
	}
}
