package com.garganttua.api.interfaces.spring.rest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.PathContainer;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import com.garganttua.api.core.service.GGAPIMethodConciliator;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.caller.IGGAPICaller;
import com.garganttua.api.spec.interfasse.IGGAPICustomizableInterface;
import com.garganttua.api.spec.service.IGGAPIServiceInfos;
import com.garganttua.api.spec.service.IGGAPIServiceResponse;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;

public class GGAPIInterfaceSpringCustomizable implements IGGAPICustomizableInterface {

	private List<IGGAPIServiceInfos> customServicesInfos = new ArrayList<IGGAPIServiceInfos>();
	private Map<IGGAPIServiceInfos, PathPattern> patterns = new HashMap<>();
	private PathPatternParser parser = new PathPatternParser();

	@Inject
	protected RequestMappingHandlerMapping requestMappingHandlerMapping;

	@Override
	public void addCustomService(IGGAPIServiceInfos serviceInfos) {
		this.customServicesInfos.add(serviceInfos);
		PathPattern pathPattern = this.parser.parse(serviceInfos.getPath());
		this.patterns.put(serviceInfos, pathPattern);
	}

	public ResponseEntity<?> customService(
			@RequestAttribute(name = GGAPICallerFilter.CALLER_ATTRIBUTE_NAME) IGGAPICaller caller,
			@RequestParam Map<String, String> customParameters, HttpServletRequest request) {

		IGGAPIServiceInfos infos = this.getServiceInfos(request, HttpMethod.valueOf(request.getMethod()));
		String servletPath = request.getServletPath();
		if (infos == null) {
			return new ResponseEntity<>(new GGAPIResponseObject(request.getRequestURI() + " does not match any service",
					GGAPIResponseObject.BAD_REQUEST), HttpStatus.BAD_REQUEST);
		}

		GGAPIMethodConciliator conciliator = new GGAPIMethodConciliator(infos.getMethod()).setCaller(caller)
				.setCustomParameters(customParameters)
				.setReferencePath(infos.getPath()).setValuedPath(servletPath);

		if (request.getMethod().equalsIgnoreCase("POST") || request.getMethod().equalsIgnoreCase("PUT")
				|| request.getMethod().equalsIgnoreCase("PATCH")) {
			conciliator.setBody(getBodyAsByteObjectArray(request));
		}
		
		Object[] parameters = conciliator.getParameters();

		try {
			Object returnedObject = infos.invoke(parameters);
			if (!IGGAPIServiceResponse.class.isAssignableFrom(returnedObject.getClass())) {
				return new ResponseEntity<>(
						new GGAPIResponseObject(
								returnedObject.getClass().getSimpleName() + " must be of type "
										+ IGGAPIServiceResponse.class.getSimpleName(),
								GGAPIResponseObject.UNEXPECTED_ERROR),
						HttpStatus.INTERNAL_SERVER_ERROR);
			}
			return GGAPIServiceResponseUtils.toResponseEntity((IGGAPIServiceResponse) returnedObject);
		} catch (GGAPIException e) {
			return new ResponseEntity<>(new GGAPIResponseObject(e.getMessage(), GGAPIResponseObject.UNEXPECTED_ERROR),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private IGGAPIServiceInfos getServiceInfos(ServletRequest request, HttpMethod method) {
		String uri = ((HttpServletRequest) request).getRequestURI();
		PathContainer pathContainer = PathContainer.parsePath(uri);

		for (Entry<IGGAPIServiceInfos, PathPattern> pattern : this.patterns.entrySet()) {
			if (pattern.getValue().matches(pathContainer)) {
				if (pattern.getKey().getOperation().getMethod() == GGAPIServiceMethodToHttpMethodBinder
						.fromHttpMethodAndEndpoint(method))
					return pattern.getKey();
			}
		}

		for (Entry<IGGAPIServiceInfos, PathPattern> pattern : this.patterns.entrySet()) {
			if (pattern.getKey().getPath().equals(uri)) {
				return pattern.getKey();
			}
		}

		return null;
	}

	protected void createCustomMappings() throws NoSuchMethodException, SecurityException {
		RequestMappingInfo.BuilderConfiguration options = new RequestMappingInfo.BuilderConfiguration();
		options.setPatternParser(new PathPatternParser());
		Method customMethod = this.getClass().getMethod("customService", IGGAPICaller.class, Map.class,
				HttpServletRequest.class);
		for (IGGAPIServiceInfos custom : this.customServicesInfos) {
			this.createMapping(custom.getPath(), customMethod, this, options, RequestMethod
					.resolve(GGAPIServiceMethodToHttpMethodBinder.fromServiceMethod(custom.getOperation())));
		}
	}

	protected void createMapping(String path, Method method, Object handler,
			RequestMappingInfo.BuilderConfiguration options, RequestMethod requestMethod) {
		final RequestMappingInfo requestMappingInfoCreate = RequestMappingInfo.paths(path).methods(requestMethod)
				.options(options).build();
		this.requestMappingHandlerMapping.registerMapping(requestMappingInfoCreate, handler, method);
	}

	public static Byte[] getBodyAsByteObjectArray(HttpServletRequest request) throws IOException {
		byte[] primitiveBytes;

		try (InputStream inputStream = request.getInputStream();
				ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {

			byte[] temp = new byte[1024];
			int bytesRead;

			while ((bytesRead = inputStream.read(temp)) != -1) {
				buffer.write(temp, 0, bytesRead);
			}

			primitiveBytes = buffer.toByteArray();
		}

		Byte[] objectBytes = new Byte[primitiveBytes.length];
		for (int i = 0; i < primitiveBytes.length; i++) {
			objectBytes[i] = primitiveBytes[i];
		}

		return objectBytes;
	}
}
