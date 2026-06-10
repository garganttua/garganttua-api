package com.garganttua.api.commons.operation;

/**
 * Operation access level. Only two levels remain: the token-authoritative redesign
 * folds tenant/owner isolation into {@code IAuthentication.reconcile} (the verified
 * token always carries the caller's tenant/owner) and the repository filter, so a
 * dedicated {@code tenant}/{@code owner} gate is no longer needed.
 */
public enum Access {

	anonymous, authenticated

}
