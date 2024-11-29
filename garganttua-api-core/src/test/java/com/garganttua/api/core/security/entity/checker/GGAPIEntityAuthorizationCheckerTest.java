package com.garganttua.api.core.security.entity.checker;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.garganttua.api.core.security.authorization.jwt.GGAPIJWTAuthorization;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.security.authorization.GGAPIAuthorizationInfos;

public class GGAPIEntityAuthorizationCheckerTest {
	
	@Test
	public void testJWTAuthorization() throws GGAPIException {
		GGAPIAuthorizationInfos infos = GGAPIEntityAuthorizationChecker.checkEntityAuthorizationClass(GGAPIJWTAuthorization.class);
		assertNotNull(infos.expirationFieldAddress());	
	}
}
