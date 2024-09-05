package com.garganttua.api.spring.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.garganttua.reflection.properties.IGGPropertyLoader;

@Service
public class GGAPIPropertyLoader implements IGGPropertyLoader {
	
	@Autowired
	private Environment environment;

	@Override
	public String getProperty(String propertyName) {
		return this.environment.getProperty(propertyName);
	}

}
