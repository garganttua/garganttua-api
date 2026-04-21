package com.garganttua.api.commons.serialization;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.MimeType;
import com.garganttua.core.reflection.IClass;

/**
 * Interface-agnostic serializer for converting between raw bytes and domain objects.
 * Registered globally on the {@code IApiBuilder.serializer()} DSL and consumed by
 * the {@code data} pipeline stages (deserialize on request, serialize on response).
 */
public interface ISerializer {

	MimeType mimeType();

	byte[] serialize(Object object) throws ApiException;

	<T> T deserialize(byte[] data, IClass<T> type) throws ApiException;
}
