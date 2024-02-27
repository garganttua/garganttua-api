package com.garganttua.api.core;

import java.lang.reflect.Method;

public class GGAPIMethodAccessManager implements AutoCloseable {
	private final Method method;
	private final boolean originalAccessibility;

	@SuppressWarnings("deprecation")
	public GGAPIMethodAccessManager(Method method) {
		this.method = method;
		this.originalAccessibility = method.isAccessible();
		method.setAccessible(true);
	}

	@Override
	public void close() {
		method.setAccessible(originalAccessibility);
	}
}
