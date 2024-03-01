package com.garganttua.api.core.tools;

import java.lang.reflect.Constructor;

public class GGAPIConstructorAccessManager implements AutoCloseable {
	private final Constructor<?> constructor;
	private final boolean originalAccessibility;

	@SuppressWarnings("deprecation")
	public GGAPIConstructorAccessManager(Constructor<?> constructor) {
		this.constructor = constructor;
		this.originalAccessibility = constructor.isAccessible();
		this.constructor.setAccessible(true);
	}

	@Override
	public void close() {
		this.constructor.setAccessible(originalAccessibility);
	}
}