package com.garganttua.api.core.objects.utils;

import java.lang.reflect.Method;

public class GGAPIMethodAccessManager implements AutoCloseable {
	private final Method method;
	private final boolean originalAccessibility;

	@SuppressWarnings("deprecation")
	public GGAPIMethodAccessManager(Method method) {
		this.method = method;
		this.originalAccessibility = method.isAccessible();
		this.method.setAccessible(true);
	}

	@Override
	public void close() {
		this.method.setAccessible(originalAccessibility);
	}
}
