package com.garganttua.api.interfaces.security.spring.rest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

public class ModifiableHttpServletRequest extends HttpServletRequestWrapper {

	 private byte[] requestBody;

	    public ModifiableHttpServletRequest(HttpServletRequest request) throws IOException {
	        super(request);
	        // Lit et tamponne le corps du ServletInputStream
	        this.requestBody = toByteArray(request.getInputStream());
	    }

	    // Permet de définir un nouveau corps pour la requête
	    public void setRequestBody(String newBody) {
	        this.requestBody = newBody.getBytes(StandardCharsets.UTF_8);
	    }

	    // Retourne un ServletInputStream basé sur le corps actuel (modifié ou non)
	    @Override
	    public ServletInputStream getInputStream() throws IOException {
	        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(this.requestBody);
	        return new ServletInputStream() {
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
	                // Pas utilisé dans cet exemple
	            }

	            @Override
	            public int read() throws IOException {
	                return byteArrayInputStream.read();
	            }
	        };
	    }

	    // Convertit l'InputStream en tableau d'octets
	    private byte[] toByteArray(InputStream inputStream) throws IOException {
	        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
	        byte[] buffer = new byte[1024];
	        int bytesRead;
	        while ((bytesRead = inputStream.read(buffer)) != -1) {
	            byteArrayOutputStream.write(buffer, 0, bytesRead);
	        }
	        return byteArrayOutputStream.toByteArray();
	    }
}