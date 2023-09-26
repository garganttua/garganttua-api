package com.garganttua.api.security.authorization.token.jwt;

import io.jsonwebtoken.ExpiredJwtException;

public class GGAPITokenExpired extends Exception {

	public GGAPITokenExpired(ExpiredJwtException e) {
		super(e);
	}

	private static final long serialVersionUID = 3546601190352821620L;

}
