package com.garganttua.api.commons.service;

import java.util.Objects;

import com.garganttua.core.reflection.IClass;

public final class ArgKey<T> {

	private final String name;
	private final IClass<T> type;

	private ArgKey(String name, IClass<T> type) {
		this.name = Objects.requireNonNull(name, "ArgKey name cannot be null");
		this.type = Objects.requireNonNull(type, "ArgKey type cannot be null");
	}

	public static <T> ArgKey<T> of(String name, IClass<T> type) {
		return new ArgKey<>(name, type);
	}

	public String name() {
		return name;
	}

	public IClass<T> type() {
		return type;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ArgKey<?> other)) return false;
		return name.equals(other.name);
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public String toString() {
		return "ArgKey[" + name + ":" + type.getSimpleName() + "]";
	}
}
