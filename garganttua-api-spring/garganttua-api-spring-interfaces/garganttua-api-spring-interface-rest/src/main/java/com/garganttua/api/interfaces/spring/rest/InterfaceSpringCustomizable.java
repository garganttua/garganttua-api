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

import com.garganttua.api.core.service.MethodConciliator;
import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.caller.ICaller;
import com.garganttua.api.spec.interfasse.ICustomizableInterface;
import com.garganttua.api.spec.service.IService;
import com.garganttua.api.spec.service.IServiceInfos;
import com.garganttua.api.spec.service.IServiceResponse;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;

public abstract class InterfaceSpringCustomizable extends AbstractInterfaceSpringRest implements ICustomizableInterface {

	private List<IServiceInfos> customServicesInfos = new ArrayList<IServiceInfos>();
	private Map<IServiceInfos, PathPattern> patterns = new HashMap<>();
	private PathPatternParser parser = new PathPatternParser();

	@Inject
	protected RequestMappingHandlerMapping requestMappingHandlerMapping;

	protected IService service;

	@Override
	public void setService(IService service) {
		this.service = service;
	}

	@Override
	public void addCustomService(IServiceInfos serviceInfos) {
		this.customServicesInfos.add(serviceInfos);
		PathPattern pathPattern = this.parser.parse(serviceInfos.getPath());
		this.patterns.put(serviceInfos, pathPattern);
	}

	public ResponseEntity<?> customService(
			@RequestAttribute(name = CallerFilter.CALLER_ATTRIBUTE_NAME) ICaller caller,
			@RequestParam Map<String, String> customParameters, HttpServletRequest request) {

		IServiceInfos infos = this.getServiceInfos(request, HttpMethod.valueOf(request.getMethod()));
		String servletPath = request.getServletPath();
		if (infos == null) {
			return new ResponseEntity<>(new ResponseObject(request.getRequestURI() + " does not match any service",
					ResponseObject.BAD_REQUEST), HttpStatus.BAD_REQUEST);
		}

		MethodConciliator conciliator = new MethodConciliator(infos.getMethod()).setCaller(caller)
				.setCustomParameters(customParameters)
				.setReferencePath(infos.getPath()).setValuedPath(servletPath);

		if (request.getMethod().equalsIgnoreCase("POST") || request.getMethod().equalsIgnoreCase("PUT")
				|| request.getMethod().equalsIgnoreCase("PATCH")) {
			try {
				conciliator.setBody(getBodyAsByteObjectArray(request));
			} catch (IOException e) {
				return new ResponseEntity<>(
						new ResponseObject("unable to read body", ResponseObject.BAD_REQUEST),
						HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}

		Object[] parameters = conciliator.getParameters();

		try {
			Object returnedObject = infos.invoke(parameters);
			if (!IServiceResponse.class.isAssignableFrom(returnedObject.getClass())) {
				return new ResponseEntity<>(
						new ResponseObject(
								returnedObject.getClass().getSimpleName() + " must be of type "
										+ IServiceResponse.class.getSimpleName(),
								ResponseObject.UNEXPECTED_ERROR),
						HttpStatus.INTERNAL_SERVER_ERROR);
			}
			return ServiceResponseUtils.toResponseEntity((IServiceResponse) returnedObject);
		} catch (CoreException e) {
			return new ResponseEntity<>(new ResponseObject(e.getMessage(), ResponseObject.UNEXPECTED_ERROR),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private IServiceInfos getServiceInfos(ServletRequest request, HttpMethod method) {
		String uri = ((HttpServletRequest) request).getRequestURI();
		PathContainer pathContainer = PathContainer.parsePath(uri);

		for (Entry<IServiceInfos, PathPattern> pattern : this.patterns.entrySet()) {
			if (pattern.getValue().matches(pathContainer)) {
				if (pattern.getKey().getOperation().getMethod() == ServiceMethodToHttpMethodBinder
						.fromHttpMethodAndEndpoint(method))
					return pattern.getKey();
			}
		}

		for (Entry<IServiceInfos, PathPattern> pattern : this.patterns.entrySet()) {
			if (pattern.getKey().getPath().equals(uri)) {
				return pattern.getKey();
			}
		}

		return null;
	}

	protected void createCustomMappings() throws NoSuchMethodException, SecurityException {
		RequestMappingInfo.BuilderConfiguration options = new RequestMappingInfo.BuilderConfiguration();
		options.setPatternParser(new PathPatternParser());
		Method customMethod = this.getClass().getMethod("customService", ICaller.class, Map.class,
				HttpServletRequest.class);
		for (IServiceInfos custom : this.customServicesInfos) {
			this.createMapping(custom.getPath(), customMethod, this, options, RequestMethod
					.resolve(ServiceMethodToHttpMethodBinder.fromServiceMethod(custom.getOperation())));
		}
	}

	protected void createMapping(String path, Method method, Object handler,
			RequestMappingInfo.BuilderConfiguration options, RequestMethod requestMethod) {
		final RequestMappingInfo requestMappingInfoCreate = RequestMappingInfo.paths(path).methods(requestMethod)
				.options(options).build();
		this.requestMappingHandlerMapping.registerMapping(requestMappingInfoCreate, handler, method);
	}

	public static byte[] getBodyAsByteObjectArray(HttpServletRequest request) throws IOException {
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
		return primitiveBytes;
	}
}
