package com.garganttua.api.commons.security;

import com.garganttua.api.commons.operation.Access;

public interface IUseCaseSecurity {

	boolean isDisabled();

	boolean hasAuthority();

	/**
	 * The custom authority name configured via {@code authority(String)}, or
	 * {@code null} when only the boolean toggle was set. When non-null the
	 * pipeline checks the caller carries an authority equal to this string;
	 * otherwise it falls back to the auto-generated default name
	 * ({@code <domain>:<operation>}).
	 */
	String customAuthority();

	Access getAccess();

}
