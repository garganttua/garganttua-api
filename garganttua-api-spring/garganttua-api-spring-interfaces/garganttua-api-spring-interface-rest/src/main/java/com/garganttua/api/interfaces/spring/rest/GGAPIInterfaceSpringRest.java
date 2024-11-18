package com.garganttua.api.interfaces.spring.rest;

import java.lang.reflect.Method;

import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.garganttua.api.spec.interfasse.GGAPIInterfaceMethod;
import com.garganttua.reflection.beans.annotation.GGBean;
import com.garganttua.reflection.beans.annotation.GGBeanLoadingStrategy;

import lombok.NoArgsConstructor;

@NoArgsConstructor
@GGBean(name = "SpringRestInterface", strategy = GGBeanLoadingStrategy.newInstance)
public class GGAPIInterfaceSpringRest extends GGAPIAbstractInterfaceSpringRest {

	@Override
	protected void createCustomMappings(RequestMappingHandlerMapping requestMappingHandlerMapping) {
		//Nothing to do
	}

}
