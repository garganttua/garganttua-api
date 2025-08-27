package com.garganttua.api.interfaces.spring.rest;

import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.garganttua.reflection.beans.annotation.GGBean;
import com.garganttua.reflection.beans.annotation.GGBeanLoadingStrategy;

import lombok.NoArgsConstructor;

@NoArgsConstructor
@GGBean(name = "SpringRestInterface", strategy = GGBeanLoadingStrategy.newInstance)
public class GGAPIInterfaceSpringRest extends GGAPIInterfaceSpringCustomizable {
	
	@Override
	protected void createCustomMappings(RequestMappingHandlerMapping requestMappingHandlerMapping)
			throws NoSuchMethodException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'createCustomMappings'");
	}
	

}
