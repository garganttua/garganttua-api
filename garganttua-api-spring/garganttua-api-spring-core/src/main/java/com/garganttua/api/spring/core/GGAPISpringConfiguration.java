package com.garganttua.api.spring.core;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.garganttua.api.core.engine.GGApiBuilder;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.engine.IGGAPIBuilder;
import com.garganttua.api.spec.engine.IGGAPIEngine;
import com.garganttua.reflection.GGReflectionException;
import com.garganttua.reflection.annotation.scanner.GGSpringAnnotationScanner;
import com.garganttua.reflection.beans.GGBeanLoaderFactory;
import com.garganttua.reflection.beans.IGGBeanLoader;
import com.garganttua.reflection.injection.GGInjector;
import com.garganttua.reflection.utils.GGObjectReflectionHelper;

@Configuration
public class GGAPISpringConfiguration {
	
	static {
		GGObjectReflectionHelper.annotationScanner = new GGSpringAnnotationScanner();
	}
	
	@Value("${com.garganttua.api.spring.scanPackages}")
	private String[] packages;
	
	@Autowired
	private GGAPISpringBeanSupplier springBeanSupplier;
	
	@Autowired
	private GGAPIPropertyLoader propLoader;

	@Bean(name = "IGGAPIEngine")
	public IGGAPIEngine createGarganttuaApiEngine() throws GGAPIException, GGReflectionException {
		IGGBeanLoader l = GGBeanLoaderFactory.getLoader(this.propLoader, List.of(this.packages), List.of(this.springBeanSupplier));
		IGGAPIBuilder builder = GGApiBuilder.builder().propertyLoader(this.propLoader).packages(List.of(this.packages)).beanLoader(l).injector(GGInjector.injector(l));
		return builder.build().init().start();
	}
}
