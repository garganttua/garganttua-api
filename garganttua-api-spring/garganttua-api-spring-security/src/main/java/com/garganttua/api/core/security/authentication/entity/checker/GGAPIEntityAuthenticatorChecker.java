package com.garganttua.api.core.security.authentication.entity.checker;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;

import com.garganttua.api.core.objects.GGAPIObjectAddress;
import com.garganttua.api.core.objects.query.GGAPIObjectQueryException;
import com.garganttua.api.core.objects.query.GGAPIObjectQueryFactory;
import com.garganttua.api.core.objects.query.IGGAPIObjectQuery;
import com.garganttua.api.core.security.authentication.GGAPIAuthenticationMode;
import com.garganttua.api.core.security.authentication.entity.annotations.GGAPIAuthenticator;
import com.garganttua.api.core.security.authentication.entity.annotations.GGAPIAuthenticatorAccountNonExpired;
import com.garganttua.api.core.security.authentication.entity.annotations.GGAPIAuthenticatorAccountNonLocked;
import com.garganttua.api.core.security.authentication.entity.annotations.GGAPIAuthenticatorAuthorities;
import com.garganttua.api.core.security.authentication.entity.annotations.GGAPIAuthenticatorCredentialsNonExpired;
import com.garganttua.api.core.security.authentication.entity.annotations.GGAPIAuthenticatorEnabled;
import com.garganttua.api.core.security.authentication.entity.annotations.GGAPIAuthenticatorLogin;
import com.garganttua.api.core.security.authentication.entity.annotations.GGAPIAuthenticatorPassword;
import com.garganttua.api.spec.GGAPICoreExceptionCode;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GGAPIEntityAuthenticatorChecker {
	
	public record GGAPIAuthenticatorInfos (
		GGAPIObjectAddress authoritiesFieldAddress,
		GGAPIObjectAddress isAccountNonExpiredFieldAddress,
		GGAPIObjectAddress isAccountNonLockedFieldAddress,
		GGAPIObjectAddress isCredentialsNonExpiredFieldAddress,
		GGAPIObjectAddress isEnabledFieldAddress,
		GGAPIObjectAddress loginFieldAddress,
		GGAPIObjectAddress passwordFieldAddress) {
		
		@Override
	    public int hashCode() {
	        return Objects.hash(
	                authoritiesFieldAddress,
	                isAccountNonExpiredFieldAddress,
	                isAccountNonLockedFieldAddress,
	                isCredentialsNonExpiredFieldAddress,
	                isEnabledFieldAddress,
	                loginFieldAddress,
	                passwordFieldAddress);
	    }

	    @Override
	    public String toString() {
	        return "GGAPIAuthenticatorInfos{" +
	                "authoritiesFieldAddress=" + authoritiesFieldAddress +
	                ", isAccountNonExpiredFieldAddress=" + isAccountNonExpiredFieldAddress +
	                ", isAccountNonLockedFieldAddress=" + isAccountNonLockedFieldAddress +
	                ", isCredentialsNonExpiredFieldAddress=" + isCredentialsNonExpiredFieldAddress +
	                ", isEnabledField=" + isEnabledFieldAddress +
	                ", loginFieldAddress=" + loginFieldAddress +
	                ", passwordFieldAddress=" + passwordFieldAddress +
	                '}';
	    }

	    @Override
	    public boolean equals(Object o) {
	        if (this == o) return true;
	        if (o == null || getClass() != o.getClass()) return false;
	        GGAPIAuthenticatorInfos that = (GGAPIAuthenticatorInfos) o;
	        return Objects.equals(authoritiesFieldAddress, that.authoritiesFieldAddress) &&
	                Objects.equals(isAccountNonExpiredFieldAddress, that.isAccountNonExpiredFieldAddress) &&
	                Objects.equals(isAccountNonLockedFieldAddress, that.isAccountNonLockedFieldAddress) &&
	                Objects.equals(isCredentialsNonExpiredFieldAddress, that.isCredentialsNonExpiredFieldAddress) &&
	                Objects.equals(isEnabledFieldAddress, that.isEnabledFieldAddress) &&
	                Objects.equals(loginFieldAddress, that.loginFieldAddress) &&
	                Objects.equals(passwordFieldAddress, that.passwordFieldAddress);
	    }
	}

	public static GGAPIAuthenticatorInfos checkEntityAuthenticatorClass(Class<?> entityAuthenticatorClass, GGAPIAuthenticationMode mode) throws GGAPIEntityAuthenticatorException {
		if (log.isDebugEnabled()) {
			log.debug("Checking entity authenticator infos from class " + entityAuthenticatorClass.getName());
		}
		
		GGAPIAuthenticator annotation = entityAuthenticatorClass.getDeclaredAnnotation(GGAPIAuthenticator.class);
		
		if( annotation == null ) {
			throw new GGAPIEntityAuthenticatorException(GGAPICoreExceptionCode.ENTITY_DEFINITION,
					"Entity Authenticator " + entityAuthenticatorClass.getSimpleName() + " is not annotated with @GGAPIAuthenticator");
		}
		
		String accountNonExpiredFieldName = GGAPIEntityAuthenticatorChecker.checkAccountNonExpiredAnnotationPresentAndFieldHasGoodType(entityAuthenticatorClass);
		String accountNonLockedFieldName = GGAPIEntityAuthenticatorChecker.checkAccountNonLockedAnnotationPresentAndFieldHasGoodType(entityAuthenticatorClass);
		String credentialsNonExpiredFieldName = GGAPIEntityAuthenticatorChecker.checkCredentialsNonLockedAnnotationPresentAndFieldHasGoodType(entityAuthenticatorClass);
		String enabledFieldName = GGAPIEntityAuthenticatorChecker.checkEnabledAnnotationPresentAndFieldHasGoodType(entityAuthenticatorClass);
		String autoritiesFieldName = GGAPIEntityAuthenticatorChecker.checkAuthoritiesAnnotationPresentAndFieldHasGoodType(entityAuthenticatorClass);
		
		String loginFieldName = null; 
		String passwordFieldName = null;
		
		if( mode == GGAPIAuthenticationMode.loginpassword ) {
			loginFieldName= GGAPIEntityAuthenticatorChecker.checkLoginAnnotationPresentAndFieldHasGoodType(entityAuthenticatorClass);
			passwordFieldName = GGAPIEntityAuthenticatorChecker.checkPasswordAnnotationPresentAndFieldHasGoodType(entityAuthenticatorClass);
		}
		
		IGGAPIObjectQuery q;
		try {
			q = GGAPIObjectQueryFactory.objectQuery(entityAuthenticatorClass);
			return new GGAPIAuthenticatorInfos(
					q.address(autoritiesFieldName), 
					q.address(accountNonExpiredFieldName), 
					q.address(accountNonLockedFieldName), 
					q.address(credentialsNonExpiredFieldName), 
					q.address(enabledFieldName), 
					loginFieldName==null?null:q.address(loginFieldName), 
					passwordFieldName==null?null:q.address(passwordFieldName));
		} catch (GGAPIObjectQueryException e) {
			throw new GGAPIEntityAuthenticatorException(e);
		}
	}
	
	private static String checkAuthoritiesAnnotationPresentAndFieldHasGoodType(Class<?> entityAuthenticatorClass) throws GGAPIEntityAuthenticatorException {
		String fieldAddress = GGAPIEntityAuthenticatorChecker.getFieldAddressAnnotatedWithAndCheckType(entityAuthenticatorClass, GGAPIAuthenticatorAuthorities.class, List.class);
		if( fieldAddress == null ) {
			throw new GGAPIEntityAuthenticatorException(GGAPICoreExceptionCode.ENTITY_DEFINITION, "Entity Authenticator "+entityAuthenticatorClass.getSimpleName()+" does not have any field annotated with @GGAPIAuthenticatorAutorities");
		}
		return fieldAddress; 
	}
	
	private static String checkPasswordAnnotationPresentAndFieldHasGoodType(Class<?> entityAuthenticatorClass) throws GGAPIEntityAuthenticatorException {
		String fieldAddress = GGAPIEntityAuthenticatorChecker.getFieldAddressAnnotatedWithAndCheckType(entityAuthenticatorClass, GGAPIAuthenticatorPassword.class, String.class);
		if( fieldAddress == null ) {
			throw new GGAPIEntityAuthenticatorException(GGAPICoreExceptionCode.ENTITY_DEFINITION, "Entity Authenticator "+entityAuthenticatorClass.getSimpleName()+" does not have any field annotated with @GGAPIAuthenticatorPassword");
		}
		return fieldAddress; 
	}
	
	private static String checkLoginAnnotationPresentAndFieldHasGoodType(Class<?> entityAuthenticatorClass) throws GGAPIEntityAuthenticatorException {
		String fieldAddress = GGAPIEntityAuthenticatorChecker.getFieldAddressAnnotatedWithAndCheckType(entityAuthenticatorClass, GGAPIAuthenticatorLogin.class, String.class);
		if( fieldAddress == null ) {
			throw new GGAPIEntityAuthenticatorException(GGAPICoreExceptionCode.ENTITY_DEFINITION, "Entity Authenticator "+entityAuthenticatorClass.getSimpleName()+" does not have any field annotated with @GGAPIAuthenticatorLogin");
		}
		return fieldAddress; 
	}
	
	private static String checkEnabledAnnotationPresentAndFieldHasGoodType(Class<?> entityAuthenticatorClass) throws GGAPIEntityAuthenticatorException {
		String fieldAddress = GGAPIEntityAuthenticatorChecker.getFieldAddressAnnotatedWithAndCheckType(entityAuthenticatorClass, GGAPIAuthenticatorEnabled.class, boolean.class);
		if( fieldAddress == null ) {
			throw new GGAPIEntityAuthenticatorException(GGAPICoreExceptionCode.ENTITY_DEFINITION, "Entity Authenticator "+entityAuthenticatorClass.getSimpleName()+" does not have any field annotated with @GGAPIAuthenticatorEnabled");
		}
		return fieldAddress; 
	}
	
	private static String checkCredentialsNonLockedAnnotationPresentAndFieldHasGoodType(Class<?> entityAuthenticatorClass) throws GGAPIEntityAuthenticatorException {
		String fieldAddress = GGAPIEntityAuthenticatorChecker.getFieldAddressAnnotatedWithAndCheckType(entityAuthenticatorClass, GGAPIAuthenticatorCredentialsNonExpired.class, boolean.class);
		if( fieldAddress == null ) {
			throw new GGAPIEntityAuthenticatorException(GGAPICoreExceptionCode.ENTITY_DEFINITION, "Entity Authenticator "+entityAuthenticatorClass.getSimpleName()+" does not have any field annotated with @GGAPIAuthenticatorCredentialsNonExpired");
		}
		return fieldAddress; 
	}
	
	private static String checkAccountNonLockedAnnotationPresentAndFieldHasGoodType(Class<?> entityAuthenticatorClass) throws GGAPIEntityAuthenticatorException {
		String fieldAddress = GGAPIEntityAuthenticatorChecker.getFieldAddressAnnotatedWithAndCheckType(entityAuthenticatorClass, GGAPIAuthenticatorAccountNonLocked.class, boolean.class);
		if( fieldAddress == null ) {
			throw new GGAPIEntityAuthenticatorException(GGAPICoreExceptionCode.ENTITY_DEFINITION, "Entity Authenticator "+entityAuthenticatorClass.getSimpleName()+" does not have any field annotated with @GGAPIAuthenticatorAccountNonLocked");
		}
		return fieldAddress; 
	}
	
	private static String checkAccountNonExpiredAnnotationPresentAndFieldHasGoodType(Class<?> entityAuthenticatorClass) throws GGAPIEntityAuthenticatorException {
		String fieldAddress = GGAPIEntityAuthenticatorChecker.getFieldAddressAnnotatedWithAndCheckType(entityAuthenticatorClass, GGAPIAuthenticatorAccountNonExpired.class, boolean.class);
		if( fieldAddress == null ) {
			throw new GGAPIEntityAuthenticatorException(GGAPICoreExceptionCode.ENTITY_DEFINITION, "Entity Authenticator "+entityAuthenticatorClass.getSimpleName()+" does not have any field annotated with @GGAPIAuthenticatorAccountNonExpired");
		}
		return fieldAddress; 
	}

	private static String getFieldAddressAnnotatedWithAndCheckType(Class<?> entityAuthenticatorClass, Class<? extends Annotation> annotationClass, Class<?> fieldClass) throws GGAPIEntityAuthenticatorException {
		String fieldAddress = null;
		for( Field field: entityAuthenticatorClass.getDeclaredFields() ) {
			if( field.isAnnotationPresent(annotationClass) ) {
				if( fieldAddress != null && !fieldAddress.isEmpty() ) {
					throw new GGAPIEntityAuthenticatorException(GGAPICoreExceptionCode.ENTITY_DEFINITION, "Entity Authenticator "+entityAuthenticatorClass.getSimpleName()+" has more than one field annotated with "+annotationClass);
				}
				if( field.getType().equals(fieldClass) ) {
					fieldAddress = field.getName();
				} else {
					throw new GGAPIEntityAuthenticatorException(GGAPICoreExceptionCode.ENTITY_DEFINITION, "Entity Authenticator "+entityAuthenticatorClass.getSimpleName()+" has field "+field.getName()+" with wrong type "+field.getType().getName()+", should be "+fieldClass);
				}
			}
		}

		if( entityAuthenticatorClass.getSuperclass() != null && fieldAddress == null ) {
			return GGAPIEntityAuthenticatorChecker.getFieldAddressAnnotatedWithAndCheckType(entityAuthenticatorClass.getSuperclass(), annotationClass, fieldClass);
		} else {
			return fieldAddress;
		}
	}
}
