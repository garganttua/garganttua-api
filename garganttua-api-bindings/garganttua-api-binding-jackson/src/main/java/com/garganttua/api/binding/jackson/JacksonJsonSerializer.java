package com.garganttua.api.binding.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.garganttua.api.commons.MimeType;
import com.garganttua.api.commons.serialization.Serializer;

/**
 * JSON {@code ISerializer} backed by Jackson's {@link ObjectMapper}. Advertises
 * {@code application/json}.
 * <p>
 * Auto-detected via {@link Serializer}: its package lives under
 * {@code com.garganttua.api}, a framework asset package, so it is registered out
 * of the box whenever this binding is on the classpath and
 * {@code includeFrameworkPackages} is on (the default).
 */
@Serializer
public class JacksonJsonSerializer extends AbstractJacksonSerializer {

	public JacksonJsonSerializer() {
		super(new ObjectMapper(), MimeType.APPLICATION_JSON);
	}
}
