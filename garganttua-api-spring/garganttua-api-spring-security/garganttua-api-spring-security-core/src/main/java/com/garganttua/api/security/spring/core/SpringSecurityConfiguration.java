package com.garganttua.api.security.spring.core;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import com.garganttua.api.core.security.engine.SecurityBuilder;
import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.engine.IEngine;
import com.garganttua.api.spec.security.ISecurityEngine;
import com.garganttua.api.spring.core.PropertyLoader;
import com.garganttua.api.spring.core.SpringBeanSupplier;
import com.garganttua.reflection.beans.GGBeanLoaderFactory;
import com.garganttua.reflection.beans.IGGBeanLoader;
import com.garganttua.reflection.injection.GGInjector;

@Configuration
@EnableWebSecurity
public class SpringSecurityConfiguration {

	@Value("${com.garganttua.api.spring.scanPackages}")
	private List<String> packages;

	@Autowired
	private IEngine engine;
	
	@Autowired
	private SpringBeanSupplier springBeanSupplier;
	
	@Autowired
	private PropertyLoader propLoader;

	private ISecurityEngine securityEngine;

	@Bean
	public ISecurityEngine createSecurityEngine() throws CoreException {
		SecurityBuilder builder = new SecurityBuilder();
		IGGBeanLoader l = GGBeanLoaderFactory.getLoader(this.propLoader, this.packages, List.of(this.springBeanSupplier));
		builder.engine(this.engine).loader(l).scanPackages(this.packages).injector(GGInjector.injector(l));
		this.securityEngine = builder.build().init().start();
		return this.securityEngine;
	}
}
