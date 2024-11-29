package com.garganttua.api.core.security.authentication.challenge;

import java.util.Arrays;

import com.garganttua.api.core.security.authentication.AbstractGGAPIAuthentication;
import com.garganttua.api.core.security.entity.tools.GGAPIEntityAuthenticatorHelper;
import com.garganttua.api.core.security.exceptions.GGAPISecurityException;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.security.annotations.GGAPIAuthentication;
import com.garganttua.api.spec.security.key.IGGAPIKey;
import com.garganttua.api.spec.security.key.IGGAPIKeyRealm;

import lombok.NoArgsConstructor;

@GGAPIAuthentication (
	findPrincipal = true
)
@NoArgsConstructor
public class GGAPIChallengeAuthentication extends AbstractGGAPIAuthentication {

	@Override
	protected void doAuthentication() throws GGAPIException {
		if( !GGAPIEntityAuthenticatorHelper.isAuthenticator(this.principal) ) {
			throw new GGAPISecurityException(GGAPIExceptionCode.UNKNOWN_ERROR, "Authenticator as principal is mandatory for Challenge authentication, verify that findPrincipal is set to true");
		}
		IGGAPIKeyRealm realm = null;
		byte[] challenge = null;

		challenge = GGAPIChallengeEntityAuthenticatorHelper.getChallenge(this.principal);
		realm = GGAPIChallengeEntityAuthenticatorHelper.getKeyRealm(this.principal);

		IGGAPIKey publicKey = realm.getKeyForUnciphering();

		byte[] decodedProvidedChallenge;
			decodedProvidedChallenge = publicKey.uncipher((byte[]) this.credential);
		byte[] reencodedProvidedChallenge = publicKey.cipher(decodedProvidedChallenge);

		if (Arrays.equals(challenge, reencodedProvidedChallenge)) {
			this.authenticated = true;
		}
	}

	@Override
	protected Object findPrincipal() {
		// TODO Auto-generated method stub
		return null;
	}

}
