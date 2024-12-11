package com.garganttua.api.core.security.authentication.challenge;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.garganttua.api.core.security.exceptions.GGAPISecurityException;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticatorChallenge;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticatorChallengeExpiration;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticatorKeyRealm;
import com.garganttua.api.spec.security.authentication.GGAPIChallengeAuthenticatorInfos;
import com.garganttua.api.spec.security.key.GGAPIKeyAlgorithm;
import com.garganttua.api.spec.security.key.IGGAPIKeyRealm;
import com.garganttua.reflection.GGReflectionException;
import com.garganttua.reflection.query.GGObjectQueryFactory;
import com.garganttua.reflection.query.IGGObjectQuery;
import com.garganttua.reflection.utils.GGObjectReflectionHelper;

public class GGAPIChallengeEntityAuthenticatorChecker {

	private static Map<Class<?>, GGAPIChallengeAuthenticatorInfos> infos = new HashMap<Class<?>, GGAPIChallengeAuthenticatorInfos>();
	
	public static GGAPIChallengeAuthenticatorInfos checkEntityAuthenticatorClass(Class<? extends Object> entityAuthenticatorClass) throws GGAPISecurityException {
		if( GGAPIChallengeEntityAuthenticatorChecker.infos.containsKey(entityAuthenticatorClass) ) {
			return GGAPIChallengeEntityAuthenticatorChecker.infos.get(entityAuthenticatorClass);  
		}

		String keyRealmFieldName = null; 
		String challengeFieldName = null;
		String challengeExpirationFieldName = null;
		
		try {
			keyRealmFieldName= GGAPIChallengeEntityAuthenticatorChecker.checkKeyRealmAnnotationPresentAndFieldHasGoodType(entityAuthenticatorClass);
		} catch (GGAPIException e) {
			throw new GGAPISecurityException(GGAPIExceptionCode.ENTITY_DEFINITION, "Entity Authenticator " + entityAuthenticatorClass.getSimpleName() + " does not have a field annotated with @GGAPIAuthenticatorKeyRealm");
		}
		
		try {
			challengeFieldName = GGAPIChallengeEntityAuthenticatorChecker.checkChallengeAnnotationPresentAndFieldHasGoodType(entityAuthenticatorClass);
		} catch (GGAPIException e) {
			throw new GGAPISecurityException(GGAPIExceptionCode.ENTITY_DEFINITION, "Entity Authenticator " + entityAuthenticatorClass.getSimpleName() + " does not have a field annotated with @GGAPIAuthenticatorChallenge");
		}
		
		try {
			challengeExpirationFieldName = GGAPIChallengeEntityAuthenticatorChecker.checkChallengeExpirationAnnotationPresentAndFieldHasGoodType(entityAuthenticatorClass);
		} catch (GGAPIException e) {
			throw new GGAPISecurityException(GGAPIExceptionCode.ENTITY_DEFINITION, "Entity Authenticator " + entityAuthenticatorClass.getSimpleName() + " does not have a field annotated with @GGAPIAuthenticatorChallengeExpiration");
		}

		GGAPIAuthenticatorKeyRealm keyRealmAnnotation = GGObjectReflectionHelper.getField(entityAuthenticatorClass, keyRealmFieldName).getAnnotation(GGAPIAuthenticatorKeyRealm.class);
		
		Class<?> keyType = keyRealmAnnotation.key();
		boolean autoCreateKey = keyRealmAnnotation.autoCreateKey();
		GGAPIKeyAlgorithm keyAlgorithm = keyRealmAnnotation.keyAlgorithm();
		int keyLifeTime = keyRealmAnnotation.keyLifeTime();
		TimeUnit keyLifeTimeUnit = keyRealmAnnotation.keyLifeTimeUnit();
		
		IGGObjectQuery q;
		try {
			q = GGObjectQueryFactory.objectQuery(entityAuthenticatorClass);
			List<Object> expiration = q.find(challengeFieldName);
			GGAPIAuthenticatorChallenge challengeAnnotation = ((Field) expiration.get(expiration.size()-1)).getAnnotation(GGAPIAuthenticatorChallenge.class);
			GGAPIChallengeAuthenticatorInfos authenticatorinfos = new GGAPIChallengeAuthenticatorInfos(
					q.address(challengeFieldName), 
					q.address(keyRealmFieldName),
					q.address(challengeExpirationFieldName),
					keyType, 
					autoCreateKey, 
					keyAlgorithm, 
					keyLifeTime,
					keyLifeTimeUnit,
					keyRealmAnnotation.encryptionMode(),
					keyRealmAnnotation.encryptionPadding(),
					keyRealmAnnotation.signatureAlgorithm(),
					challengeAnnotation.challengeType(),
					challengeAnnotation.challengeLifeTime(), 
					challengeAnnotation.challengeLifeTimeUnit());
			
			GGAPIChallengeEntityAuthenticatorChecker.infos.put(entityAuthenticatorClass, authenticatorinfos);
			return authenticatorinfos;
		} catch (Exception e) {
			throw new GGAPISecurityException(e);
		}
	}
	
	private static String checkChallengeExpirationAnnotationPresentAndFieldHasGoodType(
			Class<? extends Object> entityAuthenticatorClass) throws GGAPISecurityException {
		String fieldAddress;
		try {
			fieldAddress = GGObjectReflectionHelper.getFieldAddressAnnotatedWithAndCheckType(entityAuthenticatorClass, GGAPIAuthenticatorChallengeExpiration.class, Date.class);
			if( fieldAddress == null ) {
				throw new GGAPISecurityException(GGAPIExceptionCode.ENTITY_DEFINITION, "Entity Authenticator "+entityAuthenticatorClass.getSimpleName()+" does not have any field annotated with @GGAPIAuthenticatorChallengeExpiration");
			}
		} catch (GGReflectionException e) {
			throw new GGAPISecurityException(GGAPIExceptionCode.ENTITY_DEFINITION, "Entity Authenticator "+entityAuthenticatorClass.getSimpleName()+" does not have any field annotated with @GGAPIAuthenticatorChallengeExpiration", e);
		}
		return fieldAddress; 
	}

	private static String checkChallengeAnnotationPresentAndFieldHasGoodType(Class<?> entityAuthenticatorClass) throws GGAPIException {
		String fieldAddress;
		try {
			fieldAddress = GGObjectReflectionHelper.getFieldAddressAnnotatedWithAndCheckType(entityAuthenticatorClass, GGAPIAuthenticatorChallenge.class, byte[].class);
			if( fieldAddress == null ) {
				throw new GGAPISecurityException(GGAPIExceptionCode.ENTITY_DEFINITION, "Entity Authenticator "+entityAuthenticatorClass.getSimpleName()+" does not have any field annotated with @GGAPIAuthenticatorChallenge");
			}
		} catch (GGReflectionException e) {
			throw new GGAPISecurityException(GGAPIExceptionCode.ENTITY_DEFINITION, "Entity Authenticator "+entityAuthenticatorClass.getSimpleName()+" does not have any field annotated with @GGAPIAuthenticatorChallenge", e);
		}
		return fieldAddress; 
	}
	
	private static String checkKeyRealmAnnotationPresentAndFieldHasGoodType(Class<?> entityAuthenticatorClass) throws GGAPIException {
		String fieldAddress;
		try {
			fieldAddress = GGObjectReflectionHelper.getFieldAddressAnnotatedWithAndCheckType(entityAuthenticatorClass, GGAPIAuthenticatorKeyRealm.class, IGGAPIKeyRealm.class);
			if( fieldAddress == null ) {
				throw new GGAPISecurityException(GGAPIExceptionCode.ENTITY_DEFINITION, "Entity Authenticator "+entityAuthenticatorClass.getSimpleName()+" does not have any field annotated with @GGAPIAuthenticatorKeyRealm");
			}
		} catch (GGReflectionException e) {
			throw new GGAPISecurityException(GGAPIExceptionCode.ENTITY_DEFINITION, "Entity Authenticator "+entityAuthenticatorClass.getSimpleName()+" does not have any field annotated with @GGAPIAuthenticatorKeyRealm", e);
		}
		return fieldAddress; 
	}
}
