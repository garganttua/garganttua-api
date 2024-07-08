package com.garganttua.api.core.security.authorization.tokens.jwt;

import io.jsonwebtoken.ExpiredJwtException;

public class GGAPITokenExpired extends Exception {

	public GGAPITokenExpired(ExpiredJwtException e) {
		super(e);
	}

	private static final long serialVersionUID = 3546601190352821620L;

}
