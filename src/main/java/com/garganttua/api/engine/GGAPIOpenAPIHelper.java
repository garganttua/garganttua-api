package com.garganttua.api.engine;

import java.io.IOException;
import java.io.StringWriter;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class GGAPIOpenAPIHelper {
	
	private static final String EMPTY_OPENAPI_SCHEMA = "description: The entity schemas has not been provided. So it is left blank.";
	
	private Mustache m;

	public GGAPIOpenAPIHelper() {
		MustacheFactory mf = new DefaultMustacheFactory();
		this.m = mf.compile("template_openapi.yml");
	}
	
	public OpenAPI getOpenApi(String domain, String entityClassName, String entitySchema) throws IOException {
		
		if( entitySchema == null || entitySchema.isEmpty() ) {
			entitySchema = EMPTY_OPENAPI_SCHEMA;
		}
		
		Todo todo = new Todo(domain, entityClassName, entitySchema);
		
		StringWriter writer = new StringWriter();

		this.m.execute(writer, todo).flush();

		return new OpenAPIParser().readContents(writer.toString(), null, null).getOpenAPI();
		
	}
	
	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	public class Todo {
	    private String domain;
	    private String entityClassName;
	    private String entitySchema;
	}

}
