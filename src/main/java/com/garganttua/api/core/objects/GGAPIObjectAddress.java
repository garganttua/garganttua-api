package com.garganttua.api.core.objects;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.garganttua.api.core.exceptions.GGAPICoreExceptionCode;

import lombok.Getter;

public class GGAPIObjectAddress {

	public final static String MAP_KEY_INDICATOR = "#key";
	public final static String MAP_VALUE_INDICATOR = "#value";
	public final static String ELEMENT_SEPARATOR = ".";

	@Getter
	private String[] fields;
	private boolean detectLoops = true;

	public GGAPIObjectAddress(String address, boolean detectLoops) throws GGAPIObjectAddressException {
		this.detectLoops = detectLoops;
		if (address == null ||address.startsWith(".") || address.endsWith(".") || address.isEmpty()) {
			throw new IllegalArgumentException("Address cannot start or end with a dot, or is empty");
		}
		this.fields = address.split("\\.");
		
		if( this.detectLoops )
			this.detectLoop();
	}
	
	public GGAPIObjectAddress(String address) throws GGAPIObjectAddressException {
		this(address, true);
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
	
	public GGAPIObjectAddress subAddress(int endIndex) throws GGAPIObjectAddressException {
        if (endIndex < 0 || endIndex >= fields.length) {
            throw new IllegalArgumentException("Invalid end index");
        }
        String subAddress = String.join(ELEMENT_SEPARATOR, Arrays.copyOfRange(fields, 0, endIndex + 1));
        return new GGAPIObjectAddress(subAddress);
    }
	
	private void detectLoop() throws GGAPIObjectAddressException {
	    Set<String> visitedElements = Collections.synchronizedSet(new HashSet<>());
	    if( Arrays.stream(fields)
	            .parallel()
	            .filter(field -> !field.equals(MAP_KEY_INDICATOR) && !field.equals(MAP_VALUE_INDICATOR))
	            .anyMatch(field -> !visitedElements.add(field)) ) {
	    	throw new GGAPIObjectAddressException(GGAPICoreExceptionCode.OBJECT_ADDRESS_LOOP_DETECTED, "Loop detected ! "+this.toString());
	    }
	}
	
	public GGAPIObjectAddress addElement(String newElement) throws GGAPIObjectAddressException {
	    if (newElement == null || newElement.isEmpty()) {
	        throw new IllegalArgumentException("Element cannot be null or empty");
	    }

	    String[] newFields = Arrays.copyOf(fields, fields.length + 1);
	    newFields[newFields.length - 1] = newElement;
	    this.fields = newFields;
	    if( this.detectLoops )
	    	this.detectLoop();
	    return this;
	}
	
	@Override
	public GGAPIObjectAddress clone() {
	    try {
			return new GGAPIObjectAddress(this.toString());
		} catch (GGAPIObjectAddressException e) {
			throw new IllegalArgumentException(e);
		}
	}
}
