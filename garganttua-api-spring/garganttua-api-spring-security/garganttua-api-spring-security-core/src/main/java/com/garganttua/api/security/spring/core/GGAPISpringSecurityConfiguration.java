package com.garganttua.api.security.spring.core;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import com.garganttua.api.core.security.engine.GGAPISecurityBuilder;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.engine.IGGAPIEngine;
import com.garganttua.api.spec.security.IGGAPISecurityEngine;
import com.garganttua.api.spring.core.GGAPIPropertyLoader;
import com.garganttua.api.spring.core.GGAPISpringBeanSupplier;
import com.garganttua.reflection.beans.GGBeanLoaderFactory;
import com.garganttua.reflection.beans.IGGBeanLoader;
import com.garganttua.reflection.injection.GGInjector;

@Configuration
@EnableWebSecurity
public class GGAPISpringSecurityConfiguration {

	@Value("${com.garganttua.api.spring.scanPackages}")
	private List<String> packages;

	@Autowired
	private IGGAPIEngine engine;
	
	@Autowired
	private GGAPISpringBeanSupplier springBeanSupplier;
	
	@Autowired
	private GGAPIPropertyLoader propLoader;

	private IGGAPISecurityEngine securityEngine;

	@Bean
	public IGGAPISecurityEngine createSecurityEngine() throws GGAPIException {
		GGAPISecurityBuilder builder = new GGAPISecurityBuilder();
		IGGBeanLoader l = GGBeanLoaderFactory.getLoader(this.propLoader, this.packages, List.of(this.springBeanSupplier));
		builder.engine(this.engine).loader(l).scanPackages(this.packages).injector(GGInjector.injector(l)).servicesRegistry(this.engine.getServicesRegistry());
		this.securityEngine = builder.build().init().start();
		return this.securityEngine;
	}
}
