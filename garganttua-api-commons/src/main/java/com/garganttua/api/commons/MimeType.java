package com.garganttua.api.commons;

import java.util.Locale;
import java.util.Optional;

public enum MimeType {

	// Wildcard
	ALL("*/*"),

	// Application - structured data
	APPLICATION_JSON("application/json"),
	APPLICATION_ND_JSON("application/x-ndjson"),
	APPLICATION_PROBLEM_JSON("application/problem+json"),
	APPLICATION_HAL_JSON("application/hal+json"),
	APPLICATION_LD_JSON("application/ld+json"),
	APPLICATION_JSON_PATCH("application/json-patch+json"),
	APPLICATION_MERGE_PATCH("application/merge-patch+json"),
	APPLICATION_XML("application/xml"),
	APPLICATION_PROBLEM_XML("application/problem+xml"),
	APPLICATION_YAML("application/yaml"),
	APPLICATION_CBOR("application/cbor"),
	APPLICATION_MSGPACK("application/x-msgpack"),
	APPLICATION_GRAPHQL("application/graphql"),

	// Application - forms and binary
	APPLICATION_FORM_URLENCODED("application/x-www-form-urlencoded"),
	APPLICATION_OCTET_STREAM("application/octet-stream"),
	APPLICATION_PDF("application/pdf"),
	APPLICATION_ZIP("application/zip"),
	APPLICATION_GZIP("application/gzip"),
	APPLICATION_JAVASCRIPT("application/javascript"),

	// Text
	TEXT_PLAIN("text/plain"),
	TEXT_HTML("text/html"),
	TEXT_CSS("text/css"),
	TEXT_CSV("text/csv"),
	TEXT_XML("text/xml"),
	TEXT_MARKDOWN("text/markdown"),
	TEXT_JAVASCRIPT("text/javascript"),
	TEXT_EVENT_STREAM("text/event-stream"),

	// Multipart
	MULTIPART_FORM_DATA("multipart/form-data"),
	MULTIPART_MIXED("multipart/mixed"),

	// Image
	IMAGE_PNG("image/png"),
	IMAGE_JPEG("image/jpeg"),
	IMAGE_GIF("image/gif"),
	IMAGE_WEBP("image/webp"),
	IMAGE_SVG("image/svg+xml"),

	// Audio
	AUDIO_MPEG("audio/mpeg"),
	AUDIO_OGG("audio/ogg"),
	AUDIO_WAV("audio/wav"),

	// Video
	VIDEO_MP4("video/mp4"),
	VIDEO_WEBM("video/webm");

	private final String value;

	MimeType(String value) {
		this.value = value;
	}

	public String getValue() {
		return this.value;
	}

	public static MimeType fromValue(String value) {
		return find(value).orElseThrow(() -> new IllegalArgumentException("Unknown MIME type: " + value));
	}

	public static Optional<MimeType> find(String value) {
		if (value == null) {
			return Optional.empty();
		}
		String normalized = stripParameters(value).toLowerCase(Locale.ROOT);
		for (MimeType type : values()) {
			if (type.value.equals(normalized)) {
				return Optional.of(type);
			}
		}
		return Optional.empty();
	}

	public boolean matches(String value) {
		return find(value).filter(m -> m == this).isPresent();
	}

	private static String stripParameters(String value) {
		int semicolon = value.indexOf(';');
		return (semicolon < 0 ? value : value.substring(0, semicolon)).trim();
	}

	@Override
	public String toString() {
		return this.value;
	}
}
