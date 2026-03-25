package com.garganttua.api.core.security.authentication;

import java.lang.reflect.Method;
import java.util.List;

import com.garganttua.api.core.caller.Caller;
import com.garganttua.api.core.context.security.AuthenticationContext;
import com.garganttua.api.core.definition.AuthenticationDefinition;
import com.garganttua.api.core.filter.Filter;
import com.garganttua.api.core.service.OperationResponse;
import com.garganttua.api.spec.caller.ICaller;
import com.garganttua.api.spec.context.IDomainContext;
import com.garganttua.api.spec.security.authentication.IAuthenticationRequest;
import com.garganttua.api.spec.security.context.IAuthenticationContext;
import com.garganttua.api.spec.service.IOperationResponse;
import com.garganttua.api.spec.service.OperationResponseCode;
import com.garganttua.core.reflection.ObjectAddress;
import com.garganttua.core.reflection.binders.IContextualMethodBinder;
import com.garganttua.core.reflection.binders.IMethodBinder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuthenticationRequest implements IAuthenticationRequest {

	private final List<IAuthenticationContext> authenticationContexts;
	private final String id;
	private final byte[] credentials;
	private String tenantId;

	AuthenticationRequest(List<IAuthenticationContext> authenticationContexts,
			String id, byte[] credentials, String tenantId) {
		this.authenticationContexts = authenticationContexts;
		this.id = id;
		this.credentials = credentials;
		this.tenantId = tenantId;
	}

	@Override
	public String getId() {
		return this.id;
	}

	@Override
	public byte[] getCredentials() {
		return this.credentials;
	}

	@Override
	public String getTenantId() {
		return this.tenantId;
	}

	@Override
	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public List<IAuthenticationContext> getAuthenticationContexts() {
		return this.authenticationContexts;
	}

	@Override
	public IOperationResponse execute() {
		for (IAuthenticationContext context : this.authenticationContexts) {
			if (!(context instanceof AuthenticationContext authContext)) {
				continue;
			}
			IOperationResponse response = tryAuthenticate(authContext);
			if (response != null && response.getResponseCode() != OperationResponseCode.UNAUTHORIZED) {
				return response;
			}
		}
		return OperationResponse.unauthorized("Authentication failed");
	}

	private IOperationResponse tryAuthenticate(AuthenticationContext authContext) {
		AuthenticationDefinition definition = (AuthenticationDefinition) authContext.getAuthenticationDefinition();
		IDomainContext<?> domainContext = authContext.getDomainContext();

		IMethodBinder<?> authenticateMethod = definition.authenticateMethodBinder();
		if (authenticateMethod != null) {
			if (authenticateMethod.isContextual()) {
				IContextualMethodBinder<?, ?> contextualBinder = (IContextualMethodBinder<?, ?>) authenticateMethod;
				// TODO: contextual binder execution
			} else {
				authenticateMethod.execute();
			}
			return null;
		}

		// Fallback: method-name-based authentication
		if (domainContext == null) {
			return null;
		}

		Object principal = findPrincipal(domainContext);
		if (principal == null) {
			return null;
		}

		String methodName = definition.authenticateMethodName();
		if (methodName == null) {
			return null;
		}

		return invokeAuthenticateMethod(methodName, principal);
	}

	private Object findPrincipal(IDomainContext<?> domainContext) {
		ObjectAddress idFieldAddress = domainContext.getEntityDefinition().id();
		if (idFieldAddress == null) {
			log.atWarn().log("No id field configured for domain={}", domainContext.getDomainName());
			return null;
		}

		ICaller caller = this.tenantId != null
				? Caller.createTenantCaller(this.tenantId)
				: Caller.createSuperCaller();

		Filter idFilter = Filter.eq(idFieldAddress.toString(), this.id);
		IOperationResponse response = domainContext.readAll(idFilter, null, null, caller);

		if (response == null || response.getResponseCode() != OperationResponseCode.OK) {
			return null;
		}

		Object result = response.getResponse();
		if (result instanceof List<?> list && !list.isEmpty()) {
			return list.get(0);
		}
		return null;
	}

	private IOperationResponse invokeAuthenticateMethod(String methodName, Object principal) {
		try {
			Method method = findAuthenticateMethod(principal.getClass(), methodName);
			if (method == null) {
				log.atWarn().log("Authenticate method '{}' not found on {}", methodName, principal.getClass().getName());
				return null;
			}

			Object result = invokeMethod(method, principal);

			if (result instanceof IOperationResponse operationResponse) {
				return operationResponse;
			}
			if (result instanceof Boolean success && success) {
				return OperationResponse.ok(principal);
			}
			return null;
		} catch (Exception e) {
			log.atDebug().log("Authentication method invocation failed", e);
			return null;
		}
	}

	private Method findAuthenticateMethod(Class<?> clazz, String methodName) {
		for (Method m : clazz.getMethods()) {
			if (!m.getName().equals(methodName)) {
				continue;
			}
			Class<?>[] params = m.getParameterTypes();
			// authenticate(String id, byte[] credentials)
			if (params.length == 2 && params[0] == String.class && params[1] == byte[].class) {
				return m;
			}
			// authenticate(byte[] credentials)
			if (params.length == 1 && params[0] == byte[].class) {
				return m;
			}
			// authenticate() — no args
			if (params.length == 0) {
				return m;
			}
		}
		return null;
	}

	private Object invokeMethod(Method method, Object principal) throws Exception {
		Class<?>[] params = method.getParameterTypes();
		if (params.length == 2) {
			return method.invoke(principal, this.id, this.credentials);
		} else if (params.length == 1) {
			return method.invoke(principal, (Object) this.credentials);
		} else {
			return method.invoke(principal);
		}
	}

}
