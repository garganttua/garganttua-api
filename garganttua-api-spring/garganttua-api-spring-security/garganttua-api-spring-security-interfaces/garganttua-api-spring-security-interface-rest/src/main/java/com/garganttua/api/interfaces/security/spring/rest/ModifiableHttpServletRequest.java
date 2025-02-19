package com.garganttua.api.interfaces.security.spring.rest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ModifiableHttpServletRequest extends HttpServletRequestWrapper {

	private byte[] requestBody;

	private final Map<String, String> headers = new HashMap<>();
	
	public ModifiableHttpServletRequest(HttpServletRequest request) throws IOException {
		super(request);
		log.atTrace().log("new modifiable creation");
		this.requestBody = this.toByteArray(request.getInputStream());

        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.put(headerName, request.getHeader(headerName));
        }
        log.atTrace().log("headers in constructor " + headers);
	}

	public void setRequestBody(String newBody) {
		this.requestBody = newBody.getBytes(StandardCharsets.UTF_8);
		this.headers.put("Content-Length", String.valueOf(newBody.length()));
		this.headers.put("content-length", String.valueOf(newBody.length()));
		log.atTrace().log("headers in setrequestBody " + headers);
	}
	
	@Override
    public String getHeader(String name) {
		log.atTrace().log("in getHeader " + name);
        return headers.getOrDefault(name, super.getHeader(name));
    }

    @Override
    public Enumeration<String> getHeaderNames() {
    	log.atTrace().log("in getHeaderNames ");
        return Collections.enumeration(headers.keySet());
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
    	log.atTrace().log("in getHeaders "+name);
    	String header__ = headers.get(name);
    	if( header__ == null ) {
    		return Collections.emptyEnumeration();
    	}
    	List<String> values = List.of(headers.get(name));
        return (values == null) ? Collections.emptyEnumeration() : Collections.enumeration(values);
    }

	@Override
	public int getContentLength() {
		log.atTrace().log("getContentLength " + this.requestBody.length);
		return this.requestBody.length;
	}

	@Override
	public long getContentLengthLong() {
		log.atTrace().log("getContentLengthLong " + this.requestBody.length);
		return this.requestBody.length;
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {

		final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(this.requestBody);

		ServletInputStream stream = new ServletInputStream() {
			@Override
			public boolean isFinished() {
				return byteArrayInputStream.available() == 0;
			}

			@Override
			public boolean isReady() {
				return true;
			}

			@Override
			public void setReadListener(ReadListener listener) {
			}

			@Override
			public int read() throws IOException {
				return byteArrayInputStream.read();
			}

			@Override
			public byte[] readNBytes(int arg0) throws IOException {
				log.atTrace().log("in readNBytes ");
				return byteArrayInputStream.readAllBytes();
			}

			@Override
			public byte[] readAllBytes() throws IOException {
				log.atTrace().log("in readAllBytes ");
				return byteArrayInputStream.readAllBytes();
			}
			
			@Override
			public int read(byte[] b) throws IOException {
				return super.read(b);
			}
			
			@Override
			public int read(byte[] b, int off, int len) throws IOException {
				return super.read(b, off, len);
			}

			@Override
			public int readLine(byte[] b, int off, int len) throws IOException {
				log.atTrace().log("in Line ");
				return super.readLine(b, off, len);
			}

			@Override
			public int readNBytes(byte[] b, int off, int len) throws IOException {
				log.atTrace().log("in readNBytes 2");
				return super.readNBytes(b, off, len);
			}
			
			@Override
			public int available() throws IOException {
				log.atTrace().log("in available");
				return super.available();
			}
			
			
		};

		return stream;
	}

	private byte[] toByteArray(InputStream inputStream) throws IOException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int bytesRead;
		while ((bytesRead = inputStream.read(buffer)) != -1) {
			byteArrayOutputStream.write(buffer, 0, bytesRead);
		}
		byte[] byteArray = byteArrayOutputStream.toByteArray();
		return byteArray;
	}
}