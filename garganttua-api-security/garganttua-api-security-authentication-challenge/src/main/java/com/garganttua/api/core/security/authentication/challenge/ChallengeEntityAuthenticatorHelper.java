package com.garganttua.api.core.security.authentication.challenge;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.garganttua.api.core.context.InfosHelper;
import com.garganttua.api.core.mapper.DefaultMapper;
import com.garganttua.api.core.security.ExpirationTools;
import com.garganttua.api.core.security.entity.tools.EntityAuthenticatorHelper;
import com.garganttua.core.CoreException;
import com.garganttua.core.reflection.ObjectAddress;
import com.garganttua.api.commons.caller.ICaller;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.security.key.IKeyRealm;

public class ChallengeEntityAuthenticatorHelper {

	public static Challenge getChallenge(Object entity) throws CoreException {
		ChallengeAuthenticatorInfos infos = ChallengeEntityAuthenticatorChecker.checkEntityAuthenticatorClass(entity.getClass());

		final byte[] rawChallenge = InfosHelper.getValue(entity, ChallengeEntityAuthenticatorChecker::checkEntityAuthenticatorClass, ChallengeAuthenticatorInfos::challengeFieldAddress);
		Date challengeExpiration = InfosHelper.getValue(entity, ChallengeEntityAuthenticatorChecker::checkEntityAuthenticatorClass, ChallengeAuthenticatorInfos::challengeExpirationFieldAddress);
		ChallengeType type = infos.challengeType();
		return new Challenge(rawChallenge, type, challengeExpiration);
	}

	public static Challenge getOrCreateChallengeAndSave(ICaller caller, Object entity, IDomain<?> domainContext) throws CoreException {
		ChallengeAuthenticatorInfos infos = ChallengeEntityAuthenticatorChecker.checkEntityAuthenticatorClass(entity.getClass());
		ChallengeType type = infos.challengeType();
		int lifeTime = infos.challengeLifeTime();
		TimeUnit unit = infos.challengeLifeTimeUnit();

		Challenge challenge = getChallenge(entity);

		String newChallenge = UUID.randomUUID().toString();

		switch (type) {
		case TIME_LIMITED:
			if( challenge.getChallenge() == null ) {
				challenge.setChallenge(newChallenge.getBytes());
			}
			if( challenge.getExpiration() == null || Instant.now().isAfter(challenge.getExpiration().toInstant())
					|| !EntityAuthenticatorHelper.isCredentialsNonExpired(entity) ) {
				Date expiration = ExpirationTools.getExpirationDateFromNow(lifeTime, unit);
				challenge.setExpiration(expiration);
				challenge.setChallenge(newChallenge.getBytes());
				EntityAuthenticatorHelper.setCredentialsNonExpired(entity, true);
			}
			break;
		case ONE_TIME:
			challenge.setChallenge(newChallenge.getBytes());

			if( challenge.getExpiration() == null || Instant.now().isAfter(challenge.getExpiration().toInstant())
					|| !EntityAuthenticatorHelper.isCredentialsNonExpired(entity) ) {
				Date expiration = ExpirationTools.getExpirationDateFromNow(lifeTime, unit);
				challenge.setExpiration(expiration);
				challenge.setChallenge(newChallenge.getBytes());
				EntityAuthenticatorHelper.setCredentialsNonExpired(entity, true);
			}
			break;
		case UNLIMITED:
			if( challenge.getChallenge() == null ) {
				challenge.setChallenge(newChallenge.getBytes());
			}
			challenge.setExpiration(null);
			EntityAuthenticatorHelper.setCredentialsNonExpired(entity, true);
			break;
		}
		setChallenge(entity, challenge);
		ObjectAddress uuidAddress = domainContext.getUuidFieldAddress();
		String uuid = (String) DefaultMapper.reflection().getFieldValue(entity, uuidAddress);
		domainContext.updateOne(uuid, entity, caller);
		return challenge;
	}

	public static IKeyRealm getKeyRealm(Object entity) throws CoreException {
		return InfosHelper.getValue(entity, ChallengeEntityAuthenticatorChecker::checkEntityAuthenticatorClass, ChallengeAuthenticatorInfos::keyRealmFieldAddress);
	}

	public static void setkeyRealm(Object entity, IKeyRealm key) throws CoreException {
		InfosHelper.setValue(entity, ChallengeEntityAuthenticatorChecker::checkEntityAuthenticatorClass, ChallengeAuthenticatorInfos::keyRealmFieldAddress, key);
	}

	public static void setChallenge(Object entity, Challenge challenge) throws CoreException {
		InfosHelper.setValue(entity, ChallengeEntityAuthenticatorChecker::checkEntityAuthenticatorClass, ChallengeAuthenticatorInfos::challengeFieldAddress, challenge.getChallenge());
		InfosHelper.setValue(entity, ChallengeEntityAuthenticatorChecker::checkEntityAuthenticatorClass, ChallengeAuthenticatorInfos::challengeExpirationFieldAddress, challenge.getExpiration());
	}
}
