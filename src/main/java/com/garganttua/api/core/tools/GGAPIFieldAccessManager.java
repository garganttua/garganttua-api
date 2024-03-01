package com.garganttua.api.core.tools;

import java.lang.reflect.Field;

public class GGAPIFieldAccessManager implements AutoCloseable {
	private final Field field;
	private final boolean originalAccessibility;

	@SuppressWarnings("deprecation")
	public GGAPIFieldAccessManager(Field field) {
		this.field = field;
		this.originalAccessibility = field.isAccessible();
		this.field.setAccessible(true);
	}

	@Override
	public void close() {
		this.field.setAccessible(originalAccessibility);
	}
}
