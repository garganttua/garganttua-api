package com.garganttua.api.spring.core;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.garganttua.api.core.engine.Builder;
import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.engine.IContextBuilder;
import com.garganttua.api.spec.engine.IEngine;
import com.garganttua.reflection.GGReflectionException;
import com.garganttua.reflection.annotation.scanner.GGSpringAnnotationScanner;
import com.garganttua.reflection.beans.GGBeanLoaderFactory;
import com.garganttua.reflection.beans.IGGBeanLoader;
import com.garganttua.reflection.injection.GGInjector;
import com.garganttua.reflection.utils.GGObjectReflectionHelper;

@Configuration
public class SpringConfiguration {
	
	static {
		GGObjectReflectionHelper.annotationScanner = new GGSpringAnnotationScanner();
	}
	
	@Value("${com.garganttua.api.spring.scanPackages}")
	private String[] packages;
	
	@Autowired
	private SpringBeanSupplier springBeanSupplier;
	
	@Autowired
	private PropertyLoader propLoader;

	@Bean(name = "IEngine")
	public IEngine createGarganttuaApiEngine() throws CoreException, GGReflectionException {
		IGGBeanLoader l = GGBeanLoaderFactory.getLoader(this.propLoader, List.of(deduplicate(this.packages)), List.of(this.springBeanSupplier));
		IContextBuilder builder = Builder.builder().propertyLoader(this.propLoader).packages(List.of(deduplicate(this.packages))).beanLoader(l).injector(GGInjector.injector(l));
		return builder.build().init().start();
	}
	
	public static String[] deduplicate(String[] input) {
        if (input == null) {
            return new String[0];
        }
        
        Set<String> set = new LinkedHashSet<>();
        for (String s : input) {
            set.add(s);
        }

        return set.toArray(new String[0]);
    }
}
