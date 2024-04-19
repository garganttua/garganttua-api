package com.garganttua.api.security.authentication.entity.checker;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.garganttua.api.core.entity.annotations.GGAPIEntity;
import com.garganttua.api.core.entity.annotations.GGAPIEntityUuid;
import com.garganttua.api.core.security.authentication.GGAPIAuthenticationMode;
import com.garganttua.api.core.security.authentication.entity.annotations.GGAPIAuthenticator;
import com.garganttua.api.core.security.authentication.entity.annotations.GGAPIAuthenticatorAccountNonExpired;
import com.garganttua.api.core.security.authentication.entity.annotations.GGAPIAuthenticatorAccountNonLocked;
import com.garganttua.api.core.security.authentication.entity.annotations.GGAPIAuthenticatorAuthorities;
import com.garganttua.api.core.security.authentication.entity.annotations.GGAPIAuthenticatorCredentialsNonExpired;
import com.garganttua.api.core.security.authentication.entity.annotations.GGAPIAuthenticatorEnabled;
import com.garganttua.api.core.security.authentication.entity.annotations.GGAPIAuthenticatorLogin;
import com.garganttua.api.core.security.authentication.entity.annotations.GGAPIAuthenticatorPassword;
import com.garganttua.api.core.security.authentication.entity.checker.GGAPIEntityAuthenticatorChecker;
import com.garganttua.api.core.security.authentication.entity.checker.GGAPIEntityAuthenticatorException;
import com.garganttua.api.core.security.authentication.entity.checker.GGAPIEntityAuthenticatorChecker.GGAPIAuthenticatorInfos;

public class GGAPIEntityAuthenticatorCheckerTest {
	
	@GGAPIEntity(
			domain = ""
	)
	@GGAPIAuthenticator
	static class Entity {
		@GGAPIEntityUuid
		private String uuid;
		@GGAPIAuthenticatorAccountNonExpired
		@GGAPIAuthenticatorAccountNonLocked
		@GGAPIAuthenticatorCredentialsNonExpired
		@GGAPIAuthenticatorEnabled
		private boolean bool;
		@GGAPIAuthenticatorAuthorities
		private List<String> authorities;
		@GGAPIAuthenticatorLogin
		private String login;
		@GGAPIAuthenticatorPassword
		private String password;
	}

	@Test
	public void testEntity() {
		assertDoesNotThrow(() -> {
			GGAPIAuthenticatorInfos infos = GGAPIEntityAuthenticatorChecker.checkEntityAuthenticatorClass(Entity.class, GGAPIAuthenticationMode.loginpassword);
			assertEquals("bool", infos.isAccountNonExpiredFieldAddress().toString());
			assertEquals("bool", infos.isAccountNonLockedFieldAddress().toString());
			assertEquals("bool", infos.isCredentialsNonExpiredFieldAddress().toString());
			assertEquals("bool", infos.isEnabledFieldAddress().toString());
			assertEquals("login", infos.loginFieldAddress().toString());
			assertEquals("password", infos.passwordFieldAddress().toString());
			assertEquals("authorities", infos.authoritiesFieldAddress().toString());
		 });
	}
	
	@Test
	public void testEntityWithNoAnnotationGGAPIAuthenticatorAccountNonExpired() {
		
		@GGAPIEntity(
				domain = ""
		)
		@GGAPIAuthenticator
		class Entity {
			@GGAPIEntityUuid
			private String tuuid;
//			@GGAPIAuthenticatorAccountNonExpired
			@GGAPIAuthenticatorAccountNonLocked
			@GGAPIAuthenticatorCredentialsNonExpired
			@GGAPIAuthenticatorEnabled
			private boolean bool;
			@GGAPIAuthenticatorAuthorities
			private List<String> authorities;
			@GGAPIAuthenticatorLogin
			private String login;
			@GGAPIAuthenticatorPassword
			private String password;
			
		}
		
		GGAPIEntityAuthenticatorException exception = assertThrows(GGAPIEntityAuthenticatorException.class, () -> {
			GGAPIEntityAuthenticatorChecker.checkEntityAuthenticatorClass(Entity.class, GGAPIAuthenticationMode.loginpassword);
		 });
		
		assertEquals("Entity Authenticator "+Entity.class.getSimpleName()+" does not have any field annotated with @GGAPIAuthenticatorAccountNonExpired", exception.getMessage());
		assertEquals(GGAPIEntityAuthenticatorException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}
	
	@Test
	public void testEntityWithAnnotationGGAPIAuthenticatorAccountNonExpiredWrongType() {
		
		@GGAPIEntity(
				domain = ""
		)
		@GGAPIAuthenticator
		class Entity {
			@GGAPIEntityUuid
			private String tuuid;
			@GGAPIAuthenticatorAccountNonExpired
			private String test;
			@GGAPIAuthenticatorAccountNonLocked
			@GGAPIAuthenticatorCredentialsNonExpired
			@GGAPIAuthenticatorEnabled
			private boolean bool;
			@GGAPIAuthenticatorAuthorities
			private List<String> authorities;
			@GGAPIAuthenticatorLogin
			private String login;
			@GGAPIAuthenticatorPassword
			private String password;
			
		}
		
		GGAPIEntityAuthenticatorException exception = assertThrows(GGAPIEntityAuthenticatorException.class, () -> {
			GGAPIEntityAuthenticatorChecker.checkEntityAuthenticatorClass(Entity.class, GGAPIAuthenticationMode.loginpassword);
		 });
		
		assertEquals("Entity Authenticator "+Entity.class.getSimpleName()+" has field test with wrong type java.lang.String, should be boolean", exception.getMessage());
		assertEquals(GGAPIEntityAuthenticatorException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}
	
	@Test
	public void testEntityWithNoAnnotationGGAPIAuthenticatorAccountNonLocked() {
		
		@GGAPIEntity(
				domain = ""
		)
		@GGAPIAuthenticator
		class Entity {
			@GGAPIEntityUuid
			private String tuuid;
			@GGAPIAuthenticatorAccountNonExpired
//			@GGAPIAuthenticatorAccountNonLocked
			@GGAPIAuthenticatorCredentialsNonExpired
			@GGAPIAuthenticatorEnabled
			private boolean bool;
			@GGAPIAuthenticatorAuthorities
			private List<String> authorities;
			@GGAPIAuthenticatorLogin
			private String login;
			@GGAPIAuthenticatorPassword
			private String password;
			
		}
		
		GGAPIEntityAuthenticatorException exception = assertThrows(GGAPIEntityAuthenticatorException.class, () -> {
			GGAPIEntityAuthenticatorChecker.checkEntityAuthenticatorClass(Entity.class, GGAPIAuthenticationMode.loginpassword);
		 });
		
		assertEquals("Entity Authenticator "+Entity.class.getSimpleName()+" does not have any field annotated with @GGAPIAuthenticatorAccountNonLocked", exception.getMessage());
		assertEquals(GGAPIEntityAuthenticatorException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}
	
	@Test
	public void testEntityWithAnnotationGGAPIAuthenticatorAccountNonLockedWrongType() {
		
		@GGAPIEntity(
				domain = ""
		)
		@GGAPIAuthenticator
		class Entity {
			@GGAPIEntityUuid
			private String tuuid;
			@GGAPIAuthenticatorAccountNonLocked
			private String test;
			@GGAPIAuthenticatorAccountNonExpired
			@GGAPIAuthenticatorCredentialsNonExpired
			@GGAPIAuthenticatorEnabled
			private boolean bool;
			@GGAPIAuthenticatorAuthorities
			private List<String> authorities;
			@GGAPIAuthenticatorLogin
			private String login;
			@GGAPIAuthenticatorPassword
			private String password;
			
		}
		
		GGAPIEntityAuthenticatorException exception = assertThrows(GGAPIEntityAuthenticatorException.class, () -> {
			GGAPIEntityAuthenticatorChecker.checkEntityAuthenticatorClass(Entity.class, GGAPIAuthenticationMode.loginpassword);
		 });
		
		assertEquals("Entity Authenticator "+Entity.class.getSimpleName()+" has field test with wrong type java.lang.String, should be boolean", exception.getMessage());
		assertEquals(GGAPIEntityAuthenticatorException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}
	
	@Test
	public void testEntityWithNoAnnotationGGAPIAuthenticatorCredentialsNonExpired() {
		
		@GGAPIEntity(
				domain = ""
		)
		@GGAPIAuthenticator
		class Entity {
			@GGAPIEntityUuid
			private String tuuid;
			@GGAPIAuthenticatorAccountNonExpired
			@GGAPIAuthenticatorAccountNonLocked
//			@GGAPIAuthenticatorCredentialsNonExpired
			@GGAPIAuthenticatorEnabled
			private boolean bool;
			@GGAPIAuthenticatorAuthorities
			private List<String> authorities;
			@GGAPIAuthenticatorLogin
			private String login;
			@GGAPIAuthenticatorPassword
			private String password;
			
		}
		
		GGAPIEntityAuthenticatorException exception = assertThrows(GGAPIEntityAuthenticatorException.class, () -> {
			GGAPIEntityAuthenticatorChecker.checkEntityAuthenticatorClass(Entity.class, GGAPIAuthenticationMode.loginpassword);
		 });
		
		assertEquals("Entity Authenticator "+Entity.class.getSimpleName()+" does not have any field annotated with @GGAPIAuthenticatorCredentialsNonExpired", exception.getMessage());
		assertEquals(GGAPIEntityAuthenticatorException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}
	
	@Test
	public void testEntityWithAnnotationGGAPIAuthenticatorCredentialsNonExpiredWrongType() {
		
		@GGAPIEntity(
				domain = ""
		)
		@GGAPIAuthenticator
		class Entity {
			@GGAPIEntityUuid
			private String tuuid;
			@GGAPIAuthenticatorCredentialsNonExpired
			private String test;
			@GGAPIAuthenticatorAccountNonLocked
			@GGAPIAuthenticatorAccountNonExpired
			@GGAPIAuthenticatorEnabled
			private boolean bool;
			@GGAPIAuthenticatorAuthorities
			private List<String> authorities;
			@GGAPIAuthenticatorLogin
			private String login;
			@GGAPIAuthenticatorPassword
			private String password;
			
		}
		
		GGAPIEntityAuthenticatorException exception = assertThrows(GGAPIEntityAuthenticatorException.class, () -> {
			GGAPIEntityAuthenticatorChecker.checkEntityAuthenticatorClass(Entity.class, GGAPIAuthenticationMode.loginpassword);
		 });
		
		assertEquals("Entity Authenticator "+Entity.class.getSimpleName()+" has field test with wrong type java.lang.String, should be boolean", exception.getMessage());
		assertEquals(GGAPIEntityAuthenticatorException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}
	
}
