package com.garganttua.api.commons.context.dsl.security;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.context.dsl.IDomainBuilder;
import com.garganttua.api.commons.context.dsl.IDomainKeyBuilder;
import com.garganttua.api.commons.operation.Access;
import com.garganttua.api.commons.security.IDomainSecurityContext;
import com.garganttua.core.dsl.IAutomaticLinkedBuilder;

public interface IDomainSecurityBuilder<E>
		extends IAutomaticLinkedBuilder<IDomainSecurityBuilder<E>, IDomainBuilder<E>, IDomainSecurityContext> {

	IAuthorizationBuilder<E> authorization();

	IDomainSecurityBuilder<E> disable(boolean b);

	IAuthenticatorBuilder<E> authenticator();

	/**
	 * Marks this domain as a key domain — its entity holds cryptographic key
	 * material and the framework will use it as the storage backend when an
	 * authenticator's authorization declares {@code .key(IDomainBuilder)} for
	 * auto-create / lookup. {@code .up()} returns this security builder.
	 */
	IDomainKeyBuilder<E> key() throws ApiException;

	// Per-use-case security is configured via IUseCaseBuilder.security() — see
	// IUseCaseSecurityBuilder. There is intentionally no useCase(...) method
	// here: declaring it twice would let two different paths set the same
	// state with no merge rule.

	// --- CRUD access level ---

	IDomainSecurityBuilder<E> creationAccess(Access access);

	IDomainSecurityBuilder<E> readAllAccess(Access access);

	IDomainSecurityBuilder<E> readOneAccess(Access access);

	IDomainSecurityBuilder<E> updateAccess(Access access);

	IDomainSecurityBuilder<E> deleteOneAccess(Access access);

	IDomainSecurityBuilder<E> deleteAllAccess(Access access);

	// --- CRUD authority (boolean: auto-generated authority name) ---

	IDomainSecurityBuilder<E> creationAuthority(boolean authority);

	IDomainSecurityBuilder<E> readAllAuthority(boolean authority);

	IDomainSecurityBuilder<E> readOneAuthority(boolean authority);

	IDomainSecurityBuilder<E> updateAuthority(boolean authority);

	IDomainSecurityBuilder<E> deleteOneAuthority(boolean authority);

	IDomainSecurityBuilder<E> deleteAllAuthority(boolean authority);

	// --- CRUD authority (String: custom authority name) ---

	IDomainSecurityBuilder<E> creationAuthority(String authority);

	IDomainSecurityBuilder<E> readAllAuthority(String authority);

	IDomainSecurityBuilder<E> readOneAuthority(String authority);

	IDomainSecurityBuilder<E> updateAuthority(String authority);

	IDomainSecurityBuilder<E> deleteOneAuthority(String authority);

	IDomainSecurityBuilder<E> deleteAllAuthority(String authority);

}
