package com.garganttua.api.binding.jackson;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.MimeType;
import com.garganttua.api.commons.serialization.ISerializer;
import com.garganttua.core.reflection.IClass;

/**
 * Shared Jackson-backed {@link ISerializer}. The concrete subclasses differ only
 * by the {@link ObjectMapper} flavour (JSON {@code ObjectMapper} vs XML
 * {@code XmlMapper}) and the advertised {@link MimeType}; the (de)serialization
 * logic is identical — which is the whole point: XML sits beside JSON, the same
 * mechanism with the mapper swapped, so a client asking for {@code application/xml}
 * is served rather than refused with a {@code 406}.
 */
public abstract class AbstractJacksonSerializer implements ISerializer {

	private final ObjectMapper mapper;
	private final MimeType mimeType;

	protected AbstractJacksonSerializer(ObjectMapper mapper, MimeType mimeType) {
		// Lenient on read: an unknown field on the wire must not abort the pipeline
		// with a 400 — the DTO simply ignores it. Empty beans must not blow up writes.
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		// Java 8 date/time support: without the JSR-310 module Jackson throws on any
		// non-null java.time value (Instant, LocalDate*, …). Register it and emit
		// ISO-8601 strings rather than numeric timestamps.
		mapper.registerModule(new JavaTimeModule());
		mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		this.mapper = mapper;
		this.mimeType = mimeType;
	}

	@Override
	public MimeType mimeType() {
		return this.mimeType;
	}

	@Override
	public byte[] serialize(Object object) throws ApiException {
		if (object == null) {
			return new byte[0];
		}
		try {
			return this.mapper.writeValueAsBytes(object);
		} catch (Exception e) {
			throw new ApiException("Jackson " + this.mimeType + " serialization failed for "
					+ object.getClass().getName() + ": " + e.getMessage(), e);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T deserialize(byte[] data, IClass<T> type) throws ApiException {
		if (type == null) {
			throw new ApiException("Jackson " + this.mimeType + " deserialization: target type is null");
		}
		if (data == null || data.length == 0) {
			return null;
		}
		try {
			return this.mapper.readValue(data, (Class<T>) type.getType());
		} catch (Exception e) {
			throw new ApiException("Jackson " + this.mimeType + " deserialization failed for "
					+ type.getType().getTypeName() + ": " + e.getMessage(), e);
		}
	}
}
