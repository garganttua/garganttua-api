package com.garganttua.api.interfaces.spring.rest.swagger;

import java.io.IOException;
import java.io.StringWriter;

import org.springframework.stereotype.Service;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GGAPIOpenAPIHelper {
	
	private static final String EMPTY_OPENAPI_SCHEMA = "{ \"description\": \"The entity schemas has not been provided. So it is left blank.\"}";
	
	private Mustache m;

	public GGAPIOpenAPIHelper() {
		MustacheFactory mf = new DefaultMustacheFactory();
		this.m = mf.compile("template_openapi.json");
	}
	
	public OpenAPI getOpenApi(String domain, String entityClassName, String entitySchema) throws IOException {
		
		if( entitySchema == null || entitySchema.isEmpty() ) {
			entitySchema = EMPTY_OPENAPI_SCHEMA;
		}
		
		Todo todo = new Todo(domain, entityClassName);
		
		StringWriter writer = new StringWriter();

		this.m.execute(writer, todo).flush();
		String t = writer.toString();
		
		String newString = t.replace("entitySchema_to_be_replaced", entitySchema);
		
		if( log.isDebugEnabled() ) {
			log.debug("Created OpenAPI Schemas \n"+newString);
		}
		
		return new OpenAPIV3Parser().readContents(newString, null, null).getOpenAPI();
		
	}
	
	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	public class Todo {
	    private String domain;
	    private String entityClassName;
//	    private String entitySchema;
	}

}
