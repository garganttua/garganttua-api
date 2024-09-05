package com.garganttua.api.spring.core;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.garganttua.reflection.GGReflectionException;
import com.garganttua.reflection.beans.IGGBeanSupplier;

@Service
public class GGAPISpringBeanSupplier implements IGGBeanSupplier {
	
	@Autowired
	private ApplicationContext context;

	@Override
	public Object getBeanNamed(String name) throws GGReflectionException {
		return this.context.getBean(name);
	}

	@Override
	public <T> T getBeanOfType(Class<T> type) throws GGReflectionException {
		return this.context.getBean(type);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> List<T> getBeansImplementingInterface(Class<T> interfasse) throws GGReflectionException {
		return (List<T>) List.of(this.context.getBeansOfType(interfasse).values().toArray());
	}

	@Override
	public String getName() {
		return "spring";
	}

}
