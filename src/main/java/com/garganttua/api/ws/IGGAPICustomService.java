package com.garganttua.api.ws;

import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo.BuilderConfiguration;

import io.swagger.v3.oas.models.OpenAPI;

public interface IGGAPICustomService {

	String getMethodName();

	Class<?>[] getParameters();

	void setOpenApi(OpenAPI openAPI);

	RequestMappingInfo getRequestMappingInfos(BuilderConfiguration options);

}
