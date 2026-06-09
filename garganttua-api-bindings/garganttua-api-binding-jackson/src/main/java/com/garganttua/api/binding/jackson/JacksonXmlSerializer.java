package com.garganttua.api.binding.jackson;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.garganttua.api.commons.MimeType;
import com.garganttua.api.commons.serialization.Serializer;

/**
 * XML {@code ISerializer} backed by Jackson's {@link XmlMapper}. Advertises
 * {@code application/xml}.
 * <p>
 * Sits beside {@link JacksonJsonSerializer} — same mechanism, mapper swapped — so
 * a client sending {@code Accept: application/xml} is served rather than refused
 * with a {@code 406}. Auto-detected via {@link Serializer} exactly like its JSON
 * sibling.
 */
@Serializer
public class JacksonXmlSerializer extends AbstractJacksonSerializer {

	public JacksonXmlSerializer() {
		super(new XmlMapper(), MimeType.APPLICATION_XML);
	}
}
