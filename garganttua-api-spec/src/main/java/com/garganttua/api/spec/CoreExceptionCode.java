package com.garganttua.api.spec;

public final class CoreExceptionCode {

	private CoreExceptionCode() {
	}

	public static final int UNKNOWN_ERROR = 100;
	public static final int BAD_REQUEST = 101;
	public static final int ENTITY_NOT_FOUND = 102;
	public static final int GENERIC_SECURITY_ERROR = 200;
	public static final int TOKEN_EXPIRED = 201;
	public static final int TOKEN_REVOKED = 202;
	public static final int TOKEN_SIGNATURE_MISMATCH = 203;
	public static final int AUTHORIZATION_NOT_SIGNED = 204;
	public static final int KEY_REVOKED = 205;
	public static final int KEY_EXPIRED = 206;
	public static final int INVOKE_METHOD = 207;
	public static final int CORE_GENERIC_CODE = 208;
	public static final int ENTITY_DEFINITION = 209;
	public static final int FAILED_AUTHENTICATION = 210;

}
