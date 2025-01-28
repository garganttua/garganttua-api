package com.garganttua.api.core.security.entity.checker;

import org.junit.jupiter.api.BeforeAll;

import com.garganttua.api.core.ReflectionsAnnotationScanner;
import com.garganttua.reflection.utils.GGObjectReflectionHelper;

public class GGAPIEntityAuthorizationCheckerTest {
	@BeforeAll
	public static void setupAnnotationScanner() {
		GGObjectReflectionHelper.annotationScanner = new ReflectionsAnnotationScanner();
	}
//	@Test
//	public void testJWTAuthorization() throws GGAPIException {
//		GGAPIAuthorizationInfos infos = GGAPIEntityAuthorizationChecker.checkEntityAuthorizationClass(GGAPIJWTAuthorization.class);
//		assertNotNull(infos.expirationFieldAddress());	
//	}
}
