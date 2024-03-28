package com.garganttua.api.core.objects;

import java.util.Arrays;

import lombok.Getter;

public class GGAPIObjectAddress {

	public final static String MAP_KEY_INDICATOR = "#key";
	public final static String MAP_VALUE_INDICATOR = "#value";
	public final static String ELEMENT_SEPARATOR = ".";

	@Getter
	private final String[] fields;

	public GGAPIObjectAddress(String address) {
		if (address == null ||address.startsWith(".") || address.endsWith(".") || address.isEmpty()) {
			throw new IllegalArgumentException("Address cannot start or end with a dot, or is empty");
		}
		this.fields = address.split("\\.");
	}

	public int length() {
		return fields.length;
	}

	public String getElement(int index) {
		if (index >= 0 && index < fields.length) {
			return fields[index];
		} else {
			throw new IllegalArgumentException("Index out of bounds");
		}
	}

	@Override
	public String toString() {
		return String.join(".", fields);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(fields);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		GGAPIObjectAddress address = (GGAPIObjectAddress) obj;
		return Arrays.equals(fields, address.fields);
	}
	
	public GGAPIObjectAddress subAddress(int endIndex) {
        if (endIndex < 0 || endIndex >= fields.length) {
            throw new IllegalArgumentException("Invalid end index");
        }
        String subAddress = String.join(ELEMENT_SEPARATOR, Arrays.copyOfRange(fields, 0, endIndex + 1));
        return new GGAPIObjectAddress(subAddress);
    }
}
