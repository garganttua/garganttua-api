package com.garganttua.api.spec.security;

import com.garganttua.api.spec.GGAPIException;

@FunctionalInterface
public interface IGGAPIOrElseMethod {
	void orElse() throws GGAPIException;
}
