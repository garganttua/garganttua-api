package com.garganttua.api.core.security.authentication.challenge;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.garganttua.api.core.security.exceptions.SecurityException;
import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.CoreExceptionCode;
import com.garganttua.api.spec.security.annotations.AuthenticatorKeyRealm;
import com.garganttua.api.spec.security.key.KeyAlgorithm;
import com.garganttua.api.spec.security.key.IKeyRealm;
import com.garganttua.reflection.GGReflectionException;
import com.garganttua.reflection.query.GGObjectQueryFactory;
import com.garganttua.reflection.query.IGGObjectQuery;
import com.garganttua.reflection.utils.GGObjectReflectionHelper;

public class ChallengeEntityAuthenticatorChecker {

	private static Map<Class<?>, ChallengeAuthenticatorInfos> infos = new HashMap<Class<?>, ChallengeAuthenticatorInfos>();
	
	public static ChallengeAuthenticatorInfos checkEntityAuthenticatorClass(Class<? extends Object> entityAuthenticatorClass) throws SecurityException {
		if( ChallengeEntityAuthenticatorChecker.infos.containsKey(entityAuthenticatorClass) ) {
			return ChallengeEntityAuthenticatorChecker.infos.get(entityAuthenticatorClass);  
		}

		String keyRealmFieldName = null; 
		String challengeFieldName = null;
		String challengeExpirationFieldName = null;
		
		try {
			keyRealmFieldName= ChallengeEntityAuthenticatorChecker.checkKeyRealmAnnotationPresentAndFieldHasGoodType(entityAuthenticatorClass);
		} catch (CoreException e) {
			throw new SecurityException(CoreExceptionCode.ENTITY_DEFINITION, "Entity Authenticator " + entityAuthenticatorClass.getSimpleName() + " does not have a field annotated with @AuthenticatorKeyRealm");
		}
		
		try {
			challengeFieldName = ChallengeEntityAuthenticatorChecker.checkChallengeAnnotationPresentAndFieldHasGoodType(entityAuthenticatorClass);
		} catch (CoreException e) {
			throw new SecurityException(CoreExceptionCode.ENTITY_DEFINITION, "Entity Authenticator " + entityAuthenticatorClass.getSimpleName() + " does not have a field annotated with @AuthenticatorChallenge");
		}
		
		try {
			challengeExpirationFieldName = ChallengeEntityAuthenticatorChecker.checkChallengeExpirationAnnotationPresentAndFieldHasGoodType(entityAuthenticatorClass);
		} catch (CoreException e) {
			throw new SecurityException(CoreExceptionCode.ENTITY_DEFINITION, "Entity Authenticator " + entityAuthenticatorClass.getSimpleName() + " does not have a field annotated with @AuthenticatorChallengeExpiration");
		}

		AuthenticatorKeyRealm keyRealmAnnotation = GGObjectReflectionHelper.getField(entityAuthenticatorClass, keyRealmFieldName).getAnnotation(AuthenticatorKeyRealm.class);
		
		Class<?> keyType = keyRealmAnnotation.key();
		boolean autoCreateKey = keyRealmAnnotation.autoCreateKey();
		KeyAlgorithm keyAlgorithm = keyRealmAnnotation.keyAlgorithm();
		int keyLifeTime = keyRealmAnnotation.keyLifeTime();
		TimeUnit keyLifeTimeUnit = keyRealmAnnotation.keyLifeTimeUnit();
		
		IGGObjectQuery q;
		try {
			q = GGObjectQueryFactory.objectQuery(entityAuthenticatorClass);
			List<Object> expiration = q.find(challengeFieldName);
			AuthenticatorChallenge challengeAnnotation = ((Field) expiration.get(expiration.size()-1)).getAnnotation(AuthenticatorChallenge.class);
			ChallengeAuthenticatorInfos authenticatorinfos = new ChallengeAuthenticatorInfos(
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
			
			ChallengeEntityAuthenticatorChecker.infos.put(entityAuthenticatorClass, authenticatorinfos);
			return authenticatorinfos;
		} catch (Exception e) {
			throw new SecurityException(e);
		}
	}
	
	private static String checkChallengeExpirationAnnotationPresentAndFieldHasGoodType(
			Class<? extends Object> entityAuthenticatorClass) throws SecurityException {
		String fieldAddress;
		try {
			fieldAddress = GGObjectReflectionHelper.getFieldAddressAnnotatedWithAndCheckType(entityAuthenticatorClass, AuthenticatorChallengeExpiration.class, Date.class);
			if( fieldAddress == null ) {
				throw new SecurityException(CoreExceptionCode.ENTITY_DEFINITION, "Entity Authenticator "+entityAuthenticatorClass.getSimpleName()+" does not have any field annotated with @AuthenticatorChallengeExpiration");
			}
		} catch (GGReflectionException e) {
			throw new SecurityException(CoreExceptionCode.ENTITY_DEFINITION, "Entity Authenticator "+entityAuthenticatorClass.getSimpleName()+" does not have any field annotated with @AuthenticatorChallengeExpiration", e);
		}
		return fieldAddress; 
	}

	private static String checkChallengeAnnotationPresentAndFieldHasGoodType(Class<?> entityAuthenticatorClass) throws CoreException {
		String fieldAddress;
		try {
			fieldAddress = GGObjectReflectionHelper.getFieldAddressAnnotatedWithAndCheckType(entityAuthenticatorClass, AuthenticatorChallenge.class, byte[].class);
			if( fieldAddress == null ) {
				throw new SecurityException(CoreExceptionCode.ENTITY_DEFINITION, "Entity Authenticator "+entityAuthenticatorClass.getSimpleName()+" does not have any field annotated with @AuthenticatorChallenge");
			}
		} catch (GGReflectionException e) {
			throw new SecurityException(CoreExceptionCode.ENTITY_DEFINITION, "Entity Authenticator "+entityAuthenticatorClass.getSimpleName()+" does not have any field annotated with @AuthenticatorChallenge", e);
		}
		return fieldAddress; 
	}
	
	private static String checkKeyRealmAnnotationPresentAndFieldHasGoodType(Class<?> entityAuthenticatorClass) throws CoreException {
		String fieldAddress;
		try {
			fieldAddress = GGObjectReflectionHelper.getFieldAddressAnnotatedWithAndCheckType(entityAuthenticatorClass, AuthenticatorKeyRealm.class, IKeyRealm.class);
			if( fieldAddress == null ) {
				throw new SecurityException(CoreExceptionCode.ENTITY_DEFINITION, "Entity Authenticator "+entityAuthenticatorClass.getSimpleName()+" does not have any field annotated with @AuthenticatorKeyRealm");
			}
		} catch (GGReflectionException e) {
			throw new SecurityException(CoreExceptionCode.ENTITY_DEFINITION, "Entity Authenticator "+entityAuthenticatorClass.getSimpleName()+" does not have any field annotated with @AuthenticatorKeyRealm", e);
		}
		return fieldAddress; 
	}
}
