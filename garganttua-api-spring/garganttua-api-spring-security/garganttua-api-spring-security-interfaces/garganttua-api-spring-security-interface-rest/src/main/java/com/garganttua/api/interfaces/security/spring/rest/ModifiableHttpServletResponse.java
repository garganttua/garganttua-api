package com.garganttua.api.interfaces.security.spring.rest;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

public class ModifiableHttpServletResponse extends HttpServletResponseWrapper{
    private final CharArrayWriter charArrayWriter;
    private final PrintWriter writer;

    public ModifiableHttpServletResponse(HttpServletResponse response) {
        super(response);
        charArrayWriter = new CharArrayWriter();
        writer = new PrintWriter(charArrayWriter);
    }

    @Override
    public PrintWriter getWriter() {
        return writer; 
    }

    public String getContent() {
        return charArrayWriter.toString();
    }

    public void setContent(String newContent) throws IOException {
        charArrayWriter.reset(); 
        charArrayWriter.write(newContent); 
    }

    public void writeToResponse() {
        try {
            HttpServletResponse originalResponse = (HttpServletResponse) getResponse();
            PrintWriter originalWriter = originalResponse.getWriter();
            originalWriter.write(charArrayWriter.toString());
            originalWriter.flush();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'écriture de la réponse modifiée", e);
        }
    }
}
